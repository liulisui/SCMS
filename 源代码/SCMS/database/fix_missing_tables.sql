-- 修复缺失的数据库表
USE scms;

-- 1. 创建部门预约授权表
CREATE TABLE IF NOT EXISTS reservation_authorizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL COMMENT '部门ID',
    reservation_type ENUM('public', 'official', 'both') NOT NULL COMMENT '授权预约类型',
    granted_by BIGINT NOT NULL COMMENT '授权人ID',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
    revoked_at TIMESTAMP NULL COMMENT '撤销时间',
    status ENUM('active', 'revoked') NOT NULL DEFAULT 'active' COMMENT '授权状态',
    description TEXT COMMENT '授权说明',
    INDEX idx_department_id (department_id),
    INDEX idx_status (status),
    INDEX idx_reservation_type (reservation_type),
    UNIQUE KEY uk_dept_res_type (department_id, reservation_type)
) ENGINE=InnoDB COMMENT='部门预约授权表';

-- 2. 创建管理员审计日志表
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT COMMENT '管理员ID',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    resource_type VARCHAR(50) NOT NULL COMMENT '资源类型',
    resource_id BIGINT COMMENT '资源ID',
    details TEXT COMMENT '操作详情',
    old_value JSON COMMENT '旧值',
    new_value JSON COMMENT '新值',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    hmac_value VARCHAR(128) COMMENT 'HMAC-SM3值',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_admin_id (admin_id),
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_resource_id (resource_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB COMMENT='管理员审计日志表';

-- 3. 检查并创建其他可能缺失的表

-- 校区表
CREATE TABLE IF NOT EXISTS campuses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    campus_code VARCHAR(20) NOT NULL UNIQUE COMMENT '校区编码',
    campus_name VARCHAR(100) NOT NULL COMMENT '校区名称',
    address TEXT COMMENT '校区地址',
    description TEXT COMMENT '校区描述',
    status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_campus_code (campus_code),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='校区表';

-- 部门表
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编号',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    dept_type ENUM('administrative', 'direct', 'academic') NOT NULL COMMENT '部门类型',
    parent_id BIGINT COMMENT '上级部门ID',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    description TEXT COMMENT '部门描述',
    status ENUM('active', 'inactive') DEFAULT 'active' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT COMMENT '创建人ID',
    updated_by BIGINT COMMENT '更新人ID',
    INDEX idx_dept_code (dept_code),
    INDEX idx_dept_type (dept_type),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='部门表';

-- 管理员表
CREATE TABLE IF NOT EXISTS administrators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
    password VARCHAR(128) NOT NULL COMMENT '密码哈希值',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
    real_name VARCHAR(100) NOT NULL COMMENT '姓名',
    phone_encrypted VARCHAR(255) COMMENT '加密的联系电话',
    phone_hash VARCHAR(128) COMMENT '电话号码哈希值',
    department_id BIGINT COMMENT '所在部门ID',
    admin_type ENUM('school_admin', 'department_admin', 'system_admin', 'audit_admin') NOT NULL COMMENT '管理员类型',
    status ENUM('active', 'locked', 'disabled') NOT NULL DEFAULT 'active' COMMENT '账号状态',
    failed_login_attempts INT DEFAULT 0 COMMENT '连续登录失败次数',
    locked_until TIMESTAMP NULL COMMENT '锁定截止时间',
    password_last_changed TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '密码最后修改时间',
    last_login_time TIMESTAMP NULL COMMENT '最后登录时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_admin_type (admin_type),
    INDEX idx_department_id (department_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='管理员表';

-- 4. 插入初始数据

-- 插入校区数据
INSERT IGNORE INTO campuses (campus_code, campus_name, address, description, status) VALUES
('PF', '屏峰校区', '杭州市西湖区留和路288号', '浙江工业大学屏峰校区', 'active'),
('ZH', '朝晖校区', '杭州市下城区潮王路18号', '浙江工业大学朝晖校区', 'active'),
('MGS', '莫干山校区', '德清县阜溪街道2号大街1158号', '浙江工业大学莫干山校区', 'active');

-- 插入部门数据
INSERT IGNORE INTO departments (dept_code, dept_name, dept_type, description, status, created_by) VALUES
('ADMIN', '行政部门', 'administrative', '学校行政管理部门', 'active', 1),
('SECURITY', '保卫处', 'administrative', '负责校园安全管理', 'active', 1),
('ACADEMIC', '教务处', 'administrative', '负责教学管理', 'active', 1),
('STUDENT', '学生处', 'administrative', '负责学生事务管理', 'active', 1);

-- 插入管理员数据
INSERT IGNORE INTO administrators (username, password, salt, real_name, admin_type, status) VALUES
('admin', 'b7a875fc1ea228b9061041b7cec4bd3c52ab3ce3', 'salt123', '系统管理员', 'system_admin', 'active');

-- 插入授权数据 - 给保卫处授权管理公务预约
INSERT IGNORE INTO reservation_authorizations (department_id, reservation_type, granted_by, description) VALUES
(2, 'official', 1, '保卫处负责公务预约审批'),
(2, 'public', 1, '保卫处负责社会公众预约审批');

-- 显示创建的表
SHOW TABLES;
