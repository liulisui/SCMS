-- 预约管理功能的数据库表结构更新
USE scms;

-- 1. 校区表
CREATE TABLE
    IF NOT EXISTS campuses (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        campus_code VARCHAR(20) NOT NULL UNIQUE COMMENT '校区编码',
        campus_name VARCHAR(100) NOT NULL COMMENT '校区名称',
        address TEXT COMMENT '校区地址',
        description TEXT COMMENT '校区描述',
        status ENUM ('active', 'inactive') DEFAULT 'active' COMMENT '状态',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        INDEX idx_campus_code (campus_code),
        INDEX idx_status (status)
    ) ENGINE = InnoDB COMMENT = '校区表';

-- 2. 社会公众预约表
CREATE TABLE
    IF NOT EXISTS public_reservations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_no VARCHAR(20) NOT NULL UNIQUE COMMENT '预约编号',
        visitor_name VARCHAR(50) NOT NULL COMMENT '预约人姓名',
        visitor_id_card_encrypted VARCHAR(255) NOT NULL COMMENT '加密的身份证号(SM4)',
        visitor_id_card_hash VARCHAR(128) NOT NULL COMMENT '身份证号哈希值(SM3)',
        visitor_phone_encrypted VARCHAR(255) NOT NULL COMMENT '加密的手机号(SM4)',
        visitor_phone_hash VARCHAR(128) NOT NULL COMMENT '手机号哈希值(SM3)',
        organization VARCHAR(100) COMMENT '所在单位',
        campus_id BIGINT NOT NULL COMMENT '预约校区ID',
        visit_date DATE NOT NULL COMMENT '预约日期',
        visit_time_start TIME NOT NULL COMMENT '预约开始时间',
        visit_time_end TIME NOT NULL COMMENT '预约结束时间',
        visit_reason TEXT NOT NULL COMMENT '来访事由',
        accompanying_persons INT NOT NULL DEFAULT 0 COMMENT '随行人数',
        vehicle_number VARCHAR(20) COMMENT '车牌号',
        status ENUM (
            'pending',
            'approved',
            'rejected',
            'cancelled',
            'completed'
        ) NOT NULL DEFAULT 'pending' COMMENT '预约状态',
        approval_comment TEXT COMMENT '审批意见',
        approved_by BIGINT COMMENT '审批人ID',
        approved_at TIMESTAMP NULL COMMENT '审批时间',
        check_in_time TIMESTAMP NULL COMMENT '实际入校时间',
        check_out_time TIMESTAMP NULL COMMENT '实际离校时间',
        pass_code VARCHAR(64) COMMENT '通行证编码',
        qr_code_data TEXT COMMENT 'QR码数据',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        FOREIGN KEY (campus_id) REFERENCES campuses (id),
        FOREIGN KEY (approved_by) REFERENCES administrators (id) ON DELETE SET NULL,
        INDEX idx_reservation_no (reservation_no),
        INDEX idx_visitor_id_card_hash (visitor_id_card_hash),
        INDEX idx_visitor_phone_hash (visitor_phone_hash),
        INDEX idx_visit_date (visit_date),
        INDEX idx_status (status),
        INDEX idx_campus_id (campus_id),
        INDEX idx_created_at (created_at)
    ) ENGINE = InnoDB COMMENT = '社会公众预约表';

-- 3. 公务预约表
CREATE TABLE
    IF NOT EXISTS official_reservations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_no VARCHAR(20) NOT NULL UNIQUE COMMENT '预约编号',
        visitor_name VARCHAR(50) NOT NULL COMMENT '预约人姓名',
        visitor_id_card_encrypted VARCHAR(255) NOT NULL COMMENT '加密的身份证号(SM4)',
        visitor_id_card_hash VARCHAR(128) NOT NULL COMMENT '身份证号哈希值(SM3)',
        visitor_phone_encrypted VARCHAR(255) NOT NULL COMMENT '加密的手机号(SM4)',
        visitor_phone_hash VARCHAR(128) NOT NULL COMMENT '手机号哈希值(SM3)',
        visitor_organization VARCHAR(100) NOT NULL COMMENT '访客所在单位',
        host_department_id BIGINT NOT NULL COMMENT '接待部门ID',
        host_name VARCHAR(50) NOT NULL COMMENT '接待人姓名',
        host_phone VARCHAR(20) NOT NULL COMMENT '接待人电话',
        campus_id BIGINT NOT NULL COMMENT '预约校区ID',
        visit_date DATE NOT NULL COMMENT '预约日期',
        visit_time_start TIME NOT NULL COMMENT '预约开始时间',
        visit_time_end TIME NOT NULL COMMENT '预约结束时间',
        visit_reason TEXT NOT NULL COMMENT '来访事由',
        accompanying_persons INT NOT NULL DEFAULT 0 COMMENT '随行人数',
        vehicle_number VARCHAR(20) COMMENT '车牌号',
        status ENUM (
            'pending',
            'approved',
            'rejected',
            'cancelled',
            'completed'
        ) NOT NULL DEFAULT 'pending' COMMENT '预约状态',
        approval_comment TEXT COMMENT '审批意见',
        approved_by BIGINT COMMENT '审批人ID',
        approved_at TIMESTAMP NULL COMMENT '审批时间',
        check_in_time TIMESTAMP NULL COMMENT '实际入校时间',
        check_out_time TIMESTAMP NULL COMMENT '实际离校时间',
        pass_code VARCHAR(64) COMMENT '通行证编码',
        qr_code_data TEXT COMMENT 'QR码数据',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
        FOREIGN KEY (campus_id) REFERENCES campuses (id),
        FOREIGN KEY (host_department_id) REFERENCES departments (id),
        FOREIGN KEY (approved_by) REFERENCES administrators (id) ON DELETE SET NULL,
        INDEX idx_reservation_no (reservation_no),
        INDEX idx_visitor_id_card_hash (visitor_id_card_hash),
        INDEX idx_visitor_phone_hash (visitor_phone_hash),
        INDEX idx_host_department_id (host_department_id),
        INDEX idx_visit_date (visit_date),
        INDEX idx_status (status),
        INDEX idx_campus_id (campus_id),
        INDEX idx_created_at (created_at)
    ) ENGINE = InnoDB COMMENT = '公务预约表';

-- 4. 部门预约授权表（用于存储哪些部门有权限管理社会公众预约）
CREATE TABLE
    IF NOT EXISTS reservation_authorizations (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        department_id BIGINT NOT NULL COMMENT '部门ID',
        reservation_type ENUM ('public', 'official', 'both') NOT NULL COMMENT '授权预约类型',
        granted_by BIGINT NOT NULL COMMENT '授权人ID',
        granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
        revoked_at TIMESTAMP NULL COMMENT '撤销时间',
        status ENUM ('active', 'revoked') NOT NULL DEFAULT 'active' COMMENT '授权状态',
        description TEXT COMMENT '授权说明',
        FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
        FOREIGN KEY (granted_by) REFERENCES administrators (id),
        UNIQUE KEY uk_dept_res_type (department_id, reservation_type),
        INDEX idx_department_id (department_id),
        INDEX idx_status (status),
        INDEX idx_reservation_type (reservation_type)
    ) ENGINE = InnoDB COMMENT = '部门预约授权表';

-- 5. 预约随行人员表
CREATE TABLE
    IF NOT EXISTS accompanying_visitors (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        reservation_type ENUM ('public', 'official') NOT NULL COMMENT '预约类型',
        reservation_id BIGINT NOT NULL COMMENT '预约ID',
        name VARCHAR(50) NOT NULL COMMENT '姓名',
        id_card_encrypted VARCHAR(255) NOT NULL COMMENT '加密的身份证号(SM4)',
        id_card_hash VARCHAR(128) NOT NULL COMMENT '身份证号哈希值(SM3)',
        phone_encrypted VARCHAR(255) COMMENT '加密的手机号(SM4)',
        phone_hash VARCHAR(128) COMMENT '手机号哈希值(SM3)',
        organization VARCHAR(100) COMMENT '所在单位',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
        INDEX idx_reservation_type (reservation_type),
        INDEX idx_reservation_id (reservation_id),
        INDEX idx_id_card_hash (id_card_hash)
    ) ENGINE = InnoDB COMMENT = '预约随行人员表';

-- 6. 安全审计日志表
CREATE TABLE
    IF NOT EXISTS admin_audit_logs (
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
        FOREIGN KEY (admin_id) REFERENCES administrators (id) ON DELETE SET NULL,
        INDEX idx_admin_id (admin_id),
        INDEX idx_action (action),
        INDEX idx_resource_type (resource_type),
        INDEX idx_resource_id (resource_id),
        INDEX idx_created_at (created_at)
    ) ENGINE = InnoDB COMMENT = '管理员审计日志表';

-- 7. 初始化校区数据
INSERT INTO
    campuses (
        campus_code,
        campus_name,
        address,
        description,
        status
    )
VALUES
    ('MAIN', '主校区', '主校区地址', '学校的主校区', 'active'),
    ('NORTH', '北校区', '北校区地址', '学校的北校区', 'active'),
    ('SOUTH', '南校区', '南校区地址', '学校的南校区', 'active'),
    ('EAST', '东校区', '东校区地址', '学校的东校区', 'active');

-- 8. 初始化权限数据
INSERT INTO
    permissions (permission_code, permission_name, description)
VALUES
    ('ADMIN_MANAGE', '管理员管理', '添加、修改、删除、查询和查看管理员信息'),
    ('DEPT_MANAGE', '部门管理', '添加、修改、删除、查询和查看部门信息'),
    (
        'PUBLIC_RESERVATION_MANAGE',
        '社会公众预约管理',
        '查询和统计社会公众预约信息'
    ),
    (
        'OFFICIAL_RESERVATION_MANAGE',
        '公务预约管理',
        '查询、审核和统计公务预约信息'
    ),
    ('RESERVATION_AUTH_MANAGE', '预约授权管理', '管理部门的预约授权'),
    ('AUDIT_LOG_VIEW', '审计日志查看', '查看系统审计日志'),
    ('SYSTEM_CONFIG', '系统配置', '管理系统配置信息');