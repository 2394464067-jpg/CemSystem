-- 1. 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键(员工编号)',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
    factory_name VARCHAR(100) COMMENT '所属工厂名称',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_username (username, deleted)
) COMMENT '用户表';

-- 2. 排放因子表
CREATE TABLE t_emission_factor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    energy_name VARCHAR(50) NOT NULL COMMENT '能源类型',
    factor_value DECIMAL(12,6) NOT NULL COMMENT '碳排放因子(kgCO₂/单位)',
    unit VARCHAR(30) NOT NULL COMMENT '单位: kWh/m³/L',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME,
    UNIQUE KEY uk_energy (energy_name, deleted)
) COMMENT '碳排放因子字典';

-- 3. 供应商表
CREATE TABLE t_supplier (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact VARCHAR(50) COMMENT '联系方式',
    industry VARCHAR(50) COMMENT '所属行业',
    green_level CHAR(1) DEFAULT NULL COMMENT '绿色等级A/B/C，NULL表示未评级',
    avg_emission DECIMAL(14,2) DEFAULT NULL COMMENT '近6月平均碳排量，NULL表示未计算',
    deleted TINYINT DEFAULT 0,
    create_time DATETIME,
    update_time DATETIME
) COMMENT '供应商表';

-- 4. 碳排记录表（核心表）
DROP TABLE IF EXISTS t_carbon_record;
CREATE TABLE t_carbon_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
		version INT INT DEFAULT 0 COMMENT '乐观锁版本号',
    user_id BIGINT NOT NULL COMMENT '上报人编号',
		supplier_id INT COMMENT '供应商编号',
    energy_type VARCHAR(50) NOT NULL COMMENT '能源类型',
    consume_amount DECIMAL(14,2) NOT NULL COMMENT '消耗量',
    total_emission DECIMAL(16,4) COMMENT '总碳排量=消耗量×因子',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1待审核 2通过 3驳回',
    reject_reason VARCHAR(255) COMMENT '驳回原因',
    report_month VARCHAR(7) NOT NULL COMMENT '上报月份 yyyy-MM',
    deleted TINYINT DEFAULT 0,
    KEY idx_month (report_month),
    KEY idx_status (status)
) COMMENT '碳排记录表';

CREATE TABLE `t_sys_log` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `module`        VARCHAR(50)  DEFAULT NULL COMMENT '操作模块',
  `type`          VARCHAR(20)  DEFAULT NULL COMMENT '操作类型',
  `operator`      VARCHAR(50)  DEFAULT NULL COMMENT '操作人',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
  `request_url`   VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `request_params` TEXT        DEFAULT NULL COMMENT '请求参数',
  `target_id`     BIGINT       DEFAULT NULL COMMENT '目标数据ID',
  `ip`            VARCHAR(64)  DEFAULT NULL COMMENT '操作IP',
  `cost_time`     BIGINT       DEFAULT NULL COMMENT '耗时(ms)',
  `status`        TINYINT      DEFAULT 1 COMMENT '1成功 0失败',
  `error_msg`     VARCHAR(500) DEFAULT NULL COMMENT '异常信息',
  `create_time`   DATETIME     DEFAULT NULL COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_module_type` (`module`, `type`),
  KEY `idx_operator` (`operator`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';

-- ============ 初始化数据 ============
INSERT INTO t_user (username, password, role, factory_name)
VALUES ('admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'ADMIN', '集团总部');
-- admin/123456 (BCrypt)

INSERT INTO t_emission_factor (energy_name, factor_value, unit, create_time, update_time) VALUES
('电力', 0.570300, 'kWh', NOW(), NOW()),
('天然气', 2.162200, 'm³', NOW(), NOW()),
('柴油', 3.095900, 'L', NOW(), NOW()),
('蒸汽', 0.310000, 'kg', NOW(), NOW());

INSERT INTO t_supplier (name, contact, industry, green_level, create_time, update_time) VALUES
('华东钢铁供应商', '张三', '钢铁', 'B', NOW(), NOW()),
('南方橡胶厂', '李四', '化工', 'A', NOW(), NOW());


