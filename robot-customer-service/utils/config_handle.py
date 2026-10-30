"""
yaml格式
这是项目配置读取工具文件。
专门读取项目下 config/目录下4个yaml 配置文件，把 yaml 配置解析成 python 字典，
全局导出 rag_config、chroma_config、prompts_config、agent_config。
项目其他模块直接from utils.config_handle import rag_config就可以拿到配置字典，不用到处写读取 yaml 的重复代码。
注意：文件顶层直接执行加载函数，只要本文件被别的模块import导入，就会立刻读取解析4份yaml 文件。



"""

# 导入pyyaml库，用于解析yaml配置文件
import yaml
# 导入路径工具函数get_abs_path，把相对路径转为绝对路径，兼容Windows/Linux/macOS系统
from utils.path_tool import get_abs_path


# 加载rag相关配置，读取config/rag.yml
def load_rag_config(config_path:str=get_abs_path("config/rag.yml"),encoding='utf-8'):
    # 打开配置文件，只读模式，指定utf‑8编码
    with open(config_path,'r',encoding=encoding) as f:
         # 使用FullLoader安全加载yaml，解析yaml内容返回python字典对象
         return yaml.load(f,Loader=yaml.FullLoader)


# 加载向量库chroma配置，读取config/chroma.yml
def load_chroma_config(config_path:str=get_abs_path("config/chroma.yml"),encoding='utf-8'):
    with open(config_path,'r',encoding=encoding) as f:
         return yaml.load(f,Loader=yaml.FullLoader)


# 加载提示词prompt配置，读取config/prompts.yml
def load_prompts_config(config_path:str=get_abs_path("config/prompts.yml"),encoding='utf-8'):
    with open(config_path,'r',encoding=encoding) as f:
         return yaml.load(f,Loader=yaml.FullLoader)


# 加载agent智能体相关配置，读取config/agent.yml
def load_agent_config(config_path:str=get_abs_path("config/agent.yml"),encoding='utf-8'):
    with open(config_path,'r',encoding=encoding) as f:
         return yaml.load(f,Loader=yaml.FullLoader)


# 调用函数读取rag.yml，赋值全局变量rag_config；其他文件import本模块直接使用该变量
# 只要别的文件 import utils.config_handle，就立刻执行文件顶层代码，直接读取 yaml。
# 路径一旦算错，rag_config变成字符串，报字符串索引必须是整数。
rag_config = load_rag_config()
# 读取chroma.yml，全局向量库配置对象
chroma_config = load_chroma_config()
# 读取prompts.yml，全局提示词配置对象
prompts_config = load_prompts_config()
# 读取agent.yml，全局智能体配置对象
agent_config = load_agent_config()


# 当前脚本直接运行时执行，作为测试代码；被其他文件import导入时，这段不会执行
if __name__ == '__main__':
    # 打印rag配置里面chat_model_name字段，用来测试配置文件读取是否正常
    print(rag_config["chat_model_name"])