"""FastAPI 入口：暴露 POST /api/agent 供 Java 端 PythonAgentClient 调用。"""
import os

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app import config
from app.graph import run_agent
from app.schemas import AgentRequest, AgentResponse

app = FastAPI(title="城乡农品轻量溯源与碳足迹存证 - 多智能体服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.post("/api/agent", response_model=AgentResponse)
def agent_endpoint(req: AgentRequest) -> AgentResponse:
    out = run_agent(req.agent, req.payload)
    return AgentResponse(
        agent=req.agent,
        content=out.get("content", ""),
        data=out.get("data"),
        fallback=bool(out.get("fallback", False)),
    )


@app.get("/health")
def health() -> dict:
    return {"status": "ok", "llm_enabled": config.USE_LLM,
            "agent_port": config.PORT}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=config.PORT)
