package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.service.AccBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 业务洞察控制器
 */
@RestController
@RequestMapping("/api/accounting/insight")
public class AccInsightController {

    @Autowired
    private AccBillService billService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getInsightDashboard(HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        return Result.success(billService.getInsightDashboard(user.getUserId()));
    }

    @GetMapping("/profile")
    public Result<Map<String, Object>> getFinancialProfile(HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        return Result.success(billService.getFinancialProfile(user.getUserId()));
    }

    @GetMapping("/timeline")
    public Result<List<Map<String, Object>>> getFinancialTimeline(HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        return Result.success(billService.getFinancialTimeline(user.getUserId()));
    }

    @GetMapping("/risk")
    public Result<List<Map<String, String>>> getRiskAlerts(HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        return Result.success(billService.getRiskAlerts(user.getUserId()));
    }

    @GetMapping("/goal")
    public Result<Map<String, Object>> getGoalTracking(HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        return Result.success(billService.getGoalTracking(user.getUserId()));
    }

    @PostMapping("/goal")
    public Result<?> updateGoal(@RequestBody Map<String, Object> body, HttpSession session) {
        com.smartcampus.entity.SysUser user = (com.smartcampus.entity.SysUser) session.getAttribute("accountingUser");
        if (user == null)
            return Result.error("未登录");
        Double targetAmount = Double.valueOf(body.get("targetAmount").toString());
        String deadline = (String) body.get("deadline");
        boolean success = billService.updateGoal(user.getUserId(), targetAmount, deadline);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
}
