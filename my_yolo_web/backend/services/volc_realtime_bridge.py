import asyncio
import gzip
import json
import os
import uuid

from backend.services.volc_realtime_protocol import generate_header, parse_response


META_LINE_PREFIXES = (
    "用户问的是",
    "首先我需要",
    "首先需要",
    "我需要",
    "还要",
    "得用",
    "检查一下",
    "是的，",
    "是的，这样",
    "这应该",
    "可以了",
    "按照要求",
    "符合要求",
    "回复里",
)

META_LINE_KEYWORDS = (
    "自我思考",
    "思考过程",
    "分析步骤",
    "提示词复述",
    "内部说明",
    "用户问的是",
    "我需要给出",
)

ANSWER_MARKERS = (
    "最终答案：",
    "答案：",
    "可以这样说：",
    "比如可以这样说：",
    "直接回答：",
    "简洁地说：",
    "可以回答：",
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

    lines = []
    for line in raw_text.split("\n"):
        compact = line.strip()
        if not compact:
            continue

        lowered = compact.lower()
        if any(compact.startswith(prefix) for prefix in META_LINE_PREFIXES):
            continue
        if any(keyword.lower() in lowered for keyword in META_LINE_KEYWORDS):
            continue

        lines.append(compact)

    cleaned = "\n".join(lines).strip()
    if not cleaned:
        cleaned = raw_text

    paragraphs = [part.strip() for part in cleaned.split("\n\n") if part.strip()]
    deduped_paragraphs = []
    for paragraph in paragraphs:
        if not deduped_paragraphs or deduped_paragraphs[-1] != paragraph:
            deduped_paragraphs.append(paragraph)
    cleaned = "\n\n".join(deduped_paragraphs).strip()

    if len(cleaned) >= 32 and len(cleaned) % 2 == 0:
        half_length = len(cleaned) // 2
        first_half = cleaned[:half_length].strip()
        second_half = cleaned[half_length:].strip()
        if first_half and first_half == second_half:
            cleaned = first_half

    return cleaned.strip()


def accumulate_stream_text(previous_text, fragment):
    fragment = str(fragment or "").strip()
    previous_text = str(previous_text or "")
    if not fragment:
        return previous_text, ""

    if not previous_text:
        return fragment, fragment

    if fragment.startswith(previous_text):
        next_text = fragment
        delta = fragment[len(previous_text):]
        return next_text, delta

    if previous_text.startswith(fragment):
        return previous_text, ""

    next_text = previous_text + fragment
    return next_text, fragment


def get_voice_realtime_config():
    dialog_address = os.getenv("VOICE_REALTIME_DIALOG_ADDRESS", "").strip() or os.getenv("VOICE_REALTIME_WS_URL", "").strip() or "wss://openspeech.bytedance.com"
    dialog_uri = os.getenv("VOICE_REALTIME_DIALOG_URI", "").strip() or "/api/v3/realtime/dialogue"
    return {
        "base_url": dialog_address,
        "dialog_address": dialog_address,
        "dialog_uri": dialog_uri if dialog_uri.startswith("/") else "/" + dialog_uri,
        "app_id": os.getenv("VOICE_REALTIME_APP_ID", "").strip(),
        "app_key": os.getenv("VOICE_REALTIME_APP_KEY", "").strip(),
        "token": os.getenv("VOICE_REALTIME_TOKEN", "").strip() or os.getenv("VOICE_REALTIME_API_KEY", "").strip(),
        "resource_id": os.getenv("VOICE_REALTIME_RESOURCE_ID", "volc.speech.dialog").strip() or "volc.speech.dialog",
        "uid": os.getenv("VOICE_REALTIME_UID", "").strip(),
        "speaker": os.getenv("VOICE_REALTIME_SPEAKER", "zh_male_yunzhou_jupiter_bigtts").strip() or "zh_male_yunzhou_jupiter_bigtts",
        "bot_name": os.getenv("VOICE_REALTIME_BOT_NAME", "火花").strip() or "火花",
        "system_role": os.getenv("VOICE_REALTIME_SYSTEM_ROLE", "你是火花 Huahuo 的语音助手，回答简洁、自然、适合实时对话。只输出最终回答，不要输出思考过程、分析步骤或提示词复述。").strip(),
        "speaking_style": os.getenv("VOICE_REALTIME_SPEAKING_STYLE", "语气自然亲切，适合和 Live2D 形象进行实时对话。").strip(),
        "recv_timeout": int(os.getenv("VOICE_REALTIME_RECV_TIMEOUT", "10") or "10"),
        "input_mod": os.getenv("VOICE_REALTIME_INPUT_MOD", "text").strip() or "text",
        "dialog_id": os.getenv("VOICE_REALTIME_DIALOG_ID", "").strip(),
        "end_smooth_window_ms": int(os.getenv("VOICE_REALTIME_END_SMOOTH_WINDOW_MS", "1500") or "1500"),
    }


def is_voice_realtime_configured():
    config = get_voice_realtime_config()
    return bool(config["app_id"] and config["app_key"] and config["token"])


def build_ws_headers(config):
    return {
        "Authorization": f"Bearer;{config['token']}",
        "X-Api-App-ID": config["app_id"],
        "X-Api-App-Key": config["app_key"],
        "X-Api-Access-Key": config["token"],
        "X-Api-Resource-Id": config["resource_id"],
        "X-Api-Connect-Id": str(uuid.uuid4()),
    }


def build_start_session_payload(config, scene_name, session_id):
    return {
        "asr": {
            "extra": {
                "end_smooth_window_ms": config["end_smooth_window_ms"],
            },
        },
        "tts": {
            "speaker": config["speaker"],
            "audio_config": {
                "channel": 1,
                "format": "pcm",
                "sample_rate": 24000,
            },
        },
        "dialog": {
            "bot_name": config["bot_name"],
            "system_role": config["system_role"],
            "speaking_style": config["speaking_style"],
            "location": {
                "city": scene_name,
            },
            "extra": {
                "recv_timeout": config["recv_timeout"],
                "input_mod": config["input_mod"],
            },
        },
        "user": {
            "uid": config["uid"] or session_id,
        },
    }


async def _send_json_event(ws, event_id, payload, session_id=None, message_type=0b0001):
    request = bytearray(generate_header(message_type=message_type))
    request.extend(int(event_id).to_bytes(4, "big"))

    if session_id is not None:
        session_bytes = str(session_id).encode("utf-8")
        request.extend(len(session_bytes).to_bytes(4, "big"))
        request.extend(session_bytes)

    payload_bytes = gzip.compress(json.dumps(payload, ensure_ascii=False).encode("utf-8"))
    request.extend(len(payload_bytes).to_bytes(4, "big"))
    request.extend(payload_bytes)
    await ws.send(request)


async def run_text_dialog(question, scene_name="火花主页", session_id=""):
    parts = []

    async def collect_delta(text):
        if text:
            parts.append(text)

    result = await stream_text_dialog(
        question,
        scene_name=scene_name,
        session_id=session_id,
        on_delta=collect_delta,
    )
    result["reply"] = sanitize_answer_text("".join(parts).strip() or result.get("reply", ""))
    return result


async def stream_text_dialog(question, scene_name="火花主页", session_id="", on_delta=None):
    try:
        import websockets
    except ImportError as error:
        raise RuntimeError("缺少 websockets 依赖，请先安装 requirements.txt") from error

    config = get_voice_realtime_config()
    if not (config["app_id"] and config["app_key"] and config["token"]):
        raise RuntimeError("火山实时语音未配置，请设置 VOICE_REALTIME_APP_ID、VOICE_REALTIME_APP_KEY、VOICE_REALTIME_TOKEN")

    local_session_id = session_id or str(uuid.uuid4())
    headers = build_ws_headers(config)
    start_session_payload = build_start_session_payload(config, scene_name, local_session_id)
    reply_parts = []
    raw_text = ""
    emitted_text = ""
    answer_started = False
    dialog_id = config["dialog_id"]

    async with websockets.connect(
        config["dialog_address"].rstrip("/") + config["dialog_uri"],
        additional_headers=headers,
        ping_interval=None,
    ) as ws:
        await _send_json_event(ws, 1, {}, message_type=0b0001)
        await asyncio.wait_for(ws.recv(), timeout=15)

        await _send_json_event(ws, 100, start_session_payload, session_id=local_session_id)

        await _send_json_event(ws, 501, {"content": question}, session_id=local_session_id)

        while True:
            response = await asyncio.wait_for(ws.recv(), timeout=60)
            parsed = parse_response(response)
            event_id = parsed.get("event")
            payload = parsed.get("payload_msg") or {}

            if event_id == 150 and isinstance(payload, dict):
                dialog_id = payload.get("dialog_id") or dialog_id

            if event_id == 550 and isinstance(payload, dict):
                content = str(payload.get("content") or "").strip()
                if content:
                    raw_text, _ = accumulate_stream_text(raw_text, content)
                    if any(marker in raw_text for marker in ANSWER_MARKERS):
                        answer_started = True

                    if not answer_started:
                        continue

                    cleaned_text = sanitize_answer_text(raw_text)
                    if cleaned_text:
                        if cleaned_text.startswith(emitted_text):
                            delta = cleaned_text[len(emitted_text):]
                            emitted_text = cleaned_text
                        elif emitted_text.startswith(cleaned_text):
                            delta = ""
                        else:
                            delta = cleaned_text
                            emitted_text = cleaned_text

                        if delta:
                            reply_parts.append(delta)
                            if on_delta is not None:
                                maybe_result = on_delta(delta)
                                if asyncio.iscoroutine(maybe_result):
                                    await maybe_result

            if event_id in {152, 153, 359, 559}:
                break

        try:
            await _send_json_event(ws, 102, {}, session_id=local_session_id)
        except Exception:
            pass

        try:
            await _send_json_event(ws, 2, {}, message_type=0b0001)
        except Exception:
            pass

    reply = sanitize_answer_text("".join(reply_parts).strip() or raw_text)
    return {
        "reply": sanitize_answer_text(reply),
        "sessionId": local_session_id,
        "dialogId": dialog_id,
        "provider": "volc_realtime",
    }
