CREATE DATABASE IF NOT EXISTS personal_bookkeeping DEFAULT CHARACTER SET utf8mb4;
USE personal_bookkeeping;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  created_time DATETIME
);

-- 分类表 (预置一些数据)
CREATE TABLE IF NOT EXISTS category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  name VARCHAR(50) NOT NULL,
  type INT NOT NULL COMMENT '1:收入 2:支出'
);
INSERT INTO category (user_id, name, type) VALUES (0, '工资', 1), (0, '奖金', 1), (0, '餐饮', 2), (0, '交通', 2), (0, '购物', 2), (0, '住房', 2);

-- 账单表
CREATE TABLE IF NOT EXISTS bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  type INT NOT NULL,
  category_id BIGINT,
  bill_date DATE,
  remark VARCHAR(255)
);

-- 预算表
CREATE TABLE IF NOT EXISTS budget (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  month VARCHAR(7),
  total_budget DECIMAL(10,2),
  used_amount DECIMAL(10,2)
);

-- 财务目标表
CREATE TABLE IF NOT EXISTS financial_goal (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_amount DECIMAL(10,2),
  target_date DATE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
