package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务目标实体
 */
@Data
@TableName("acc_financial_goal")
public class AccFinancialGoal {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 sys_user.user_id
     */
    private Long userId;

    /**
     * 目标名称
     */
    private String goalName;

    /**
     * 目标金额
     */
    private BigDecimal targetAmount;

    /**
     * 当前金额
     */
    private BigDecimal currentAmount;

    /**
     * 目标日期
     */
    private LocalDate targetDate;

    /**
     * 状态：0=进行中，1=已完成，2=已放弃
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
