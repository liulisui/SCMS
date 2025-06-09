-- 部门级权限控制：部门-校区映射表
USE scms;

-- 1. 创建部门-校区映射表
CREATE TABLE IF NOT EXISTS department_campus_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL COMMENT '部门ID',
    campus_id BIGINT NOT NULL COMMENT '校区ID',
    permission_type ENUM('public', 'official', 'both') NOT NULL DEFAULT 'both' COMMENT '权限类型：公共预约、公务预约、全部',
    status ENUM('active', 'inactive') NOT NULL DEFAULT 'active' COMMENT '状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE,
    FOREIGN KEY (campus_id) REFERENCES campuses (id) ON DELETE CASCADE,
    UNIQUE KEY uk_dept_campus_type (department_id, campus_id, permission_type),
    INDEX idx_department_id (department_id),
    INDEX idx_campus_id (campus_id),
    INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='部门-校区权限映射表';

-- 2. 插入初始映射数据（为现有部门分配校区权限）
-- 保卫处负责所有校区的预约管理
INSERT IGNORE INTO department_campus_mapping (department_id, campus_id, permission_type, status) 
SELECT d.id, c.id, 'both', 'active'
FROM departments d, campuses c 
WHERE d.department_code = 'SECURITY'
AND c.status = 'active';

-- 为其他行政部门分配主校区权限（如果存在MAIN校区）
INSERT IGNORE INTO department_campus_mapping (department_id, campus_id, permission_type, status) 
SELECT d.id, c.id, 'official', 'active'
FROM departments d, campuses c 
WHERE d.department_type = 'administrative' 
AND d.department_code != 'SECURITY'
AND c.campus_code IN ('MAIN', 'PF')  -- 主校区或屏峰校区
AND c.status = 'active';

-- 3. 显示创建结果
SELECT 'Department-Campus Mapping Table Created' as result;
SELECT COUNT(*) as mapping_count FROM department_campus_mapping;
