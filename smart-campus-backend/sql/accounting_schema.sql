-- ===================================================================
-- 合并后：与当前后端实体 `AccCategory` 和运行时代码默认分类一致的记账模块初始化脚本
-- 说明：此文件为本仓库权威的记账模块初始化脚本，包含表结构和按当前系统默认分类的种子数据。
-- 执行：Spring Boot 启动时可自动执行（如需自动执行，请在 `application.yml` 中设置 `spring.sql.init.mode=always`）
-- ===================================================================

USE smart_campus;

-- 1. 分类表（与实体 `AccCategory` 对应）
CREATE TABLE IF NOT EXISTS acc_category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
  user_id BIGINT NOT NULL DEFAULT 0 COMMENT '用户ID，0 表示系统预置',
  name VARCHAR(50) NOT NULL COMMENT '分类名称',
  type INT NOT NULL COMMENT '类型：1=收入，2=支出',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_type (user_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记账分类表';

-- 2. 账单表
CREATE TABLE IF NOT EXISTS acc_bill (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '账单ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
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
  user_id BIGINT NOT NULL COMMENT '用户ID',
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
  user_id BIGINT NOT NULL COMMENT '用户ID',
  goal_name VARCHAR(100) NOT NULL COMMENT '目标名称',
  target_amount DECIMAL(10,2) NOT NULL COMMENT '目标金额',
  current_amount DECIMAL(10,2) DEFAULT 0 COMMENT '当前金额',
  target_date DATE COMMENT '目标日期',
  status INT DEFAULT 0 COMMENT '状态：0=进行中，1=已完成，2=已放弃',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务目标表';

-- 5. 与当前系统运行时代码默认分类完全对应的预置分类（user_id=0 表示系统公共）
-- 注：分类名称来自 `AccCategoryServiceImpl.initDefaultCategories()` 的默认列表，以保证前端/后端一致性。
INSERT INTO acc_category (user_id, name, type) VALUES
  (0, '奖学金', 1),
  (0, '理财收益', 1),
  (0, '兼职收入', 1),
  (0, '生活费', 1),
  (0, '其他收入', 1),

  (0, '餐饮美食', 2),
  (0, '服饰美容', 2),
  (0, '交通出行', 2),
  (0, '娱乐休闲', 2),
  (0, '生活日用', 2),
  (0, '住宿租赁', 2),
  (0, '学术学习', 2),
  (0, '医疗健康', 2),
  (0, '人情往来', 2),
  (0, '其他杂项', 2)
ON DUPLICATE KEY UPDATE name=VALUES(name), type=VALUES(type);

-- End of merged authoritative accounting schema
