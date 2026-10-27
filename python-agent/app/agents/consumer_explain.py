"""消费者解释 Agent：用通俗语言解读碳标签含义。"""
from typing import Any, Dict, Tuple


def _level(total: float) -> str:
    if total <= 50:
        return "优（低碳）"
    if total <= 200:
        return "良（较低碳）"
    return "一般"


def run(payload: Dict[str, Any]) -> Tuple[str, Dict[str, Any]]:
    # payload 可能为 carbon 对象，也可能直接携带 totalEmission
    carbon = payload.get("carbon") if isinstance(payload.get("carbon"), dict) else payload
    total = carbon.get("totalEmission")
    unit = carbon.get("unitEmission")

    try:
        total_f = float(total) if total is not None else None
    except (TypeError, ValueError):
        total_f = None

    level = _level(total_f) if total_f is not None else "待评估"
    content = (
        f"【碳标签解读】该农产品碳足迹等级：{level}。"
        f"全流程碳排放约 {total} kgCO2e"
        + (f"，每公斤产品碳排放 {unit} kgCO2e。" if unit is not None else "。")
        + "数据已上联盟链存证，扫码即可验真，请放心选购低碳农产品。"
    )
    return content, {"level": level, "totalEmission": total, "unitEmission": unit}
