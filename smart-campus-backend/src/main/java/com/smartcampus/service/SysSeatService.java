package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.SysSeat;

public interface SysSeatService extends IService<SysSeat> {

    // 初始化座位数据
    void initSeats();

    // 预约
    boolean reserve(Long seatId, String userName, Integer usageTimeSec, String startTime);

    // 签到
    boolean checkIn(Long seatId);

    // 续约
    boolean renew(Long seatId, Integer extendedTimeSec);

    // 签退/释放
    boolean checkOut(Long seatId);

    // 重置所有
    void resetAll();

    // 模拟数据
    void simulate();

    // 检查过期
    void checkExpiration();
}
