"""可选大模型润色：仅在配置了 OPENAI_API_KEY 时启用。

未安装 openai SDK 或未配置密钥时返回 None，
调用方据此保持「纯规则兜底」输出，保证服务独立可运行。
"""
from typing import Any, Dict, Optional

from app import config

_SYSTEM_PROMPTS = {
    "farming_validate": "你是农业数据质量审核助手，请用简洁中文向农户说明数据校验结论。",
    "carbon_advice": "你是低碳农业顾问，请用通俗中文给出可落地的种植减排建议。",
    "subsidy_report": "你是农业补贴申报助理，请基于给定要点润色一段正式、条理清晰的申报说明。",
    "consumer_explain": "你是面向消费者的科普助手，请用生活化语言解释碳标签含义。",
}


def llm_polish(agent: str, base_text: str, payload: Dict[str, Any]) -> Optional[str]:
    if not config.USE_LLM:
        return None
    try:
        from openai import OpenAI
    except Exception:
        return None

    client = OpenAI(api_key=config.OPENAI_API_KEY, base_url=config.OPENAI_BASE_URL)
    system = _SYSTEM_PROMPTS.get(agent, "你是农业智能助手。")
    user = base_text + "\n\n参考原始数据(前2000字)：\n" + str(payload)[:2000]
    try:
        resp = client.chat.completions.create(
            model=config.MODEL_NAME,
            messages=[
                {"role": "system", "content": system},
                {"role": "user", "content": user},
            ],
            temperature=0.3,
        )
        polished = resp.choices[0].message.content.strip()
        return polished or None
    except Exception:
        # 任何调用异常都降级为规则文本，不阻塞主流程
        return None
