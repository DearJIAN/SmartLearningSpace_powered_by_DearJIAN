package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_seat")
public class SysSeat {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 座位编号 S001 - S180
    private String seatCode;

    // 状态 0:未被占用, 1:已预约, 2:已被占用
    private Integer status;

    private String userName;

    private LocalDateTime startTime;
    private LocalDateTime checkInTime;
    private LocalDateTime endTime;

    // 预约或使用时长（秒）
    private Integer duration;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
