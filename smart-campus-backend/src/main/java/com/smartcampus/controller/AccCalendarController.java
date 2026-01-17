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
import java.util.List;
import java.util.Map;

/**
 * 账单日历控制器
 */
@RestController
@RequestMapping("/api/accounting/calendar")
public class AccCalendarController {

    @Autowired
    private AccBillService billService;

    @GetMapping("/events")
    public Result<List<Map<String, Object>>> getCalendarEvents(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            HttpSession session) {
        SysUser user = (SysUser) session.getAttribute("accountingUser");
        if (user == null) {
            return Result.error("未登录");
        }
        return Result.success("获取日历事件成功", billService.getCalendarData(user.getUserId(), type, categoryId, start, end));
    }
}
