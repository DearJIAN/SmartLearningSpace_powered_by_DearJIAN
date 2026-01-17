package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.AccBill;
import com.smartcampus.entity.AccBudget;
import com.smartcampus.mapper.AccBillMapper;
import com.smartcampus.mapper.AccBudgetMapper;
import com.smartcampus.service.AccBudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 月度预算服务实现
 */
@Service
public class AccBudgetServiceImpl extends ServiceImpl<AccBudgetMapper, AccBudget> implements AccBudgetService {

    @Autowired
    private AccBillMapper billMapper;

    @Override
    public Map<String, Object> getBudgetStatus(Long userId, String month) {
        AccBudget budget = this.getOne(new LambdaQueryWrapper<AccBudget>()
                .eq(AccBudget::getUserId, userId)
                .eq(AccBudget::getMonth, month));

        BigDecimal total = BigDecimal.ZERO;
        if (budget != null) {
            total = budget.getTotalBudget();
        }

        // 计算当月实际支出（始终计算，无论预算记录是否存在）
        BigDecimal used = calculateMonthExpense(userId, month);

        BigDecimal remaining = total.subtract(used);
        BigDecimal progress = BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            progress = used.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            if (progress.compareTo(BigDecimal.valueOf(100)) > 0) {
                progress = BigDecimal.valueOf(100);
            }
        }

        boolean isOver = remaining.compareTo(BigDecimal.ZERO) < 0;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("used", used);
        result.put("remaining", remaining);
        result.put("progress", progress.intValue());
        result.put("isOver", isOver);

        return result;
    }

    @Override
    public void setBudget(Long userId, String month, BigDecimal totalBudget) {
        AccBudget budget = this.getOne(new LambdaQueryWrapper<AccBudget>()
                .eq(AccBudget::getUserId, userId)
                .eq(AccBudget::getMonth, month));

        if (budget == null) {
            budget = new AccBudget();
            budget.setUserId(userId);
            budget.setMonth(month);
            budget.setTotalBudget(totalBudget);
            budget.setUsedAmount(BigDecimal.ZERO);
            this.save(budget);
        } else {
            budget.setTotalBudget(totalBudget);
            this.updateById(budget);
        }
    }

    /**
     * 计算指定月份的支出总额
     */
    private BigDecimal calculateMonthExpense(Long userId, String month) {
        // 解析月份，例如 "2024-01"
        LocalDate startOfMonth = LocalDate.parse(month + "-01");
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId)
                .eq(AccBill::getType, 2) // 仅统计支出
                .ge(AccBill::getBillDate, startOfMonth)
                .le(AccBill::getBillDate, endOfMonth);

        List<AccBill> bills = billMapper.selectList(wrapper);

        BigDecimal total = BigDecimal.ZERO;
        for (AccBill bill : bills) {
            total = total.add(bill.getAmount());
        }

        return total;
    }
}
