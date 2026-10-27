-- =============================================================
-- 城乡农品轻量溯源与碳足迹存证系统
-- 02_seed.sql  —— 初始化数据
-- =============================================================
-- 包含：
--   1. 碳排放因子库种子数据（kgCO2e/单位）
--   2. 初始化管理员 / 收购商 / 农户 演示账号
-- 默认密码统一为：123456（BCrypt 密文见下）
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 演示账号密码 123456 的 BCrypt 密文（Spring Security 兼容 $2a/$2b/$2y）
-- 如需更换密码，请用 new BCryptPasswordEncoder().encode("新密码") 重新生成替换。
SET @admin_pwd = '$2b$10$lgwpaFIgfLYOnnFcDTk//Owz6Z6P7z89TKNWtLgfffglBIWMADKJe';

-- -------------------------------------------------------------
-- 1. 碳排放因子库种子数据
--    因子值均为 kgCO2e / 单位，取值参考国家农业碳排相关标准，
--    具体部署时请按最新官方系数校准。
-- -------------------------------------------------------------
INSERT INTO carbon_factor (id, farming_type, material_name, unit, factor_value, standard_source, remark, deleted, create_time, version)
VALUES
  (1,  'FERTILIZE', '尿素',     'kg',  1.630000, 'GB/T 32150 近似', '氮肥代表',      0, NOW(), 0),
  (2,  'FERTILIZE', '复合肥',   'kg',  1.000000, '国标估算',        '通用复合肥',    0, NOW(), 0),
  (3,  'FERTILIZE', '有机肥',   'kg',  0.200000, '低排放',          '推荐替代化肥',  0, NOW(), 0),
  (4,  'FERTILIZE', '碳酸氢铵', 'kg',  1.200000, '估算',            '铵态氮肥',      0, NOW(), 0),
  (5,  'IRRIGATE',  '灌溉用电', 'kWh', 0.581000, '中国区域电网均值', '电动泵耗电',    0, NOW(), 0),
  (6,  'IRRIGATE',  '灌溉用水', 'm3',  0.340000, '提水能耗估算',     '清水抽取',      0, NOW(), 0),
  (7,  'MACHINE',   '柴油',     'L',   2.680000, '柴油燃烧排放因子', '农机作业',      0, NOW(), 0),
  (8,  'MACHINE',   '汽油',     'L',   2.310000, '汽油排放因子',     '小型农机',      0, NOW(), 0),
  (9,  'PESTICIDE', '农药',     'kg',  4.000000, '农药生产排放估算', '植保投入',      0, NOW(), 0),
  (10, 'FILM',      '农膜',     'kg',  6.000000, '聚乙烯薄膜生产排放', '地膜覆盖',    0, NOW(), 0),
  (11, 'HARVEST',   '收获柴油', 'L',   2.680000, '同柴油',          '采收作业',      0, NOW(), 0),
  (12, 'PACKAGE',   '纸箱',     'kg',  0.900000, '瓦楞纸生产排放',   '绿色包装',      0, NOW(), 0),
  (13, 'PACKAGE',   '塑料包装', 'kg',  3.000000, 'PE 包装',         '常规包装',      0, NOW(), 0),
  (14, 'TRANSPORT', '柴油运输', 't*km',0.120000, '公路货运排放因子', '干线运输',      0, NOW(), 0),
  (15, 'TRANSPORT', '冷链运输', 't*km',0.210000, '含制冷能耗',      '生鲜冷链',      0, NOW(), 0)
ON DUPLICATE KEY UPDATE
  factor_value = VALUES(factor_value),
  standard_source = VALUES(standard_source),
  remark = VALUES(remark),
  update_time = NOW();

-- -------------------------------------------------------------
-- 2. 初始化演示账号
-- -------------------------------------------------------------
INSERT INTO sys_user (id, username, password, real_name, role, phone, coop_id, channel, status, deleted, create_time, version)
VALUES
  (1, 'admin',    @admin_pwd, '系统管理员', 'ADMIN',  '13800000000', 1, 'WEB', 1, 0, NOW(), 0),
  (2, 'buyer01',  @admin_pwd, '示范收购商', 'BUYER',  '13800000001', 1, 'WEB', 1, 0, NOW(), 0),
  (3, 'farmer01', @admin_pwd, '示范农户',   'FARMER', '13800000002', 1, 'APP', 1, 0, NOW(), 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  status = VALUES(status),
  update_time = NOW();

SET FOREIGN_KEY_CHECKS = 1;
