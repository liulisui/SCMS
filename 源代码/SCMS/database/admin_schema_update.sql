-- 管理员和部门管理功能的数据库表结构更新
USE scms;

-- 1. 管理员表
CREATE TABLE
    IF NOT EXISTS administrators (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
        password VARCHAR(128) NOT NULL COMMENT '密码哈希值(SM3)',
        salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
        real_name VARCHAR(100) NOT NULL COMMENT '姓名',
        phone_encrypted VARCHAR(255) COMMENT '加密的联系电话(SM4)',
        phone_hash VARCHAR(128) COMMENT '电话号码哈希值(SM3)',
        department_id BIGINT COMMENT '所在部门ID',
        admin_type ENUM (
            'school_admin',
            'department_admin',
            'system_admin',
            'audit_admin'
        ) NOT NULL COMMENT '管理员类型',
        status ENUM ('active', 'locked', 'disabled') NOT NULL DEFAULT 'active' COMMENT '账号状态',
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
    ) ENGINE = InnoDB COMMENT = '管理员表';

-- 2. 部门表
CREATE TABLE
    IF NOT EXISTS departments (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        dept_code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编号',
        dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
        dept_type ENUM ('administrative', 'direct', 'academic') NOT NULL COMMENT '部门类型(行政部门、直属部门、学院)',
        parent_id BIGINT COMMENT '上级部门ID',
        contact_phone VARCHAR(20) COMMENT '联系电话',
        contact_email VARCHAR(100) COMMENT '联系邮箱',
        description TEXT COMMENT '部门描述',
        status ENUM ('active', 'inactive') DEFAULT 'active' COMMENT '状态',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        created_by BIGINT NOT NULL COMMENT '创建人ID',
        updated_by BIGINT COMMENT '更新人ID',
        FOREIGN KEY (parent_id) REFERENCES departments (id) ON DELETE SET NULL,
        FOREIGN KEY (created_by) REFERENCES administrators (id),
        FOREIGN KEY (updated_by) REFERENCES administrators (id) ON DELETE SET NULL,
        INDEX idx_dept_code (dept_code),
        INDEX idx_dept_type (dept_type),
        INDEX idx_parent_id (parent_id),
        INDEX idx_status (status)
    ) ENGINE = InnoDB COMMENT = '部门表';

-- 添加部门外键约束到管理员表
ALTER TABLE administrators ADD CONSTRAINT fk_admin_department FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE SET NULL;

-- 3. 权限表
CREATE TABLE
    IF NOT EXISTS permissions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        permission_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
        permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
        description TEXT COMMENT '权限描述',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE = InnoDB COMMENT = '权限表';

-- 4. 管理员权限关联表
CREATE TABLE
    IF NOT EXISTS admin_permissions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        admin_id BIGINT NOT NULL COMMENT '管理员ID',
        permission_id BIGINT NOT NULL COMMENT '权限ID',
        granted_by BIGINT NOT NULL COMMENT '授权人ID',
        granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
        FOREIGN KEY (admin_id) REFERENCES administrators (id) ON DELETE CASCADE,
        FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE,
        FOREIGN KEY (granted_by) REFERENCES administrators (id),
        UNIQUE KEY uk_admin_permission (admin_id, permission_id),
        INDEX idx_admin_id (admin_id),
        INDEX idx_permission_id (permission_id)
    ) ENGINE = InnoDB COMMENT = '管理员权限关联表';

-- 5. 部门权限关联表（用于存储部门管理员的权限授权范围）
CREATE TABLE
    IF NOT EXISTS department_permissions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        department_id BIGINT NOT NULL COMMENT '部门ID',
        permission_id BIGINT NOT NULL COMMENT '权限ID',
        granted_by BIGINT NOT NULL COMMENT '授权人ID',
        granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
        FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
        FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE,
        FOREIGN KEY (granted_by) REFERENCES administrators (id),
        UNIQUE KEY uk_dept_permission (department_id, permission_id),
        INDEX idx_department_id (department_id),
        INDEX idx_permission_id (permission_id)
    ) ENGINE = InnoDB COMMENT = '部门权限关联表';

-- 6. 登录会话表（用于跟踪登录状态和自动登出）
CREATE TABLE
    IF NOT EXISTS login_sessions (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        admin_id BIGINT NOT NULL COMMENT '管理员ID',
        session_id VARCHAR(128) NOT NULL UNIQUE COMMENT '会话ID',
        ip_address VARCHAR(45) NOT NULL COMMENT 'IP地址',
        user_agent TEXT COMMENT '用户代理',
        login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
        last_activity_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '最后活动时间',
        expire_time TIMESTAMP NOT NULL COMMENT '会话过期时间',
        logout_time TIMESTAMP NULL COMMENT '登出时间',
        status ENUM ('active', 'expired', 'logged_out') DEFAULT 'active' COMMENT '会话状态',
        FOREIGN KEY (admin_id) REFERENCES administrators (id) ON DELETE CASCADE,
        INDEX idx_admin_id (admin_id),
        INDEX idx_session_id (session_id),
        INDEX idx_status (status),
        INDEX idx_expire_time (expire_time)
    ) ENGINE = InnoDB COMMENT = '登录会话表';

-- 7. 密码历史表（用于防止密码重用）
CREATE TABLE
    IF NOT EXISTS password_history (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        admin_id BIGINT NOT NULL COMMENT '管理员ID',
        password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希值',
        salt VARCHAR(32) NOT NULL COMMENT '密码盐值',
        changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '密码修改时间',
        FOREIGN KEY (admin_id) REFERENCES administrators (id) ON DELETE CASCADE,
        INDEX idx_admin_id (admin_id),
        INDEX idx_changed_at (changed_at)
    ) ENGINE = InnoDB COMMENT = '密码历史表';