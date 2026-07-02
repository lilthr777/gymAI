-- gymAI v2.0 数据库迁移脚本
-- 用途：为已有数据库新增多角色管理所需的字段和表
-- 执行方式：mysql -u root -p gymai < migration_v2.sql

USE gymai;

-- 1. user 表新增 role 列
SET @sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''MEMBER'' COMMENT ''角色'' AFTER status', 'SELECT 1') FROM information_schema.columns WHERE table_schema = 'gymai' AND table_name = 'user' AND column_name = 'role');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2. course 表新增 price 和 location 列
SET @sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE course ADD COLUMN price DECIMAL(10,2) DEFAULT 0 COMMENT ''价格'' AFTER current_count', 'SELECT 1') FROM information_schema.columns WHERE table_schema = 'gymai' AND table_name = 'course' AND column_name = 'price');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(COUNT(*) = 0, 'ALTER TABLE course ADD COLUMN location VARCHAR(255) COMMENT ''地点'' AFTER description', 'SELECT 1') FROM information_schema.columns WHERE table_schema = 'gymai' AND table_name = 'course' AND column_name = 'location');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. 创建缺失的表（如果不存在）
CREATE TABLE IF NOT EXISTS card_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_type VARCHAR(20) NOT NULL COMMENT '会员卡类型: MONTH/QUARTER/YEAR/LIFETIME',
    amount INT NOT NULL COMMENT '金额（元）',
    status TINYINT DEFAULT 1 COMMENT '状态 0-已取消 1-已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '会员卡订单表';

CREATE TABLE IF NOT EXISTS course_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    rating INT NOT NULL COMMENT '评分 1-5',
    comment VARCHAR(500) COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '课程评价表';

CREATE TABLE IF NOT EXISTS user_favorite_coach (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_coach (user_id, coach_id)
) COMMENT '用户收藏教练表';

-- 4. 插入管理员账号 (密码: 123456)，如已存在则跳过
INSERT IGNORE INTO user (username, phone, password, nickname, gender, role) VALUES
('admin', '13800000000', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '管理员', 1, 'ADMIN');

-- 5. 为现有教练创建登录账号 (密码: 123456, 角色: COACH)
INSERT IGNORE INTO user (username, phone, password, nickname, gender, role)
SELECT CONCAT('coach_', id), phone, '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', name, gender, 'COACH'
FROM coach WHERE status = 1;

-- 6. 将现有普通用户的 role 默认设为 MEMBER
UPDATE user SET role = 'MEMBER' WHERE role IS NULL OR role = '';
