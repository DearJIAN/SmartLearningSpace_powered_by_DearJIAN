-- 记账分类表
DROP TABLE IF EXISTS `acc_category`;
CREATE TABLE `acc_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '用户ID (0表示系统默认)',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `type` tinyint(2) NOT NULL COMMENT '类型: 1=收入, 2=支出',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='记账-分类表';

-- 账单记录表
DROP TABLE IF EXISTS `acc_bill`;
CREATE TABLE `acc_bill` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `type` tinyint(2) NOT NULL COMMENT '类型: 1=收入, 2=支出',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `bill_date` date NOT NULL COMMENT '账单日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`bill_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='记账-账单表';

-- 插入一些系统默认分类 (作为种子数据，虽然代码里有初始化，但数据库里有一份更稳妥)
INSERT INTO `acc_category` (`user_id`, `name`, `type`) VALUES 
(0, '工资奖金', 1),
(0, '理财收益', 1),
(0, '兼职收入', 1),
(0, '其他收入', 1),
(0, '餐饮美食', 2),
(0, '服饰美容', 2),
(0, '交通出行', 2),
(0, '休闲娱乐', 2),
(0, '生活日用', 2),
(0, '住房物业', 2),
(0, '医疗健康', 2),
(0, '人情往来', 2);
