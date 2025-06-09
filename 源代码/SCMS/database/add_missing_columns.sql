-- 为reservations表添加缺少的列
-- 执行日期: 2025-06-03
USE scms;

-- 添加来访目的字段 (必填)
ALTER TABLE reservations
ADD COLUMN purpose TEXT NOT NULL COMMENT '来访目的';

-- 添加目的地字段 (必填)
ALTER TABLE reservations
ADD COLUMN destination VARCHAR(200) NOT NULL COMMENT '目的地';

-- 添加预约状态字段 (用于跟踪审核状态)
ALTER TABLE reservations
ADD COLUMN status ENUM ('pending', 'approved', 'rejected', 'cancelled') NOT NULL DEFAULT 'pending' COMMENT '预约状态';

-- 为了兼容性，先将这些字段设为允许NULL，然后更新现有数据
ALTER TABLE reservations MODIFY COLUMN purpose TEXT NULL;

ALTER TABLE reservations MODIFY COLUMN destination VARCHAR(200) NULL;

-- 更新现有记录的默认值
UPDATE reservations
SET
    purpose = '待补充来访目的'
WHERE
    purpose IS NULL;

UPDATE reservations
SET
    destination = '待补充目的地'
WHERE
    destination IS NULL;

-- 重新设为NOT NULL（新记录必须填写）
ALTER TABLE reservations MODIFY COLUMN purpose TEXT NOT NULL;

ALTER TABLE reservations MODIFY COLUMN destination VARCHAR(200) NOT NULL;

-- 添加索引以提高查询性能
CREATE INDEX idx_reservations_status ON reservations (status);

CREATE INDEX idx_reservations_visit_time ON reservations (visit_time);

CREATE INDEX idx_reservations_campus ON reservations (campus);

-- 验证表结构
DESCRIBE reservations;