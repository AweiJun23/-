


#
# 这是项目全局日志配置模块。
# 封装日志器创建函数get_logger()，统一配置控制台输出 + 文件输出两套日志处理器：
# 控制台输出：默认 INFO 级别，打印到终端
# 文件输出：默认 DEBUG 级别，日志写入logs/目录，日志文件名携带模块名 + 当前时间戳
# 做防重复 handler 判断，避免多次调用get_logger()重复挂载日志处理器造成日志重复打印
# 模块顶层直接实例化logger，项目其他地方直接from utils.logger_handler import logger即可使用。
# 注意：每次程序启动，会生成一个带启动时间戳的全新日志文件，不会追加到旧日志。
#



# 导入python标准日志模块
import logging
# 导入时间模块，用于生成日志文件名的时间戳
from datetime import datetime

# 导入bokeh框架内置的默认日志格式常量（本代码并未使用该导入）
from bokeh.command.subcommands.serve import DEFAULT_LOG_FORMAT

# 导入路径工具，把相对路径转为绝对路径
from utils.path_tool import get_abs_path
# 操作系统文件目录工具
import os

#日志保存的根目录
LOG_ROOT = get_abs_path('logs')

#确保日志的目录存在，exist_ok=True目录已存在不会抛异常
os.makedirs(LOG_ROOT, exist_ok=True)
#日志的格式配置   error  info  debug

# 创建日志格式化对象，定义日志输出模板：时间、日志器名称、日志级别、文件名:行号、日志消息
DEFAULT_LOG_FORMAT=logging.Formatter(
    '%(asctime)s - %(name)s - %(levelname)s -%(filename)s:%(lineno)d - %(message)s'
)

def get_logger(
        name:str="agent",          # 日志器名称，默认agent
        console_level=logging.INFO,# 控制台输出日志级别，默认INFO
        file_level=logging.DEBUG,  # 文件输出日志级别，默认DEBUG
        log_file=None,             # 自定义日志文件路径，不传则自动生成文件名
)->logging.Logger:
    # 根据name获取或者创建logger实例
    logger = logging.getLogger(name)
    # logger全局最低级别设为DEBUG，handler再各自控制输出级别
    logger.setLevel(logging.DEBUG)
    # 避免重复添加handler
    # 判断是否已经存在处理器，handlers是list，直接访问，不要括号
    if logger.handlers:
        # 如果已经挂载过handler，直接返回logger，防止重复添加处理器
        return logger


    #控制台handler：输出日志到终端控制台
    console_handler = logging.StreamHandler()
    # 设置控制台输出的日志级别
    console_handler.setLevel(console_level)
    # 设置控制台输出的日志格式
    console_handler.setFormatter(DEFAULT_LOG_FORMAT)
    # 将控制台处理器挂载到logger对象
    logger.addHandler(console_handler)

    # 文件handler：输出日志到磁盘文件
    if not log_file:       #日志文件的存放路径
        # 没有传入自定义路径，则拼接日志根目录，生成带【日志器名称_时间戳】的日志文件名
        log_file = os.path.join(LOG_ROOT, f'{name}_{datetime.now().strftime("%Y%m%d-%H%M%S")}.log')

    # 创建文件处理器，utf‑8编码写入日志文件
    file_handler = logging.FileHandler(log_file,encoding='utf-8')
    # 设置文件输出日志级别
    file_handler.setLevel(file_level)
    # 设置文件输出日志格式
    file_handler.setFormatter(DEFAULT_LOG_FORMAT)
    # 将文件处理器挂载到logger对象
    logger.addHandler(file_handler)



    return logger

#快捷获取日志器，使用默认参数创建全局logger实例，其他模块import直接使用
logger = get_logger()


if __name__ == "__main__":
    # 当前脚本直接运行，测试各级别日志输出；被import导入不执行
    logger.info("信息日志")
    logger.error("错误日志")
    logger.warning("警告日志")
    logger.debug("调试日志")