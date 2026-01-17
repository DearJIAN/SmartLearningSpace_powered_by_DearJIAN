# ai_api.py
import requests
import json

API_KEY = "c0a5e654-d53a-4480-87f8-a7956b27601d"
MODEL_ID = "doubao-seed-1-6-251015"
API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"


class DoubaoClient:
    def __init__(self):
        self.api_key = API_KEY
        self.model_id = MODEL_ID
        self.api_url = API_URL
        self.history = [
            {
                "role": "system",
                "content": "你是一个乐于助人的AI助手，请准确、友好地回答用户的任何问题。"
            }
        ]

    # 改名为 get_response_stream 以示区别
    def get_response_stream(self, user_input):
        self.history.append({"role": "user", "content": user_input})

        headers = {
            "Content-Type": "application/json",
            "X-Volc-Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}"
        }

        # 【修改1】开启流式
        payload = {
            "model": self.model_id,
            "messages": self.history,
            "stream": True,
            "max_tokens": 2048
        }

        full_content = ""  # 用于记录完整回复，存入历史

        try:
            # 【修改2】stream=True，保留 proxies=None 解决代理问题
            resp = requests.post(
                self.api_url,
                headers=headers,
                json=payload,
                timeout=None,
                proxies={"http": None, "https": None},
                stream=True
            )

            if resp.status_code != 200:
                yield f"Error: {resp.status_code} - {resp.text}"
                return

            # 【修改3】按行读取流数据
            for line in resp.iter_lines():
                if not line:
                    continue

                # 数据格式通常是: data: {...json...}
                decoded_line = line.decode('utf-8').strip()

                if decoded_line.startswith("data:"):
                    json_str = decoded_line[5:].strip()  # 去掉 "data:"

                    if json_str == "[DONE]":  # 结束标志
                        break

                    try:
                        data = json.loads(json_str)
                        if "choices" in data:
                            # 提取增量内容 (delta)
                            delta = data["choices"][0].get("delta", {}).get("content", "")
                            if delta:
                                full_content += delta
                                yield delta  # 【核心】把这几个字立刻吐给前端
                    except json.JSONDecodeError:
                        continue

            # 对话结束后，把完整的回答存入历史
            self.history.append({"role": "assistant", "content": full_content})

        except Exception as e:
            yield f"Network Error: {str(e)}"

    def clear_history(self):
        self.history = [
            {
                "role": "system",
                "content": "你是一个乐于助人的AI助手，请准确、友好地回答用户的任何问题。"
            }
        ]