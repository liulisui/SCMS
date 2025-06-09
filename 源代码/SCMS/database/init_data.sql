-- 校园通行证预约管理系统初始化数据
-- Campus Pass Reservation Management System Initial Data
USE scms;

-- 插入默认管理员账户
-- 密码: admin123 (使用SM3加密)
INSERT INTO
    users (username, password, salt, full_name, phone, role)
VALUES
    (
        'admin',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '系统管理员',
        '13800138000',
        'admin'
    );

-- 插入示例教师账户
INSERT INTO
    users (
        username,
        password,
        salt,
        full_name,
        phone,
        student_id,
        role
    )
VALUES
    (
        'teacher1',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '张老师',
        '13800138001',
        'T001',
        'teacher'
    ),
    (
        'teacher2',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '李老师',
        '13800138002',
        'T002',
        'teacher'
    );

-- 插入示例学生账户
INSERT INTO
    users (
        username,
        password,
        salt,
        full_name,
        phone,
        student_id,
        role
    )
VALUES
    (
        'student1',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '王小明',
        '13800138003',
        '20210001',
        'student'
    ),
    (
        'student2',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '李小红',
        '13800138004',
        '20210002',
        'student'
    ),
    (
        'student3',
        'e8c8b9c7c8f4e5d6a9b8c7d6e5f4g3h2i1j0k9l8m7n6o5p4q3r2s1t0u9v8w7x6y5z4',
        'abcdef1234567890',
        '赵小刚',
        '13800138005',
        '20210003',
        'student'
    );

-- 插入示例预约记录
INSERT INTO
    reservations (
        user_id,
        purpose,
        departure_time,
        return_time,
        destination,
        emergency_contact,
        emergency_phone,
        status,
        pass_code
    )
VALUES
    (
        4,
        '回家看望父母',
        '2024-01-15 08:00:00',
        '2024-01-17 20:00:00',
        '北京市海淀区',
        '王父',
        '13600136001',
        'approved',
        'PASS20240115001'
    ),
    (
        5,
        '参加学术会议',
        '2024-01-16 09:00:00',
        '2024-01-18 18:00:00',
        '上海市浦东新区',
        '李母',
        '13600136002',
        'pending',
        NULL
    ),
    (
        6,
        '实习工作',
        '2024-01-17 07:30:00',
        '2024-01-19 19:00:00',
        '深圳市南山区',
        '赵父',
        '13600136003',
        'approved',
        'PASS20240117001'
    );

-- 插入系统配置
INSERT INTO
    system_config (config_key, config_value, description)
VALUES
    ('system.name', '校园通行证预约管理系统', '系统名称'),
    ('system.version', '1.0.0', '系统版本'),
    ('reservation.advance_days', '7', '预约提前天数'),
    ('reservation.max_duration', '30', '单次预约最大天数'),
    ('qr.code.expiry_hours', '24', 'QR码有效期（小时）'),
    ('notification.email.enabled', 'true', '邮件通知开关'),
    ('audit.log.retention_days', '365', '审计日志保留天数');

-- 插入示例通知
INSERT INTO
    notifications (user_id, title, content, type)
VALUES
    (
        4,
        '预约审批通过',
        '您的出入校预约申请已通过审批，请及时查看通行证信息。',
        'success'
    ),
    (5, '预约待审批', '您的出入校预约申请正在审批中，请耐心等待。', 'info'),
    (
        6,
        '预约审批通过',
        '您的出入校预约申请已通过审批，请及时查看通行证信息。',
        'success'
    );

-- 插入审计日志示例
INSERT INTO
    audit_logs (
        user_id,
        action,
        table_name,
        record_id,
        new_values,
        ip_address
    )
VALUES
    (
        1,
        'CREATE',
        'users',
        4,
        '{"username":"student1","role":"student"}',
        '127.0.0.1'
    ),
    (
        1,
        'CREATE',
        'users',
        5,
        '{"username":"student2","role":"student"}',
        '127.0.0.1'
    ),
    (
        1,
        'APPROVE',
        'reservations',
        1,
        '{"status":"approved","pass_code":"PASS20240115001"}',
        '127.0.0.1'
    );