package com.smartcampus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.smartcampus.common.Result;

@RestController
@RequestMapping("/api/accounting/setup")
public class AccSetupController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public Result<String> initDatabase() {
        try {
            // 1. Create acc_category table
            String sqlCategory = "CREATE TABLE IF NOT EXISTS `acc_category` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `name` varchar(50) NOT NULL COMMENT '分类名称'," +
                    "  `type` int(11) NOT NULL COMMENT '1:收入 2:支出'," +
                    "  `user_id` int(11) DEFAULT NULL COMMENT '所属用户ID，NULL为系统默认'," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            jdbcTemplate.execute(sqlCategory);

            // Fix: Force existing table to allow NULL user_id
            try {
                jdbcTemplate.execute("ALTER TABLE acc_category MODIFY COLUMN user_id bigint(20) NULL");
            } catch (Exception e) {
                // Ignore if fails
            }

            // Fix: Force existing table to allow NULL user_id
            try {
                jdbcTemplate.execute("ALTER TABLE acc_category MODIFY COLUMN user_id bigint(20) NULL");
            } catch (Exception e) {
                // Ignore if fails
            }

            // 2. Create acc_bill table
            String sqlBill = "CREATE TABLE IF NOT EXISTS `acc_bill` (" +
                    "  `id` int(11) NOT NULL AUTO_INCREMENT," +
                    "  `user_id` int(11) NOT NULL COMMENT '用户ID'," +
                    "  `category_id` int(11) NOT NULL COMMENT '分类ID'," +
                    "  `amount` decimal(10,2) NOT NULL COMMENT '金额'," +
                    "  `type` int(11) NOT NULL COMMENT '1:收入 2:支出'," +
                    "  `bill_date` date NOT NULL COMMENT '账单日期'," +
                    "  `remark` varchar(255) DEFAULT NULL COMMENT '备注'," +
                    "  `create_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            jdbcTemplate.execute(sqlBill);

            // 3. Create acc_financial_goal table
            String sqlGoal = "CREATE TABLE IF NOT EXISTS `acc_financial_goal` (" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT," +
                    "  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID'," +
                    "  `goal_name` varchar(100) DEFAULT NULL COMMENT '目标名称'," +
                    "  `target_amount` decimal(15,2) DEFAULT NULL COMMENT '目标金额'," +
                    "  `current_amount` decimal(15,2) DEFAULT '0.00' COMMENT '当前金额'," +
                    "  `target_date` date DEFAULT NULL COMMENT '目标日期'," +
                    "  `status` int(11) DEFAULT '0' COMMENT '状态'," +
                    "  `created_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            jdbcTemplate.execute(sqlGoal);

            // 4. Create acc_budget table
            String sqlBudget = "CREATE TABLE IF NOT EXISTS `acc_budget` (" +
                    "  `id` bigint(20) NOT NULL AUTO_INCREMENT," +
                    "  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID'," +
                    "  `month` varchar(7) DEFAULT NULL COMMENT '月份'," +
                    "  `total_budget` decimal(15,2) DEFAULT NULL COMMENT '总预算'," +
                    "  `used_amount` decimal(15,2) DEFAULT '0.00' COMMENT '已使用'," +
                    "  `created_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                    "  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  PRIMARY KEY (`id`)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            jdbcTemplate.execute(sqlBudget);

            // 5. Insert default categories if they don't exist
            String checkCount = "SELECT count(*) FROM acc_category WHERE user_id IS NULL";
            Integer count = jdbcTemplate.queryForObject(checkCount, Integer.class);

            if (count != null && count == 0) {
                String insertData = "INSERT INTO `acc_category` (`name`, `type`, `user_id`) VALUES " +
                        "('奖学金', 1, NULL), ('理财收益', 1, NULL), ('兼职收入', 1, NULL), ('生活费', 1, NULL), ('其他收入', 1, NULL),"
                        +
                        "('餐饮美食', 2, NULL), ('服饰美容', 2, NULL), ('交通出行', 2, NULL), ('娱乐休闲', 2, NULL), ('生活日用', 2, NULL),"
                        +
                        "('住宿租赁', 2, NULL), ('学术学习', 2, NULL), ('医疗健康', 2, NULL), ('人情往来', 2, NULL), ('其他杂项', 2, NULL);";
                jdbcTemplate.execute(insertData);
            }

            return Result.success("Database tables created and initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Initialization failed: " + e.getMessage());
        }
    }

    @GetMapping("/reset-categories")
    public Result<String> resetCategories() {
        try {
            // 1. Clear all categories (System + User)
            jdbcTemplate.execute("TRUNCATE TABLE acc_category");

            // 2. Insert new System Default Categories
            String insertData = "INSERT INTO `acc_category` (`name`, `type`, `user_id`) VALUES " +
                    "('奖学金', 1, NULL), ('理财收益', 1, NULL), ('兼职收入', 1, NULL), ('生活费', 1, NULL), ('其他收入', 1, NULL),"
                    +
                    "('餐饮美食', 2, NULL), ('服饰美容', 2, NULL), ('交通出行', 2, NULL), ('娱乐休闲', 2, NULL), ('生活日用', 2, NULL),"
                    +
                    "('住宿租赁', 2, NULL), ('学术学习', 2, NULL), ('医疗健康', 2, NULL), ('人情往来', 2, NULL), ('其他杂项', 2, NULL);";
            jdbcTemplate.execute(insertData);

            return Result.success("Categories reset successfully! Old categories deleted.");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("Reset failed: " + e.getMessage());
        }
    }
}
