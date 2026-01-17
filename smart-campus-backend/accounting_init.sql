-- 个人记账系统数据库初始化脚本 (MySQL)
-- 建议在执行前确保数据库 smart_campus 已创建并选中

-- 1. 记账分类表
CREATE TABLE IF NOT EXISTS `acc_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `type` int(11) NOT NULL COMMENT '1:收入 2:支出',
  `user_id` bigint(20) DEFAULT NULL COMMENT '所属用户ID，NULL为系统默认',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 强制 user_id 允许为空 (兼容旧表结构)
ALTER TABLE `acc_category` MODIFY `user_id` bigint(20) NULL;

-- 2. 账单明细表
CREATE TABLE IF NOT EXISTS `acc_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `category_id` int(11) NOT NULL COMMENT '分类ID',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `type` int(11) NOT NULL COMMENT '1:收入 2:支出',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`bill_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. 财务目标表
CREATE TABLE IF NOT EXISTS `acc_financial_goal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `goal_name` varchar(100) DEFAULT '我的储蓄目标' COMMENT '目标名称',
  `target_amount` decimal(15,2) DEFAULT '0.00' COMMENT '目标金额',
  `current_amount` decimal(15,2) DEFAULT '0.00' COMMENT '当前已达成金额',
  `target_date` date DEFAULT NULL COMMENT '目标日期',
  `status` int(11) DEFAULT '0' COMMENT '0:进行中 1:已达成',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. 预算设置表
CREATE TABLE IF NOT EXISTS `acc_budget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `month` varchar(7) NOT NULL COMMENT '月份 (yyyy-MM)',
  `total_budget` decimal(15,2) DEFAULT '0.00' COMMENT '预算总额',
  `used_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已使用金额',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_month` (`user_id`,`month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. 插入系统默认分类 (如果不存在)
INSERT IGNORE INTO `acc_category` (`id`, `name`, `type`, `user_id`) VALUES 
(1, '工资薪金', 1, NULL), (2, '兼职收入', 1, NULL), (3, '理财收益', 1, NULL), (4, '礼金红包', 1, NULL), (5, '其他收入', 1, NULL),
(6, '餐饮美食', 2, NULL), (7, '交通出行', 2, NULL), (8, '服饰美容', 2, NULL), (9, '日用百货', 2, NULL), (10, '休闲娱乐', 2, NULL),
(11, '学习进修', 2, NULL), (12, '医疗健康', 2, NULL), (13, '住房物业', 2, NULL), (14, '水电煤气', 2, NULL), (15, '人情往来', 2, NULL);
