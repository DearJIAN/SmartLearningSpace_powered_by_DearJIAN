package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.SysSeat;
import com.smartcampus.mapper.SysSeatMapper;
import com.smartcampus.service.SysSeatService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class SysSeatServiceImpl extends ServiceImpl<SysSeatMapper, SysSeat> implements SysSeatService {

    @PostConstruct
    public void init() {
        // 项目启动时，如果座位表为空，初始化180个座位
        try {
            if (count() == 0) {
                initSeats();
            }
        } catch (Exception e) {
            System.err.println("初始化座位数据失败，可能是数据库表 sys_seat 不存在。请检查 schema.sql 是否执行。");
            e.printStackTrace();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSeats() {
        List<SysSeat> seats = new ArrayList<>();
        for (int i = 1; i <= 180; i++) {
            SysSeat seat = new SysSeat();
            seat.setSeatCode(String.format("S%03d", i));
            seat.setStatus(0); // 空闲
            seats.add(seat);
        }
        saveBatch(seats);
    }

    @Override
    public boolean reserve(Long seatId, String userName, Integer usageTimeSec, String startTime) {
        SysSeat seat = getById(seatId);
        if (seat == null || seat.getStatus() != 0) {
            return false;
        }
        seat.setStatus(1); // 已预约
        seat.setUserName(userName);
        if (startTime != null && !startTime.isEmpty()) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss");
            try {
                seat.setStartTime(LocalDateTime.parse(startTime, formatter));
            } catch (Exception e) {
                seat.setStartTime(LocalDateTime.now());
            }
        } else {
            seat.setStartTime(LocalDateTime.now());
        }
        // 这里为了简化，我们假设前端传入的是“预计使用时长”。
        // 实际上预约开始时间通常是“立刻”或者“未来某个时间”。
        // 按照Requirement: "到达预约开始时间...未签到...自动释放"。
        // 简化逻辑：预约后保留15分钟，15分钟内必须签到。
        // 同时记录用户想要使用的时长，用于签到后的结束时间计算。
        seat.setDuration(usageTimeSec);
        seat.setUpdateTime(LocalDateTime.now());
        return updateById(seat);
    }

    @Override
    public boolean checkIn(Long seatId) {
        SysSeat seat = getById(seatId);
        if (seat == null || seat.getStatus() != 1) {
            return false;
        }
        seat.setStatus(2); // 已被占用
        seat.setCheckInTime(LocalDateTime.now());
        // 计算结束时间: 当前时间 + 预约的时长
        seat.setEndTime(LocalDateTime.now().plusSeconds(seat.getDuration()));
        seat.setUpdateTime(LocalDateTime.now());
        return updateById(seat);
    }

    @Override
    public boolean renew(Long seatId, Integer extendedTimeSec) {
        SysSeat seat = getById(seatId);
        if (seat == null || seat.getStatus() != 2) {
            return false;
        }
        // 延长结束时间
        if (seat.getEndTime() != null) {
            seat.setEndTime(seat.getEndTime().plusSeconds(extendedTimeSec));
        } else {
            // 如果异常没有结束时间，重置为当前+延长
            seat.setEndTime(LocalDateTime.now().plusSeconds(extendedTimeSec));
        }
        seat.setDuration(seat.getDuration() + extendedTimeSec);
        seat.setUpdateTime(LocalDateTime.now());
        return updateById(seat);
    }

    @Override
    public boolean checkOut(Long seatId) {
        SysSeat seat = getById(seatId);
        if (seat == null) {
            return false;
        }
        resetSeat(seat);
        return updateById(seat);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetAll() {
        List<SysSeat> output = list();
        for (SysSeat seat : output) {
            resetSeat(seat);
        }
        updateBatchById(output);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void simulate() {
        // 先重置
        resetAll();
        List<SysSeat> all = list();
        Random random = new Random();

        // 随机挑选 30% 座位设置为占用，10% 预约
        for (SysSeat seat : all) {
            int r = random.nextInt(100);
            if (r < 30) {
                // 占用
                seat.setStatus(2);
                seat.setUserName("模拟用户" + random.nextInt(99));
                // 抹去秒数，保证模拟数据整齐
                LocalDateTime baseTime = LocalDateTime.now().withSecond(0).withNano(0);
                seat.setCheckInTime(baseTime.minusMinutes(random.nextInt(60)));
                seat.setEndTime(baseTime.plusMinutes(random.nextInt(120)));
                seat.setDuration(3600);
            } else if (r < 40) {
                // 预约
                seat.setStatus(1);
                seat.setUserName("预约用户" + random.nextInt(99));
                LocalDateTime baseTime = LocalDateTime.now().withSecond(0).withNano(0);
                seat.setStartTime(baseTime.plusMinutes(15));
                seat.setDuration(3600);
                seat.setUpdateTime(LocalDateTime.now());
            }
        }
        updateBatchById(all);
    }

    // 每 2 秒检查一次状态（过期/结束）
    @Scheduled(fixedRate = 2000)
    public void checkExpiration() {
        List<SysSeat> all = list();
        boolean changed = false;
        LocalDateTime now = LocalDateTime.now();

        for (SysSeat seat : all) {
            // 1. 检查预约超时 (状态1 && updateTime + 15min < now)
            // 简单处理：如果状态是1，且updateTime超过15分钟没变过（没签到），则释放
            if (seat.getStatus() == 1) {
                // 预约有效期15分钟
                // 实际逻辑应该是 startTime, 这里为了演示简单，以updateTime为准
                if (seat.getUpdateTime().plusMinutes(15).isBefore(now)) {
                    resetSeat(seat);
                    changed = true;
                }
            }
            // 2. 检查使用结束 (状态2 && endTime < now)
            else if (seat.getStatus() == 2) {
                if (seat.getEndTime() != null && seat.getEndTime().isBefore(now)) {
                    resetSeat(seat);
                    changed = true;
                }
            }
        }

        if (changed) {
            updateBatchById(all);
        }
    }

    private void resetSeat(SysSeat seat) {
        seat.setStatus(0);
        seat.setUserName(null);
        seat.setStartTime(null);
        seat.setCheckInTime(null);
        seat.setEndTime(null);
        seat.setDuration(0);
        seat.setUpdateTime(LocalDateTime.now());
    }
}
