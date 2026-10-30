

#
# 这是 RAG 知识库向量库服务 VectorStoreService，专门做本地知识库加载、文档切片、向量化入库、向量检索。
# 底层使用 Chroma 轻量级本地向量数据库，搭配通义的 embed_model向量模型，给你的 Agent 提供知识库检索能力。
# 简单说：读取 PDF/TXT 知识库文件 →切成小块 →生成向量存入本地 Chroma 库 →大模型提问的时候，可以去知识库检索相关片段给大模型做参考。
#



# 导入操作系统模块，用于判断文件是否存在、文件路径相关操作
import os
# 从工具模块导入文件处理工具：pdf读取、txt读取、获取指定后缀文件列表、获取文件md5哈希值
from utils.file_handler import (
    pdf_loader,
    txt_loader,
    listdir_with_allowed_type,
    get_file_md5_hex,
)
# 导入Chroma向量数据库，用来存储文本向量，实现知识库检索
from langchain_chroma import Chroma
# LangChain文档对象，每一份文档/文本片段都封装成Document对象，包含page_content页面内容、metadata元数据
from langchain_core.documents import Document
# 项目日志工具，用来输出info/warning/error运行日志
from utils.logger_handler import logger
# 路径工具：把相对路径转换成绝对路径，Windows/macOS/Linux路径兼容
from utils.path_tool import get_abs_path
# 导入全局实例embed_model：我们自己封装好的阿里云通义向量模型
from model.factory import embed_model
# 读取chroma知识库相关yaml配置
from utils.config_handle import chroma_config
# LangChain文本分割器：递归字符文本切分器，把长文档切成多个小片段，用于向量化
from langchain_text_splitters import RecursiveCharacterTextSplitter


class VectorStoreService:
    """向量库服务类，封装知识库初始化、文档加载入库、检索全套逻辑"""
    def __init__(self):
        """类初始化：创建Chroma向量库实例，初始化文本切分器"""
        # 实例化Chroma向量数据库对象
        self.vector_store = Chroma(
            collection_name=chroma_config["collection_name"],  # 向量集合名称，相当于数据库表名
            embedding_function=embed_model,                     # 指定向量模型，把文本转向量
            persist_directory=chroma_config["persist_directory"],# 向量持久化本地磁盘文件夹
        )

        # 实例化递归文本切分器，用来把长PDF/TXT切分成小块
        self.spliter = RecursiveCharacterTextSplitter(
            chunk_size=chroma_config["chunk_size"],      # 每个切片最大字符长度
            separators=chroma_config["separators"],       # 切分优先分隔符：换行、句号、逗号等
            length_function=len,                          # 使用len()计算文本字符长度
        )

    def get_retriever(self):
        """获取检索器对象，用于RAG召回知识库片段"""
        # 把Chroma转为Retriever检索器；k代表返回相似度最高的k条结果
        return self.vector_store.as_retriever(search_kwargs={"k": chroma_config["k"]})

    def load_document(self):
        """加载知识库入口函数：扫描目录，读取pdf/txt，去重，切分，写入向量库"""

        def check_md5_hex(md5_for_check: str):
            """内部函数：根据文件md5哈希，判断该文件是否已经导入过知识库，实现去重"""
            # 获取md5记录文件的绝对路径
            md5_file = get_abs_path(chroma_config["md5_hex_store"])
            # 如果md5记录文件不存在，则新建空文件，返回False代表文件未导入
            if not os.path.exists(md5_file):
                open(md5_file, "w", encoding="utf-8").close()
                return False

            # 读取md5记录文件，逐行比对哈希值
            with open(md5_file, encoding="utf-8") as f:
                for line in f.readlines():
                    line = line.strip()   # 去掉换行、前后空格
                    if line == md5_for_check:
                        return True      # md5命中 → 文件已经导入过
                return False             # 遍历完成没有命中 → 文件未导入

        def save_md5_hex(md5_for_check: str):
            """内部函数：文件成功入库之后，把md5写入记录文件，下次运行可以跳过该文件"""
            md5_file = get_abs_path(chroma_config["md5_hex_store"])
            # 追加模式a：在文件末尾写入一行md5哈希
            with open(md5_file, "a", encoding="utf-8") as f:
                f.write(md5_for_check + "\n")

        def get_file_documents(read_path: str):
            """内部函数：根据文件后缀，调用对应加载器，返回Document文档列表"""
            if read_path.endswith(".txt"):
                return txt_loader(read_path)  # txt文件调用txt加载器
            if read_path.endswith(".pdf"):
                return pdf_loader(read_path)  # pdf文件调用pdf加载器
            return []                         # 不支持的后缀返回空列表

        # 获取知识库目录下所有允许后缀的文件路径列表
        allowed_files_path: list[str] = listdir_with_allowed_type(
            get_abs_path(chroma_config["data_path"]),                     # 知识库源文件目录绝对路径
            tuple(chroma_config.get("allow_knowledge_file_type", [".txt", ".pdf"])), # 允许的文件后缀，配置缺失给默认值
        )

        # 循环遍历每一个待处理知识库文件
        for path in allowed_files_path:
            # 计算当前文件md5哈希，用来判断是否重复导入
            md5_hex = get_file_md5_hex(path)
            # 如果md5已经存在记录，打印日志，直接跳过该文件
            if check_md5_hex(md5_hex):
                logger.info(f"[加载知识库]{path}内容已存在知识库中，跳过")
                continue

            # try捕获单个文件异常，一个文件损坏不影响其他文件继续导入
            try:
                # 读取文件内容，得到Document对象列表
                documents: list[Document] = get_file_documents(path)
                # 判断读取结果为空，打印警告，跳过
                if not documents:
                    logger.warning(f"[加载知识库]{path}读取文档为空，跳过")
                    continue

                # 使用切分器，把原始大Document切分成多个小Document片段
                split_document: list[Document] = self.spliter.split_documents(documents)
                # 切分后结果为空，打印警告，跳过
                if not split_document:
                    logger.warning(f"[加载知识库]{path}切分后文档为空，跳过")
                    continue

                # 将切分后的文档片段批量添加进Chroma向量库，内部会调用embed_model生成向量
                self.vector_store.add_documents(split_document)

                # 入库成功，保存该文件md5，下次启动不会重复导入
                save_md5_hex(md5_hex)
                logger.info(f"[加载知识库]{path}内容加载成功")

            except Exception as e:
                # 捕获异常，打印完整错误堆栈exc_info=True，继续处理下一个文件
                logger.error(f"[加载知识库]{path}加载失败：{str(e)}", exc_info=True)
                continue


# 脚本直接运行时执行下面代码；被其他模块import导入时，这部分不会执行
if __name__ == "__main__":
    vs = VectorStoreService()          # 实例化向量库服务
    vs.load_document()                 # 执行知识库全部文档加载入库
    retriever = vs.get_retriever()     # 获取检索器
    res = retriever.invoke("迷路")      # 测试检索：传入查询词，召回相关知识库片段
    for r in res:                      # 遍历打印每一条召回结果
        print(r.page_content)          # 打印文档片段文本内容
        print("." * 20)                # 打印分割线，方便看结果