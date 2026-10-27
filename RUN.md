# 本地运行指南（前后端全栈）

> 本机已验证可运行的最小依赖：**JDK 17**、**Maven 3.9**、**Node 20+**、**Python 3.13**、**MySQL 8**（端口 3306，root/123456）。
> RabbitMQ / Nacos / Redis 为“增强项”：本系统已改造为**单机演示模式**（禁用 Nacos 注册发现、Feign 走显式 URL、MQ 发布失败不阻断业务），因此即使没有 RabbitMQ/Nacos 也能启动并演示核心溯源流程。

## 1. 数据库初始化（MySQL）
```sql
CREATE DATABASE IF NOT EXISTS agri_trace CHARACTER SET utf8mb4;
-- 依次执行
mysql -u root -p123456 agri_trace < sql/01_schema.sql
mysql -u root -p123456 agri_trace < sql/02_seed.sql      -- 碳因子 + 演示账号(admin/buyer01/farmer01, 密码 123456)
mysql -u root -p123456 agri_trace < sql/03_sample.sql    -- 样例批次 AGRI20260001（溯源/碳足迹/存证演示数据）
```

## 2. Python 智能体（:8000）
```bash
cd python-agent
python -m venv .venv && .venv/Scripts/activate
pip install fastapi uvicorn pydantic
uvicorn app.main:app --host 0.0.0.0 --port 8000
# 无 LLM Key 时自动走本地规则兜底（/health、/api/agent 均可调）
```

## 3. Java 微服务（网关 :9000）
已用 JDK17 + Maven 在 `D:\agri_build` 完成构建（jar 位于各模块 `target/`）。
启动全部 9 个进程（含网关）：
```bash
bash D:/agri_build/run_services.sh
# 每个服务日志在 D:/agri_build/logs/<module>.log
```
服务端口：auth 8081 / farming 8082 / batch 8083 / carbon 8084 / chain 8085 /
agent 8086 / report 8087 / file 8088 / **gateway 9000**。
网关通过 `http://127.0.0.1:9000` 统一对外，前端 `baseURL` 指向它。

> 若要启用完整的 Nacos 服务发现 + RabbitMQ 事件驱动，请改用 `docker-compose.yml`
> 拉起 mysql/redis/rabbitmq/nacos，并去掉各服务启动参数 `-Dspring.cloud.nacos.discovery.enabled=false`、
> 将 Feign 的 `url` 改为走注册中心（或删掉 `url` 属性）。

## 4. 前端
```bash
# 管理后台（收购商/管理员）
cd web-admin && npm install && npm run dev -- --host --port 5173
# 消费者扫码溯源 H5
cd web-h5    && npm install && npm run dev -- --host --port 5174
```
- web-admin 默认代理 `/api` → `localhost:9000`（网关）
- web-h5 扫码/输入批次号 `AGRI20260001` 即可查看：批次信息、农事时间线、碳足迹报告、联盟链存证核验

## 5. 演示账号
| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | 123456 |
| 收购商 | buyer01 | 123456 |
| 农户 | farmer01 | 123456 |

## 6. 端口总览
| 服务 | 端口 |
|------|------|
| 网关 Gateway | 9000 |
| auth / farming / batch / carbon / chain / agent / report / file | 8081–8088 |
| Python AI 智能体 | 8000 |
| web-admin | 5173 |
| web-h5 | 5174 |
| MySQL / Redis / RabbitMQ / Nacos | 3306 / 6379 / 5672 / 8848 |
