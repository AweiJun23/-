"""农事校验 Agent：识别农事录入数据异常（数值区间、时间逻辑）。"""
import datetime
from typing import Any, Dict, List, Tuple


# 各农事类型「单亩用量」合理上限（单位见 farming_type 约定）
_AMOUNT_UPPER = {
    "FERTILIZE": 100.0,   # kg / mu
    "IRRIGATE": 1200.0,   # kWh / mu
    "MACHINE": 60.0,      # L / mu
    "PESTICIDE": 10.0,    # kg / mu
    "FILM": 20.0,         # kg / mu
    "HARVEST": 60.0,      # L / mu
    "PACKAGE": 200.0,     # kg / mu
    "TRANSPORT": 500.0,   # t*km / mu
}


def _to_float(v: Any) -> float:
    try:
        return float(v)
    except (TypeError, ValueError):
        return None


def run(payload: Dict[str, Any]) -> Tuple[str, Dict[str, Any]]:
    """返回 (content, data)。content 含「异常」字样时 Java 端标记为 ABNORMAL。"""
    records = payload.get("records") or []
    anomalies: List[Dict[str, Any]] = []

    for idx, rec in enumerate(records):
        ft = str(rec.get("farmingType") or "").upper()
        amt = _to_float(rec.get("amount"))
        area = _to_float(rec.get("areaMu"))
        work_date = rec.get("workDate")
        rid = rec.get("recordId", idx)

        # 1) 用量非正
        if amt is not None and amt <= 0:
            anomalies.append({"recordId": rid, "level": "ERROR", "field": "amount",
                              "message": f"用量非正({amt})，请核实录入"})
            continue

        # 2) 单亩用量合理区间校验
        if ft in _AMOUNT_UPPER and amt is not None:
            upper = _AMOUNT_UPPER[ft]
            denom = area if (area and area > 0) else 1.0
            per_mu = amt / denom
            if per_mu > upper:
                anomalies.append({
                    "recordId": rid, "level": "WARN", "field": "amount",
                    "message": f"{ft} 单亩用量约 {per_mu:.1f}，超出合理上限 {upper}",
                })

        # 3) 时间逻辑校验
        if work_date:
            try:
                wd = datetime.date.fromisoformat(str(work_date)[:10])
                if wd > datetime.date.today():
                    anomalies.append({
                        "recordId": rid, "level": "ERROR", "field": "workDate",
                        "message": f"作业日期 {wd} 晚于今天，存在时间逻辑冲突",
                    })
            except (ValueError, TypeError):
                pass

    passed = len(anomalies) == 0
    if passed:
        content = (f"【农事校验】共校验 {len(records)} 条记录，未发现明显异常，"
                   f"数据可信。")
    else:
        msgs = "; ".join(f"记录{ a['recordId'] }: {a['message']}" for a in anomalies)
        content = f"【农事校验】发现 {len(anomalies)} 处异常：{msgs}"
    return content, {"anomalies": anomalies, "passed": passed}
