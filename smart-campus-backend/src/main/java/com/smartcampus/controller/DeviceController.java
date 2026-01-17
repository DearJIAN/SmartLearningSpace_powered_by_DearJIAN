package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.entity.VisualStatsLog;
import com.smartcampus.service.VisualStatsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 设备数据接收Controller
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private VisualStatsLogService visualStatsLogService;

    /**
     * 接收视觉设备上报的数据
     * 
     * @param roomId      教室ID
     * @param personCount 人数
     * @param phoneCount  玩手机人数
     * @return 处理结果
     */
    @PostMapping("/receive")
    public Result<String> receiveData(@RequestParam("roomId") Long roomId,
            @RequestParam("personCount") Integer personCount,
            @RequestParam("phoneCount") Integer phoneCount) {
        // 计算专注度指数，处理分母为0的情况
        Double focusIndex = 0.0;
        if (personCount != null && personCount > 0) {
            focusIndex = (double) (personCount - phoneCount) / personCount;
        }

        // 构建日志对象并保存
        VisualStatsLog log = new VisualStatsLog();
        log.setRoomId(roomId);
        log.setPersonCount(personCount);
        log.setPhoneCount(phoneCount);
        log.setFocusIndex(focusIndex);
        log.setCreateTime(new Date());

        boolean saved = visualStatsLogService.save(log);
        if (saved) {
            System.out.println(">>> [Device] 接收数据成功: 教室=" + roomId + ", 人数=" + personCount + ", 专注度=" + focusIndex);
            return Result.success("数据接收成功");
        } else {
            System.err.println(">>> [Device] 数据保存失败: " + log);
            return Result.error("数据保存失败");
        }
    }
}
