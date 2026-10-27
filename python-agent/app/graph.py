"""LangGraph 多智能体编排。

设计：
- 状态 AgentState 携带 {agent, payload, content, data, fallback}
- 编译一张最简单的图：entry -> dispatch -> END
- dispatch 节点按 agent 名称路由到对应规则实现
- 若配置了 LLM，则在返回前对 content 做轻量润色（仍由 Java 端规则引擎二次校验）

健壮性：
- 若 langgraph 未安装（requirements 中标记为可选），自动降级为直接分发，
  服务依旧可运行（纯规则兜底）。
"""
from typing import Any, Dict, TypedDict

from app import config
from app.agents import AGENTS
from app.llm import llm_polish


class AgentState(TypedDict):
    agent: str
    payload: Dict[str, Any]
    content: str
    data: Any
    fallback: bool


def _dispatch(state: AgentState) -> Dict[str, Any]:
    fn = AGENTS.get(state["agent"])
    if not fn:
        return {
            "content": f"未知智能体类型: {state['agent']}",
            "data": None,
            "fallback": True,
        }
    try:
        content, data = fn(state["payload"] or {})
        return {"content": content, "data": data, "fallback": True}
    except Exception as e:  # 规则执行异常也降级为安全结论
        return {
            "content": f"【规则兜底】智能体执行异常：{e}",
            "data": None,
            "fallback": True,
        }


def _build_graph():
    try:
        from langgraph.graph import StateGraph, END
    except Exception:
        return None
    wf = StateGraph(AgentState)
    wf.add_node("dispatch", _dispatch)
    wf.set_entry_point("dispatch")
    wf.add_edge("dispatch", END)
    try:
        return wf.compile()
    except Exception:
        return None


_COMPILED = _build_graph()


def run_agent(agent: str, payload: Dict[str, Any]) -> Dict[str, Any]:
    state: AgentState = {
        "agent": agent,
        "payload": payload,
        "content": "",
        "data": None,
        "fallback": True,
    }
    out = _COMPILED.invoke(state) if _COMPILED is not None else _dispatch(state)

    # fallback 语义：True=纯规则（Java 端免二次校验）；False=经 LLM 润色（需校验）
    if config.USE_LLM:
        polished = llm_polish(agent, out.get("content", ""), payload)
        if polished:
            out["content"] = polished
            out["fallback"] = False
        else:
            out["fallback"] = True  # 配置了 LLM 但调用失败，降级为规则
    else:
        out["fallback"] = True
    return out
