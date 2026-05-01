# ai_api.py
import json
import os

import requests


def get_env_value(*names, fallback=""):
    for name in names:
        value = os.getenv(name, "").strip()
        if value:
            return value
    return fallback


class DoubaoClient:
    """Backward-compatible streaming client used by older YOLO pages."""

    def __init__(self):
        self.api_key = get_env_value("ARK_API_KEY", "DOUBAO_API_KEY")
        self.model_id = get_env_value("ARK_MODEL", "DOUBAO_MODEL_ID", fallback="doubao-seed-1-6-251015")
        base_url = get_env_value("ARK_BASE_URL", fallback="https://ark.cn-beijing.volces.com/api/v3").rstrip("/")
        self.api_url = f"{base_url}/chat/completions"
        self.history = [
            {
                "role": "system",
                "content": "你是智学空间校园智慧空间治理系统中的 AI 助手，请准确、友好地回答用户问题。",
            }
        ]

    def get_response_stream(self, user_input):
        if not self.api_key:
            yield "Error: 缺少 ARK_API_KEY，请在 my_yolo_web/.env 中配置。"
            return

        self.history.append({"role": "user", "content": user_input})
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}",
        }
        payload = {
            "model": self.model_id,
            "messages": self.history,
            "stream": True,
            "max_tokens": 2048,
        }

        full_content = ""
        try:
            resp = requests.post(
                self.api_url,
                headers=headers,
                json=payload,
                timeout=120,
                proxies={"http": None, "https": None},
                stream=True,
            )

            if resp.status_code != 200:
                yield f"Error: {resp.status_code} - {resp.text}"
                return

            for line in resp.iter_lines(decode_unicode=True):
                if not line:
                    continue
                decoded_line = line.strip()
                if not decoded_line.startswith("data:"):
                    continue
                json_str = decoded_line[5:].strip()
                if json_str == "[DONE]":
                    break
                try:
                    data = json.loads(json_str)
                    delta = data["choices"][0].get("delta", {}).get("content", "")
                    if delta:
                        full_content += delta
                        yield delta
                except (KeyError, json.JSONDecodeError, IndexError):
                    continue

            self.history.append({"role": "assistant", "content": full_content})
        except Exception as e:
            yield f"Network Error: {str(e)}"

    def clear_history(self):
        self.history = [
            {
                "role": "system",
                "content": "你是智学空间校园智慧空间治理系统中的 AI 助手，请准确、友好地回答用户问题。",
            }
        ]
