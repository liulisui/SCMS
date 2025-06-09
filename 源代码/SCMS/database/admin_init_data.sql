-- 初始化管理员角色和权限数据
USE scms;

-- 系统管理员初始化数据（临时密码：Admin@123，将在首次登录时强制修改）
-- 注意：实际项目中应该使用真实的SM3哈希值和随机盐值
-- 这里的密码哈希示例只是占位符，实际使用时应该替换为真正的SM3哈希值
INSERT INTO administrators 
(username, password, salt, real_name, admin_type, status, password_last_changed, created_at) 
VALUES 
('sysadmin', 
 'bf07b7c6e3467af2d62189496af310ad03d953257c76f70b944b2c48142cbae2', -- 临时密码Admin@123的SM3哈希值 
 'a1b2c3d4e5f6g7h8', -- 盐值
 '系统管理员',
 'system_admin',
 'active',
 NOW(),
 NOW()
);

-- 获取系统管理员ID
SET @sysadmin_id = LAST_INSERT_ID();

-- 创建初始部门（信息技术部）
INSERT INTO departments
(dept_code, dept_name, dept_type, description, status, created_by)
VALUES
('IT', '信息技术部', 'administrative', '负责系统运维和管理', 'active', @sysadmin_id);

-- 获取信息技术部ID
SET @it_dept_id = LAST_INSERT_ID();

-- 将系统管理员关联到信息技术部
UPDATE administrators SET department_id = @it_dept_id WHERE id = @sysadmin_id;

-- 审计管理员
INSERT INTO administrators 
(username, password, salt, real_name, department_id, admin_type, status, password_last_changed, created_at) 
VALUES 
('auditadmin', 
 'bf07b7c6e3467af2d62189496af310ad03d953257c76f70b944b2c48142cbae2', -- 临时密码Admin@123的SM3哈希值 
 'h8g7f6e5d4c3b2a1', -- 盐值 
 '审计管理员',
 @it_dept_id,
 'audit_admin',
 'active',
 NOW(),
 NOW()
);

-- 获取审计管理员ID
SET @auditadmin_id = LAST_INSERT_ID();

-- 学校管理员
INSERT INTO administrators 
(username, password, salt, real_name, department_id, admin_type, status, password_last_changed, created_at) 
VALUES 
('schooladmin', 
 'bf07b7c6e3467af2d62189496af310ad03d953257c76f70b944b2c48142cbae2', -- 临时密码Admin@123的SM3哈希值
 '1a2b3c4d5e6f7g8h', -- 盐值
 '学校管理员',
 @it_dept_id,
 'school_admin',
 'active',
 NOW(),
 NOW()
);

-- 获取学校管理员ID
SET @schooladmin_id = LAST_INSERT_ID();

-- 为系统管理员分配所有权限
INSERT INTO admin_permissions (admin_id, permission_id, granted_by)
SELECT @sysadmin_id, id, @sysadmin_id FROM permissions;

-- 为审计管理员分配日志查看权限
INSERT INTO admin_permissions (admin_id, permission_id, granted_by)
SELECT @auditadmin_id, id, @sysadmin_id FROM permissions WHERE permission_code = 'AUDIT_LOG_VIEW';

-- 为学校管理员分配除系统配置外的所有权限
INSERT INTO admin_permissions (admin_id, permission_id, granted_by)
SELECT @schooladmin_id, id, @sysadmin_id FROM permissions WHERE permission_code != 'SYSTEM_CONFIG';