# 导入操作系统模块，用于判断文件是否存在
import os
# 导入日志处理工具，用于输出警告、运行日志
from utils.logger_handler import logger
# langchain工具装饰器，将普通函数包装为大模型可调用工具
from langchain_core.tools import tool
# 读取项目yaml配置，获取配置参数
from utils.config_handle import agent_config
# RAG检索总结服务类，实现知识库向量检索功能
from rag.rag_service import RagSummarizerService
# random随机模块，用于随机选取id、周期、城市
import random
# 路径工具，将相对路径转换为项目绝对路径
from utils.path_tool import get_abs_path

# 实例化RAG服务对象，后续工具调用该对象的方法完成检索
rag = RagSummarizerService()

# 模拟可用的用户ID列表
user_ids=["1001","1002","1003","1004","1005","1006","1007"]
# 模拟观测周期候选列表
month_arr=["第1周期","第2周期","第3周期","第4周期"]
# 全局缓存字典，存放解析csv得到的外部业务数据
external_data={}


# 工具装饰器，声明该函数是大模型可以调用的工具，附带工具描述
@tool(description="从向量存储中检索参考资料")
def rag_summarize(query:str)->str:
    # 调用rag对象的rag_summarize方法，执行向量知识库检索总结，返回字符串结果
    return rag.rag_summarize(query)


# 工具装饰器，模拟获取城市天气的工具
@tool(description="获取指定城市的天气，以消息字符串的形式返回")
def get_weather(city:str)->str:
    # 返回写死的模拟天气信息字符串，city为传入的城市参数
    return f"城市{city}天气为晴天，气温26°C，空气湿度50%，南风1级，AQI 21, 最近6小时降雨概率极低"


# 工具装饰器，模拟获取用户所在城市工具
@tool(description="读取用户所在城市的名称以纯字符串形式返回")
def get_user_location()->str:
    # 从城市列表随机选一个城市返回
    return random.choice(["深圳","北京","杭州"])


# 工具装饰器，模拟获取用户ID工具
@tool(description="获取用户的ID，以纯字符串形式返回")
def get_user_id(id:str)->str:
    # 从user_ids列表随机挑选一个用户id返回，入参id未实际使用
    return random.choice(user_ids)


# 工具装饰器，模拟获取当前观测周期工具
@tool(description="获取当前月份,以纯字符串形式返回")
def get_current_month()->str:
    # 从周期列表随机选取一个周期返回
    return random.choice(month_arr)


def generate_external_data():
    """
    {
          "user_id":{
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              ...
          },
          "user_id":{
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              ...
          },
          "user_id":{
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              "month":{"特征": XXX,"效率": XXX, ...}
              ...
          }


    }


    """
    # 判断全局external_data是否为空，空则代表还未加载csv数据，执行加载逻辑；不为空跳过加载
    if not external_data:
        # 读取配置中的csv文件路径，转换为项目绝对路径
        external_data_path=get_abs_path(agent_config["external_data_path"])

        # 判断csv文件是否存在，不存在抛出文件找不到异常
        if not os.path.exists(external_data_path):
            raise FileNotFoundError(f"外部数据文件{external_data_path}不存在")

        # 以utf‑8编码打开csv文件，f为文件句柄
        with open(external_data_path,"r",encoding="utf-8") as f:
            # readlines读取全部行；[1:]跳过表头行，循环遍历每一条数据行
            for line in f.readlines()[1:]:
                # strip去除行首尾换行、空格；split按逗号分割字符串，得到字段列表arr
                arr: list[str]=line.strip().split(",")

                # 取下标0的用户id字段，替换掉双引号，赋值给user_id变量
                user_id:str=arr[0].replace('"',"")
                # 取下标1的特征字段，去除双引号
                feature:str=arr[1].replace('"',"")
                # 取下标2的清洁效率字段，去除双引号
                efficiency:str=arr[2].replace('"',"")
                # 取下标3的耗材字段，去除双引号
                consumable:str=arr[3].replace('"',"")
                # 取下标4的对比字段，去除双引号
                comparison:str=arr[4].replace('"',"")
                # 取下标5的时间周期字段，去除双引号
                time:str=arr[5].replace('"',"")

                # 如果当前用户id不在全局缓存external_data中，则新建该用户的子字典
                if user_id not in external_data:
                    external_data[user_id]={}

                # 将解析出来的一条记录存入全局缓存，key为周期time，value为包含4个字段的字典
                external_data[user_id][time]={
                    "特征":feature,
                    "效率":efficiency,
                    "耗材":consumable,
                    "对比":comparison,
                }


# 工具装饰器，作为langchain工具，根据用户id和周期读取外部csv记录
@tool(description="从外部系统中获取用户的使用记录，以纯字符串形式返回,如果未检索到返回空")
def fetch_external_data(user_id:str,month:str)->str:
    # 调用函数，加载csv数据到全局缓存external_data
    generate_external_data()

    try:
        # 从全局缓存取出对应user_id、month的记录字典，直接返回
        return external_data[user_id][month]
    except KeyError:
        # 捕获key不存在异常，打印警告日志，返回空字符串
        logger.warning(f"[fetch_external_data]未能检索到用户：{user_id}在{month}的使用记录")
        return ""



@tool(description="无入参，无返回值，调用后触发中间件，最终为报告生成的场景动态注入上下文信息，为后续提示词切换提供上下文信息")
def fill_context_for_report():
    return "fill_context_for_report已调用"






# python脚本入口，直接运行本文件时执行该代码块；import导入本文件时不会执行
if __name__ == "__main__":
    # 测试调用：查询用户1001，第1周期的数据，打印返回结果
    print(fetch_external_data("1001","第1周期"))
    # 测试调用：查询不存在用户1011，第1周期，触发KeyError分支，打印空并输出警告日志
    print(fetch_external_data("1011", "第1周期"))

