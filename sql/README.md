# 数据库脚本说明

本目录包含城乡农品轻量溯源与碳足迹存证系统的 MySQL 8.0 建库建表与初始化脚本。

## 文件清单

| 文件 | 说明 |
| --- | --- |
| `01_schema.sql` | 业务主库 9 张表完整结构（含软删除、审计字段、乐观锁） |
| `02_seed.sql` | 碳排放因子库种子数据 + 演示账号初始化 |

## 执行顺序

```bash
# 1. 创建业务库（可自定义库名，默认 trace_db）
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS trace_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"

# 2. 执行建表
mysql -uroot -p trace_db < 01_schema.sql

# 3. 执行初始化数据
mysql -uroot -p trace_db < 02_seed.sql
```

或在 MySQL 客户端中按顺序 `source` 执行。

## 演示账号（密码均为 `123456`）

| 用户名 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | ADMIN | 系统管理员，可后台全局管理 |
| `buyer01` | BUYER | 示范收购商，批次 / 农户管理 |
| `farmer01` | FARMER | 示范农户，农事录入入口 |

> 密码以 BCrypt 密文存储，密文由脚本常量 `@admin_pwd` 提供。
> 若需自定义密码，请用 `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder#encode`
> 生成新密文后替换 `02_seed.sql` 中的 `@admin_pwd` 值。

## 表结构速览

| 表名 | 关键字段 | 说明 |
| --- | --- | --- |
| `sys_user` | username / role / openid | 用户与鉴权 |
| `product_batch` | batch_no(唯一) | 批次溯源 + 二维码入口 |
| `farming_record` | batch_id / farming_type / amount | 农事记录 |
| `carbon_factor` | (farming_type, material_name) | 排放因子库 |
| `carbon_report` | batch_id / total_emission | 碳足迹聚合报告 |
| `carbon_report_item` | report_id / emission | 分项明细 |
| `carbon_record_emission` | record_id / emission | 单条记录核算明细 |
| `chain_record` | biz_type / biz_id / biz_hash | 哈希存证 |
| `subsidy_report` | report_no(唯一) / content_md | 补贴申报材料 |

## 设计要点

1. **软删除**：所有业务表含 `deleted` 字段（0/1），由 MyBatis-Plus `@TableLogic` 自动过滤。
2. **审计字段**：`create_time / update_time / create_by / update_by` 由 MP 自动填充插件写入。
3. **乐观锁**：`version` 字段防止并发修改导致存证哈希错乱。
4. **主键策略**：业务主键统一 `BIGINT`，由雪花算法生成（`ASSIGN_ID`），建表不使用 `AUTO_INCREMENT`。
5. **碳排放因子**：存储在 `carbon_factor`，核算服务按 `(farming_type, material_name)` 精确匹配，缺失时走保守兜底因子，保证碳排计算合规、可控。
