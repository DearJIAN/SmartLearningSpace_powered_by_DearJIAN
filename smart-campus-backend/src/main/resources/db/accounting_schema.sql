-- ===================================================================
-- 个人记账模块数据库初始化脚本
-- 作用：创建 acc_* 表并插入预置数据
-- 执行：Spring Boot 启动时自动执行（需配置 spring.sql.init.mode=always）
-- ===================================================================

USE smart_campus;

-- 1. 分类表
CREATE TABLE IF NOT EXISTS acc_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  user_id BIGINT NOT NULL COMMENT '用户ID，关联 sys_user.user_id',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  type INT NOT NULL COMMENT '类型：1=收入，2=支出',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账分类表';

-- 2. 账单表
CREATE TABLE IF NOT EXISTS acc_bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
  user_id BIGINT NOT NULL COMMENT '用户ID，关联 sys_user.user_id',
  amount DECIMAL(10,2) NOT NULL COMMENT '金额',
  type INT NOT NULL COMMENT '类型：1=收入，2=支出',
  category_id BIGINT COMMENT '分类ID，关联 acc_category.id',
  bill_date DATE NOT NULL COMMENT '账单日期',
  remark VARCHAR(255) COMMENT '备注',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_user_date (user_id, bill_date),
  INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账单记录表';

-- 3. 预算表
CREATE TABLE IF NOT EXISTS acc_budget (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '预算ID',
  user_id BIGINT NOT NULL COMMENT '用户ID，关联 sys_user.user_id',
  month VARCHAR(7) NOT NULL COMMENT '月份，格式：YYYY-MM',
  total_budget DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '总预算',
  used_amount DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已使用金额',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_month (user_id, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度预算表';

-- 4. 财务目标表
CREATE TABLE IF NOT EXISTS acc_financial_goal (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '目标ID',
  user_id BIGINT NOT NULL COMMENT '用户ID，关联 sys_user.user_id',
  goal_name VARCHAR(100) NOT NULL COMMENT '目标名称',
  target_amount DECIMAL(10,2) NOT NULL COMMENT '目标金额',
  current_amount DECIMAL(10,2) DEFAULT 0 COMMENT '当前金额',
  target_date DATE COMMENT '目标日期',
  status INT DEFAULT 0 COMMENT '状态：0=进行中，1=已完成，2=已放弃',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务目标表';

-- 5. 预置分类数据（系统公共，user_id=0）
INSERT INTO acc_category (id, user_id, name, type) VALUES 
(1, 0, '工资', 1),
(2, 0, '奖金', 1),
(3, 0, '投资收益', 1),
(4, 0, '其他收入', 1),
(5, 0, '餐饮', 2),
(6, 0, '交通', 2),
(7, 0, '购物', 2),
(8, 0, '住房', 2),
(9, 0, '娱乐', 2),
(10, 0, '医疗', 2),
(11, 0, '教育', 2),
(12, 0, '其他支出', 2)
ON DUPLICATE KEY UPDATE name=name;
