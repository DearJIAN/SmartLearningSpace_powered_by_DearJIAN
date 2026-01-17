package com.example.bookkeeping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookkeeping.entity.Budget;
import java.math.BigDecimal;
import java.util.Map;

public interface BudgetService extends IService<Budget> {
    // 获取指定月份的预算情况（包括总额和已用）
    Map<String, Object> getBudgetStatus(Long userId, String month);
    // 保存预算
    void saveBudget(Long userId, String month, BigDecimal amount);
}
