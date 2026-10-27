# 多智能体服务（Python + LangGraph）

本服务实现规范中定义的 4 个 AI 智能体，由 Java 端 `trace-agent` 通过
`POST /api/agent` 调用，承担**非确定性推理任务**：

| 智能体 | agent 值 | 输入 payload | 职责 |
| --- | --- | --- | --- |
| 农事校验 Agent | `farming_validate` | `{records:[...]}` | 识别施肥量超区间、日期逻辑冲突等异常 |
| 碳优化建议 Agent | `carbon_advice` | `{items:[...]}` | 定位高碳环节，输出低碳种植建议 |
| 补贴报表生成 Agent | `subsidy_report` | `{batchNo,productName,origin,actualYield,carbon}` | 生成 Markdown 申报材料 |
| 消费者解释 Agent | `consumer_explain` | `{carbon:{totalEmission,unitEmission}}` | 通俗解读碳标签 |

## 接口契约

请求：
```json
{ "agent": "carbon_advice", "payload": { "items": [ { "farmingType": "MACHINE", "materialName": "柴油", "emission": 120.5, "proportion": 55.0 } ] } }
```

响应（与 Java `AgentResponse` 对齐）：
```json
{ "agent": "carbon_advice", "content": "【低碳优化建议】...", "data": { "highCarbonItems": [...], "suggestions": [...] }, "fallback": true }
```

> `fallback=true` 表示纯规则输出（Java 端免二次校验）；`false` 表示经大模型润色（Java 端会经规则引擎二次校验，防幻觉）。

## 运行方式

### 1. 纯规则兜底模式（无需大模型，推荐用于演示/离线）

```bash
cd python-agent
pip install -r requirements.txt
cp .env.example .env        # 保持 OPENAI_API_KEY 为空
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

### 2. 启用大模型增强（可选）

在 `.env` 中填入 `OPENAI_API_KEY`（及可选 `OPENAI_BASE_URL` / `AGENT_MODEL`），
服务会在规则结论基础上调用 LLM 润色文本；任何调用异常都会自动降级为规则文本。

### 3. Docker（可选）

```dockerfile
FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

## 设计要点

- **LangGraph 编排**：`app/graph.py` 用 `StateGraph` 编译状态图，入口路由到 `dispatch` 节点；
  即使未安装 `langgraph`，也会自动降级为直接分发，保证服务始终可启动。
- **规则兜底优先**：四个 Agent 的 `run()` 均为纯 Python 实现，不依赖任何外部服务，
  数值与逻辑判定确定可控，规避大模型幻觉。
- **幻觉防护**：大模型仅负责文本润色，所有数值/结论仍由规则引擎产出；
  Java 端 `AgentServiceImpl` 还会对返回内容做二次规则校验。
- **解耦**：服务通过 HTTP 暴露，与 Java 微服务物理隔离，可独立部署、独立扩缩容。
