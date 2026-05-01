import os
import hashlib
import time
import requests
from typing import Dict, Optional
from pathlib import Path

TTS_CACHE_DIR = Path(__file__).parent / "tts_cache"
TTS_CACHE_DIR.mkdir(exist_ok=True)

TTS_CACHE: Dict[str, Dict] = {}
CACHE_TTL = 3600 * 24 * 7

TTS_API_URL = "https://openspeech.bytedance.com/api/v1/tts"

SPEAKER_CONFIG = {
    "default": {
        "speaker": "zh_male_yunzhou_jupiter_bigtts",
        "speed": 1.0,
        "volume": 1.0,
        "pitch": 0,
    }
}


def generate_tts_cache_key(text: str, speaker: str = "default") -> str:
    raw = f"{text}:{speaker}"
    return hashlib.md5(raw.encode("utf-8")).hexdigest()


def get_cached_tts(cache_key: str) -> Optional[str]:
    if cache_key in TTS_CACHE:
        entry = TTS_CACHE[cache_key]
        if time.time() - entry["timestamp"] < CACHE_TTL:
            if os.path.exists(entry["file_path"]):
                return entry["file_path"]
            else:
                del TTS_CACHE[cache_key]
    return None


def cache_tts(cache_key: str, file_path: str):
    TTS_CACHE[cache_key] = {
        "file_path": file_path,
        "timestamp": time.time()
    }


def synthesize_speech(text: str, speaker_config: Optional[Dict] = None) -> Optional[str]:
    cache_key = generate_tts_cache_key(text, "default")
    
    cached = get_cached_tts(cache_key)
    if cached:
        return cached
    
    config = SPEAKER_CONFIG["default"]
    if speaker_config:
        config.update(speaker_config)
    
    api_key = os.getenv("VOICE_REALTIME_TOKEN") or os.getenv("VOICE_REALTIME_API_KEY")
    app_id = os.getenv("VOICE_REALTIME_APP_ID")
    
    if not api_key or not app_id:
        return None
    
    payload = {
        "app": {
            "appid": app_id,
            "token": api_key,
            "cluster": "volcano_tts",
        },
        "user": {
            "uid": "tts_" + str(int(time.time()))
        },
        "audio": {
            "voice_type": config["speaker"],
            "encoding": "mp3",
            "speed_ratio": config["speed"],
            "volume_ratio": config["volume"],
            "pitch_ratio": config["pitch"],
        },
        "request": {
            "reqid": str(int(time.time() * 1000)),
            "text": text,
            "operation": "query",
        }
    }
    
    try:
        response = requests.post(
            TTS_API_URL,
            json=payload,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get("code") == 3000:
                audio_data = result.get("data", "")
                if audio_data:
                    import base64
                    audio_bytes = base64.b64decode(audio_data)
                    
                    file_path = str(TTS_CACHE_DIR / f"{cache_key}.mp3")
                    with open(file_path, "wb") as f:
                        f.write(audio_bytes)
                    
                    cache_tts(cache_key, file_path)
                    return file_path
        
        return None
    except Exception as e:
        print(f"TTS Error: {e}")
        return None


def get_tts_cache_stats() -> Dict:
    total_size = 0
    for entry in TTS_CACHE.values():
        file_path = entry.get("file_path")
        if file_path and os.path.exists(file_path):
            total_size += os.path.getsize(file_path)
    
    return {
        "cache_size": len(TTS_CACHE),
        "total_bytes": total_size,
        "cache_dir": str(TTS_CACHE_DIR)
    }


def clear_tts_cache():
    for entry in TTS_CACHE.values():
        file_path = entry.get("file_path")
        if file_path and os.path.exists(file_path):
            try:
                os.remove(file_path)
            except:
                pass
    
    TTS_CACHE.clear()
    
    for file in TTS_CACHE_DIR.glob("*.mp3"):
        try:
            file.unlink()
        except:
            pass
    
    return {"message": "语音缓存已清除"}
