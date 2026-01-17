package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 记账分类实体
 */
@Data
@TableName("acc_category")
public class AccCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联 sys_user.user_id
     * user_id=0 表示系统预置分类
     */
    private Long userId;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 类型：1=收入，2=支出
     */
    private Integer type;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
