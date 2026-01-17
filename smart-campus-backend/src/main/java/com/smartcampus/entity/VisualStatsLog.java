package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视觉数据日志实体类
 */
@Data
@TableName("visual_stats_log")
public class VisualStatsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long roomId;

    private Integer personCount;

    private Integer phoneCount;

    /**
     * 专注度指数
     */
    private Double focusIndex;

    private Date createTime;

    /**
     * 原始JSON数据
     */
    private String rawJson;
}
