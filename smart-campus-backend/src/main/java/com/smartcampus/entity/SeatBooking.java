package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 座位预约实体类
 */
@Data
@TableName("seat_booking")
public class SeatBooking implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "booking_id", type = IdType.AUTO)
    private Long bookingId;

    private Long userId;

    private Long roomId;

    private String seatNumber;

    private Date startTime;

    private Date endTime;

    /**
     * 状态：0-已预约, 1-使用中, 2-已签退, 3-违规
     */
    private Integer status;
}
