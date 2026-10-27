"""四个智能体模块统一导出。"""
from app.agents import carbon_advice
from app.agents import consumer_explain
from app.agents import farming_validate
from app.agents import subsidy_report

AGENTS = {
    "farming_validate": farming_validate.run,
    "carbon_advice": carbon_advice.run,
    "subsidy_report": subsidy_report.run,
    "consumer_explain": consumer_explain.run,
}

__all__ = ["AGENTS", "carbon_advice", "consumer_explain", "farming_validate", "subsidy_report"]
