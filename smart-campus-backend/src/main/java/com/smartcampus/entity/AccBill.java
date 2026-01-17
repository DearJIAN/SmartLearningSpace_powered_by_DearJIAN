package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单记录实体
 */
@Data
@TableName("acc_bill")
public class AccBill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 sys_user.user_id
     */
    private Long userId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 类型：1=收入，2=支出
     */
    private Integer type;

    /**
     * 分类ID，关联 acc_category.id
     */
    private Long categoryId;

    /**
     * 账单日期
     */
    private LocalDate billDate;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    // ========== 非数据库字段，用于前端展示 ==========

    /**
     * 分类名称（关联查询后填充）
     */
    @TableField(exist = false)
    private String categoryName;
}
