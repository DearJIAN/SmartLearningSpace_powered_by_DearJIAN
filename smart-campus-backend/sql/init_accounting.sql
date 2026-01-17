-- 1. Create acc_category table
CREATE TABLE IF NOT EXISTS `acc_category` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `type` int(11) NOT NULL COMMENT '1:收入 2:支出',
  `user_id` bigint(20) DEFAULT NULL COMMENT '所属用户ID，NULL为系统默认',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- FIX: Ensure user_id allows NULL
ALTER TABLE `acc_category` MODIFY `user_id` bigint(20) NULL;

-- 2. Create acc_bill table
CREATE TABLE IF NOT EXISTS `acc_bill` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `category_id` int(11) NOT NULL COMMENT '分类ID',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `type` int(11) NOT NULL COMMENT '1:收入 2:支出',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Create acc_financial_goal table
CREATE TABLE IF NOT EXISTS `acc_financial_goal` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `goal_name` varchar(100) DEFAULT NULL COMMENT '目标名称',
  `target_amount` decimal(15,2) DEFAULT NULL COMMENT '目标金额',
  `current_amount` decimal(15,2) DEFAULT '0.00' COMMENT '当前金额',
  `target_date` date DEFAULT NULL COMMENT '目标日期',
  `status` int(11) DEFAULT '0' COMMENT '状态',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Create acc_budget table
CREATE TABLE IF NOT EXISTS `acc_budget` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `month` varchar(7) DEFAULT NULL COMMENT '月份',
  `total_budget` decimal(15,2) DEFAULT NULL COMMENT '总预算',
  `used_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已使用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Insert default categories
INSERT INTO `acc_category` (`name`, `type`, `user_id`) VALUES 
('工资薪金', 1, NULL), ('兼职收入', 1, NULL), ('理财收益', 1, NULL), ('礼金红包', 1, NULL), ('其他收入', 1, NULL),
('餐饮美食', 2, NULL), ('交通出行', 2, NULL), ('服饰美容', 2, NULL), ('日用百货', 2, NULL), ('休闲娱乐', 2, NULL),
('学习进修', 2, NULL), ('医疗健康', 2, NULL), ('住房物业', 2, NULL), ('水电煤气', 2, NULL), ('人情往来', 2, NULL);
