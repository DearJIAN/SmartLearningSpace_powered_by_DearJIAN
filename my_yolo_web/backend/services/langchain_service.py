import os
import hashlib
import json
import time
from typing import Dict, Optional, List, Any
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from langchain_core.language_models import BaseChatModel
from langchain_core.messages import HumanMessage, SystemMessage, AIMessage
from langchain_core.outputs import ChatResult, ChatGeneration

EXPLANATION_CACHE: Dict[str, Dict] = {}
CACHE_TTL = 3600 * 24

EXPLANATION_PROMPT = ChatPromptTemplate.from_messages([
    ("system", """你是一个专业的软件工程课程讲师，正在为学生讲解"面向对象分析与设计(OOA&D)"课程的内容。

你的讲解风格应该：
1. 通俗易懂，将复杂概念用简单语言表达
2. 条理清晰，按照逻辑顺序讲解
3. 生动有趣，适当使用比喻和例子
4. 重点突出，强调关键知识点
5. 自然流畅，适合语音朗读

要求：
- 只输出讲解内容，不要输出任何标题、标记或额外说明
- 讲解长度控制在 200-500 字之间
- 使用中文，语言自然流畅
- 不要使用 Markdown 格式或特殊符号
- 不要包含"好的"、"让我"等开场白，直接开始讲解
- 结尾自然，不要刻意总结"""),
    ("human", """请为以下章节内容生成讲解文本：

章节标题：{title}
章节内容：{content}

请直接开始讲解：""")
])


class VolcEngineChatModel(BaseChatModel):
    model_name: str = "doubao-seed-1-6-251015"
    api_key: str = ""
    base_url: str = "https://ark.cn-beijing.volces.com/api/v3"
    temperature: float = 0.7
    client: Any = None
    
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self._init_client()
    
    def _init_client(self):
        try:
            from volcenginesdkarkruntime import Ark
            self.client = Ark(
                base_url=self.base_url,
                api_key=self.api_key
            )
        except ImportError:
            raise RuntimeError("缺少火山方舟 SDK")
    
    def _generate(self, messages: List[Any], stop: Optional[List[str]] = None, **kwargs) -> ChatResult:
        formatted_messages = []
        for msg in messages:
            if isinstance(msg, SystemMessage):
                formatted_messages.append({
                    "role": "system",
                    "content": [{"type": "input_text", "text": msg.content}]
                })
            elif isinstance(msg, HumanMessage):
                formatted_messages.append({
                    "role": "user",
                    "content": [{"type": "input_text", "text": msg.content}]
                })
            elif isinstance(msg, AIMessage):
                formatted_messages.append({
                    "role": "assistant",
                    "content": [{"type": "input_text", "text": msg.content}]
                })
        
        response = self.client.responses.create(
            model=self.model_name,
            input=formatted_messages,
            temperature=self.temperature
        )
        
        content = self._extract_content_from_response(response)
        
        message = AIMessage(content=content)
        generation = ChatGeneration(message=message)
        return ChatResult(generations=[generation])
    
    def _extract_content_from_response(self, response) -> str:
        content = ""
        
        if hasattr(response, 'output') and response.output:
            output = response.output
            
            if isinstance(output, list):
                for item in output:
                    item_type = getattr(item, 'type', None) or (isinstance(item, dict) and item.get('type'))
                    
                    if item_type == 'message' or getattr(item, 'role', None) == 'assistant':
                        if hasattr(item, 'content') and item.content:
                            for content_item in item.content:
                                if hasattr(content_item, 'text') and content_item.text:
                                    content = content_item.text
                                    break
                                elif isinstance(content_item, dict) and content_item.get('text'):
                                    content = content_item['text']
                                    break
                        elif isinstance(item, dict) and item.get('content'):
                            for content_item in item['content']:
                                if isinstance(content_item, dict) and content_item.get('text'):
                                    content = content_item['text']
                                    break
                        if content:
                            break
                    
                    elif hasattr(item, 'text') and item.text:
                        content = item.text
                        break
                    elif isinstance(item, dict) and item.get('text'):
                        content = item['text']
                        break
            
            elif hasattr(output, 'text'):
                content = output.text
            elif isinstance(output, dict):
                content = output.get('text', '')
        
        elif isinstance(response, dict):
            output = response.get('output', {})
            if isinstance(output, dict):
                content = output.get('text', '')
            elif isinstance(output, list):
                for item in output:
                    if isinstance(item, dict) and item.get('text'):
                        content = item['text']
                        break
        
        return content
    
    @property
    def _llm_type(self) -> str:
        return "volcengine"


def generate_cache_key(title: str, content: str) -> str:
    raw = f"{title}:{content}"
    return hashlib.md5(raw.encode("utf-8")).hexdigest()


def get_cached_explanation(cache_key: str) -> Optional[str]:
    if cache_key in EXPLANATION_CACHE:
        entry = EXPLANATION_CACHE[cache_key]
        if time.time() - entry["timestamp"] < CACHE_TTL:
            return entry["explanation"]
        else:
            del EXPLANATION_CACHE[cache_key]
    return None


def cache_explanation(cache_key: str, explanation: str):
    EXPLANATION_CACHE[cache_key] = {
        "explanation": explanation,
        "timestamp": time.time()
    }


def generate_explanation(title: str, content: str, llm) -> str:
    cache_key = generate_cache_key(title, content)
    
    cached = get_cached_explanation(cache_key)
    if cached:
        return cached
    
    chain = EXPLANATION_PROMPT | llm | StrOutputParser()
    
    explanation = chain.invoke({
        "title": title,
        "content": content
    })
    
    explanation = explanation.strip()
    if explanation:
        cache_explanation(cache_key, explanation)
    
    return explanation


def get_cache_stats() -> Dict:
    return {
        "cache_size": len(EXPLANATION_CACHE),
        "cache_keys": list(EXPLANATION_CACHE.keys())
    }


def clear_cache():
    EXPLANATION_CACHE.clear()
    return {"message": "缓存已清除"}
