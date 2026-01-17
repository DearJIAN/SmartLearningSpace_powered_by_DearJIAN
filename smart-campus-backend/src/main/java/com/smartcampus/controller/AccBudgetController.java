package com.smartcampus.controller;

import com.smartcampus.common.Result;
import com.smartcampus.entity.AccBudget;
import com.smartcampus.entity.SysUser;
import com.smartcampus.service.AccBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 预算管理接口
 */
@RestController
@RequestMapping("/api/accounting/budget")
@CrossOrigin
public class AccBudgetController {

    @Autowired
    private AccBudgetService budgetService;

    /**
     * 获取预算状态
     */
    @GetMapping("/{month}")
    public Result<Map<String, Object>> getBudgetStatus(
            @PathVariable String month,
            HttpSession session) {

        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        Map<String, Object> status = budgetService.getBudgetStatus(user.getUserId(), month);
        return Result.success(status);
    }

    /**
     * 设置预算
     */
    @PostMapping
    public Result<String> setBudget(@RequestBody Map<String, Object> params, HttpSession session) {
        SysUser user = getCurrentUser(session);
        if (user == null) {
            return Result.error("未登录");
        }

        String month = (String) params.get("month");
        BigDecimal totalBudget = new BigDecimal(params.get("totalBudget").toString());

        budgetService.setBudget(user.getUserId(), month, totalBudget);
        return Result.success("设置成功");
    }

    // 获取当前用户
    private SysUser getCurrentUser(HttpSession session) {
        return (SysUser) session.getAttribute("accountingUser");
    }
}
