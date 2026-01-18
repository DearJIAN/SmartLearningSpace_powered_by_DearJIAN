-- 失物招领模块数据库表结构
-- 文件名：lost_found_table.sql
-- 描述：失物招领智能辅助模块的数据库表定义

-- 创建失物招领表
CREATE TABLE IF NOT EXISTS `lf_lost_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `room_id` BIGINT(20) NOT NULL COMMENT '教室ID',
    `room_name` VARCHAR(50) NOT NULL COMMENT '教室名称',
    `item_type` VARCHAR(50) NOT NULL COMMENT '物品类型（书包/水杯/雨伞/电子设备等）',
    `item_count` INT(11) NOT NULL DEFAULT 1 COMMENT '物品数量',
    `found_time` DATETIME NOT NULL COMMENT '发现时间',
    `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '状态：0-未认领，1-已认领',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_room_id` (`room_id`),
    KEY `idx_found_time` (`found_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物招领表';

-- 说明：
-- 1. 该表用于存储AI识别到的教室遗留物品信息
-- 2. room_id关联sys_classroom表的room_id字段
-- 3. item_type字段存储YOLO识别到的物品类别
-- 4. status字段标识物品的认领状态
-- 5. 建议在系统初始化时执行此SQL文件，或通过数据库迁移工具执行
