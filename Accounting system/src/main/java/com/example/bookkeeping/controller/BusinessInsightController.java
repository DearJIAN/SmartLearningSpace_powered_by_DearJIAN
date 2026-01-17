package com.example.bookkeeping.controller;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.bookkeeping.service.BillService;
import com.example.bookkeeping.entity.Bill;
import com.example.bookkeeping.entity.Category;
import com.example.bookkeeping.entity.FinancialGoal;
import com.example.bookkeeping.mapper.FinancialGoalMapper;

@RestController
@RequestMapping("/api/insight")
public class BusinessInsightController {

    @Resource
    private BillService billService;
    
    @Resource
    private FinancialGoalMapper goalMapper;

    // 1. 消费洞察中心
    @GetMapping("/dashboard")
    public Map<String, Object> getInsightDashboard() {
        List<Bill> bills = billService.list(); 
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有分类映射
        Map<Long, String> categoryMap = billService.getAllCategories().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        // 计算每日支出
        Map<String, Double> dailyExpense = new HashMap<>();
        Map<String, Double> categoryExpense = new HashMap<>();
        double totalExpense = 0;

        for (Bill b : bills) {
            // 类型判断：Type 1: 收入, 2: 支出
            if (b.getType() != null && b.getType() == 2) {
                double amount = b.getAmount() != null ? b.getAmount().doubleValue() : 0.0;
                String day = b.getBillDate() != null ? b.getBillDate().toString() : "Unknown";

                dailyExpense.merge(day, amount, (oldVal, newVal) -> oldVal + newVal);
                
                // 使用分类名称
                String catName = categoryMap.getOrDefault(b.getCategoryId(), "未分类");
                categoryExpense.merge(catName, amount, (oldVal, newVal) -> oldVal + newVal);
                
                totalExpense += amount;
            }
        }

        double avgDaily = dailyExpense.isEmpty() ? 0 : totalExpense / dailyExpense.size();
        
        // 识别高消费日 (日均 1.5 倍)
        List<String> highCostDays = dailyExpense.entrySet().stream()
                .filter(e -> e.getValue() > avgDaily * 1.5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 识别高风险类别 (占比 > 40%)
        double finalTotalExpense = totalExpense;
        List<String> riskyCategories = categoryExpense.entrySet().stream()
                .filter(e -> finalTotalExpense > 0 && (e.getValue() / finalTotalExpense) > 0.4) 
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        result.put("highCostDays", highCostDays);
        result.put("riskyCategories", riskyCategories);
        result.put("summaryText", "您的消费主要集中在 " + (riskyCategories.isEmpty() ? "分散类别" : riskyCategories.get(0)) + "，建议关注高消费日期的具体支出。");
        return result;
    }

    // 2. 个人财务画像
    @GetMapping("/profile")
    public Map<String, Object> getFinancialProfile() {
        List<Bill> bills = billService.list();
        
        double income = bills.stream()
            .filter(b -> b.getType() != null && b.getType() == 1)
            .mapToDouble(b -> b.getAmount() == null ? 0 : b.getAmount().doubleValue())
            .sum();
            
        double expense = bills.stream()
            .filter(b -> b.getType() != null && b.getType() == 2)
            .mapToDouble(b -> b.getAmount() == null ? 0 : b.getAmount().doubleValue())
            .sum();

        double savingsRate = income == 0 ? 0 : (income - expense) / income;
        
        Map<String, Object> profile = new HashMap<>();
        if (savingsRate > 0.4) profile.put("type", "稳健储蓄型 🛡️");
        else if (savingsRate > 0.1) profile.put("type", "平衡生活型 ⚖️");
        else profile.put("type", "及时行乐型 🎸");

        profile.put("savingsRate", String.format("%.1f%%", savingsRate * 100));
        profile.put("score", savingsRate > 0.3 ? "A" : (savingsRate > 0 ? "B" : "C"));
        return profile;
    }

    // 3. 财务时间轴
    @GetMapping("/timeline")
    public List<Map<String, Object>> getTimeline() {
        List<Bill> bills = billService.list();
        // 获取所有分类映射
        Map<Long, String> categoryMap = billService.getAllCategories().stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        
        return bills.stream()
                .filter(b -> b.getBillDate() != null) 
                .sorted(Comparator.comparing(Bill::getBillDate).reversed())
                .map(b -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", b.getBillDate());
                    item.put("amount", b.getAmount());
                    // 修复：使用名称
                    item.put("category", categoryMap.getOrDefault(b.getCategoryId(), "其他"));
                    item.put("isKeyInfo", b.getAmount() != null && b.getAmount().doubleValue() > 500); 
                    return item;
                }).collect(Collectors.toList());
    }

    // 4. 消费异常预警
    @GetMapping("/risk")
    public List<Map<String, String>> getRiskAlerts() {
        List<Map<String, String>> alerts = new ArrayList<>();
        List<Bill> bills = billService.list();
        
        long largeBills = bills.stream()
            .filter(b -> b.getType() != null && b.getType() == 2 && b.getAmount() != null && b.getAmount().doubleValue() > 1000)
            .count();
            
        if (largeBills > 0) {
            Map<String, String> alert = new HashMap<>();
            alert.put("level", "warning"); 
            alert.put("title", "大额支出预警");
            alert.put("desc", "检测到 " + largeBills + " 笔超过 1000 元的支出，请确认是否为本人操作。");
            alerts.add(alert);
        }
        
        return alerts;
    }

    // 5. 财务目标管理 - 获取
    @GetMapping("/goal")
    public Map<String, Object> getGoalTracking() {
        Map<String, Object> res = new HashMap<>();
        
        // 从数据库查询最新目标 (假设单用户 ID=0 或取最新一条)
        // 实际项目应获取 Session 中的 userId，这里简化为默认
        FinancialGoal goal = goalMapper.selectOne(new QueryWrapper<FinancialGoal>().orderByDesc("id").last("LIMIT 1"));
        
        double target = (goal != null && goal.getTargetAmount() != null) ? goal.getTargetAmount().doubleValue() : 50000.0;
        String deadline = (goal != null && goal.getTargetDate() != null) ? goal.getTargetDate().toString() : LocalDate.now().plusYears(1).toString();
        
        List<Bill> bills = billService.list();
        double income = bills.stream().filter(b -> b.getType() == 1).mapToDouble(b -> b.getAmount().doubleValue()).sum();
        double expense = bills.stream().filter(b -> b.getType() == 2).mapToDouble(b -> b.getAmount().doubleValue()).sum();
        
        double currentSaved = Math.max(0, income - expense);
        
        res.put("targetAmount", target);
        res.put("currentSaved", currentSaved);
        res.put("progress", target == 0 ? 0 : Math.min(100, (currentSaved / target) * 100));
        res.put("estimateDate", deadline);
        return res;
    }

    // 5.1 设置/更新财务目标
    @PostMapping("/goal")
    public Map<String, Object> updateGoal(@RequestBody Map<String, String> params) {
        String amountStr = params.get("amount");
        String dateStr = params.get("date");
        
        FinancialGoal goal = new FinancialGoal();
        goal.setUserId(0L); // 默认用户
        goal.setTargetAmount(new BigDecimal(amountStr));
        goal.setTargetDate(LocalDate.parse(dateStr));
        goal.setCreatedAt(java.time.LocalDateTime.now());
        
        goalMapper.insert(goal); // 简单处理：每次都是插入新记录作为最新目标
        
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        return res;
    }
}
