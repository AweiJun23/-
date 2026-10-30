

#
# 这是ReAct 智能体业务封装类 ReactAgent，专门对接 FastAPI 的StreamingResponse做 SSE 流式输出。
# 不再使用猴子补丁修改工具invoke，改为在业务层解析 stream 返回的 chunk 消息对象实现工具调用日志，不侵入 LangGraph 框架内部。
# 封装create_react_agent构建智能体图
# execute_stream()生成器函数：支持普通问答 / 报告模式动态切换 system 提示词，流式迭代 Agent 每一步输出，通过yield吐出文本片段
# 在循环解析 chunk 的时候捕获tool_calls、ToolMessage打印工具调用日志
# 预留configurable上下文存放业务标记，可给 graph 内部节点读取
# 文件底部自带测试入口，直接运行文件就可以测试流式效果；该类会被persional_chief.py导入，生成器交给 FastAPI StreamingResponse返回前端。
# 关键点：日志全部放在外层 stream 循环做，不去修改工具实例、不去修改 graph 内部节点，规避之前猴子补丁带来的版本兼容 bug。
#






import warnings
# 导入python警告模块，用于过滤程序运行时产生的警告信息
# 屏蔽langgraph弃用警告
warnings.filterwarnings(
    "ignore",
    message=r"create_react_agent has been moved to `langchain\.agents`.*"
)
# 设置警告过滤规则：忽略匹配该正则表达式的警告，屏蔽create_react_agent版本迁移的提示


from langgraph.prebuilt import create_react_agent
# 从langgraph预构建模块导入create_react_agent，快速生成ReAct智能体的图实例
from langchain_core.messages import ToolMessage
# 导入工具消息类，工具执行完成后返回给大模型的消息对象类型

from agent.tools.agent_toos import (
    get_user_location, get_weather, fetch_external_data, fill_context_for_report, get_user_id,
    rag_summarize, get_current_month
)
# 导入项目全部自定义工具函数，全部由@tool装饰器生成，供智能体调用
from model.factory import chat_model
# 导入已经实例化完成的大模型对象（通义千问），来自模型工厂模块
from utils.prompt_loader import load_system_prompts, load_report_prompts
# 导入提示词加载函数：普通问答系统提示词、报告模式专用系统提示词
from utils.logger_handler import logger
# 导入项目全局日志实例，负责输出info、error日志到控制台/日志文件


class ReactAgent:
    # 封装ReAct智能体的类，对外提供流式执行能力
    def __init__(self):
        # 类构造方法，实例化对象的时候自动执行
        raw_tools = [
            rag_summarize, get_weather, get_user_location, get_user_id,
            get_current_month, fetch_external_data, fill_context_for_report
        ]
        # 组装原始工具列表，存放所有可供智能体调用的工具，不做任何修改包装
        # 模型作为第一个位置参数，不修改任何工具，不篡改graph内部节点
        self.agent = create_react_agent(chat_model, tools=raw_tools)
        # 构建langgraph智能体图；chat_model作为第一个位置入参，传入原始工具列表；赋值给实例属性self.agent


    def execute_stream(self, query: str, is_report: bool = False):
        """
        流式输出，工具日志通过解析stream返回的messages实现，不侵入框架内部
        :param query: 用户提问
        :param is_report: 是否报告模式，切换system prompt
        """
        # 定义生成器方法execute_stream，实现流式返回结果；query用户输入；is_report标记是否开启报告模式，默认False

        if is_report:
            sys_prompt = load_report_prompts()
            # 如果is_report为True，加载报告模式的系统提示词
        else:
            sys_prompt = load_system_prompts()
            # 如果is_report为False，加载普通问答模式的系统提示词

        logger.info(f"[log_before_model]即将调用模型，用户输入：{query}")
        # 打印调用模型前的业务日志，记录用户提问内容

        input_dict = {
            "messages": [("user", query)],
            # 构造输入消息，元组格式 ("user",内容)，langgraph内部自动转换为HumanMessage对象
            "system": sys_prompt
            # 动态传入系统提示词，create_react_agent支持该入参，内部自动生成SystemMessage
        }
        config = {
            "configurable": {
                "report": is_report
            }
        }
        # 构造langgraph运行配置；configurable存放业务自定义上下文，把报告模式标记传入，节点内部可以读取


        for chunk in self.agent.stream(
            input_dict,
            config=config,
            stream_mode="values"
        ):
        # 循环调用agent的stream流式接口；传入输入字典、配置；stream_mode="values"表示每一步完整状态返回一次
            messages = chunk.get("messages", [])
            # 从每一步返回的状态chunk中取出messages消息列表，取不到则返回空列表
            if not messages:
                continue
            # 如果消息列表为空，跳过本次循环，不做后续处理

            # 在这里解析消息，打印工具调用日志（业务层实现日志，不修改框架内部）
            for msg in messages:
                # 遍历本轮所有消息对象
                # 打印工具调用请求
                if hasattr(msg, "tool_calls") and msg.tool_calls:
                # 判断消息对象是否存在tool_calls属性，并且存在工具调用请求
                    for call in msg.tool_calls:
                    # 遍历本次所有工具调用
                        logger.info(f"[tool monitor]准备调用工具：{call['name']}")
                        # 日志打印即将调用的工具名称
                        logger.info(f"[tool monitor]入参：{call['args']}")
                        # 日志打印工具调用传入的参数
                # 打印工具返回结果
                if isinstance(msg, ToolMessage):
                # 判断该消息是不是工具返回结果消息ToolMessage
                    logger.info(f"[tool monitor]工具 {msg.name} 执行完成")
                    # 日志打印该工具执行完毕

            latest_message = messages[-1]
            # 取出消息列表中最后一条最新消息
            if latest_message.content:
                yield latest_message.content.strip() + "\n"
                # 如果消息有文本内容，去除首尾空格，拼接换行；使用yield以生成器形式向外返回流式片段


if __name__ == "__main__":
    # Python程序入口标记：直接运行本py文件，下面代码执行；被其他文件import导入时不会执行
    agent = ReactAgent()
    # 实例化ReactAgent智能体对象，执行__init__构造函数
    for chunk in agent.execute_stream("扫地机器人在我所在的地区的气温下如何保养", is_report=False):
    # 循环迭代流式生成器，传入用户问题，普通问答模式（非报告模式）
        print(chunk, end="", flush=True)
        # 打印流式片段；end=""不自动换行；flush=True强制刷新输出缓冲区，实现实时打印效果