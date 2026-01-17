package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 月度预算实体
 */
@Data
@TableName("acc_budget")
public class AccBudget {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 sys_user.user_id
     */
    private Long userId;

    /**
     * 月份，格式：YYYY-MM（如 2024-01）
     */
    private String month;

    /**
     * 总预算
     */
    private BigDecimal totalBudget;

    /**
     * 已使用金额
     */
    private BigDecimal usedAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
