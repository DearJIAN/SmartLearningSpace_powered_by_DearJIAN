package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.mapper.SysClassroomMapper;
import com.smartcampus.mapper.VisualStatsLogMapper;
import com.smartcampus.entity.VisualStatsLog;
import com.smartcampus.service.VisualStatsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 统计分析Controller
 */
@RestController
@RequestMapping("/api/stats")
@CrossOrigin // 确保跨域请求正常
public class StatsController {

    @Autowired
    private VisualStatsLogMapper visualStatsLogMapper;

    @Autowired
    private SysClassroomMapper sysClassroomMapper;

    @Autowired
    private VisualStatsLogService visualStatsLogService;

    /**
     * 重置指定教室的数据并注入过去24小时的拟真趋势数据
     * 
     * @param roomId 教室ID
     * @return 操作结果
     */
    @PostMapping("/reset")
    public Result<String> resetStats(@RequestParam Long roomId) {
        // 1. 清空该教室的所有历史记录
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<VisualStatsLog> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        visualStatsLogService.remove(queryWrapper);

        // 2. 注入过去 24 小时拟真的趋势数据
        // 使用 roomId 作为固定种子，确保每次重置后数据是固定不变的默认值
        Random random = new Random(roomId);
        List<VisualStatsLog> logs = new ArrayList<>();

        // 生成过去24小时数据，每30分钟一条
        for (int i = 47; i >= 0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -i * 30);

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            boolean isClassTime = isInClassTime(hour, minute);

            VisualStatsLog log = new VisualStatsLog();
            log.setRoomId(roomId);

            int personCount;
            int phoneCount;

            if (isClassTime) {
                // 上课时间：80-160人
                personCount = 80 + random.nextInt(81);
                phoneCount = 2 + random.nextInt(10);
            } else {
                // 休息时间：0-15人
                personCount = random.nextInt(16);
                phoneCount = personCount > 0 ? random.nextInt(Math.min(5, personCount + 1)) : 0;
            }

            log.setPersonCount(personCount);
            log.setPhoneCount(phoneCount);

            double focusIndex = 0.0;
            if (personCount > 0) {
                focusIndex = (double) (personCount - phoneCount) / personCount;
            }
            log.setFocusIndex(Math.round(focusIndex * 100.0) / 100.0);
            log.setCreateTime(calendar.getTime());

            logs.add(log);
        }

        visualStatsLogService.saveBatch(logs);

        System.out.println(">>> [Stats] 教室 " + roomId + " 数据已重置，已生成 24 小时拟真趋势数据。");
        return Result.success("该教室数据已重置，包含 24 小时拟真趋势数据");
    }

    /**
     * 判断时间是否在上课时段
     */
    private boolean isInClassTime(int hour, int minute) {
        int timeInMinutes = hour * 60 + minute;
        // 上午 08:00 - 11:50
        if (timeInMinutes >= 8 * 60 && timeInMinutes < 11 * 60 + 50)
            return true;
        // 下午 14:30 - 18:00
        if (timeInMinutes >= 14 * 60 + 30 && timeInMinutes < 18 * 60)
            return true;
        // 晚自习 18:40 - 22:00
        if (timeInMinutes >= 18 * 60 + 40 && timeInMinutes < 22 * 60)
            return true;
        return false;
    }

    /**
     * 获取最近24小时专注度趋势数据
     * 
     * @return 按小时分组的平均专注度
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(@RequestParam Long roomId) {
        List<Map<String, Object>> trendData = visualStatsLogMapper.selectTrendLast24Hours(roomId);
        return Result.success(trendData);
    }

    /**
     * 获取首页摘要数据
     * 
     * @return 包含当前总人数、今日平均专注度、开放教室数量、违规占座预警
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary(@RequestParam Long roomId) {
        Map<String, Object> summary = new java.util.HashMap<>();

        // 1. 当前教室总人数 (totalPeople)：查询指定 roomId 的最新一条记录
        Integer totalPeople = visualStatsLogMapper.selectLatestPersonCount(roomId);
        summary.put("totalPeople", totalPeople != null ? totalPeople : 0);

        // 2. 今日平均专注度 (avgFocus)：计算指定 roomId 今日所有记录的平均值
        Double avgFocus = visualStatsLogMapper.selectTodayAvgFocus(roomId);
        summary.put("avgFocus", avgFocus != null ? Math.round(avgFocus * 100.0) / 100.0 : 0.0);

        // 3. 开放教室数量 (activeClassrooms)：统计总行数
        Long activeClassrooms = sysClassroomMapper.selectCount(null);
        summary.put("activeClassrooms", activeClassrooms);

        // 4. 违规占座预警 (warnings)：暂时返回固定值 0
        summary.put("warnings", 0);

        return Result.success(summary);
    }
}
