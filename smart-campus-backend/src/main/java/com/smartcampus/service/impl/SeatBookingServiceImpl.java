package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.SeatBooking;
import com.smartcampus.mapper.SeatBookingMapper;
import com.smartcampus.service.SeatBookingService;
import org.springframework.stereotype.Service;

/**
 * 座位预约Service实现类
 */
@Service
public class SeatBookingServiceImpl extends ServiceImpl<SeatBookingMapper, SeatBooking> implements SeatBookingService {
}
