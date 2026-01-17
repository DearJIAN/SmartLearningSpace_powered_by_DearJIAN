package com.example.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookkeeping.entity.Bill;
import com.example.bookkeeping.entity.Budget;
import com.example.bookkeeping.mapper.BillMapper;
import com.example.bookkeeping.mapper.BudgetMapper;
import com.example.bookkeeping.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    @Autowired
    private BillMapper billMapper;

    @Override
    public Map<String, Object> getBudgetStatus(Long userId, String month) {
        // 1. 查询设定预算
        Budget budget = this.getOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, month));

        BigDecimal totalBudget = (budget != null) ? budget.getTotalBudget() : BigDecimal.ZERO;

        // 2. 查询实际支出 (Type=2)
        // 假设 month 格式为 "YYYY-MM"
        QueryWrapper<Bill> query = new QueryWrapper<>();
        query.select("IFNULL(SUM(amount), 0) as total")
             .eq("user_id", userId)
             .eq("type", 2)
             .likeRight("bill_date", month); // 匹配 2023-01%
        
        Map<String, Object> result = billMapper.selectMaps(query).get(0);
        BigDecimal usedAmount = (BigDecimal) result.get("total");

        // 3. 计算进度
        BigDecimal progress = BigDecimal.ZERO;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            progress = usedAmount.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        }
        // 不能超过100%显示
        BigDecimal displayProgress = progress.compareTo(new BigDecimal(100)) > 0 ? new BigDecimal(100) : progress;

        Map<String, Object> map = new HashMap<>();
        map.put("month", month);
        map.put("total", totalBudget);
        map.put("used", usedAmount);
        map.put("remaining", totalBudget.subtract(usedAmount));
        map.put("progress", displayProgress);
        map.put("isOver", usedAmount.compareTo(totalBudget) > 0); // 是否超支

        return map;
    }

    @Override
    public void saveBudget(Long userId, String month, BigDecimal amount) {
        Budget budget = this.getOne(new LambdaQueryWrapper<Budget>()
                .eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, month));
        
        if (budget == null) {
            budget = new Budget();
            budget.setUserId(userId);
            budget.setMonth(month);
            budget.setUsedAmount(BigDecimal.ZERO); // 初始值
        }
        budget.setTotalBudget(amount);
        this.saveOrUpdate(budget);
    }
}
