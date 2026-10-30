

#
# 这是一份Streamlit前端页面代码，快速搭建一个浏览器网页，用来可视化调试你的ReactAgent智能体。
# 不用写 FastAPI、不用写前端 JS，直接打开网页就能和大模型 Agent 聊天。
# 功能：
# 网页聊天界面，支持聊天历史保存，页面刷新智能体实例不会重复创建；
# 调用后端ReactAgent的execute_stream获取流式输出；
# 实现打字机逐字输出效果；
# 使用st.session_state保存智能体实例、全部聊天记录。





import time
import streamlit as st
from agent.tools.react_agent import ReactAgent


#标题
st.title("React Agent")   # 在页面渲染一级标题，显示文字React Agent
st.divider()             # 在页面画一条水平分割线，做界面排版分隔


if "agent" not in st.session_state:                     # 判断会话状态中是否不存在key为agent的对象
    st.session_state["agent"] = ReactAgent()            # 会话不存在agent，则实例化ReactAgent智能体存入session_state，页面刷新不会重复创建


if "message" not in st.session_state:                   # 判断会话状态中是否不存在key为message的聊天记录
    st.session_state["message"] = []                    # 初始化message为空列表，用来存放完整对话历史


for message in st.session_state["message"]:             # 循环遍历会话中保存的全部聊天历史
    st.chat_message(message["role"]).write(message["content"])  # 根据role区分用户/助手气泡，把历史消息渲染到页面


prompt=st.chat_input()                                  # 渲染底部聊天输入框，获取用户输入的文本，没有输入返回None


if prompt:                                              # 判断：当用户输入不为空，进入对话处理逻辑
    st.chat_message("user").write(prompt)                # 在页面渲染用户消息气泡，展示用户输入内容
    st.session_state["message"].append({"role":"user","content":prompt})  # 将用户消息追加存入session_state聊天记录


    response_messages=[]                                # 定义空列表，用来缓存流式返回的全部chunk片段
    with st.spinner("Loading model..."):                # 上下文管理器：模型执行期间页面显示加载转圈提示“Loading model...”
        res_stream=st.session_state["agent"].execute_stream(prompt)  # 调用智能体execute_stream，拿到生成器流式对象


        def capture(generator,cache_list):              # 定义生成器包装函数；参数generator：agent返回的流，cache_list：缓存片段的列表
            for chunk in generator:                     # 循环迭代agent流式输出的每一块数据chunk
                cache_list.append(chunk)                # 将每一块chunk存入缓存列表，用于后续保存完整回答
                for char in chunk:                       # 把chunk字符串拆解成单个字符
                    time.sleep(0.01)                     # 每个字符间隔0.01秒，模拟打字机输出效果
                    yield char                           # yield单个字符，交给streamlit write_stream做流式渲染


        st.chat_message("assistant").write_stream(capture(res_stream,response_messages)) # 渲染助手气泡，传入包装后的生成器，页面实现打字效果
        st.session_state["message"].append({"role":"assistant","content":response_messages[-1]}) # 把最后完整的回答存入会话聊天历史
        st.rerun()                                       # 触发页面强制重新运行，刷新界面，保证历史消息完整显示





