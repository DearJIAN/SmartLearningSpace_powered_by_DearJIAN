package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.entity.SysSeat;
import com.smartcampus.service.SysSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seat")
@CrossOrigin
public class SeatController {

    @Autowired
    private SysSeatService seatService;

    @GetMapping("/list")
    public Result<List<SysSeat>> list() {
        return Result.success(seatService.list());
    }

    @PostMapping("/reserve")
    public Result<Object> reserve(@RequestBody Map<String, Object> params) {
        // params: seatId, userName, usageTimeSec
        Long seatId = Long.valueOf(params.get("seatId").toString());
        String userName = (String) params.get("userName");
        Integer usageTimeSec = Integer.valueOf(params.get("usageTimeSec").toString());
        String startTime = params.containsKey("startTime") ? (String) params.get("startTime") : null;

        boolean success = seatService.reserve(seatId, userName, usageTimeSec, startTime);
        return success ? Result.success() : Result.error("预约失败，座位已被占用或参数错误");
    }

    @PostMapping("/check-in")
    public Result<Object> checkIn(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        boolean success = seatService.checkIn(seatId);
        return success ? Result.success() : Result.error("签到失败");
    }

    @PostMapping("/renew")
    public Result<Object> renew(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        Integer extendedTimeSec = Integer.valueOf(params.get("extendedTimeSec").toString());
        boolean success = seatService.renew(seatId, extendedTimeSec);
        return success ? Result.success() : Result.error("续约失败");
    }

    @PostMapping("/check-out")
    public Result<Object> checkOut(@RequestBody Map<String, Object> params) {
        Long seatId = Long.valueOf(params.get("seatId").toString());
        boolean success = seatService.checkOut(seatId);
        return success ? Result.success() : Result.error("签退失败");
    }

    @PostMapping("/reset")
    public Result<Object> reset() {
        seatService.resetAll();
        return Result.success();
    }

    @PostMapping("/simulate")
    public Result<Object> simulate() {
        seatService.simulate();
        return Result.success();
    }
}
