"""多智能体服务运行时配置（环境变量驱动，支持 .env 文件）。"""
import os

# 加载项目根 .env（若存在）；无 python-dotenv 时静默跳过
try:
    from dotenv import load_dotenv
    load_dotenv()
except Exception:
    pass

# 服务端口（Java 端通过 agent.python.base-url 指向该端口，默认 8000）
PORT = int(os.getenv("AGENT_PORT", "8000"))

# 大模型配置：兼容 DashScope/百炼（OpenAI 兼容模式）与标准 OpenAI。
# 优先读 OPENAI_*，未设置则回退到 DASHSCOPE_*；两者都为空则以纯规则兜底模式运行。
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY") or os.getenv("DASHSCOPE_API_KEY", "")
OPENAI_BASE_URL = (
    os.getenv("OPENAI_BASE_URL")
    or os.getenv("DASHSCOPE_BASE_URL")
    or "https://api.openai.com/v1"
)
MODEL_NAME = os.getenv("AGENT_MODEL", "qwen-plus")

# 联网搜索（Tavily），可选；预留，供需要检索的智能体使用
TAVILY_API_KEY = os.getenv("TAVILY_API_KEY", "")

# 是否启用大模型增强（仅在配置了 API Key 时为真）
USE_LLM = bool(OPENAI_API_KEY)
