

#
# 这是LangGraph ReAct 智能体封装工具文件。
# 因为旧版langchain.agents中间件存在版本 Bug 被废弃，不再使用旧的中间件装饰器，自己手写实现三套能力：
# 给所有工具增加调用日志、异常捕获（wrap_tool_with_log，猴子补丁包装工具invoke）
# 封装create_react_agent，统一构建 ReAct 智能体图对象（build_react_agent）
# 实现动态切换系统提示词，普通对话 / 报告两套 prompt 动态选择，同时增加模型调用前日志（agent_invoke_with_dynamic_prompt）
#
# 核心设计：不在构建 graph 的时候写死 system prompt，每次调用 invoke 的时候动态传入 system 参数，
#实现一套智能体图，可以切换两套不同系统提示词。
# 本文件只做封装，不写业务工具、不写 SSE 生成器；业务层（persional_chief.py）调用本文件的函数拿到 agent 图，再做流式输出。
#
#



from typing import Callable, Any
# 导入类型注解：Callable代表可调用对象，Any代表任意类型数据

from langgraph.prebuilt import create_react_agent
# 从langgraph内置模块导入create_react_agent，用于快速构建ReAct模式智能体graph

from langchain_core.tools import BaseTool
# 导入langchain工具的基类，所有@tool装饰生成的工具都继承这个类

from utils.prompt_loader import load_report_prompts, load_system_prompts
# 导入自定义工具函数：加载普通问答系统提示词、加载报告模式系统提示词

from utils.logger_handler import logger
# 导入项目全局日志对象，用于打印info/error日志


def wrap_tool_with_log(tool: BaseTool) -> BaseTool:
    """
    复现 @wrap_tool_call 中间件能力：工具调用日志、异常捕获
    替代 @wrap_tool_call 装饰器（该装饰器属于 langchain.agents.middleware，版本有bug）
    """
    original_invoke = tool.invoke
    # 保存工具对象原本的invoke执行方法，后续包装完成后需要调用原始逻辑

    def logged_invoke(tool_input: Any, *args, **kwargs) -> Any:
        # 定义包装后的工具执行函数；tool_input是工具入参，*args/**kwargs接收额外可变参数
        tool_name = tool.name
        # 获取当前被调用工具的名称，用于日志打印
        logger.info(f"[tool monitor]执行工具：{tool_name}")
        # 打印日志：标记即将执行哪一个工具
        logger.info(f"[tool monitor]传入参数：{tool_input}")
        # 打印日志：打印工具接收到的输入参数

        try:
            result = original_invoke(tool_input, *args, **kwargs)
            # 调用工具原始invoke方法，执行业务逻辑，拿到工具返回结果
            logger.info(f"[tool monitor]工具：{tool_name}调用成功")
            # 打印日志：工具执行成功标记
            return result
            # 将工具原始返回结果向上返回，维持原有调用链路不变
        except Exception as e:
            logger.error(f"工具{tool_name}调用失败，原因：{str(e)}")
            # 捕获工具执行发生的异常，打印错误日志
            raise e
            # 重新抛出异常，上层业务可以捕获处理，不吞掉异常

    tool.invoke = logged_invoke
    # 猴子补丁：把工具实例的invoke属性替换为我们写好的带日志的包装函数
    return tool
    # 返回包装完成后的工具实例


def build_react_agent(llm, tools: list[BaseTool]):
    """
    基于官方 create_react_agent 构建智能体
    复现原有3个中间件逻辑，不再使用 langchain.agents.middleware 装饰器
    """
    # 给所有工具包装日志
    wrapped_tools = [wrap_tool_with_log(t) for t in tools]
    # 列表推导式：遍历传入全部工具，逐个执行wrap_tool_with_log，得到包装日志后的工具列表

    # create_react_agent 本身不支持 dynamic_prompt / before_model 装饰器
    # 👉动态系统提示词、模型前置日志，我们封装外层调用函数实现
    agent_graph = create_react_agent(
        model=llm,
        # 传入大模型实例，作为智能体思考的底层模型
        tools=wrapped_tools,
        # 传入已经包装好日志的全部工具集合
        # 这里不要填固定system prompt，我们每次invoke前动态注入
    )
    return agent_graph
    # 返回构建完成的langgraph图对象，后续调用invoke/stream执行智能体逻辑


# 不要这样：手动拼 SystemMessage + HumanMessage 全部messages塞进去
# messages = [SystemMessage(sys_prompt), HumanMessage(user_query)]
# result = graph.invoke({"messages": messages})

# 正确调用方式
def agent_invoke_with_dynamic_prompt(
    graph,
    user_query: str,
    is_report: bool = False,
    config: dict = None
):
    # graph：build_react_agent返回的智能体graph对象
    # user_query：用户原始提问字符串
    # is_report：布尔标记，True=报告模式，False普通问答模式，用来切换system提示词
    # config：langgraph运行配置，可传入checkpointer、configurable等参数，默认为None

    if is_report:
        sys_prompt = load_report_prompts()
        # 如果开启报告模式，读取报告专用的系统提示词
    else:
        sys_prompt = load_system_prompts()
        # 普通问答模式，读取普通业务系统提示词

    logger.info(f"[log_before_model]即将调用模型，用户输入：{user_query}")
    # 打印模型调用前日志，复现before_model中间件效果

    result = graph.invoke(
        {
            "messages": [("user", user_query)],
            # 用户消息，使用元组格式，langgraph内部自动转成HumanMessage
            "system": sys_prompt   # create_react_agent支持system参数，内部自动处理SystemMessage
            # 动态传入系统提示词，每次调用可以不一样，实现dynamic_prompt动态切换提示词
        },
        config=config or {}
        # 传入运行config；如果外部没有传config，就使用空字典
    )
    return result
    # 返回graph执行完成的完整结果字典，里面包含全部messages消息


# ---------------- 使用示例 ----------------
if __name__ == "__main__":
    # python入口，直接运行本文件会执行下面测试代码，被import导入时不会执行
    from langchain_openai import ChatOpenAI
    # 导入兼容OpenAI接口的ChatOpenAI模型类，用来对接通义dashscope兼容接口
    import os
    # 导入os模块，用于读取环境变量
    from dotenv import load_dotenv
    # 导入dotenv，用来读取项目根目录.env文件的环境变量
    load_dotenv()
    # 加载.env文件，把配置写入系统环境变量

    llm = ChatOpenAI(
        model="qwen3.5-plus",
        # 指定模型名称，使用通义千问qwen3.5‑plus
        api_key=os.getenv("DASHSCOPE_API_KEY"),
        # 从环境变量读取dashscope的api‑key
        base_url="https://dashscope.aliyuncs.com/compatible-mode/v1"
        # 指定阿里云dashscope兼容openai的接口地址
    )
    my_tools = []
    # 定义工具列表，示例这里为空，实际项目填入自己的@tool工具

    agent = build_react_agent(llm, my_tools)
    # 调用构建函数，生成ReAct智能体graph实例

    # 普通模式
    resp1 = agent_invoke_with_dynamic_prompt(agent, "扫地机器人在我所在的地区的气温下如何保养", is_report=False)
    # 使用普通问答模式调用智能体，is_report=False，使用普通system prompt


    # 报告模式 is_report=True，自动切换load_report_prompts
    resp2 = agent_invoke_with_dynamic_prompt(agent, "生成一份保养报告", is_report=True)
    # 使用报告模式调用智能体，自动切换报告专用system prompt
    print(resp2["messages"][-1].content)
    # 取出消息列表中最后一条AI回复内容，打印输出