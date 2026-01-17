package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.entity.SysUser;
import com.smartcampus.service.AccBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 记账分析控制器
 */
@RestController
@RequestMapping("/api/accounting/analysis")
public class AccAnalysisController {

    @Autowired
    private AccBillService billService;

    @GetMapping("/trend")
    public Result<Map<String, Object>> getAnalysisData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取分析数据成功", billService.getAnalysisData(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/treemap")
    public Result<Map<String, Object>> getTreeMapData(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取TreeMap数据成功", billService.getTreeMapData(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/top5")
    public Result<?> getTop5(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取Top5成功", billService.getExpenditureTop5(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/yoy")
    public Result<?> getYoY(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取同比环比成功", billService.getYoYAnalysis(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/health")
    public Result<?> getHealth(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取财务健康度成功", billService.getFinancialHealth(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/weekly")
    public Result<?> getWeekly(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取周分布成功", billService.getWeeklyConsumption(user.getUserId(), startDate, endDate));
    }

    @GetMapping("/budget-trend")
    public Result<?> getBudgetTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取预算趋势成功", billService.getBudgetBurnTrend(user.getUserId(), startDate, endDate));
    }
}
