-- 修复 admin_type 列的值，确保使用正确的枚举值
-- Fix admin_type column values to use correct enum values

USE scms;

-- 查看当前所有管理员的 admin_type 值
SELECT id, username, real_name, admin_type, status 
FROM administrators 
ORDER BY admin_type, real_name;

-- 更新可能的错误值
-- 如果有使用 'department' 的记录，更新为 'department_admin'
UPDATE administrators 
SET admin_type = 'department_admin' 
WHERE admin_type = 'department';

-- 如果有使用 'general' 的记录，更新为 'school_admin'
UPDATE administrators 
SET admin_type = 'school_admin' 
WHERE admin_type = 'general';

-- 如果有使用 'super' 的记录，更新为 'system_admin'
UPDATE administrators 
SET admin_type = 'system_admin' 
WHERE admin_type = 'super';

-- 显示更新后的结果
SELECT id, username, real_name, admin_type, status 
FROM administrators 
ORDER BY admin_type, real_name;
