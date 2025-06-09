-- 校园通行证预约管理系统数据库架构
-- Campus Pass Reservation Management System Database Schema
-- 创建数据库
CREATE DATABASE IF NOT EXISTS scms DEFAULT CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE scms;

-- 用户表
CREATE TABLE
    users (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
        password VARCHAR(128) NOT NULL COMMENT '密码哈希值',
        salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
        full_name VARCHAR(100) NOT NULL COMMENT '真实姓名',
        phone VARCHAR(20) NOT NULL COMMENT '手机号码',
        student_id VARCHAR(20) COMMENT '学号',
        role ENUM ('student', 'teacher', 'admin') NOT NULL DEFAULT 'student' COMMENT '用户角色',
        status ENUM ('active', 'inactive', 'suspended') NOT NULL DEFAULT 'active' COMMENT '用户状态',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        INDEX idx_username (username),
        INDEX idx_student_id (student_id),
        INDEX idx_role (role),
        INDEX idx_status (status)
    ) ENGINE = InnoDB COMMENT = '用户表';

-- 预约表
CREATE TABLE
    reservations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL COMMENT '用户ID',
        purpose TEXT NOT NULL COMMENT '出入校事由',
        departure_time DATETIME NOT NULL COMMENT '预计离校时间',
        return_time DATETIME NOT NULL COMMENT '预计返校时间',
        destination VARCHAR(200) NOT NULL COMMENT '目的地',
        emergency_contact VARCHAR(100) NOT NULL COMMENT '紧急联系人',
        emergency_phone VARCHAR(20) NOT NULL COMMENT '紧急联系电话',
        status ENUM (
            'pending',
            'approved',
            'rejected',
            'used',
            'expired'
        ) NOT NULL DEFAULT 'pending' COMMENT '预约状态',
        approval_comment TEXT COMMENT '审批意见',
        approved_by BIGINT COMMENT '审批人ID',
        approved_at TIMESTAMP NULL COMMENT '审批时间',
        pass_code VARCHAR(64) COMMENT '通行证编码',
        qr_code_data TEXT COMMENT 'QR码数据',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL,
        INDEX idx_user_id (user_id),
        INDEX idx_status (status),
        INDEX idx_departure_time (departure_time),
        INDEX idx_return_time (return_time),
        INDEX idx_pass_code (pass_code),
        INDEX idx_approved_by (approved_by)
    ) ENGINE = InnoDB COMMENT = '预约表';

-- 审计日志表
CREATE TABLE
    audit_logs (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT COMMENT '操作用户ID',
        action VARCHAR(50) NOT NULL COMMENT '操作类型',
        table_name VARCHAR(50) NOT NULL COMMENT '表名',
        record_id BIGINT COMMENT '记录ID',
        old_values JSON COMMENT '旧值',
        new_values JSON COMMENT '新值',
        ip_address VARCHAR(45) COMMENT 'IP地址',
        user_agent TEXT COMMENT '用户代理',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,
        INDEX idx_user_id (user_id),
        INDEX idx_action (action),
        INDEX idx_table_name (table_name),
        INDEX idx_record_id (record_id),
        INDEX idx_created_at (created_at)
    ) ENGINE = InnoDB COMMENT = '审计日志表';

-- 系统配置表
CREATE TABLE
    system_config (
        id INT AUTO_INCREMENT PRIMARY KEY,
        config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
        config_value TEXT NOT NULL COMMENT '配置值',
        description TEXT COMMENT '配置说明',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        INDEX idx_config_key (config_key)
    ) ENGINE = InnoDB COMMENT = '系统配置表';

-- 通知表
CREATE TABLE
    notifications (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        user_id BIGINT NOT NULL COMMENT '用户ID',
        title VARCHAR(200) NOT NULL COMMENT '标题',
        content TEXT NOT NULL COMMENT '内容',
        type ENUM ('info', 'warning', 'success', 'error') NOT NULL DEFAULT 'info' COMMENT '通知类型',
        is_read BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已读',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
        INDEX idx_user_id (user_id),
        INDEX idx_is_read (is_read),
        INDEX idx_type (type),
        INDEX idx_created_at (created_at)
    ) ENGINE = InnoDB COMMENT = '通知表';