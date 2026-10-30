

#
# 这是大模型对象工厂类文件（模型工厂）
# 使用抽象工厂模式，统一创建通义千问对话大模型 (ChatTongyi)和通义向量嵌入模型 (DashScopeEmbeddings)。
# 项目其他地方直接导入 chat_model、embed_model 就可以直接使用，不用到处写模型初始化代码。
#
#




from abc import ABC, abstractmethod
from typing import Optional
from langchain_core.embeddings import Embeddings
from langchain_community.chat_models.tongyi import BaseChatModel
from langchain_community.embeddings import DashScopeEmbeddings
from langchain_community.chat_models.tongyi import ChatTongyi
from utils.config_handle import rag_config
import os
from dotenv import load_dotenv

# ⚠️在本文件最顶部先load_dotenv！！
load_dotenv()

class BaseModelFactory(ABC):
    @abstractmethod
    def generator(self) -> Optional[Embeddings | BaseChatModel]:
        pass



class ChatModelFactory(BaseModelFactory):
    def generator(self)-> Optional[Embeddings | BaseChatModel]:
        api_key = os.getenv("DASHSCOPE_API_KEY")
        return ChatTongyi(model=rag_config ["chat_model_name"])




class EmbeddingsFactory(BaseModelFactory):
    def generator (self) -> Optional[Embeddings | BaseChatModel]:
        api_key = os.getenv("DASHSCOPE_API_KEY")
        return DashScopeEmbeddings(model=rag_config["embedding_model_name"])



# 实例化工厂对象，调用generator()方法，生成全局可用对话模型实例
chat_model = ChatModelFactory().generator()
# 实例化工厂对象，调用generator()方法，生成全局可用向量模型实例
embed_model = EmbeddingsFactory().generator()