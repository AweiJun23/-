"""请求 / 响应数据模型，字段与 Java 端 AgentRequest / AgentResponse 对齐。"""
from typing import Any, Dict, Optional

from pydantic import BaseModel


class AgentRequest(BaseModel):
    """Java 端 PythonAgentClient 的入参：{ agent, payload }。"""
    agent: str
    payload: Dict[str, Any] = {}


class AgentResponse(BaseModel):
    """与 Java 端 AgentResponse 完全一致：{ agent, content, data, fallback }。"""
    agent: str
    content: str = ""
    data: Any = None
    fallback: bool = False
