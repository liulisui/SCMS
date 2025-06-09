-- 为手机端功能添加新字段，解决SQL字段不匹配问题
-- 首先添加代码中使用但表中缺失的核心字段
-- 添加用户姓名字段
ALTER TABLE reservations
ADD COLUMN user_name VARCHAR(100) COMMENT '用户姓名';

-- 添加手机号字段
ALTER TABLE reservations
ADD COLUMN phone VARCHAR(20) COMMENT '手机号';

-- 添加身份证号字段 
ALTER TABLE reservations
ADD COLUMN id_card VARCHAR(18) COMMENT '身份证号';

-- 添加预约类型字段
ALTER TABLE reservations
ADD COLUMN reservation_type VARCHAR(20) COMMENT '预约类型(public/official)';

-- 添加访问时间字段
ALTER TABLE reservations
ADD COLUMN visit_time DATETIME COMMENT '访问时间';

-- 添加停留时长字段
ALTER TABLE reservations
ADD COLUMN duration VARCHAR(20) COMMENT '停留时长';

-- 添加创建和更新时间字段
ALTER TABLE reservations
ADD COLUMN create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';

ALTER TABLE reservations
ADD COLUMN update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 添加审核相关字段
ALTER TABLE reservations
ADD COLUMN review_reason TEXT COMMENT '审核意见';

ALTER TABLE reservations
ADD COLUMN reviewer_id INT COMMENT '审核人ID';

ALTER TABLE reservations
ADD COLUMN review_time TIMESTAMP NULL COMMENT '审核时间';

-- 添加预约校区字段
ALTER TABLE reservations
ADD COLUMN campus VARCHAR(100) COMMENT '预约校区';

-- 添加所在单位字段
ALTER TABLE reservations
ADD COLUMN organization VARCHAR(200) COMMENT '所在单位';

-- 添加交通方式字段
ALTER TABLE reservations
ADD COLUMN transport_mode VARCHAR(50) COMMENT '交通方式';

-- 添加车牌号字段（可选）
ALTER TABLE reservations
ADD COLUMN license_plate VARCHAR(20) COMMENT '车牌号';

-- 添加随行人员信息字段（JSON格式存储）
ALTER TABLE reservations
ADD COLUMN companions JSON COMMENT '随行人员信息';

-- 添加公务访问部门字段
ALTER TABLE reservations
ADD COLUMN official_department VARCHAR(200) COMMENT '公务访问部门';

-- 添加公务访问接待人字段
ALTER TABLE reservations
ADD COLUMN official_contact_person VARCHAR(100) COMMENT '公务访问接待人';

-- 添加来访事由字段（针对公务预约）
ALTER TABLE reservations
ADD COLUMN official_reason TEXT COMMENT '公务来访事由';

-- 添加二维码数据字段
ALTER TABLE reservations
ADD COLUMN qr_code_data TEXT COMMENT '二维码数据';

-- 添加真实身份证号字段
ALTER TABLE reservations
ADD COLUMN real_id_card VARCHAR(18) COMMENT '真实身份证号';

-- 添加真实姓名字段 
ALTER TABLE reservations
ADD COLUMN real_name VARCHAR(100) COMMENT '真实姓名';

-- 添加真实手机号字段
ALTER TABLE reservations
ADD COLUMN real_phone VARCHAR(20) COMMENT '真实手机号';