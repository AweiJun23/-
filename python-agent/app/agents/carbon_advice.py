"""碳优化建议 Agent：定位高碳环节，输出低碳种植优化建议。"""
from typing import Any, Dict, List, Tuple


def _label(it: Dict[str, Any]) -> str:
    return it.get("materialName") or it.get("farmingType") or "未知环节"


def run(payload: Dict[str, Any]) -> Tuple[str, Dict[str, Any]]:
    items = payload.get("items") or []
    enriched = []
    for it in items:
        try:
            em = float(it.get("emission") or 0)
        except (TypeError, ValueError):
            em = 0.0
        try:
            prop = float(it.get("proportion") or 0)
        except (TypeError, ValueError):
            prop = 0.0
        enriched.append({**it, "emission": em, "proportion": prop})

    # 高碳环节：占比 >= 25% 或排放值前两名
    by_prop = [e for e in enriched if e["proportion"] >= 25.0]
    high = by_prop or sorted(enriched, key=lambda x: x["emission"], reverse=True)[:2]

    suggestions: List[str] = []
    seen = set()
    for e in high:
        ft = str(e.get("farmingType") or "").upper()
        mat = str(e.get("materialName") or "")
        if any(k in ft for k in ("MACHINE", "TRANSPORT", "HARVEST")) or "柴油" in mat or "汽油" in mat:
            if "energy" not in seen:
                suggestions.append("优化农机作业方案：采用电动农机、错峰作业、合并运输路径，降低柴油/汽油直接排放。")
                seen.add("energy")
        elif "FERTILIZE" in ft or "尿素" in mat or "肥" in mat:
            if "fert" not in seen:
                suggestions.append("优化施肥结构：以有机肥部分替代化肥、推行测土配方施肥，降低氮肥碳排放。")
                seen.add("fert")
        elif "IRRIGATE" in ft or "电" in mat:
            if "water" not in seen:
                suggestions.append("节水灌溉：推广滴灌/喷灌，结合光伏提水，降低灌溉用电排放。")
                seen.add("water")
        elif "FILM" in ft or "膜" in mat:
            if "film" not in seen:
                suggestions.append("减少地膜依赖：使用可降解地膜或减量覆膜，降低塑料生产排放。")
                seen.add("film")
        elif "PESTICIDE" in ft or "药" in mat:
            if "pest" not in seen:
                suggestions.append("绿色防控：精准施药、优先生物防治，替代高排放农药投入。")
                seen.add("pest")

    if not suggestions:
        suggestions.append("当前各环节碳排放较均衡，建议持续监测并优先优化占比最高的环节。")

    total = sum(e["emission"] for e in enriched)
    high_desc = "、".join(f"{_label(e)}({e['proportion']:.1f}%)" for e in high)
    content = (f"【低碳优化建议】全流程总排放约 {total:.2f} kgCO2e，"
               f"高碳环节：{high_desc}。建议：{' '.join(suggestions)}")
    return content, {"highCarbonItems": high, "suggestions": suggestions, "totalEmission": total}
