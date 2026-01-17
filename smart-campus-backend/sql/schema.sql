-- 智学空间 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS smart_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE smart_campus;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    role INT DEFAULT 2 COMMENT '0:管理员, 1:老师, 2:学生',
    credit_score INT DEFAULT 100
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 教室表
CREATE TABLE IF NOT EXISTS sys_classroom (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(50),
    capacity INT,
    camera_url VARCHAR(255),
    is_active TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教室表';

-- 视觉数据日志表 (核心业务)
CREATE TABLE IF NOT EXISTS visual_stats_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT,
    person_count INT,
    phone_count INT,
    focus_index DOUBLE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    raw_json TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视觉数据日志表';

-- 预约表
CREATE TABLE IF NOT EXISTS seat_booking (
    booking_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    room_id BIGINT,
    seat_number VARCHAR(20),
    start_time DATETIME,
    end_time DATETIME,
    status INT DEFAULT 0 COMMENT '0:已预约, 1:使用中, 2:已签退, 3:违规'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位预约表';
