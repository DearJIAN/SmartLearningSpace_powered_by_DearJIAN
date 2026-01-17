package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 教室实体类
 */
@Data
@TableName("sys_classroom")
public class SysClassroom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "room_id", type = IdType.AUTO)
    private Long roomId;

    private String roomName;

    private Integer capacity;

    private String cameraUrl;

    /**
     * 是否启用：1-启用, 0-禁用
     */
    private Integer isActive;
}
