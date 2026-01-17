-- ===================================================================
-- 🟢 自动初始化脚本
-- 当项目启动时，Spring Boot 会自动检测并执行此文件。
-- 作用：如果对方数据库里没有表，这里会自动创建，确保新环境能直接运行！
-- ===================================================================

-- 1. 用户表 (用于登录和注册)
CREATE TABLE IF NOT EXISTS user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  created_time DATETIME
);

-- 2. 分类表
CREATE TABLE IF NOT EXISTS category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  name VARCHAR(50) NOT NULL,
  type INT NOT NULL COMMENT '1:收入 2:支出'
);

-- 预置一些分类数据 (使用 INSERT IGNORE 避免重复报错)
-- 这里的 user_id = 0 代表系统公共分类
INSERT IGNORE INTO category (id, user_id, name, type) VALUES 
(1, 0, '工资', 1), 
(2, 0, '奖金', 1), 
(3, 0, '餐饮', 2), 
(4, 0, '交通', 2), 
(5, 0, '购物', 2), 
(6, 0, '住房', 2);

-- 3. 账单表
CREATE TABLE IF NOT EXISTS bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  type INT NOT NULL,
  category_id BIGINT,
  bill_date DATE,
  remark VARCHAR(255)
);

-- 4. 预算表
CREATE TABLE IF NOT EXISTS budget (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  month VARCHAR(7),
  total_budget DECIMAL(10,2),
  used_amount DECIMAL(10,2)
);

-- 5. 财务目标表
CREATE TABLE IF NOT EXISTS financial_goal (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  target_amount DECIMAL(10,2),
  target_date DATE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
