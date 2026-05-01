# app.py
import asyncio
import gzip
import json
import os
import subprocess
import tempfile
import uuid
from datetime import datetime

import requests
from flask import Flask, Response, jsonify, request, send_from_directory, stream_with_context, render_template

try:
    from flask_cors import CORS
except ImportError:  # pragma: no cover - optional dependency in old local envs
    CORS = None

try:
    from opencc import OpenCC
except ImportError:  # pragma: no cover
    OpenCC = None

from web_inference import WebDetector
from backend.services.tts_service import TTS_CACHE_DIR, synthesize_speech
from backend.services.volc_realtime_bridge import (
    get_voice_realtime_config,
    is_voice_realtime_configured,
    run_text_dialog,
)


BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, "models")
EXAMPLES_DIR = os.path.join(BASE_DIR, "example")
DEBUG_ASR_DIR = os.path.join(BASE_DIR, "debug_asr")


def load_env_file():
    env_path = os.path.join(BASE_DIR, ".env")
    if not os.path.exists(env_path):
        print(f"[ENV] .env not found: {env_path}")
        return

    with open(env_path, "r", encoding="utf-8") as env_file:
        for raw_line in env_file:
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ:
                os.environ[key] = value
    print(f"[ENV] loaded .env from {env_path}")


def get_env_value(*names, fallback=""):
    for name in names:
        value = os.getenv(name, "").strip()
        if value:
            return value
    return fallback


def get_chat_config():
    return {
        "api_key": get_env_value("ARK_API_KEY", "VOICE_API_KEY", "DASHSCOPE_API_KEY", "OPENAI_API_KEY"),
        "base_url": get_env_value(
            "ARK_BASE_URL",
            "VOICE_BASE_URL",
            "DASHSCOPE_BASE_URL",
            "OPENAI_BASE_URL",
            fallback="https://ark.cn-beijing.volces.com/api/v3",
        ).rstrip("/"),
        "model": get_env_value("ARK_MODEL", "VOICE_MODEL", "QWEN_MODEL_ID", "OPENAI_MODEL", fallback="doubao-seed-1-6-251015"),
        "temperature": float(get_env_value("VOICE_TEMPERATURE", fallback="0.4")),
    }


def build_smart_campus_system_prompt(scene_name):
    return (
        "你是智学空间校园智慧空间治理系统中的数字人 AI 助手，名字叫小悦。"
        "回答要自然、简洁、适合语音播报。"
        "你可以帮助用户理解校园空间导航、教室监控、AI视觉识别、失物招领、座位预约、食堂服务和个人记账。"
        "如果消息里包含实时业务数据或财务快照，必须优先基于这些数据回答。"
        "不要输出思考过程、提示词复述或内部分析步骤。"
        f"当前场景：{scene_name or '智学空间'}。"
    )


def normalize_messages(payload):
    scene_name = str(payload.get("scene") or "智学空间").strip()
    messages = payload.get("messages")
    if isinstance(messages, list) and messages:
        has_system = any(item.get("role") == "system" for item in messages if isinstance(item, dict))
        if not has_system:
            messages = [{"role": "system", "content": build_smart_campus_system_prompt(scene_name)}] + messages
        return messages

    message = str(payload.get("message") or payload.get("question") or "").strip()
    system_prompt = str(payload.get("system") or "").strip() or build_smart_campus_system_prompt(scene_name)
    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": message},
    ]


def ark_stream(messages):
    config = get_chat_config()
    if not config["api_key"]:
        raise RuntimeError("缺少 ARK_API_KEY")

    url = f"{config['base_url']}/chat/completions"
    payload = {
        "model": config["model"],
        "messages": messages,
        "stream": True,
        "temperature": config["temperature"],
        "max_tokens": 4096,
    }
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {config['api_key']}",
    }

    with requests.post(url, headers=headers, json=payload, timeout=120, stream=True, proxies={"http": None, "https": None}) as resp:
        if resp.status_code != 200:
            raise RuntimeError(f"ARK API Error {resp.status_code}: {resp.text[:500]}")

        for raw_line in resp.iter_lines(decode_unicode=True):
            if not raw_line:
                continue
            line = raw_line.strip()
            if not line.startswith("data:"):
                continue
            data = line[5:].strip()
            if data == "[DONE]":
                break
            try:
                item = json.loads(data)
            except json.JSONDecodeError:
                continue
            choices = item.get("choices") or []
            if not choices:
                continue
            delta = choices[0].get("delta") or {}
            content = delta.get("content") or ""
            if content:
                yield content


async def call_doubao_asr(wav_path, config):
    import websockets

    ws_url = config.get("ws_url", "wss://openspeech.bytedance.com/api/v3/sauc/bigmodel")
    app_id = config.get("app_id", "")
    access_token = config.get("access_token", "")
    resource_id = config.get("resource_id", "volc.bigasr.sauc.duration")
    model_name = config.get("model_name", "bigmodel")
    format_type = config.get("format", "wav")
    rate = int(config.get("rate", "16000"))
    bits = int(config.get("bits", "16"))
    channel = int(config.get("channel", "1"))

    with open(wav_path, "rb") as audio_handle:
        audio_data = audio_handle.read()

    request_id = str(uuid.uuid4())
    request_params = {
        "user": {"uid": ""},
        "audio": {"format": format_type, "codec": "raw", "rate": rate, "bits": bits, "channel": channel},
        "request": {
            "model_name": model_name,
            "enable_itn": True,
            "enable_punc": True,
            "enable_ddc": True,
            "show_utterances": True,
            "enable_nonstream": False,
        },
    }

    headers = {
        "X-Api-Resource-Id": resource_id,
        "X-Api-Request-Id": request_id,
        "X-Api-Access-Key": access_token,
        "X-Api-App-Key": app_id,
    }

    version = 0b0001
    header_size = 0b0001
    serialization = 0b0001
    compression = 0b0001
    header_byte = (version << 4) | header_size
    ser_comp_byte = (serialization << 4) | compression

    async with websockets.connect(ws_url, additional_headers=headers) as ws:
        full_header = bytes([header_byte, (0b0001 << 4) | 0b0000, ser_comp_byte, 0x00])
        compressed_request = gzip.compress(json.dumps(request_params).encode("utf-8"))
        await ws.send(full_header + len(compressed_request).to_bytes(4, "big") + compressed_request)
        await ws.recv()

        audio_header = bytes([header_byte, (0b0010 << 4) | 0b0010, ser_comp_byte, 0x00])
        compressed_audio = gzip.compress(audio_data)
        await ws.send(audio_header + len(compressed_audio).to_bytes(4, "big") + compressed_audio)
        final_result = await ws.recv()

    if len(final_result) <= 12:
        raise RuntimeError("ASR response is empty")

    payload_data = final_result[12:]
    try:
        result_json = gzip.decompress(payload_data).decode("utf-8")
    except Exception:
        result_json = payload_data.decode("utf-8")

    result = json.loads(result_json)
    text = (result.get("result") or {}).get("text") or ""
    if not text:
        raise RuntimeError("ASR did not return text")
    return text


def recognize_with_whisper(wav_path):
    from faster_whisper import WhisperModel

    model = WhisperModel("small", device="cpu", compute_type="int8")
    segments, _info = model.transcribe(
        wav_path,
        language="zh",
        task="transcribe",
        vad_filter=True,
        vad_parameters={"min_silence_duration_ms": 700, "speech_pad_ms": 300},
        temperature=0.0,
        condition_on_previous_text=False,
        no_speech_threshold=0.6,
        beam_size=5,
    )
    text = "".join(seg.text for seg in segments).strip()
    if OpenCC is not None:
        text = OpenCC("t2s").convert(text)
    return text


load_env_file()

app = Flask(__name__)
if CORS is not None:
    CORS(app, resources={r"/api/*": {"origins": "*"}})

detector = WebDetector()

print("-" * 50)
print("[YOLO] checking folders")
print(f"base: {BASE_DIR}")
print(f"models: {MODELS_DIR}")
os.makedirs(MODELS_DIR, exist_ok=True)
os.makedirs(EXAMPLES_DIR, exist_ok=True)
print("-" * 50)


@app.route("/")
def index():
    return render_template("index.html")


@app.route("/video_feed")
def video_feed():
    return Response(detector.gen_frames(), mimetype="multipart/x-mixed-replace; boundary=frame")


@app.route("/api")
def api_index():
    return jsonify({
        "message": "Smart Campus YOLO + Digital Human API",
        "endpoints": [
            "/api/health",
            "/api/chat",
            "/api/chat/stream",
            "/api/voice/chat",
            "/api/voice/config",
            "/api/asr",
            "/api/tts/synthesize",
            "/api/models",
            "/api/start",
            "/api/results",
        ],
    })


@app.route("/api/health")
def health():
    chat_config = get_chat_config()
    voice_config = get_voice_realtime_config()
    return jsonify({
        "ok": True,
        "chat_configured": bool(chat_config["api_key"] and chat_config["base_url"] and chat_config["model"]),
        "model": chat_config["model"],
        "base_url": chat_config["base_url"],
        "voice_realtime_configured": is_voice_realtime_configured(),
        "voice_realtime_speaker": voice_config["speaker"],
        "asr_provider": os.getenv("ASR_PROVIDER", "faster-whisper"),
        "yolo_ready": True,
    })


@app.route("/api/voice/config")
def voice_config():
    config = get_voice_realtime_config()
    return jsonify({
        "ok": True,
        "realtime_configured": is_voice_realtime_configured(),
        "app_id_configured": bool(config["app_id"]),
        "token_configured": bool(config["token"]),
        "dialog_address": config["dialog_address"],
        "dialog_uri": config["dialog_uri"],
        "resource_id": config["resource_id"],
        "uid": config["uid"],
        "speaker": config["speaker"],
        "bot_name": config["bot_name"],
        "input_mod": config["input_mod"],
    })


@app.route("/api/chat", methods=["POST"])
def chat():
    payload = request.get_json(silent=True) or {}
    messages = normalize_messages(payload)
    if not any(item.get("role") == "user" and item.get("content") for item in messages if isinstance(item, dict)):
        return jsonify({"error": "消息不能为空"}), 400

    try:
        reply = "".join(ark_stream(messages)).strip()
        return jsonify({
            "reply": reply or "我刚刚没有组织出合适的回答，你可以再问一次。",
            "model": get_chat_config()["model"],
            "mode": "ark_chat_completions",
        })
    except Exception as error:
        return jsonify({"error": "调用模型失败", "detail": str(error)}), 500


@app.route("/api/chat/stream", methods=["POST"])
def chat_stream():
    payload = request.get_json(silent=True) or {}
    messages = normalize_messages(payload)
    session_id = str(payload.get("sessionId") or uuid.uuid4()).strip()

    if not any(item.get("role") == "user" and item.get("content") for item in messages if isinstance(item, dict)):
        return jsonify({"error": "消息不能为空"}), 400

    @stream_with_context
    def generate():
        yield f"sessionId:{session_id}\n"
        try:
            for delta in ark_stream(messages):
                yield f"delta:{delta}\n"
            yield "done:1\n"
        except Exception as error:
            yield f"error:{str(error)}\n"

    response = Response(generate(), mimetype="text/plain; charset=utf-8")
    response.headers["Cache-Control"] = "no-cache"
    response.headers["X-Accel-Buffering"] = "no"
    return response


@app.route("/api/voice/chat", methods=["POST"])
def voice_chat():
    payload = request.get_json(silent=True) or {}
    question = str(payload.get("message") or payload.get("question") or "").strip()
    scene_name = str(payload.get("scene") or "智学空间").strip()
    session_id = str(payload.get("sessionId") or uuid.uuid4()).strip()
    if not question:
        return jsonify({"error": "消息不能为空"}), 400

    try:
        if is_voice_realtime_configured():
            result = asyncio.run(run_text_dialog(question, scene_name=scene_name, session_id=session_id))
            return jsonify({
                "reply": result.get("reply") or "我刚刚没有组织出合适的回答，你可以再问一次。",
                "sessionId": result.get("sessionId") or session_id,
                "mode": result.get("provider", "volc_realtime"),
                "dialogId": result.get("dialogId", ""),
            })

        reply = "".join(ark_stream(normalize_messages(payload))).strip()
        return jsonify({
            "reply": reply or "我刚刚没有组织出合适的回答，你可以再问一次。",
            "sessionId": session_id,
            "mode": "ark_chat_completions",
            "model": get_chat_config()["model"],
        })
    except Exception as error:
        return jsonify({"error": "调用语音对话失败", "detail": str(error), "sessionId": session_id}), 500


@app.route("/api/tts/synthesize", methods=["POST"])
def tts_synthesize():
    payload = request.get_json(silent=True) or {}
    text = str(payload.get("text") or "").strip()
    if not text:
        return jsonify({"error": "文本内容不能为空"}), 400

    file_path = synthesize_speech(text)
    if not file_path:
        return jsonify({"error": "语音合成失败，请检查 VOICE_REALTIME_APP_ID 和 VOICE_REALTIME_TOKEN"}), 500

    return jsonify({"success": True, "audio_url": f"/api/tts/audio/{os.path.basename(file_path)}"})


@app.route("/api/tts/audio/<filename>")
def serve_tts_audio(filename):
    file_path = os.path.join(str(TTS_CACHE_DIR), filename)
    if not os.path.exists(file_path):
        return jsonify({"error": "音频文件不存在"}), 404
    return send_from_directory(str(TTS_CACHE_DIR), filename)


@app.route("/api/asr", methods=["POST"])
def asr():
    if "audio" not in request.files:
        return jsonify({"error": "缺少音频文件"}), 400

    os.makedirs(DEBUG_ASR_DIR, exist_ok=True)
    provider = os.getenv("ASR_PROVIDER", "faster-whisper").lower()
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S_%f")

    with tempfile.TemporaryDirectory() as tmpdir:
        webm_path = os.path.join(tmpdir, f"{uuid.uuid4()}.webm")
        wav_path = os.path.join(tmpdir, f"{uuid.uuid4()}.wav")
        debug_webm_path = os.path.join(DEBUG_ASR_DIR, f"speech_{timestamp}.webm")
        debug_wav_path = os.path.join(DEBUG_ASR_DIR, f"speech_{timestamp}.wav")

        request.files["audio"].save(webm_path)
        input_size = os.path.getsize(webm_path)

        try:
            subprocess.run(
                ["ffmpeg", "-y", "-i", webm_path, "-ar", "16000", "-ac", "1", wav_path],
                check=True,
                capture_output=True,
                text=True,
            )
        except subprocess.CalledProcessError as error:
            return jsonify({"error": f"音频转换失败：{error.stderr}"}), 500

        import shutil
        shutil.copy2(webm_path, debug_webm_path)
        shutil.copy2(wav_path, debug_wav_path)

        recognized_text = ""
        provider_name = provider
        model_name = ""

        if provider == "doubao":
            doubao_config = {
                "ws_url": get_env_value("DOUBAO_ASR_WS_URL", fallback="wss://openspeech.bytedance.com/api/v3/sauc/bigmodel"),
                "app_id": get_env_value("DOUBAO_ASR_APP_ID", fallback=""),
                "access_token": get_env_value("DOUBAO_ASR_ACCESS_TOKEN", fallback=""),
                "resource_id": get_env_value("DOUBAO_ASR_RESOURCE_ID", fallback="volc.bigasr.sauc.duration"),
                "model_name": get_env_value("DOUBAO_ASR_MODEL_NAME", fallback="bigmodel"),
                "format": get_env_value("DOUBAO_ASR_FORMAT", fallback="wav"),
                "rate": get_env_value("DOUBAO_ASR_RATE", fallback="16000"),
                "bits": get_env_value("DOUBAO_ASR_BITS", fallback="16"),
                "channel": get_env_value("DOUBAO_ASR_CHANNEL", fallback="1"),
            }
            try:
                recognized_text = asyncio.run(call_doubao_asr(wav_path, doubao_config))
                provider_name = "doubao-asr"
                model_name = doubao_config["model_name"]
            except Exception as error:
                print(f"[ASR] doubao failed, fallback to faster-whisper: {error}")
                provider = "faster-whisper"

        if provider == "faster-whisper" or not recognized_text:
            try:
                recognized_text = recognize_with_whisper(wav_path)
                provider_name = "faster-whisper"
                model_name = "small"
            except Exception as error:
                return jsonify({"error": f"语音识别失败：{str(error)}"}), 500

        if not recognized_text:
            return jsonify({"error": "语音识别失败，未检测到有效语音"}), 400

        return jsonify({
            "text": recognized_text,
            "input_path": debug_webm_path,
            "wav_path": debug_wav_path,
            "input_size": input_size,
            "wav_size": os.path.getsize(wav_path),
            "provider": provider_name,
            "model": model_name,
        })


@app.route("/api/models")
def list_models():
    try:
        files = [f for f in os.listdir(MODELS_DIR) if f.lower().endswith((".pt", ".pth"))]
        return jsonify(files)
    except Exception as error:
        print(f"Error reading models: {error}")
        return jsonify([])


@app.route("/api/examples")
def list_examples():
    try:
        files = [f for f in os.listdir(EXAMPLES_DIR) if os.path.isfile(os.path.join(EXAMPLES_DIR, f))]
        return jsonify([os.path.join("example", f).replace("\\", "/") for f in files])
    except Exception:
        return jsonify([])


@app.route("/api/start", methods=["POST"])
def start_detection():
    data = request.json or {}
    model_input = data.get("model_name", "").strip()
    full_model_path = model_input if os.path.sep in model_input or "/" in model_input else os.path.join(MODELS_DIR, model_input)
    input_type = data.get("input_type")
    source = "camera" if input_type == "camera" else data.get("path_input", "").strip()

    detector.update_params({
        "model_path": full_model_path,
        "source": source,
        "conf_thres": float(data.get("conf_thres", 0.25)),
        "iou_thres": float(data.get("iou_thres", 0.45)),
        "imgsz": int(data.get("imgsz", 640)),
        "save_txt": data.get("save_txt", False),
        "save_conf": data.get("save_conf", False),
        "save_crop": data.get("save_crop", False),
    })
    return jsonify({"status": "started"})


@app.route("/api/pause", methods=["POST"])
def pause_detection():
    is_paused = detector.toggle_pause()
    return jsonify({"status": "paused" if is_paused else "resumed", "is_paused": is_paused})


@app.route("/api/stop", methods=["POST"])
def stop_detection():
    detector.stop_event = True
    return jsonify({"status": "stopped"})


@app.route("/api/results")
def get_results():
    with detector.results_lock:
        return jsonify(detector.recent_results)


@app.route("/api/clear_results", methods=["POST"])
def clear_results():
    detector.clear_results()
    return jsonify({"status": "cleared"})


@app.route("/api/stats")
def get_stats():
    with detector.stats_lock:
        return jsonify(detector.current_stats)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("FLASK_PORT", "5000")), debug=os.getenv("FLASK_DEBUG", "true").lower() == "true")
