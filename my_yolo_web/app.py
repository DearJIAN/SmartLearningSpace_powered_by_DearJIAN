from datetime import datetime, timedelta
import importlib
import asyncio
import threading
from queue import Queue, Empty
import time
import os
import sys
from opencc import OpenCC

cc = OpenCC("t2s")

# ============================================================
# .env 手动加载（不依赖 python-dotenv）
# ============================================================
def manual_load_env():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    env_path = os.path.join(base_dir, '.env')
    print(f"[LEAR-CODE Flask] 尝试加载 .env 文件：{env_path}")
    if os.path.exists(env_path):
        with open(env_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#'):
                    if '=' in line:
                        key, value = line.split('=', 1)
                        key = key.strip()
                        value = value.strip()
                        os.environ[key] = value
        print(f"[LEAR-CODE Flask] 已加载 .env 文件：{env_path}")
    else:
        print(f"[LEAR-CODE Flask] .env 文件不存在：{env_path}，将使用系统环境变量")

    print(f"[LEAR-CODE Flask] ARK_API_KEY: {'已设置' if os.environ.get('ARK_API_KEY') else '未设置'}")
    print(f"[LEAR-CODE Flask] ARK_MODEL: {os.environ.get('ARK_MODEL', '未设置')}")

manual_load_env()

# ============================================================
# Flask 与应用初始化
# ============================================================
from flask import Flask, Response, jsonify, request, send_from_directory, stream_with_context, render_template
from flask_cors import CORS

from web_inference import WebDetector
from backend.services.volc_realtime_bridge import is_voice_realtime_configured, get_voice_realtime_config, run_text_dialog
from backend.services.langchain_service import VolcEngineChatModel
from backend.services.tts_service import synthesize_speech, get_tts_cache_stats, clear_tts_cache

app = Flask(__name__)
CORS(app, resources={r"/api/*": {"origins": "*"}})

detector = WebDetector()

# ============================================================
# 基础路径
# ============================================================
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODELS_DIR = os.path.join(BASE_DIR, 'models')
EXAMPLES_DIR = os.path.join(BASE_DIR, 'example')

# ============================================================
# 启动自检
# ============================================================
print("-" * 50)
print(f"[LEAR-CODE Flask] 启动中...")
print(f"[LEAR-CODE Flask] 项目根目录: {BASE_DIR}")
print(f"[LEAR-CODE Flask] 模型文件夹: {MODELS_DIR}")

if not os.path.exists(MODELS_DIR):
    print(f"[LEAR-CODE Flask] [WARN] models 文件夹不存在，正在创建...")
    os.makedirs(MODELS_DIR)
else:
    files = [f for f in os.listdir(MODELS_DIR) if f.endswith('.pt')]
    print(f"[LEAR-CODE Flask] 发现 {len(files)} 个模型文件: {files}")

print(f"[LEAR-CODE Flask] 示例文件夹: {EXAMPLES_DIR}")
if not os.path.exists(EXAMPLES_DIR):
    os.makedirs(EXAMPLES_DIR)
print("-" * 50)

# ============================================================
# 会话管理
# ============================================================
SESSION_TTL = timedelta(hours=6)
HISTORY_LIMIT = 6
chat_session_store = {}
ark_client = None
ark_symbols = None


def get_env_value(*names, fallback=""):
    for name in names:
        value = os.getenv(name, "").strip()
        if value:
            return value
    return fallback


def load_ark_symbols():
    global ark_symbols
    if ark_symbols is not None:
        return ark_symbols
    try:
        ark_module = importlib.import_module("volcenginesdkarkruntime")
    except ImportError as error:
        raise RuntimeError(
            "缺少火山方舟 SDK，请先安装: pip install volcengine-python-sdk[ark]"
        ) from error
    ark_symbols = {"Ark": ark_module.Ark}
    return ark_symbols


def get_chat_config():
    return {
        "api_key": get_env_value("ARK_API_KEY"),
        "base_url": get_env_value("ARK_BASE_URL", fallback="https://ark.cn-beijing.volces.com/api/v3"),
        "model_name": get_env_value("ARK_MODEL", fallback="doubao-seed-1-6-251015"),
        "temperature": float(get_env_value("VOICE_TEMPERATURE", fallback="0.4")),
    }


def get_ark_client():
    global ark_client
    if ark_client is not None:
        return ark_client
    config = get_chat_config()
    if not config["api_key"]:
        raise RuntimeError("缺少模型密钥，请设置 ARK_API_KEY")
    if not config["base_url"]:
        raise RuntimeError("缺少模型服务地址，请设置 ARK_BASE_URL")
    symbols = load_ark_symbols()
    ark_client = symbols["Ark"](
        base_url=config["base_url"],
        api_key=config["api_key"],
    )
    return ark_client


def normalize_session_id(raw_value):
    session_id = str(raw_value or "").strip()
    return session_id[:120] if session_id else ""


def cleanup_chat_sessions(now=None):
    current_time = now or datetime.utcnow()
    expired = []
    for session_id, record in chat_session_store.items():
        updated_at = record.get("updated_at")
        if not updated_at or current_time - updated_at > SESSION_TTL:
            expired.append(session_id)
    for session_id in expired:
        chat_session_store.pop(session_id, None)


def get_chat_record(session_id):
    if not session_id:
        return None
    cleanup_chat_sessions()
    record = chat_session_store.get(session_id)
    if record is None:
        record = {"updated_at": datetime.utcnow(), "messages": []}
        chat_session_store[session_id] = record
    else:
        record["updated_at"] = datetime.utcnow()
    return record


def trim_chat_history(record):
    max_messages = HISTORY_LIMIT * 2
    if len(record["messages"]) > max_messages:
        record["messages"] = record["messages"][-max_messages:]


# ============================================================
# AI 回复解析工具
# ============================================================
def extract_answer_fragment(value):
    if value is None:
        return ""
    if isinstance(value, str):
        return value
    if isinstance(value, list):
        parts = []
        for item in value:
            fragment = extract_answer_fragment(item)
            if fragment:
                parts.append(fragment)
        return "".join(parts)
    if isinstance(value, dict):
        value_type = str(value.get("type") or value.get("role") or "").strip().lower()
        if value_type in {"reasoning", "thinking", "tool_call", "tool_result"}:
            return ""
        if "output_text" in value:
            fragment = extract_answer_fragment(value.get("output_text"))
            if fragment:
                return fragment
        if "text" in value:
            fragment = extract_answer_fragment(value.get("text"))
            if fragment:
                return fragment
        if "delta" in value:
            fragment = extract_answer_fragment(value.get("delta"))
            if fragment:
                return fragment
        content = value.get("content")
        if content is not None:
            fragment = extract_answer_fragment(content)
            if fragment:
                return fragment
        if "choices" in value:
            fragment = extract_answer_fragment(value.get("choices"))
            if fragment:
                return fragment
        if "message" in value:
            fragment = extract_answer_fragment(value.get("message"))
            if fragment:
                return fragment
        if "output" in value:
            fragment = extract_answer_fragment(value.get("output"))
            if fragment:
                return fragment
        return ""

    # Handle objects
    value_type = str(getattr(value, "type", getattr(value, "role", ""))).strip().lower()
    if value_type in {"reasoning", "thinking", "tool_call", "tool_result"}:
        return ""
    for attr in ("delta", "text", "output_text", "content", "choices", "message", "output"):
        fragment = extract_answer_fragment(getattr(value, attr, None))
        if fragment:
            return fragment
    return ""


ANSWER_MARKERS = (
    "最终答案：", "答案：", "可以这样说：", "比如可以这样说：",
    "直接回答：", "简洁地说：", "可以回答：",
)


def sanitize_answer_text(text):
    raw_text = str(text or "").replace("\r\n", "\n").replace("\r", "\n").strip()
    if not raw_text:
        return ""

    marker_index = -1
    selected_marker = ""
    for marker in ANSWER_MARKERS:
        index = raw_text.rfind(marker)
        if index > marker_index:
            marker_index = index
            selected_marker = marker
    if marker_index >= 0:
        raw_text = raw_text[marker_index + len(selected_marker):].strip()

    cleaned = raw_text.strip()
    paragraphs = [part.strip() for part in cleaned.split("\n\n") if part.strip()]
    deduped = []
    for p in paragraphs:
        if not deduped or deduped[-1] != p:
            deduped.append(p)
    cleaned = "\n\n".join(deduped).strip()

    if len(cleaned) >= 32 and len(cleaned) % 2 == 0:
        half = len(cleaned) // 2
        if cleaned[:half].strip() == cleaned[half:].strip():
            cleaned = cleaned[:half].strip()

    return cleaned.strip()


def read_response_text(response):
    return sanitize_answer_text(extract_answer_fragment(response))


def split_stream_chunks(text, chunk_size=10):
    source = str(text or "").strip()
    if not source:
        return []
    chunks = []
    buffer = ""
    for char in source:
        buffer += char
        if len(buffer) >= chunk_size or char in "。！？!?；;\n":
            chunks.append(buffer)
            buffer = ""
    if buffer:
        chunks.append(buffer)
    return chunks


def accumulate_stream_text(previous_text, fragment):
    fragment = str(fragment or "").strip()
    previous_text = str(previous_text or "")
    if not fragment:
        return previous_text, ""
    if not previous_text:
        return fragment, fragment
    if fragment.startswith(previous_text):
        return fragment, fragment[len(previous_text):]
    if previous_text.startswith(fragment):
        return previous_text, ""
    return previous_text + fragment, fragment


# ============================================================
# 消息构建与 AI 调用
# ============================================================
def build_messages(question, session_id, scene_name="智学空间"):
    system_prompt = (
        "你是智学空间校园智慧治理平台的数字人助手，名字叫'火花'。"
        "请始终使用中文，回答简洁、自然、友好。"
        "只输出最终回答，不要输出思考过程或内部说明。"
        "你可以帮助用户了解：教室监控、座位预约、失物招领、食堂服务、个人记账、校园导航等功能。"
        "如果上下文里有历史对话，请延续上下文继续回答。"
    )

    record = get_chat_record(session_id)
    history = record["messages"] if record else []
    messages = [
        {"role": "system", "content": [{"type": "input_text", "text": system_prompt}]}
    ]

    for entry in history:
        content = str(entry.get("content") or "").strip()
        if not content:
            continue
        role = "assistant" if entry.get("role") == "assistant" else "user"
        messages.append({"role": role, "content": [{"type": "input_text", "text": content}]})

    user_prompt = f"当前场景：{scene_name}\n用户问题：{question}"
    messages.append({"role": "user", "content": [{"type": "input_text", "text": user_prompt}]})
    return messages, user_prompt, record


def call_ark_responses(question, scene_name, session_id):
    messages, user_prompt, record = build_messages(question, session_id, scene_name)
    config = get_chat_config()
    client = get_ark_client()
    request_kwargs = {"model": config["model_name"], "input": messages}
    if config["temperature"] is not None:
        request_kwargs["temperature"] = config["temperature"]
    response = client.responses.create(**request_kwargs)
    reply = read_response_text(response)
    return reply, user_prompt, record


def stream_ark_responses(question, scene_name, session_id):
    messages, user_prompt, record = build_messages(question, session_id, scene_name)
    config = get_chat_config()
    client = get_ark_client()
    request_kwargs = {
        "model": config["model_name"],
        "input": messages,
        "stream": True,
    }
    if config["temperature"] is not None:
        request_kwargs["temperature"] = config["temperature"]

    response = client.responses.create(**request_kwargs)
    raw_text = ""
    emitted_text = ""
    for chunk in response:
        fragment = extract_answer_fragment(chunk).strip()
        if not fragment:
            continue
        raw_text, _ = accumulate_stream_text(raw_text, fragment)
        sanitized = sanitize_answer_text(raw_text)
        if not sanitized:
            continue
        if sanitized.startswith(emitted_text):
            delta = sanitized[len(emitted_text):]
            emitted_text = sanitized
        elif emitted_text.startswith(sanitized):
            delta = ""
        else:
            delta = sanitized
            emitted_text = sanitized
        if delta:
            yield delta, user_prompt, record

    if not emitted_text and raw_text:
        fallback = sanitize_answer_text(raw_text)
        if fallback:
            yield fallback, user_prompt, record


def _stream_text_model(queue, question, scene_name, session_id):
    def runner():
        try:
            reply, user_prompt, record = call_ark_responses(question, scene_name, session_id)
            reply = sanitize_answer_text(reply) or "我刚刚没有组织出合适的回答，你可以换个方式再问一次。"
            for delta in split_stream_chunks(reply):
                if delta:
                    queue.put(delta)
                    time.sleep(0.01)
            if record is not None and reply:
                record['messages'].append({'role': 'user', 'content': user_prompt})
                record['messages'].append({'role': 'assistant', 'content': reply})
                trim_chat_history(record)
                record['updated_at'] = datetime.utcnow()
        except Exception as error:
            queue.put({'error': '调用模型失败', 'detail': str(error)})
        finally:
            queue.put(None)

    asyncio.run(runner())


# ============================================================
# 路由：首页 & YOLO 相关 (保留)
# ============================================================
@app.route('/')
def index():
    return render_template('index.html')


@app.route('/video_feed')
def video_feed():
    return Response(detector.gen_frames(), mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/api/models')
def list_models():
    try:
        if not os.path.exists(MODELS_DIR):
            return jsonify([])
        files = [f for f in os.listdir(MODELS_DIR) if f.lower().endswith(('.pt', '.pth'))]
        return jsonify(files)
    except Exception:
        return jsonify([])


@app.route('/api/examples')
def list_examples():
    try:
        if not os.path.exists(EXAMPLES_DIR):
            return jsonify([])
        files = [f for f in os.listdir(EXAMPLES_DIR) if os.path.isfile(os.path.join(EXAMPLES_DIR, f))]
        paths = [os.path.join('example', f).replace('\\', '/') for f in files]
        return jsonify(paths)
    except Exception:
        return jsonify([])


@app.route('/api/start', methods=['POST'])
def start_detection():
    data = request.get_json(silent=True) or {}
    model_input = data.get('model_name', '').strip()
    if os.path.sep in model_input or '/' in model_input:
        full_model_path = model_input
    else:
        full_model_path = os.path.join(MODELS_DIR, model_input)

    input_type = data.get('input_type')
    source = 'camera' if input_type == 'camera' else data.get('path_input', '').strip()

    params = {
        "model_path": full_model_path,
        "source": source,
        "conf_thres": float(data.get('conf_thres', 0.25)),
        "iou_thres": float(data.get('iou_thres', 0.45)),
        "imgsz": int(data.get('imgsz', 640)),
        "save_txt": data.get('save_txt', False),
        "save_conf": data.get('save_conf', False),
        "save_crop": data.get('save_crop', False)
    }

    detector.prepare_run()
    detector.update_params(params)
    return jsonify({
        "status": "started",
        "is_running": detector.is_running,
        "is_paused": detector.is_paused,
        "stop_event": detector.stop_event
    })


@app.route('/api/pause', methods=['POST'])
def pause_detection():
    is_paused = detector.toggle_pause()
    return jsonify({"status": "paused" if is_paused else "resumed", "is_paused": is_paused})


@app.route('/api/stop', methods=['POST'])
def stop_detection():
    detector.stop()
    return jsonify({"status": "stopped", "stop_event": detector.stop_event})


@app.route('/api/results')
def get_results():
    with detector.results_lock:
        return jsonify(detector.recent_results)


@app.route('/api/clear_results', methods=['POST'])
def clear_results():
    detector.clear_results()
    return jsonify({"status": "cleared"})


@app.route('/api/stats')
def get_stats():
    with detector.stats_lock:
        return jsonify(detector.current_stats)


# ============================================================
# 路由：AI / 数字人 / 语音 (新增)
# ============================================================
@app.route('/api/health')
def health():
    config = get_chat_config()
    voice_realtime = get_voice_realtime_config()
    yolo_models = []
    if os.path.exists(MODELS_DIR):
        yolo_models = [f for f in os.listdir(MODELS_DIR) if f.endswith('.pt')]
    return jsonify({
        "ok": True,
        "chat_configured": bool(config["api_key"] and config["base_url"] and config["model_name"]),
        "voice_realtime_configured": is_voice_realtime_configured(),
        "model": config["model_name"],
        "base_url": config["base_url"],
        "voice_realtime_speaker": voice_realtime["speaker"],
        "yolo_models": yolo_models,
        "history_sessions": len(chat_session_store),
    })


@app.route('/api/voice/config')
def voice_config():
    voice_realtime = get_voice_realtime_config()
    return jsonify({
        "ok": True,
        "realtime_configured": is_voice_realtime_configured(),
        "token_length": len(voice_realtime["token"]),
        "dialog_address": voice_realtime["dialog_address"],
        "dialog_uri": voice_realtime["dialog_uri"],
        "app_id": voice_realtime["app_id"],
        "uid": voice_realtime["uid"],
        "speaker": voice_realtime["speaker"],
        "bot_name": voice_realtime["bot_name"],
        "input_mod": voice_realtime["input_mod"],
    })


@app.route('/api/debug/env')
def debug_env():
    config = get_chat_config()
    return jsonify({
        "ARK_API_KEY": (os.getenv("ARK_API_KEY", "")[:10] + "...") if os.getenv("ARK_API_KEY") else "未设置",
        "ARK_BASE_URL": os.getenv("ARK_BASE_URL", "未设置"),
        "ARK_MODEL": os.getenv("ARK_MODEL", "未设置"),
        "cwd": os.getcwd(),
        "env_file_exists": os.path.exists(os.path.join(BASE_DIR, '.env')),
    })


@app.route('/api/chat', methods=['POST'])
def chat():
    payload = request.get_json(silent=True) or {}
    question = str(payload.get('message') or payload.get('question') or '').strip()
    scene_name = str(payload.get('scene') or '智学空间').strip() or '智学空间'
    session_id = normalize_session_id(payload.get('sessionId'))

    if not question:
        return jsonify({"error": "消息不能为空"}), 400
    if not session_id:
        session_id = os.urandom(8).hex()

    try:
        reply, user_prompt, record = call_ark_responses(question, scene_name, session_id)
        if not reply:
            reply = '我刚刚没有组织出合适的回答，你可以换个方式再问一次。'
        if record is not None:
            record['messages'].append({'role': 'user', 'content': user_prompt})
            record['messages'].append({'role': 'assistant', 'content': reply})
            trim_chat_history(record)
            record['updated_at'] = datetime.utcnow()
        return jsonify({
            'reply': reply,
            'sessionId': session_id,
            'model': get_chat_config()['model_name'],
            'mode': 'ark_responses',
        })
    except Exception as error:
        return jsonify({'error': '调用模型失败', 'detail': str(error)}), 500


@app.route('/api/chat/stream', methods=['POST'])
def chat_stream():
    payload = request.get_json(silent=True) or {}
    question = str(payload.get('question') or (payload.get('messages') or [{}])[-1].get('content', '')).strip()
    if not question:
        question = str(payload.get('message') or '').strip()
    scene_name = str(payload.get('scene') or '智学空间').strip() or '智学空间'
    session_id = normalize_session_id(payload.get('sessionId'))

    if not question:
        return jsonify({"error": "消息不能为空"}), 400
    if not session_id:
        session_id = os.urandom(8).hex()

    queue = Queue()
    worker = threading.Thread(target=_stream_text_model, args=(queue, question, scene_name, session_id), daemon=True)
    worker.start()

    @stream_with_context
    def generate():
        yield f"sessionId:{session_id}\n"
        while True:
            try:
                item = queue.get(timeout=0.5)
            except Empty:
                if not worker.is_alive():
                    break
                continue
            if item is None:
                break
            if isinstance(item, dict) and item.get('error'):
                yield f"\nerror:{item.get('detail', '未知错误')}\n"
                break
            yield f"delta:{str(item)}\n"
        yield "done:1\n"

    response = Response(generate(), mimetype='text/plain; charset=utf-8')
    response.headers['Cache-Control'] = 'no-cache'
    response.headers['X-Accel-Buffering'] = 'no'
    return response


@app.route('/api/voice/chat', methods=['POST'])
def voice_chat():
    payload = request.get_json(silent=True) or {}
    question = str(payload.get('message') or '').strip()
    scene_name = str(payload.get('scene') or '智学空间').strip() or '智学空间'
    session_id = normalize_session_id(payload.get('sessionId'))

    if not question:
        return jsonify({"error": "消息不能为空"}), 400
    if not session_id:
        session_id = os.urandom(8).hex()

    try:
        if is_voice_realtime_configured():
            result = asyncio.run(run_text_dialog(question, scene_name, session_id))
            return jsonify({
                'reply': result.get('reply', '没有生成合适的回答'),
                'sessionId': result['sessionId'],
                'mode': result.get('provider', 'volc_realtime'),
            })
        reply, user_prompt, record = call_ark_responses(question, scene_name, session_id)
        if not reply:
            reply = '我刚刚没有组织出合适的回答，你可以换个方式再问一次。'
        if record is not None:
            record['messages'].append({'role': 'user', 'content': user_prompt})
            record['messages'].append({'role': 'assistant', 'content': reply})
            trim_chat_history(record)
        return jsonify({'reply': reply, 'sessionId': session_id, 'mode': 'ark_responses'})
    except Exception as error:
        return jsonify({'error': str(error)}), 500


# ============================================================
# 路由：TTS 语音合成
# ============================================================
@app.route('/api/tts/synthesize', methods=['POST'])
def synthesize_chapter_tts():
    payload = request.get_json(silent=True) or {}
    text = str(payload.get('text') or '').strip()
    if not text:
        return jsonify({"error": "文本内容不能为空"}), 400
    try:
        file_path = synthesize_speech(text)
        if not file_path:
            return jsonify({"error": "语音合成失败"}), 500
        return jsonify({
            "success": True,
            "audio_url": f"/api/tts/audio/{os.path.basename(file_path)}"
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/api/tts/audio/<filename>')
def serve_tts_audio(filename):
    from backend.services.tts_service import TTS_CACHE_DIR
    file_path = str(TTS_CACHE_DIR / filename)
    if not os.path.exists(file_path):
        return jsonify({"error": "音频文件不存在"}), 404
    return send_from_directory(str(TTS_CACHE_DIR), filename)


# ============================================================
# 路由：ASR 语音识别（Firefox / 不支持 Web Speech 的浏览器）
# ============================================================
import json as _json
import gzip
import uuid as _uuid
import subprocess
import tempfile
import shutil

async def call_doubao_asr(wav_path, config):
    import websockets

    ws_url = config.get("ws_url", "wss://openspeech.bytedance.com/api/v3/sauc/bigmodel")
    app_id = config.get("app_id", "")
    access_token = config.get("access_token", "")
    resource_id = config.get("resource_id", "volc.bigasr.sauc.duration")
    model_name = config.get("model_name", "bigmodel")

    with open(wav_path, "rb") as f:
        audio_data = f.read()

    compressed_audio = gzip.compress(audio_data)
    request_id = str(_uuid.uuid4())

    request_params = {
        "user": {"uid": ""},
        "audio": {"format": "wav", "codec": "raw", "rate": 16000, "bits": 16, "channel": 1},
        "request": {
            "model_name": model_name,
            "enable_itn": True,
            "enable_punc": True,
            "enable_ddc": True,
            "show_utterances": True,
        }
    }
    request_payload = _json.dumps(request_params).encode('utf-8')
    compressed_request = gzip.compress(request_payload)

    headers = {
        "X-Api-Resource-Id": resource_id,
        "X-Api-Request-Id": request_id,
        "X-Api-Access-Key": access_token,
        "X-Api-App-Key": app_id,
    }

    async with websockets.connect(ws_url, additional_headers=headers, ping_interval=None) as ws:
        version = 0b0001
        header_size = 0b0001
        message_type = 0b0001
        message_flags = 0b0000
        serialization = 0b0001
        compression = 0b0001

        header_byte = (version << 4) | header_size
        type_flag_byte = (message_type << 4) | message_flags
        ser_comp_byte = (serialization << 4) | compression

        header = bytes([header_byte, type_flag_byte, ser_comp_byte, 0x00])
        message = header + len(compressed_request).to_bytes(4, 'big') + compressed_request
        await ws.send(message)
        await ws.recv()

        audio_message_type = 0b0010
        audio_message_flags = 0b0010
        audio_type_flag_byte = (audio_message_type << 4) | audio_message_flags
        audio_header = bytes([header_byte, audio_type_flag_byte, ser_comp_byte, 0x00])
        audio_message = audio_header + len(compressed_audio).to_bytes(4, 'big') + compressed_audio
        await ws.send(audio_message)

        final_result = await ws.recv()
        text = ""
        try:
            if len(final_result) > 12:
                payload_data = final_result[12:]
                try:
                    result_json = gzip.decompress(payload_data).decode('utf-8')
                except Exception:
                    result_json = payload_data.decode('utf-8')
                result = _json.loads(result_json)
                text = result.get("result", {}).get("text", "")
        except Exception:
            pass

        if not text:
            raise Exception("未收到有效识别结果")
        return text


@app.route('/api/asr', methods=['POST'])
def asr():
    try:
        if 'audio' not in request.files:
            return jsonify({"error": "缺少音频文件"}), 400

        audio_file = request.files['audio']
        asr_provider = os.getenv("ASR_PROVIDER", "faster-whisper").lower()

        with tempfile.TemporaryDirectory() as tmpdir:
            webm_path = os.path.join(tmpdir, f"{_uuid.uuid4()}.webm")
            wav_path = os.path.join(tmpdir, f"{_uuid.uuid4()}.wav")

            audio_file.save(webm_path)
            input_size = os.path.getsize(webm_path)

            try:
                subprocess.run([
                    "ffmpeg", "-y", "-i", webm_path,
                    "-ar", "16000", "-ac", "1", wav_path
                ], check=True, capture_output=True, text=True)
            except subprocess.CalledProcessError as e:
                return jsonify({"error": f"音频转换失败：{e.stderr}"}), 500

            recognized_text = ""
            provider_name = ""

            if asr_provider == "doubao":
                try:
                    doubao_config = {
                        "ws_url": get_env_value("DOUBAO_ASR_WS_URL", fallback="wss://openspeech.bytedance.com/api/v3/sauc/bigmodel"),
                        "app_id": get_env_value("DOUBAO_ASR_APP_ID"),
                        "access_token": get_env_value("DOUBAO_ASR_ACCESS_TOKEN"),
                        "resource_id": get_env_value("DOUBAO_ASR_RESOURCE_ID", fallback="volc.bigasr.sauc.duration"),
                        "model_name": get_env_value("DOUBAO_ASR_MODEL_NAME", fallback="bigmodel"),
                    }
                    loop = asyncio.new_event_loop()
                    asyncio.set_event_loop(loop)
                    recognized_text = loop.run_until_complete(call_doubao_asr(wav_path, doubao_config))
                    loop.close()
                    provider_name = "doubao-asr"
                except Exception as e:
                    print(f"[ASR] 豆包 ASR 失败: {e}，回退到 faster-whisper")
                    asr_provider = "faster-whisper"

            if asr_provider == "faster-whisper" or not recognized_text:
                try:
                    global _whisper_model
                    if _whisper_model is None:
                        from faster_whisper import WhisperModel
                        _whisper_model = WhisperModel("small", device="cpu", compute_type="int8")
                    
                    segments, _ = _whisper_model.transcribe(wav_path, language="zh", vad_filter=True)
                    text_parts = []
                    for seg in segments:
                        text_parts.append(seg.text)
                    recognized_text = "".join(text_parts).strip()
                    recognized_text = cc.convert(recognized_text)
                    provider_name = "faster-whisper"
                except Exception as e:
                    return jsonify({"error": f"语音识别失败：{str(e)}"}), 500

            if not recognized_text:
                return jsonify({"error": "未检测到有效语音"}), 400

            return jsonify({
                "text": recognized_text,
                "provider": provider_name,
                "input_size": input_size,
            })
    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ============================================================
# 启动
# ============================================================
if __name__ == '__main__':
    port = int(os.getenv("FLASK_PORT", "5000"))
    debug = os.getenv("FLASK_DEBUG", "False").lower() == "true"
    print(f"[LEAR-CODE Flask] 启动在 http://0.0.0.0:{port}")
    app.run(host='0.0.0.0', port=port, debug=debug)
