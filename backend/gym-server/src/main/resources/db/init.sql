-- gymAI C端 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS gymai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gymai;

-- 用户表（合并原 admin + member）
DROP TABLE IF EXISTS user;
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt 加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    card_type VARCHAR(20) COMMENT '会员卡类型: MONTH/QUARTER/YEAR/LIFETIME',
    card_start_date DATE COMMENT '开卡日期',
    card_end_date DATE COMMENT '到期日期',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '角色: ADMIN/COACH/MEMBER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '用户表';

-- 教练表
DROP TABLE IF EXISTS coach;
CREATE TABLE coach (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    specialty VARCHAR(255) COMMENT '特长（逗号分隔）',
    description TEXT COMMENT '简介',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 0-离职 1-在职',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '教练表';

-- 课程表
DROP TABLE IF EXISTS course;
CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    course_date DATE NOT NULL COMMENT '上课日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    max_capacity INT DEFAULT 20 COMMENT '最大人数',
    current_count INT DEFAULT 0 COMMENT '当前报名人数',
    price DECIMAL(10,2) DEFAULT 0 COMMENT '课程价格',
    description VARCHAR(500) COMMENT '课程描述',
    location VARCHAR(255) COMMENT '上课地点',
    status TINYINT DEFAULT 1 COMMENT '状态 0-取消 1-正常 2-满员',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '课程表';

-- 签到记录表
DROP TABLE IF EXISTS checkin;
CREATE TABLE checkin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    status TINYINT DEFAULT 1 COMMENT '签到状态 1-已签到 2-迟到 3-缺席',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '签到记录表';

-- 用户-课程关联表
DROP TABLE IF EXISTS user_course;
CREATE TABLE user_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    UNIQUE KEY uk_user_course (user_id, course_id)
) COMMENT '用户-课程报名表';

-- 会员卡订单表
DROP TABLE IF EXISTS card_order;
CREATE TABLE card_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    card_type VARCHAR(20) NOT NULL COMMENT '会员卡类型: MONTH/QUARTER/YEAR/LIFETIME',
    amount INT NOT NULL COMMENT '金额（元）',
    status TINYINT DEFAULT 1 COMMENT '状态 0-已取消 1-已完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '会员卡订单表';

-- 课程评价表
DROP TABLE IF EXISTS course_review;
CREATE TABLE course_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    rating INT NOT NULL COMMENT '评分 1-5',
    comment VARCHAR(500) COMMENT '评价内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '课程评价表';

-- 用户收藏教练表
DROP TABLE IF EXISTS user_favorite_coach;
CREATE TABLE user_favorite_coach (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_coach (user_id, coach_id)
) COMMENT '用户收藏教练表';

-- 插入测试用户 (密码: 123456)
INSERT INTO user (username, phone, password, nickname, gender, role, card_type, card_start_date, card_end_date) VALUES
('admin', '13800000000', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '管理员', 1, 'ADMIN', NULL, NULL, NULL),
('zhangsan', '13800000001', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '张三', 1, 'MEMBER', 'MONTH', '2025-01-01', '2025-02-01'),
('lisi', '13800000002', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '李四', 2, 'MEMBER', 'QUARTER', '2025-01-01', '2025-04-01'),
('wangwu', '13800000003', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '王五', 1, 'MEMBER', 'YEAR', '2025-01-01', '2026-01-01'),
('zhaoliu', '13800000004', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '赵六', 2, 'MEMBER', 'LIFETIME', '2025-01-01', NULL);

-- 插入测试教练数据
INSERT INTO coach (name, phone, gender, specialty, description) VALUES
('刘教练', '13900000001', 1, '增肌,减脂', '国家一级健身教练，从业10年'),
('陈教练', '13900000002', 2, '瑜伽,普拉提', '国际瑜伽联盟认证导师'),
('张教练', '13900000003', 1, '拳击,搏击', '退役运动员，实战经验丰富');

-- 为教练创建登录账号 (密码: 123456, 角色: COACH)
INSERT INTO user (username, phone, password, nickname, gender, role) VALUES
('coach_liu', '13900000001', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '刘教练', 1, 'COACH'),
('coach_chen', '13900000002', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '陈教练', 2, 'COACH'),
('coach_zhang', '13900000003', '$2a$10$nuYKUvkwd7yqb.FBgYrDYOi2lgJHuwsVgny86/xybD8OCK1472qAy', '张教练', 1, 'COACH');

-- 插入测试课程数据
INSERT INTO course (name, coach_id, course_date, start_time, end_time, max_capacity, price, location, description) VALUES
('力量训练', 1, CURDATE(), '08:00', '09:30', 20, 99.00, 'A区力量训练房', '基础力量训练，适合初学者'),
('瑜伽课', 2, CURDATE(), '10:00', '11:00', 15, 79.00, 'B区瑜伽室', '哈他瑜伽，身心放松'),
('拳击课', 3, CURDATE(), '14:00', '15:30', 10, 129.00, 'C区搏击训练区', '拳击基础技术训练'),
('高强度间歇', 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00', '09:00', 15, 89.00, 'A区力量训练房', 'HIIT训练，高效燃脂');
