

#
# 这是提示词加载工具文件。
# 读取prompts.yml配置中的文件路径，读取磁盘上的文本提示词文件，返回提示字符串。一共提供 3 个函数：
# load_system_prompts()：加载普通问答模式的系统提示词
# load_rag_prompts()：加载 RAG 检索工具使用的提示词
# load_report_prompts()：加载报告生成模式的系统提示词
# 被智能体模块调用，每次切换模式时读取外部 txt 提示词文件，做到提示词与代码分离，修改提示词不需要改动 Python 代码。
#


# 导入prompts配置字典，解析自config/prompts.yml
from utils.config_handle import   prompts_config
# 导入路径工具，将相对路径转换为绝对路径
from utils.path_tool import get_abs_path
# 导入全局日志对象，打印错误日志
from utils.logger_handler import logger


def load_system_prompts():
    """加载普通对话模式系统提示词文本"""
    try:
        # 从yaml配置取出main_prompt_path，转为绝对路径，赋值给system_prompt_path
        system_prompt_path = get_abs_path(prompts_config['main_prompt_path'])
    except KeyError as e:
        # 捕获字典key不存在异常，打印日志
        logger.error(f"[load_system_prompts]在yaml配置项中没有main_prompt_path")
        # 重新抛出异常，上层需要处理该错误
        raise e

    try:
        # 只读utf‑8打开提示词文件，读取全部文本内容返回
        return open(system_prompt_path,"r",encoding="utf-8").read()
    except Exception as e:
        # 捕获文件打开、读取各类异常，打印错误日志
        logger.error(f"[load_system_prompts]解析系统提示词出错,{str(e)}")
        # 重新抛出异常向上传递
        raise e



def load_rag_prompts():
    """加载rag检索工具对应的提示词"""
    try:
        # 读取yaml配置中rag_summarize_prompt_path，转绝对路径
        rag_prompt_path = get_abs_path(prompts_config["rag_summarize_prompt_path"])
    except KeyError as e:
        # key缺失捕获，打印错误日志
        logger.error(f"[load_system_prompts]在yaml配置项中没有rag_prompt_path")
        raise e

    try:
        # 打开rag提示词文件，读取全部内容返回
        return open(rag_prompt_path, "r", encoding="utf-8").read()
    except Exception as e:
        # 文件读取异常捕获打印日志
        logger.error(f"[load_rag_prompts]解析系统报告生成提示词出错,{str(e)}")
        raise e




def load_report_prompts():
    """加载报告模式专用系统提示词"""
    try:
        # 读取yaml配置report_prompt_path，转为绝对路径
        report_prompt_path = get_abs_path(prompts_config["report_prompt_path"])
    except KeyError as e:
        # key缺失捕获打印日志
        logger.error(f"[load_system_prompts]在yaml配置项中没有report_prompt_path")
        raise e

    try:
        # 打开报告提示词文件读取全部文本返回
        return open(report_prompt_path, "r", encoding="utf-8").read()
    except Exception as e:
        # 文件读取异常捕获打印日志
        logger.error(f"[load_report_prompts]解析系统提示词出错,{str(e)}")
        raise e


if __name__ == "__main__":
    # 当前脚本直接运行，测试打印报告提示词；被import导入不会执行
    print(load_report_prompts())