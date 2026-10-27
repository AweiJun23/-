# 城乡农品轻量溯源与碳足迹存证系统

面向城乡农产品的**轻量级溯源 + 碳足迹核算 + 联盟链哈希存证**一体化平台。
后端采用 Java Spring Cloud 微服务（10 个服务 + 网关），智能体编排采用 Python（FastAPI + LangGraph），
多端覆盖：收购商 Web 后台（Vue3）、消费者扫码 H5（Vue3）、农户录入微信小程序、农户 APP（Flutter，离线缓存）。

---

## 一、架构总览

```
                         ┌─────────────────────────────────────────┐
   农户/合作社  ──▶ 微信小程序 / Flutter APP  (农事录入、离线缓存)
   收购商      ──▶ Vue3 Web 后台          (批次建档、溯源、碳报表)
   消费者      ──▶ Vue3 H5 扫码页         (溯源验真、碳标签)
                         │
                         ▼
                 ┌──────────────┐
                 │  API 网关     │  :9000  鉴权 / 路由 / 限流 / 跨域
                 │ (Spring GW)  │
                 └──────┬───────┘
        ┌───────────────┼───────────────────────────────┐
        ▼               ▼               ▼                ▼
   trace-auth      trace-farming    trace-batch      trace-carbon
   :8081 账号权限   :8082 农事记录   :8083 批次建档    :8084 碳核算
        │               │               │                │
        │  MQ 事件       │               │                │
        │ (fanout)       │               │                ▼
        │               ▼               │          trace-chain :8085
        │        trace-agent :8086 ─────┘          联盟链哈希存证
        │        (Java 编排)               trace-report :8087 补贴报表
        │               │                  trace-file  :8088 文件
        │               ▼
        │        python-agent :8000 (FastAPI + LangGraph 4 智能体)
        │          farming_validate / carbon_advice / subsidy_report / consumer_explain
        └──────────────▶ Nacos 注册中心 / RabbitMQ 事件总线 / MySQL / Redis
```

**数据流（农事录入 → 全链路触发）：**
1. 小程序/APP 提交农事记录 → `trace-farming` 保存。
2. 保存后向 `farming-event` 广播交换机发布事件（fanout），并行触发三条链路：
   - `trace-carbon` 消费者：按碳排因子库核算碳足迹并生成报告；
   - `trace-agent` 消费者：调用 `python-agent` 做农事 AI 校验；
   - `trace-chain` 消费者：对农事数据做哈希存证。
3. 碳报告生成后再向 `chain` 发布事件，`trace-chain` 对碳报告哈希存证。
4. 消费者扫码 → H5 调 `trace-batch /trace/{batchNo}` 聚合展示，并可调 `trace-chain /verify` 链上验真。

---

## 二、技术栈

| 层 | 技术 |
|----|------|
| 后端微服务 | Spring Boot 3.x、Spring Cloud Gateway、Nacos、OpenFeign、MyBatis-Plus |
| 事件总线 | RabbitMQ（fanout 广播交换机，保证三链路解耦） |
| 存证 | 本地轻量哈希链（可替换为 FISCO BCOS / WeBank 联盟链节点） |
| 数据库 | MySQL 8.x、Redis（限流/缓存） |
| 智能体 | Python 3.11+、FastAPI、LangGraph、langchain；无 LLM 时本地规则兜底 |
| 前端 | Vue3 + Vite + Element Plus（Web 后台）、Vue3 + Vite（H5） |
| 小程序 | 微信小程序原生 |
| APP | Flutter 3.x（含本地离线缓存 sqflite） |

---

## 三、模块与服务端口

| 模块 | 应用名 | 端口 | 职责 |
|------|--------|------|------|
| trace-gateway | trace-gateway | 9000 | 网关：鉴权、路由、限流、跨域 |
| trace-auth | trace-auth | 8081 | 账号、角色、登录（密码/微信）、JWT |
| trace-farming | trace-farming | 8082 | 农事记录、离线同步、MQ 生产 |
| trace-batch | trace-batch | 8083 | 批次建档、二维码、公开溯源聚合 |
| trace-carbon | trace-carbon | 8084 | 碳因子库、碳足迹核算、报告、MQ 消费 |
| trace-chain | trace-chain | 8085 | 联盟链哈希存证、验真 |
| trace-agent | trace-agent | 8086 | 智能体编排（调用 python-agent） |
| trace-report | trace-report | 8087 | 绿色补贴报表生成 |
| trace-file | trace-file | 8088 | 图片/附件上传下载 |
| python-agent | python-agent | 8000 | LangGraph 4 智能体（独立于 Java 可运行） |

**网关路由前缀**：所有 `/api/**` 经 `:9000` 统一接入，按服务名转发到上述微服务。
**免鉴权白名单**：`/api/auth/login|register|wechat/login`、`/api/batch/trace/**`、
`/api/chain/verify`、`/api/file/public/**`、`/files/**`、`/actuator/**`。

---

## 四、快速启动

### 1. 基础设施（MySQL / Redis / RabbitMQ / Nacos）
```bash
# 以 Docker 为例
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8
docker run -d --name redis -p 6379:6379 redis:7
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
docker run -d --name nacos -p 8848:8848 nacos/nacos-server:v2.2.3
```

### 2. 初始化数据库
```bash
mysql -uroot -p < sql/01_schema.sql      # 建库建表
mysql -uroot -p < sql/02_seed.sql        # 种子数据（含管理员 admin/123456、碳因子库）
```
详见 `sql/README.md`。

### 3. 启动后端微服务
使用 Maven 聚合构建（需要可访问 Maven 中央仓库的网络环境）：
```bash
cd 城乡农品轻量溯源与碳足迹存证系统
mvn -q clean package -DskipTests
# 依次启动（建议先 common 外的各服务）
java -jar trace-gateway/target/trace-gateway.jar &
java -jar trace-auth/target/trace-auth.jar &
# ... 其余服务
```
各服务 `application.yml` 中已配置 Nacos/RabbitMQ/MySQL/Redis 连接，按实际环境修改。

### 4. 启动 Python 智能体服务（可独立运行，无 LLM 也能兜底）
```bash
cd python-agent
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
# 可选：配置 .env 启用真实 LLM（OPENAI_API_KEY 等），不配则使用本地规则兜底
cp .env.example .env
uvicorn app.main:app --host 0.0.0.0 --port 8000
```
接口：`POST /api/agent`，body `{"agent": "carbon_advice", "payload": {...}}`。

### 5. 启动前端
```bash
# Web 后台（收购商）
cd web-admin && npm i && npm run dev      # 默认 5173

# 扫码 H5（消费者）
cd web-h5 && npm i && npm run dev          # 默认 5174

# 微信小程序（农户录入）
# 用微信开发者工具打开 miniprogram 目录，配置 appid 后预览

# Flutter APP（农户，含离线缓存）
cd flutter_app && flutter pub get && flutter run
```
> 前端 `baseURL` 默认指向网关 `http://localhost:9000`（H5 走相对路径，由网关同域代理）。

---

## 五、智能体（LangGraph）说明

`python-agent` 编排 4 个 Agent，由 `trace-agent` 通过 HTTP 调用：

| agent 名 | 触发场景 | 输出 |
|----------|----------|------|
| `farming_validate` | 农事记录保存后 | 逐条校验（施肥/用药超量、日期异常等），返回 `valid / abnormal` |
| `carbon_advice` | 碳报告生成后 | 减排优化建议（替换柴油、有机肥替代等） |
| `subsidy_report` | 补贴报表生成时 | 绿色补贴申报文案与核算 |
| `consumer_explain` | 消费者扫码时 | 通俗化碳标签解读 |

设计要点：**`USE_LLM=false` 时走本地规则兜底**，保证无大模型 API 也能完整跑通链路；
配置 LLM 后由 `langgraph` 状态机编排并对结果做润色。`graph.py` 中的 `_dispatch` 根据 agent 名路由，
`llm.py` 负责可选的 LLM 润色，`agents/*.py` 为各智能体的纯函数实现。

---

## 六、目录结构

```
城乡农品轻量溯源与碳足迹存证系统/
├── pom.xml                  # 父工程（聚合 10 个 Java 模块）
├── trace-common/            # 公共模块：统一返回、异常、JWT、常量、MQ 事件、拦截器
├── trace-gateway/           # 网关：鉴权/路由/限流/跨域
├── trace-auth/              # 认证授权
├── trace-farming/           # 农事记录（MQ 生产）
├── trace-batch/             # 批次建档 / 公开溯源聚合
├── trace-carbon/            # 碳足迹核算（MQ 消费）
├── trace-chain/             # 联盟链哈希存证与验真
├── trace-agent/             # 智能体编排（调用 python-agent）
├── trace-report/            # 绿色补贴报表
├── trace-file/              # 文件服务
├── python-agent/            # FastAPI + LangGraph 4 智能体（可独立运行）
├── sql/                     # 建库建表 + 种子数据 + 说明
├── web-admin/               # Vue3 收购商后台
├── web-h5/                  # Vue3 消费者扫码 H5
├── miniprogram/             # 微信小程序农户录入端
└── flutter_app/             # Flutter 农户 APP（离线缓存）
```

---

## 七、关键设计点

- **轻量溯源**：批次建档生成批次号 → 绑定二维码 → 扫码直达公开溯源聚合接口（免登录）。
- **碳足迹核算**：维护 `carbon_factor` 碳排因子库，按农事类型/物料/数量自动计算，支持单位排放对比。
- **链上存证**：农事记录与碳报告均做 SHA-256 哈希上链（本地轻节点实现，防篡改、可验真）。
- **事件解耦**：农事保存后通过 RabbitMQ fanout 广播，碳核算 / AI 校验 / 存证三链路互不阻塞。
- **离线优先**：Flutter APP 与小程序支持农事记录本地缓存，网络恢复后批量同步（幂等令牌去重）。
- **智能体兜底**：Python 服务在无 LLM 环境下使用本地规则，保证系统可独立部署跑通。
