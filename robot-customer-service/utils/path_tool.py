
#
# 为整个工程提供统一的绝对路径

#
# 这是项目路径工具模块。
# 用于统一管理项目路径，解决不同工作目录启动程序带来的相对路径错乱问题：
# get_project_root()：通过当前 py 文件的位置向上找一级，得到项目根目录绝对路径
# get_abs_path()：接收项目内的相对路径，拼接项目根目录，返回完整绝对路径。
# 项目其他模块读取配置、提示词、日志目录、知识库文件全部调用该函数做路径转换，保证无论在哪种工作目录启动程序，文件都可以正常找到。
#
# 前提约定：本文件放在 utils/path_tool.py，所以向上退一层就是项目根目录。
#
#




import os



def get_project_root()->str:
    """
    获取工程所在的根目录
    :return:
    """
    # python内置变量，当前这一份代码文件的绝对路径
    current_file=os.path.abspath(__file__)
    # 获取工程的根目录，先获取文件所在的文件夹绝对路径
    current_dir=os.path.dirname(current_file)
#     获取工程根目录
    project_root=os.path.dirname(current_dir)

    return project_root


def get_abs_path(relative_path:str)->str:
    """
    传递相对路径，得到绝对路径
    :param relative_path:相对路径
    :return:绝对路径
    """
    project_root=get_project_root()
    return os.path.join(project_root,relative_path)



if __name__ == '__main__':
    print(get_abs_path("config/config.txt"))
