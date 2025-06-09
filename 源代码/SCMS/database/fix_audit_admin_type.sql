-- 修复审计管理员的管理员类型
-- 将real_name为"审计管理员"的用户admin_type设置为'audit_admin'

USE scms;

-- 更新审计管理员的类型
UPDATE administrators 
SET admin_type = 'audit_admin' 
WHERE real_name = '审计管理员';

-- 验证更新结果
SELECT id, username, real_name, admin_type, status 
FROM administrators 
WHERE real_name = '审计管理员';

-- 如果没有审计管理员，则创建一个示例
INSERT IGNORE INTO administrators 
(username, password, salt, real_name, admin_type, status, password_last_changed, created_at) 
VALUES 
('auditadmin', 
 'bf07b7c6e3467af2d62189496af310ad03d953257c76f70b944b2c48142cbae2', -- 临时密码Admin@123的SM3哈希值 
 'a1b2c3d4e5f6g7h8', -- 盐值
 '审计管理员',
 'audit_admin',
 'active',
 NOW(),
 NOW()
);

-- 显示所有管理员的类型信息
SELECT id, username, real_name, admin_type, status 
FROM administrators 
ORDER BY admin_type, real_name;
