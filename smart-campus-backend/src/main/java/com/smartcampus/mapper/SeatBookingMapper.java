package com.smartcampus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.entity.SeatBooking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 座位预约Mapper接口
 */
@Mapper
public interface SeatBookingMapper extends BaseMapper<SeatBooking> {
}
