-- =============================================================
-- 城乡农品轻量溯源与碳足迹存证系统
-- 01_schema.sql  —— 业务主库表结构（MySQL 8.0）
-- =============================================================
-- 说明：
--   1. 所有业务表均包含软删除字段 deleted(0/1) 与审计字段
--      (create_time / update_time / create_by / update_by / version)，
--      由 MyBatis-Plus 自动填充（见 trace-common 的 BaseEntity）。
--   2. 主键统一采用 BIGINT，业务层使用雪花算法(ASSIGN_ID)生成，
--      故建表时不使用 AUTO_INCREMENT。
--   3. 字符集统一 utf8mb4，兼容 emoji 与中文。
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -------------------------------------------------------------
-- 1. 系统用户表（农户 / 收购商 / 管理员）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_user (
  id           BIGINT       NOT NULL COMMENT '主键(雪花)',
  username     VARCHAR(64)  NOT NULL COMMENT '登录账号',
  password     VARCHAR(100) NOT NULL COMMENT 'BCrypt 加密密码',
  real_name    VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  role         VARCHAR(20)  NOT NULL COMMENT 'FARMER/BUYER/ADMIN',
  phone        VARCHAR(20)  DEFAULT NULL COMMENT 'APP 手机号快捷登录',
  coop_id      BIGINT       DEFAULT NULL COMMENT '所属合作社',
  channel      VARCHAR(20)  DEFAULT NULL COMMENT '最近登录渠道 MINIAPP/APP/WEB',
  openid       VARCHAR(64)  DEFAULT NULL COMMENT '微信小程序 openid',
  status       INT          NOT NULL DEFAULT 1 COMMENT '0-禁用 1-正常',
  last_login_time DATETIME  DEFAULT NULL,
  deleted      INT          NOT NULL DEFAULT 0,
  create_time  DATETIME     DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  create_by    BIGINT       DEFAULT NULL,
  update_by    BIGINT       DEFAULT NULL,
  version      INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_openid (openid),
  KEY idx_phone (phone),
  KEY idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- -------------------------------------------------------------
-- 2. 农产品批次表（溯源二维码入口）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS product_batch (
  id            BIGINT       NOT NULL,
  batch_no      VARCHAR(64)  NOT NULL COMMENT '批次编号(扫码入口标识)',
  farmer_id     BIGINT       DEFAULT NULL COMMENT '所属农户',
  coop_id       BIGINT       DEFAULT NULL COMMENT '所属合作社',
  product_name  VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
  origin_province VARCHAR(50) DEFAULT NULL COMMENT '产地省',
  origin_city   VARCHAR(50)  DEFAULT NULL COMMENT '产地市',
  origin_detail VARCHAR(200) DEFAULT NULL COMMENT '产地详细地址',
  plant_start_date DATE      DEFAULT NULL COMMENT '种植开始日期',
  plant_end_date   DATE      DEFAULT NULL COMMENT '种植结束日期',
  area_mu       DECIMAL(12,2) DEFAULT NULL COMMENT '种植面积(亩)',
  expected_yield DECIMAL(12,2) DEFAULT NULL COMMENT '预估产量(kg)',
  actual_yield  DECIMAL(12,2) DEFAULT NULL COMMENT '实际产量(kg)',
  status        VARCHAR(20)  DEFAULT NULL COMMENT 'GROWING/HARVESTED/STORED',
  qr_code       TEXT         DEFAULT NULL COMMENT '二维码 Base64',
  qr_url        VARCHAR(255) DEFAULT NULL COMMENT '二维码访问地址',
  flow_info     TEXT         DEFAULT NULL COMMENT '流转信息(JSON)',
  deleted       INT          NOT NULL DEFAULT 0,
  create_time   DATETIME     DEFAULT NULL,
  update_time   DATETIME     DEFAULT NULL,
  create_by     BIGINT       DEFAULT NULL,
  update_by     BIGINT       DEFAULT NULL,
  version       INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_batch_no (batch_no),
  KEY idx_farmer (farmer_id),
  KEY idx_coop (coop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农产品批次溯源表';

-- -------------------------------------------------------------
-- 3. 农事记录表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS farming_record (
  id            BIGINT       NOT NULL,
  batch_id      BIGINT       DEFAULT NULL COMMENT '批次 id',
  batch_no      VARCHAR(64)  DEFAULT NULL COMMENT '批次编号(冗余)',
  farmer_id     BIGINT       DEFAULT NULL COMMENT '农户 id',
  farming_type  VARCHAR(30)  DEFAULT NULL COMMENT 'FERTILIZE/IRRIGATE/MACHINE/PESTICIDE/FILM/HARVEST/PACKAGE/TRANSPORT',
  material_name VARCHAR(60)  DEFAULT NULL COMMENT '物料名称(尿素/柴油/灌溉用电)',
  amount        DECIMAL(12,3) DEFAULT NULL COMMENT '用量',
  unit          VARCHAR(10)  DEFAULT NULL COMMENT 'kg/L/kWh/mu',
  work_date     DATE         DEFAULT NULL COMMENT '作业日期',
  area_mu       DECIMAL(12,2) DEFAULT NULL COMMENT '作业面积(亩)',
  images        VARCHAR(1000) DEFAULT NULL COMMENT '现场照片(逗号分隔)',
  remark        VARCHAR(500) DEFAULT NULL COMMENT '备注',
  source        VARCHAR(20)  DEFAULT NULL COMMENT '提交来源端 MINIAPP/APP',
  sync_token    VARCHAR(64)  DEFAULT NULL COMMENT 'APP 离线同步幂等令牌',
  check_status  VARCHAR(20)  DEFAULT NULL COMMENT 'AI 校验状态 WAITING/NORMAL/ABNORMAL',
  check_remark  VARCHAR(500) DEFAULT NULL COMMENT 'AI 校验结论',
  record_hash   VARCHAR(128) DEFAULT NULL COMMENT '业务数据哈希(存证回写)',
  chained       INT          DEFAULT 0 COMMENT '是否已上链 0/1',
  deleted       INT          NOT NULL DEFAULT 0,
  create_time   DATETIME     DEFAULT NULL,
  update_time   DATETIME     DEFAULT NULL,
  create_by     BIGINT       DEFAULT NULL,
  update_by     BIGINT       DEFAULT NULL,
  version       INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_batch (batch_id),
  KEY idx_farmer (farmer_id),
  KEY idx_sync (sync_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农事记录表';

-- -------------------------------------------------------------
-- 4. 农业碳排放因子库（国标基准，按 农事类型+物料 维护）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carbon_factor (
  id             BIGINT       NOT NULL,
  farming_type   VARCHAR(30)  NOT NULL COMMENT '农事类型编码',
  material_name  VARCHAR(60)  NOT NULL COMMENT '物料名称',
  unit           VARCHAR(10)  NOT NULL COMMENT '计量单位',
  factor_value   DECIMAL(12,6) NOT NULL COMMENT '排放因子(kgCO2e/单位)',
  standard_source VARCHAR(100) DEFAULT NULL COMMENT '标准来源(如 GB/T XXXXX)',
  remark         VARCHAR(255) DEFAULT NULL,
  deleted        INT          NOT NULL DEFAULT 0,
  create_time    DATETIME     DEFAULT NULL,
  update_time    DATETIME     DEFAULT NULL,
  create_by      BIGINT       DEFAULT NULL,
  update_by      BIGINT       DEFAULT NULL,
  version        INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_type_material (farming_type, material_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农业碳排放因子库';

-- -------------------------------------------------------------
-- 5. 碳足迹报告表（按批次聚合）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carbon_report (
  id             BIGINT       NOT NULL,
  batch_id       BIGINT       DEFAULT NULL,
  batch_no       VARCHAR(64)  DEFAULT NULL,
  total_emission DECIMAL(14,3) DEFAULT NULL COMMENT '全流程总排放(kgCO2e)',
  unit_emission  DECIMAL(14,6) DEFAULT NULL COMMENT '单位产量碳排放(kgCO2e/kg)',
  ai_advice      TEXT         DEFAULT NULL COMMENT 'AI 低碳优化建议',
  report_hash    VARCHAR(128) DEFAULT NULL COMMENT '报告哈希(供存证)',
  status         VARCHAR(20)  DEFAULT NULL COMMENT 'GENERATED/CONFIRMED',
  report_time    DATETIME     DEFAULT NULL,
  deleted        INT          NOT NULL DEFAULT 0,
  create_time    DATETIME     DEFAULT NULL,
  update_time    DATETIME     DEFAULT NULL,
  create_by      BIGINT       DEFAULT NULL,
  update_by      BIGINT       DEFAULT NULL,
  version        INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳足迹报告表';

-- -------------------------------------------------------------
-- 6. 碳足迹分项明细表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carbon_report_item (
  id            BIGINT       NOT NULL,
  report_id     BIGINT       DEFAULT NULL COMMENT '关联碳报告 id',
  farming_type  VARCHAR(30)  DEFAULT NULL,
  material_name VARCHAR(60)  DEFAULT NULL,
  amount        DECIMAL(12,3) DEFAULT NULL,
  unit          VARCHAR(10)  DEFAULT NULL,
  factor_value  DECIMAL(12,6) DEFAULT NULL,
  emission      DECIMAL(14,3) DEFAULT NULL COMMENT '该环节排放(kgCO2e)',
  proportion    DECIMAL(6,2)  DEFAULT NULL COMMENT '占比(%)',
  deleted       INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_report (report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='碳足迹分项明细';

-- -------------------------------------------------------------
-- 7. 单条农事记录碳排放计算明细（核算服务内部表）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carbon_record_emission (
  id            BIGINT       NOT NULL,
  record_id     BIGINT       DEFAULT NULL COMMENT '农事记录 id',
  batch_id      BIGINT       DEFAULT NULL,
  farming_type  VARCHAR(30)  DEFAULT NULL,
  material_name VARCHAR(60)  DEFAULT NULL,
  amount        DECIMAL(12,3) DEFAULT NULL,
  unit          VARCHAR(10)  DEFAULT NULL,
  factor_value  DECIMAL(12,6) DEFAULT NULL,
  emission      DECIMAL(14,3) DEFAULT NULL COMMENT '该记录排放(kgCO2e)',
  deleted       INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农事记录碳排放明细';

-- -------------------------------------------------------------
-- 8. 联盟链存证记录表（仅哈希摘要）
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chain_record (
  id           BIGINT       NOT NULL,
  biz_type     VARCHAR(30)  NOT NULL COMMENT 'FARMING_RECORD/CARBON_REPORT',
  biz_id       VARCHAR(64)  NOT NULL COMMENT '业务主键',
  biz_hash     VARCHAR(128) NOT NULL COMMENT '业务数据 SHA256 哈希',
  tx_id        VARCHAR(128) DEFAULT NULL COMMENT '链上交易ID',
  block_height BIGINT       DEFAULT NULL COMMENT '区块高度',
  chain_time   DATETIME     DEFAULT NULL COMMENT '上链时间',
  status       VARCHAR(20)  DEFAULT NULL COMMENT 'PENDING/CONFIRMED',
  deleted      INT          NOT NULL DEFAULT 0,
  create_time  DATETIME     DEFAULT NULL,
  update_time  DATETIME     DEFAULT NULL,
  create_by    BIGINT       DEFAULT NULL,
  update_by    BIGINT       DEFAULT NULL,
  version      INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='联盟链存证记录表';

-- -------------------------------------------------------------
-- 9. 补贴申报报表表
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subsidy_report (
  id             BIGINT       NOT NULL,
  batch_id       BIGINT       DEFAULT NULL,
  farmer_id      BIGINT       DEFAULT NULL,
  report_no      VARCHAR(64)  NOT NULL COMMENT '申报单号',
  title          VARCHAR(200) DEFAULT NULL,
  content_md     MEDIUMTEXT   DEFAULT NULL COMMENT 'Markdown 申报材料',
  file_url       VARCHAR(255) DEFAULT NULL COMMENT '导出文件地址',
  status         VARCHAR(20)  DEFAULT NULL COMMENT 'GENERATED/SUBMITTED',
  generated_by   VARCHAR(64)  DEFAULT NULL,
  generated_time DATETIME     DEFAULT NULL,
  report_hash    VARCHAR(128) DEFAULT NULL COMMENT '报告哈希(供存证)',
  deleted        INT          NOT NULL DEFAULT 0,
  create_time    DATETIME     DEFAULT NULL,
  update_time    DATETIME     DEFAULT NULL,
  create_by      BIGINT       DEFAULT NULL,
  update_by      BIGINT       DEFAULT NULL,
  version        INT          NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_report_no (report_no),
  KEY idx_batch (batch_id),
  KEY idx_farmer (farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补贴申报报表表';

SET FOREIGN_KEY_CHECKS = 1;
