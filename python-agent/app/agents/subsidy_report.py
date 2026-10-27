"""补贴报表生成 Agent：按模板自动整理农事台账与碳报告，生成申报材料。"""
from typing import Any, Dict, Tuple


def run(payload: Dict[str, Any]) -> Tuple[str, Dict[str, Any]]:
    batch_no = payload.get("batchNo", "未知批次")
    product = payload.get("productName", "农产品")
    origin = payload.get("origin", "待补充")
    yield_ = payload.get("actualYield", "待填报")
    carbon = payload.get("carbon") or {}
    if not isinstance(carbon, dict):
        carbon = {}

    total = carbon.get("totalEmission")
    unit = carbon.get("unitEmission")
    if total is not None:
        carbon_text = f"全流程碳足迹 {total} kgCO2e，单位产量碳排放 {unit} kgCO2e/kg"
    else:
        carbon_text = "碳足迹数据采集中"

    md = (
        f"# 绿色低碳农产品补贴申报材料\n\n"
        f"- **批次编号**：{batch_no}\n"
        f"- **产品名称**：{product}\n"
        f"- **产地**：{origin}\n"
        f"- **实际产量**：{yield_} kg\n"
        f"- **碳足迹核算**：{carbon_text}\n\n"
        f"## 申报要点\n"
        f"1. 本批次已纳入联盟链轻节点哈希存证，农事记录与碳报告可扫码验真，数据不可篡改。\n"
        f"2. 全流程碳排放已量化，符合绿色低碳农产品认定基础要求。\n"
        f"3. 建议附具农事台账（施肥、灌溉、农机、植保记录）与碳足迹报告作为佐证材料。\n\n"
        f"> 本材料由多智能体自动生成（规则兜底模式），请人工复核后提交主管部门。\n"
    )
    return md, {"title": f"绿色低碳农产品补贴申报-{batch_no}", "markdown": md}
