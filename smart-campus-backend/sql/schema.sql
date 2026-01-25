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

-- 图书馆座位表
CREATE TABLE IF NOT EXISTS sys_seat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_code VARCHAR(20) NOT NULL UNIQUE,
    status INT DEFAULT 0 COMMENT '0:未被占用, 1:已预约, 2:已被占用',
    user_name VARCHAR(50),
    start_time DATETIME,
    check_in_time DATETIME,
    end_time DATETIME,
    duration INT DEFAULT 0 COMMENT '使用时长(秒)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书馆座位表';
