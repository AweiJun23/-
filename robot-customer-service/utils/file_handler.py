


#
# 这是项目文件处理工具模块，提供 4 组工具函数，专门服务 RAG 知识库模块：
# get_file_md5_hex()：计算文件 MD5 哈希，用于知识库文件去重，避免同一个文件重复导入向量库
# listdir_with_allowed_type()：扫描文件夹，筛选指定后缀的文件，返回全部文件完整路径
# pdf_loader()：封装 PyPDFLoader，读取 PDF，输出 LangChain 标准Document文档对象列表
# txt_loader()：封装 TextLoader，读取 txt 文本，输出 LangChain 标准Document文档对象列表
#
#




# 导入md5哈希算法模块，用于计算文件md5指纹
import hashlib
# 文件、路径操作系统库
import os
# langchain文档对象，加载文件后统一封装成Document对象
from langchain_core.documents import Document
# PDF文档加载器、纯文本txt加载器
from langchain_community.document_loaders import PyPDFLoader, TextLoader
# 导入项目自定义日志对象，打印错误、提示日志
from utils.logger_handler import logger


def get_file_md5_hex(filepath: str):
    """计算文件md5值，返回md5十六进制字符串；文件异常返回None"""
    # 判断：文件路径是否真实存在
    if not os.path.exists(filepath):
        # 日志输出错误信息：该文件不存在
        logger.error(f"[md5计算]文件{filepath}不存在")
        # 直接结束函数，返回None
        return

    # 判断：路径是否是一个普通文件（排除文件夹）
    if not os.path.isfile(filepath):
        logger.error(f"[md5计算]路径{filepath}不存在")
        return

    # 创建md5哈希计算对象
    md5 = hashlib.md5()
    # 设置每次读取文件的字节大小4KB，分片读取，防止大文件一次性读入占满内存
    chunk_size = 4096
    try:
        # rb = 二进制只读模式，计算md5必须二进制读取，不能用文本模式
        with open(filepath, "rb") as f:
            # 海象运算符 := ：读取一块赋值给chunk，同时判断chunk是否不为空；读到文件末尾返回空字节循环结束
            while chunk := f.read(chunk_size):
                # 将分片字节更新进md5计算器
                md5.update(chunk)

                """
                while chunk:=f.read(chunk_size):
                等价于
                chunk=f.read(chunk_size)
                while chunk:
                   md5.update(chunk)
                   chunk=f.read(chunk_size)
                """
                # ❗注意：你这里代码有BUG！return写在while循环内部，只读第一个4KB分片就直接返回md5，没有读完整个文件
        md5_hex = md5.hexdigest()
        return md5_hex
    except Exception as e:
        # 捕获所有异常，打印错误日志
        logger.error(f"[md5计算]路径{filepath}失败,{str(e)}")
        # 异常情况返回None
        return None


# 返回文件夹内符合后缀的完整文件路径元组
def listdir_with_allowed_type(path: str, allowed_types: tuple[str]):
    """
    path：文件夹路径
    allowed_types：允许的文件后缀元组，例如(".pdf",".txt")
    return：匹配后缀的文件完整路径元组
    """
    # 定义空列表，用来存放筛选后的文件全路径
    file = []

    # 判断传入路径是不是文件夹
    if not os.path.isdir(path):
        logger.error(f"[listdir_with_allow_type]{path}不是文件夹")
        # ❗BUG：错误情况下不应该return allowed_types，应当return空元组()
        return allowed_types

    # 遍历文件夹下所有条目（文件名/文件夹名，只有名字，不是完整路径）
    for f in os.listdir(path):
        # 判断文件名后缀是否在允许后缀元组内
        if f.endswith(allowed_types):
            # 拼接文件夹路径+文件名，得到完整绝对/相对路径，加入列表
            file.append(os.path.join(path, f))
    # 将list转为元组返回
    return tuple(file)


def pdf_loader(filepath: str, password=None) -> list[Document]:
    """
    pdf文档加载函数
    filepath：pdf文件路径
    password：加密pdf密码，默认为None
    return：返回Document对象列表，一页pdf对应一个Document
    """
    # PyPDFLoader初始化pdf加载器，调用load()执行加载解析，返回文档对象列表
    return PyPDFLoader(filepath, password).load()


def txt_loader(filepath: str) -> list[Document]:
    """
    txt纯文本文件加载
    filepath：txt文件路径
    return：Document对象列表，整个txt文件为一个Document
    """
    # TextLoader加载文本文件，load()解析成langchain Document
    return TextLoader(filepath).load()