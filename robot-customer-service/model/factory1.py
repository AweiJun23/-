
# 导入抽象基类模块，用于定义抽象工厂模板
from abc import ABC, abstractmethod
# 类型提示：可选类型、列表类型
from typing import Optional, List
from typing import Any

from langchain_core.prompt_values import StringPromptValue
from langchain_core.runnables import Runnable
from langchain_core.messages import BaseMessage
# LangChain 核心抽象接口：Embeddings，自定义向量类必须实现该接口，才能被Chroma向量库识别调用
from langchain_core.embeddings import Embeddings
# 导入阿里云dashscope官方SDK
import dashscope
# 导入通义大模型生成模块，用于对话调用
from dashscope import Generation

# 读取yaml配置文件，获取模型密钥、模型名称等配置
from utils.config_handle import rag_config


class MyDashEmbedding(Embeddings):
    """
    自定义通义向量模型类
    继承LangChain的Embeddings抽象类，实现标准接口，替换废弃的DashscopeEmbeddings
    解决langchain‑dashscope依赖tiktoken导致Windows编译失败问题
    """
    def __init__(self, api_key: str, model: str):
        """
        :param api_key: 阿里云百炼API‑KEY
        :param model: 向量模型名称，例如 text‑embedding‑v3
        """
        # 设置dashscope全局密钥，后续所有dashscope接口都会自动读取这个密钥
        dashscope.api_key = api_key
        # 保存向量模型名字，后续调用接口时使用
        self.model = model
        # 阿里云向量接口限制：单次最多只能传10条文本，设置批次上限
        self._batch_limit = 10

    def embed_documents(self, texts: List[str]) -> List[List[float]]:
        """
        LangChain标准接口：批量对一批文档文本生成向量
        :param texts: 字符串列表，多个文档切片文本
        :return: 返回二维列表，每一条文本对应一组浮点向量
        """
        # 存储全部文本最终向量结果
        all_embeds = []
        # 循环分片，按照 _batch_limit=10 对文本列表切片，规避接口单次条数限制
        for i in range(0, len(texts), self._batch_limit):
            # 截取一小批文本，最多10条
            batch_texts = texts[i:i + self._batch_limit]
            # 调用阿里云向量接口，传入当前批次文本
            resp = dashscope.TextEmbedding.call(model=self.model, input=batch_texts)
            # 打印调试信息：当前处理分片下标、http状态码
            print(f"【向量接口】处理分片 {i}~{i+len(batch_texts)}, status={resp.status_code}")

            # 判断接口是否调用失败，状态码非200或者返回数据为空则抛出异常
            if resp.status_code != 200 or resp.output is None:
                raise RuntimeError(
                    f"向量接口调用失败！code:{resp.status_code}，message:{resp.message}"
                )
            # 提取本批次返回的向量数据，遍历返回结果取出embedding数组
            batch_embeds = [item["embedding"] for item in resp.output["embeddings"]]
            # 将本批次向量追加到总结果列表
            all_embeds.extend(batch_embeds)
        # 返回全部文本对应的向量集合
        return all_embeds

    def embed_query(self, text: str) -> List[float]:
        """
        LangChain标准接口：对用户的单个查询语句生成向量（检索时调用）
        :param text: 用户查询字符串
        :return: 一维向量数组
        """
        # 复用批量接口，传入单条文本，取返回列表第一个向量
        return self.embed_documents([text])[0]


# ---------------- 不再继承 BaseChatModel，避开Pydantic校验坑 ----------------
class SimpleDashChat:
    """
    自定义大模型对话封装类
    不继承LangChain BaseChatModel，规避Pydantic v2字段校验报错
    模拟langchain风格invoke方法，上层业务直接调用
    """

    def bind_tools(self, tools, **kwargs):
        # 兼容create_agent工厂需要的接口，原样返回self
        return self




    def __init__(self, api_key: str, model: str):
        """
        :param api_key: 阿里云百炼密钥
        :param model: 大模型名称，如 qwen3.5‑plus
        """
        # 设置dashscope全局密钥
        dashscope.api_key = api_key
        # 保存要调用的大模型名称
        self.model = model

    def invoke(self, messages: list):
        """
        模拟langchain的invoke调用方法
        :param messages: langchain消息对象列表，包含HumanMessage/AIMessage
        :return: 返回大模型输出文本字符串
        """
        msg_list = []
        # 遍历langchain消息对象，转换为dashscope需要的 {"role":"xxx","content":"xxx"} 格式
        for m in messages:
            # human消息 → user角色；其他 → assistant角色
            role = "user" if m.type == "human" else "assistant"
            # 组装dashscope要求的消息字典
            msg_list.append({"role": role, "content": m.content})
        # 调用阿里云大模型生成接口，result_format="message"使用消息格式返回结果
        resp = Generation.call(
            model=self.model,
            messages=msg_list,
            result_format="message"
        )
        # 取出大模型返回文本内容直接返回
        return resp.output.choices[0].message.content

    def invoke_raw_dict(self, msg_list: list[dict]) -> str:
        """直接接收dashscope原生格式消息字典，不需要langchain消息对象，给Runnable包装器使用"""
        resp = Generation.call(
            model=self.model, messages=msg_list, result_format="message"
        )
        # 返回模型输出文本
        return resp.output.choices[0].message.content




class RunnableDashChat(Runnable):
    """
    将自研 SimpleDashChat 包装为 LangChain Runnable，可直接用 | 管道
    不继承 BaseChatModel，无Pydantic校验问题
    """

    def __init__(self, dash_chat: SimpleDashChat):
        # 接收已经实例化好的对话模型对象
        self.dash_chat = dash_chat

    def invoke(self, input_data: Any, config: dict | None = None) -> str:
        """Runnable必须实现的invoke方法，适配langchain管道传入的多种输入类型"""
        # 普通PromptTemplate输出 StringPromptValue，是字符串包装对象
        if isinstance(input_data, StringPromptValue):
            # 把prompt对象转为纯文本字符串
            prompt_text = input_data.to_string()
            # 组装单轮user消息字典
            msg_list = [{"role":"user", "content": prompt_text}]
            # 调用原生字典版本的对话方法
            return self.dash_chat.invoke_raw_dict(msg_list)

        elif isinstance(input_data, BaseMessage):
            # 如果传入单个Message对象，包装成列表
            messages = [input_data]
        elif isinstance(input_data, list):
            # 如果传入消息列表，直接赋值
            messages = input_data
        else:
            # 输入类型不支持，抛出类型异常
            raise TypeError(f"RunnableDashChat 仅支持消息列表/单Message/StringPromptValue，收到：{type(input_data)}")

        # 调用SimpleDashChat的invoke，传入langchain消息对象列表
        return self.dash_chat.invoke(messages)

# ---------------- 工厂类 ----------------
class BaseChatModelFactory(ABC):
    """抽象工厂基类，定义统一生成模型对象的接口模板"""
    @abstractmethod
    def generator(self) -> Optional[Embeddings | SimpleDashChat]:
        """抽象方法，子类必须实现，返回向量实例 或者 对话大模型实例"""
        pass


class ChatModelFactory(BaseChatModelFactory):
    """对话大模型工厂，负责创建SimpleDashChat实例"""
    def generator(self) -> Optional[Embeddings | SimpleDashChat]:
        # 读取配置文件密钥和模型名，实例化对话模型对象并返回
        return SimpleDashChat(
            api_key=rag_config["dashscope_api_key"],
            model=rag_config["chat_model_name"]
        )


class EmbeddingsModelFactory(BaseChatModelFactory):
    """向量模型工厂，负责创建MyDashEmbedding向量实例"""
    def generator(self) -> Optional[Embeddings | SimpleDashChat]:
        # 读取配置，实例化自定义向量对象返回
        return MyDashEmbedding(
            api_key=rag_config["dashscope_api_key"],
            model=rag_config["embedding_model_name"]
        )


# 实例化工厂对象，调用generator()方法，生成全局可用对话模型实例
chat_model = ChatModelFactory().generator()
# 实例化工厂对象，调用generator()方法，生成全局可用向量模型实例
embed_model = EmbeddingsModelFactory().generator()