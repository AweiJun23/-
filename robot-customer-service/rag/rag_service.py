"""
总结服务类：用户提问，搜索参数资料，将提问的参考资料提交给模型，让模型总结回复
"""
# LangChain文档对象，每一块知识库切片都封装为Document，包含page_content正文、metadata元数据


#
# 该文件是 RAG 检索增强生成业务服务模块，封装整套 RAG业务逻辑：向量知识库检索、上下文拼接、prompt模板组装、调用大模型生成回答。
# RagSummarizerService：RAG业务服务类
# 内部构建 LangChain 链式执行链：PromptTemplate → 调试打印prompt钩子 → 大模型 → 字符串输出解析器
# retriever_docs()：执行向量检索，召回相关 Document 切片
# rag_summarize()：RAG对外主方法，完成检索、拼接上下文、调用 chain 得到最终回答
# print_prompt()调试钩子，打印最终组装完成发给大模型的完整 prompt，方便排查 prompt问题。
# 会被agent_tools.py包装成@tool工具，供 ReAct 智能体调用。
#




from langchain_core.documents import Document
# 输出解析器：把大模型返回的消息对象提取为纯字符串文本
from langchain_core.output_parsers import StrOutputParser
# 导入向量存储服务，负责向量库检索、获取检索器
from rag.vector_store import VectorStoreService
# 工具函数：读取外部prompt文本文件，拿到RAG提示词模板字符串
from utils.prompt_loader import load_rag_prompts
# LangChain提示词模板类，用来填充变量 {input} {context}
from langchain_core.prompts import PromptTemplate
# 从模型工厂导入对话模型实例、Runnable包装适配器
from model.factory import chat_model


def print_prompt(prompt):
    """
    自定义中间Runnable钩子函数，打印组装完成后的完整prompt，方便调试
    :param prompt: StringPromptValue对象，管道流动过来的提示词对象
    :return: 原prompt对象，继续向后传递给大模型
    """
    # 打印20个等号作为分隔线
    print("="*20)
    # 将prompt对象转为字符串打印，看实际发给大模型的完整提示词
    print(prompt.to_string())
    print("="*20)
    # 原样返回prompt，交给管道下一个环节（传给大模型）
    return prompt


class RagSummarizerService(object):
    """RAG检索增强生成业务服务类：检索知识库 + 组装上下文 + 调用大模型回答问题"""
    def __init__(self):#Python类 （class） 的构造方
        # 实例化向量库服务对象，内部封装Chroma向量库
        self.vector_store = VectorStoreService()
        # 获取向量检索器：根据用户query向量，召回相似度最高的Document文档切片
        self.retriever=self.vector_store.get_retriever()
        # 加载外部文件里的RAG提示词模板文本
        self.prompt_text=load_rag_prompts()
        # 使用模板字符串创建PromptTemplate对象，支持{input}、{context}占位符变量替换
        # self.prompt_text对象身上保存的原始提示字符串，就是你的大模型系统提示词，里面可以带占位符
        self.prompt_template=PromptTemplate.from_template(self.prompt_text)
        # 拿到原始的通义对话模型实例（SimpleDashChat对象）
        self.model= chat_model
        # 初始化LangChain链式流水线，赋值给实例属性
        self.chain=self.__init__chain()

    def __init__chain(self):
        """私有方法：构建RAG完整执行链 prompt ->打印prompt->大模型->字符串解析"""
        # 构建链式流水线：
        # 1.prompt_template：填充{input}{context}生成prompt对象
        # 2.print_prompt：钩子打印完整prompt调试
        # 3.self.model：调用阿里云通义大模型
        # 4.StrOutputParser()：把模型输出解析为普通字符串
        chain=self.prompt_template | print_prompt | self.model | StrOutputParser()
        # 返回组装好的chain对象
        return chain

    def retriever_docs(self,query:str)->list[Document]:
        """
        根据用户问题执行向量检索，召回知识库文档切片
        :param query: 用户提问字符串
        :return: list[Document] 召回的文档对象列表
        """
        # 调用检索器invoke，根据query向量相似度检索，返回Document列表
        return self.retriever.invoke(query)

    def rag_summarize(self,query:str)->str:
        """
        RAG主业务方法：检索知识库、拼接参考上下文、执行链式调用返回回答
        :param query: 用户原始提问
        :return: 大模型生成的回答文本
        """
        # 根据用户query，从向量库召回相关文档切片
        context_docs=self.retriever_docs(query)

        # 初始化拼接参考材料的字符串
        context=""
        # 计数器，标记第几份参考材料
        counter=0
        # 遍历召回的每一份Document文档
        for doc in context_docs:
            counter+=1
            # 拼接参考材料序号、文档正文page_content、文档元数据metadata，全部塞到context字符串
            context+=f"[参考材料{counter}]:参考材料:{doc.page_content} | 参考数据：{doc.metadata}\n\n"

        # 执行chain流水线，传入字典参数，替换模板中的 {input} {context}
        return self.chain.invoke(
            {
                "input":query,   # 用户问题，填充模板中input占位符
                "context":context # 拼接好的全部参考知识库材料，填充模板context占位符
            }
        )


if __name__ == "__main__":
    # 程序入口，实例化RAG服务
    rag = RagSummarizerService()
    # 调用RAG方法提问，打印大模型返回结果
    print(rag.rag_summarize("小户型适合哪些扫地机器人"))