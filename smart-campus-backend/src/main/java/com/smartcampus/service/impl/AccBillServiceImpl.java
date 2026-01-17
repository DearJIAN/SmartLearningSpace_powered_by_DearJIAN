package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.AccBill;
import com.smartcampus.entity.AccCategory;
import com.smartcampus.entity.AccFinancialGoal;
import com.smartcampus.mapper.AccBillMapper;
import com.smartcampus.mapper.AccBudgetMapper;
import com.smartcampus.mapper.AccCategoryMapper;
import com.smartcampus.mapper.AccFinancialGoalMapper;
import com.smartcampus.service.AccBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 账单记录服务实现
 */
@Service
public class AccBillServiceImpl extends ServiceImpl<AccBillMapper, AccBill> implements AccBillService {

    @Autowired
    private AccCategoryMapper categoryMapper;

    @Autowired
    private com.smartcampus.mapper.AccBudgetMapper budgetMapper;

    @Autowired
    private AccFinancialGoalMapper goalMapper;

    @Override
    public List<AccBill> getBillList(Long userId, Long categoryId, String startDate, String endDate, Integer type) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        if (categoryId != null)
            wrapper.eq(AccBill::getCategoryId, categoryId);
        if (type != null)
            wrapper.eq(AccBill::getType, type);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));
        wrapper.orderByDesc(AccBill::getBillDate, AccBill::getId);

        List<AccBill> list = this.list(wrapper);
        for (AccBill bill : list) {
            if (bill.getCategoryId() != null) {
                AccCategory category = categoryMapper.selectById(bill.getCategoryId());
                if (category != null)
                    bill.setCategoryName(category.getName());
            }
        }
        return list;
    }

    @Override
    public Page<AccBill> getBillPage(Page<AccBill> page, Long userId, Long categoryId, String startDate, String endDate,
            Integer type) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(AccBill::getCategoryId, categoryId);
        }

        // 类型筛选
        if (type != null) {
            wrapper.eq(AccBill::getType, type);
        }

        // 日期范围筛选
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));
        }

        wrapper.orderByDesc(AccBill::getBillDate, AccBill::getId);

        Page<AccBill> result = this.page(page, wrapper);

        // 填充分类名称
        for (AccBill bill : result.getRecords()) {
            if (bill.getCategoryId() != null) {
                AccCategory category = categoryMapper.selectById(bill.getCategoryId());
                if (category != null) {
                    bill.setCategoryName(category.getName());
                }
            }
        }

        return result;
    }

    @Override
    public Map<String, Object> getBillStats(Long userId, Long categoryId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);

        if (categoryId != null) {
            wrapper.eq(AccBill::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));
        }

        List<AccBill> bills = this.list(wrapper);

        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (AccBill bill : bills) {
            BigDecimal amount = bill.getAmount() != null ? bill.getAmount() : BigDecimal.ZERO;
            if (bill.getType() != null && bill.getType() == 1) {
                income = income.add(amount);
            } else {
                expense = expense.add(amount);
            }
        }

        BigDecimal balance = income.subtract(expense);

        Map<String, Object> result = new HashMap<>();
        result.put("income", income);
        result.put("expense", expense);
        result.put("balance", balance);

        return result;
    }

    @Override
    public void generateRandomBills(Long userId, Integer count) {
        Random random = new Random();
        LocalDate today = LocalDate.now();

        // 获取该用户真实的分类
        LambdaQueryWrapper<AccCategory> catWrapper = new LambdaQueryWrapper<>();
        catWrapper.eq(AccCategory::getUserId, userId).or().eq(AccCategory::getUserId, 0L);
        List<AccCategory> categories = categoryMapper.selectList(catWrapper);

        if (categories.isEmpty()) {
            return; // 理论上不会发生，因为会自动初始化
        }

        List<Long> incomeIds = categories.stream().filter(c -> c.getType() == 1).map(AccCategory::getId)
                .collect(Collectors.toList());
        List<Long> expenseIds = categories.stream().filter(c -> c.getType() == 2).map(AccCategory::getId)
                .collect(Collectors.toList());

        for (int i = 0; i < count; i++) {
            AccBill bill = new AccBill();
            bill.setUserId(userId);

            // 80% 支出，20% 收入
            boolean isExpense = random.nextDouble() < 0.8 && !expenseIds.isEmpty();
            if (incomeIds.isEmpty())
                isExpense = true;
            if (expenseIds.isEmpty())
                isExpense = false;

            bill.setType(isExpense ? 2 : 1);

            // 随机分类
            if (isExpense) {
                bill.setCategoryId(expenseIds.get(random.nextInt(expenseIds.size())));
                // 支出金额：10-500
                bill.setAmount(BigDecimal.valueOf(10 + random.nextInt(490)));
            } else if (!incomeIds.isEmpty()) {
                bill.setCategoryId(incomeIds.get(random.nextInt(incomeIds.size())));
                // 收入金额：500-5000
                bill.setAmount(BigDecimal.valueOf(500 + random.nextInt(4500)));
            }

            // 随机日期：最近30天
            bill.setBillDate(today.minusDays(random.nextInt(30)));

            // 随机备注
            String[] remarks = { "", "", "", "日常消费", "必要支出", "临时花销", "计划内", "额外收入" };
            bill.setRemark(remarks[random.nextInt(remarks.length)]);

            this.save(bill);
        }
    }

    @Override
    public void clearBills(Long userId) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        this.remove(wrapper);
    }

    @Override
    public Map<String, Object> getAnalysisData(Long userId, String startDate, String endDate) {
        LocalDate start, end;
        try {
            start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId)
                .ge(AccBill::getBillDate, start)
                .le(AccBill::getBillDate, end)
                .orderByAsc(AccBill::getBillDate);
        List<AccBill> bills = this.list(wrapper);

        // 1. Trend Data (daily)
        Map<String, BigDecimal[]> dailyMap = new TreeMap<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dailyMap.put(current.toString(), new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
            current = current.plusDays(1);
        }

        for (AccBill b : bills) {
            String day = b.getBillDate().toString();
            BigDecimal[] val = dailyMap.get(day);
            if (val != null) {
                if (b.getType() == 1)
                    val[0] = val[0].add(b.getAmount());
                else
                    val[1] = val[1].add(b.getAmount());
            }
        }

        List<String> dates = new ArrayList<>(dailyMap.keySet());
        List<BigDecimal> incomes = dailyMap.values().stream().map(v -> v[0]).collect(Collectors.toList());
        List<BigDecimal> expenses = dailyMap.values().stream().map(v -> v[1]).collect(Collectors.toList());

        // 2. Category Pie
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));
        Map<String, BigDecimal> catSum = bills.stream()
                .filter(b -> b.getType() == 2)
                .collect(Collectors.groupingBy(b -> catMap.getOrDefault(b.getCategoryId(), "其他"),
                        Collectors.reducing(BigDecimal.ZERO, AccBill::getAmount, BigDecimal::add)));

        List<Map<String, Object>> categories = catSum.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).collect(Collectors.toList());

        // 3. Weekly Data
        Map<String, BigDecimal> weeklyMap = getWeeklyConsumption(userId, startDate, endDate);
        List<BigDecimal> weeklyList = new ArrayList<>(weeklyMap.values());

        // 4. Health & Top5
        Map<String, Object> health = getFinancialHealth(userId, startDate, endDate);
        List<Map<String, Object>> top5Raw = getExpenditureTop5(userId, startDate, endDate);
        BigDecimal totalExp = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Map<String, Object>> top5 = top5Raw.stream().map(t -> {
            BigDecimal amt = (BigDecimal) t.get("amount");
            int percent = totalExp.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : amt.multiply(new BigDecimal(100)).divide(totalExp, 0, RoundingMode.HALF_UP).intValue();
            t.put("percent", percent);
            return t;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("incomes", incomes);
        result.put("expenses", expenses);
        result.put("categories", categories);
        result.put("weekly", weeklyList);
        result.put("healthScore", health.get("score"));
        result.put("top5", top5);
        return result;
    }

    @Override
    public Map<String, Object> getTreeMapData(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));
        wrapper.orderByDesc(AccBill::getBillDate);
        List<AccBill> bills = this.list(wrapper);

        List<AccCategory> categories = categoryMapper.selectList(null);
        Map<Long, String> catNameMap = categories.stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));

        Map<String, Object> root = new HashMap<>();
        root.put("name", "账本资金流向");
        List<Map<String, Object>> rootChildren = new ArrayList<>();

        Map<String, Object> incomeNode = new HashMap<>();
        incomeNode.put("name", "收入流入");
        List<Map<String, Object>> incomeChildren = new ArrayList<>();

        Map<String, Object> expenseNode = new HashMap<>();
        expenseNode.put("name", "支出流向");
        List<Map<String, Object>> expenseChildren = new ArrayList<>();

        Map<String, List<Map<String, Object>>> incomeCatGroups = new HashMap<>();
        Map<String, List<Map<String, Object>>> expenseCatGroups = new HashMap<>();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd");

        for (AccBill bill : bills) {
            if (bill.getBillDate() == null || bill.getAmount() == null)
                continue;
            String catName = catNameMap.getOrDefault(bill.getCategoryId(), "其他");
            int type = bill.getType();

            Map<String, Object> leafNode = new HashMap<>();
            leafNode.put("name",
                    "[" + bill.getBillDate().format(df) + "] " + bill.getRemark() + " : " + bill.getAmount());
            leafNode.put("value", bill.getAmount());

            if (type == 1) {
                incomeCatGroups.computeIfAbsent(catName, k -> new ArrayList<>()).add(leafNode);
            } else {
                expenseCatGroups.computeIfAbsent(catName, k -> new ArrayList<>()).add(leafNode);
            }
        }

        incomeCatGroups.forEach((catName, children) -> {
            Map<String, Object> catNode = new HashMap<>();
            catNode.put("name", catName);
            catNode.put("children", children);
            incomeChildren.add(catNode);
        });

        expenseCatGroups.forEach((catName, children) -> {
            Map<String, Object> catNode = new HashMap<>();
            catNode.put("name", catName);
            catNode.put("children", children);
            expenseChildren.add(catNode);
        });

        incomeNode.put("children", incomeChildren);
        expenseNode.put("children", expenseChildren);
        rootChildren.add(expenseNode);
        rootChildren.add(incomeNode);
        root.put("children", rootChildren);

        return root;
    }

    @Override
    public List<Map<String, Object>> getCalendarData(Long userId, Integer type, Long categoryId, String startStr,
            String endStr) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        if (StringUtils.hasText(startStr))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startStr));
        if (StringUtils.hasText(endStr))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endStr));
        if (type != null)
            wrapper.eq(AccBill::getType, type);
        if (categoryId != null)
            wrapper.eq(AccBill::getCategoryId, categoryId);

        List<AccBill> bills = this.list(wrapper);
        List<AccCategory> categories = categoryMapper.selectList(null);
        Map<Long, String> catNameMap = categories.stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));

        List<Map<String, Object>> events = new ArrayList<>();
        for (AccBill bill : bills) {
            Map<String, Object> event = new HashMap<>();
            String catName = catNameMap.getOrDefault(bill.getCategoryId(), "其他");
            String symbol = bill.getType() == 1 ? "+" : "-";
            event.put("title", "[" + catName + "] " + symbol + bill.getAmount());
            event.put("start", bill.getBillDate().toString());
            event.put("color", bill.getType() == 1 ? "#67C23A" : "#F56C6C");

            Map<String, Object> extendedProps = new HashMap<>();
            extendedProps.put("amount", bill.getAmount());
            extendedProps.put("remark", bill.getRemark());
            extendedProps.put("category", catName);
            event.put("extendedProps", extendedProps);
            events.add(event);
        }
        return events;
    }

    @Override
    public List<Map<String, Object>> getExpenditureTop5(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.eq(AccBill::getType, 2);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));

        List<AccBill> bills = this.list(wrapper);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));

        Map<Long, BigDecimal> grouped = bills.stream()
                .filter(b -> b.getCategoryId() != null && b.getAmount() != null)
                .collect(Collectors.groupingBy(
                        AccBill::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, AccBill::getAmount, BigDecimal::add)));

        return grouped.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", catMap.getOrDefault(e.getKey(), "其他"));
                    map.put("amount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getYoYAnalysis(Long userId, String startDate, String endDate) {
        LocalDate start, end;
        try {
            start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        long days = end.toEpochDay() - start.toEpochDay();
        LocalDate prevStart = start.minusDays(days + 1);
        LocalDate prevEnd = end.minusDays(days + 1);

        Map<String, BigDecimal> current = getSumByTypeInternal(userId, start, end);
        Map<String, BigDecimal> previous = getSumByTypeInternal(userId, prevStart, prevEnd);

        Map<String, Object> result = new HashMap<>();
        result.put("current", current);
        result.put("previous", previous);
        result.put("incomeRate", calculateRate(current.get("income"), previous.get("income")));
        result.put("expenseRate", calculateRate(current.get("expense"), previous.get("expense")));

        return result;
    }

    private Map<String, BigDecimal> getSumByTypeInternal(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.ge(AccBill::getBillDate, start);
        wrapper.le(AccBill::getBillDate, end);
        List<AccBill> bills = this.list(wrapper);

        BigDecimal income = bills.stream().filter(b -> b.getType() == 1 && b.getAmount() != null)
                .map(AccBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = bills.stream().filter(b -> b.getType() == 2 && b.getAmount() != null)
                .map(AccBill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> map = new HashMap<>();
        map.put("income", income);
        map.put("expense", expense);
        return map;
    }

    private Double calculateRate(BigDecimal curr, BigDecimal prev) {
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0)
            return (curr != null && curr.compareTo(BigDecimal.ZERO) > 0) ? 1.0 : 0.0;
        return curr.subtract(prev).divide(prev, 4, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public List<Map<String, Object>> getConsumptionFrequency(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.eq(AccBill::getType, 2);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));

        List<AccBill> bills = this.list(wrapper);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));

        Map<Long, Long> counts = bills.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(AccBill::getCategoryId, Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", catMap.getOrDefault(e.getKey(), "其他"));
                    map.put("count", e.getValue());
                    return map;
                })
                .sorted((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, BigDecimal> getWeeklyConsumption(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.eq(AccBill::getType, 2);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));

        List<AccBill> bills = this.list(wrapper);
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String[] weeks = { "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY" };
        for (String w : weeks)
            result.put(w, BigDecimal.ZERO);

        for (AccBill bill : bills) {
            if (bill.getBillDate() == null || bill.getAmount() == null)
                continue;
            result.merge(bill.getBillDate().getDayOfWeek().toString(), bill.getAmount(), BigDecimal::add);
        }
        return result;
    }

    @Override
    public Map<String, Object> getBudgetBurnTrend(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.eq(AccBill::getType, 2);
        if (StringUtils.hasText(startDate))
            wrapper.ge(AccBill::getBillDate, LocalDate.parse(startDate));
        if (StringUtils.hasText(endDate))
            wrapper.le(AccBill::getBillDate, LocalDate.parse(endDate));
        wrapper.orderByAsc(AccBill::getBillDate);
        List<AccBill> bills = this.list(wrapper);

        String month = StringUtils.hasText(startDate) ? startDate.substring(0, 7)
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        BigDecimal totalBudget = BigDecimal.ZERO;
        try {
            com.smartcampus.entity.AccBudget budgetObj = budgetMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.smartcampus.entity.AccBudget>()
                            .eq(com.smartcampus.entity.AccBudget::getUserId, userId)
                            .eq(com.smartcampus.entity.AccBudget::getMonth, month));
            if (budgetObj != null)
                totalBudget = budgetObj.getTotalBudget();
        } catch (Exception e) {
        }

        List<Map<String, Object>> dailyUsed = new ArrayList<>();
        BigDecimal accum = BigDecimal.ZERO;
        Map<String, BigDecimal> daySumMap = new TreeMap<>();
        for (AccBill b : bills) {
            if (b.getBillDate() == null || b.getAmount() == null)
                continue;
            daySumMap.merge(b.getBillDate().toString(), b.getAmount(), BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> entry : daySumMap.entrySet()) {
            accum = accum.add(entry.getValue());
            Map<String, Object> point = new HashMap<>();
            point.put("date", entry.getKey());
            point.put("used", accum);
            dailyUsed.add(point);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("budget", totalBudget);
        result.put("dailyUsed", dailyUsed);
        return result;
    }

    @Override
    public Map<String, Object> getBudgetRisk(Long userId, String startDate, String endDate) {
        Map<String, Object> trend = getBudgetBurnTrend(userId, startDate, endDate);
        BigDecimal budget = (BigDecimal) trend.get("budget");
        List<Map<String, Object>> daily = (List<Map<String, Object>>) trend.get("dailyUsed");
        BigDecimal used = daily.isEmpty() ? BigDecimal.ZERO : (BigDecimal) daily.get(daily.size() - 1).get("used");

        Map<String, Object> result = new HashMap<>();
        result.put("total", budget);
        result.put("used", used);
        if (budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
            result.put("rate", used.divide(budget, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
        } else {
            result.put("rate", BigDecimal.ZERO);
        }
        return result;
    }

    @Override
    public Map<String, Object> getFinancialHealth(Long userId, String startDate, String endDate) {
        LocalDate start, end;
        try {
            start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        Map<String, BigDecimal> sums = getSumByTypeInternal(userId, start, end);
        BigDecimal income = sums.get("income");
        BigDecimal expense = sums.get("expense");

        // 1. 储蓄率 (30%)
        double savingRate = 0;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingRate = income.subtract(expense).divide(income, 4, RoundingMode.HALF_UP).doubleValue();
        }

        // 2. 支出集中度 (20%) - Top1 占比越低越健康
        List<Map<String, Object>> top5 = getExpenditureTop5(userId, startDate, endDate);
        double expenseFocus = 0;
        if (!top5.isEmpty() && expense.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal top1 = (BigDecimal) top5.get(0).get("amount");
            expenseFocus = top1.divide(expense, 4, RoundingMode.HALF_UP).doubleValue();
        }
        double focusScore = Math.max(0, 1.0 - expenseFocus); // 反向指标

        // 3. 收支平衡度 (20%) - 支出占总流水的比例，越接近 0.5 (即支出=收入的一半) 越好，或者单纯看是否有结余
        double balanceScore = 0.5;
        if (income.add(expense).compareTo(BigDecimal.ZERO) > 0) {
            // 简单逻辑：只要有结余就是满分，否则按比例扣分
            balanceScore = income.compareTo(expense) >= 0 ? 1.0
                    : income.divide(expense, 4, RoundingMode.HALF_UP).doubleValue();
        }

        // 4. 稳定性 (30%) - 基于每日支出方差
        double stability = calculateStability(userId, start, end);

        // 综合评分计算
        // 权重：储蓄率 30%, 稳定性 30%, 集中度 20%, 平衡度 20%
        double totalScore = (Math.max(0, savingRate) * 30) +
                (stability * 30) +
                (focusScore * 20) +
                (balanceScore * 20);

        // 修正：储蓄率可能为负，导致扣分，但在 0-100 分制中最低为 0
        totalScore = Math.max(10, Math.min(100, totalScore)); // 兜底 10 分

        Map<String, Object> res = new HashMap<>();
        res.put("savingRate", Math.max(0, savingRate));
        res.put("expenseFocus", expenseFocus);
        res.put("balance", balanceScore); // 返回分数形式供参考
        res.put("stability", stability);
        res.put("score", (int) totalScore); // 最终整数分数
        return res;
    }

    /**
     * 计算消费稳定性 (0.0 - 1.0)
     * 基于变异系数 (Coefficient of Variation): 标准差 / 平均值
     * CV 越低越稳定。
     */
    private double calculateStability(Long userId, LocalDate start, LocalDate end) {
        Map<String, BigDecimal> dailyMap = getDailyExpenseMap(userId, start, end);
        if (dailyMap.isEmpty())
            return 1.0; // 无消费视为非常稳定

        List<Double> values = dailyMap.values().stream()
                .map(BigDecimal::doubleValue)
                .collect(Collectors.toList());

        // 补全没有消费的日期为 0 (这一步很重要，否则稳定性虚高)
        long days = end.toEpochDay() - start.toEpochDay() + 1;
        while (values.size() < days) {
            values.add(0.0);
        }

        double sum = values.stream().mapToDouble(Double::doubleValue).sum();
        double mean = sum / values.size();

        if (mean == 0)
            return 1.0; // 平均值为 0，完全稳定

        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .sum() / values.size();
        double stdDev = Math.sqrt(variance);

        double cv = stdDev / mean;

        // 映射 CV 到 0-1 分数。假设 CV > 2.0 为非常不稳定(0分)，CV=0 为满分。
        // 一般日常消费 CV 在 0.5 - 1.5 之间。
        double stability = 1.0 - (cv / 3.0);
        return Math.max(0.1, Math.min(1.0, stability));
    }

    private Map<String, BigDecimal> getDailyExpenseMap(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId)
                .eq(AccBill::getType, 2)
                .ge(AccBill::getBillDate, start)
                .le(AccBill::getBillDate, end);
        List<AccBill> bills = this.list(wrapper);

        Map<String, BigDecimal> map = new HashMap<>();
        for (AccBill bill : bills) {
            String date = bill.getBillDate().toString();
            map.merge(date, bill.getAmount(), BigDecimal::add);
        }
        return map;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getInsightDashboard(Long userId) {
        // 直接复用分析数据，确保一致性
        Map<String, Object> analysis = getAnalysisData(userId, null, null);

        List<BigDecimal> expenses = (List<BigDecimal>) analysis.get("expenses");
        BigDecimal totalExpense = expenses.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        List<BigDecimal> incomes = (List<BigDecimal>) analysis.get("incomes");
        BigDecimal totalIncome = incomes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        double savingRate = 0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingRate = totalIncome.subtract(totalExpense)
                    .divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
        }

        List<String> dates = (List<String>) analysis.get("dates");
        double dailyAvg = dates.isEmpty() ? 0 : totalExpense.doubleValue() / dates.size();

        List<Map<String, Object>> cats = (List<Map<String, Object>>) analysis.get("categories");
        List<Map<String, Object>> topCategories = cats.stream()
                .sorted((a, b) -> ((BigDecimal) b.get("value")).compareTo((BigDecimal) a.get("value")))
                .limit(3)
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", m.get("name"));
                    BigDecimal val = (BigDecimal) m.get("value");
                    item.put("amount", val);
                    int percent = totalExpense.compareTo(BigDecimal.ZERO) == 0 ? 0
                            : val.multiply(new BigDecimal(100)).divide(totalExpense, 0, RoundingMode.HALF_UP)
                                    .intValue();
                    item.put("percent", percent);
                    return item;
                }).collect(Collectors.toList());

        List<Map<String, String>> alerts = getRiskAlerts(userId);
        Map<String, Object> goal = getGoalTracking(userId);
        double goalProgress = (goal != null && goal.containsKey("progress"))
                ? ((Number) goal.get("progress")).doubleValue()
                : 0;

        Map<String, Object> profile = getFinancialProfile(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("summary", "您的财务状况" + (savingRate > 20 ? "良好" : "一般") + "，" +
                (topCategories.isEmpty() ? "暂无消费建议。" : "建议关注" + topCategories.get(0).get("name") + "支出。"));
        result.put("savingRate", Math.round(savingRate));
        result.put("dailyAvg", Math.round(dailyAvg));
        result.put("riskCount", alerts.size());
        result.put("topCategories", topCategories);
        result.put("goalProgress", Math.round(goalProgress));
        result.put("grade", profile.get("score"));

        List<String> tags = new ArrayList<>();
        if (savingRate > 30)
            tags.add("储蓄达人");
        if (dailyAvg < 100)
            tags.add("省钱向导");
        if (alerts.isEmpty())
            tags.add("风险规避者");
        if (tags.isEmpty())
            tags.add("财务探索者");
        result.put("userTags", tags);

        return result;
    }

    @Override
    public Map<String, Object> getFinancialProfile(Long userId) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        List<AccBill> bills = this.list(wrapper);

        double totalIncome = bills.stream()
                .filter(b -> b.getType() == 1)
                .mapToDouble(b -> b.getAmount() == null ? 0 : b.getAmount().doubleValue())
                .sum();
        double totalExpense = bills.stream()
                .filter(b -> b.getType() == 2)
                .mapToDouble(b -> b.getAmount() == null ? 0 : b.getAmount().doubleValue())
                .sum();

        double savingsRate = totalIncome == 0 ? 0 : (totalIncome - totalExpense) / totalIncome;
        double netWorth = totalIncome - totalExpense;

        // 计算雷达图 5 维数据 (0-100)
        // 1. 储蓄能力：由储蓄率决定
        double r1 = Math.max(10, Math.min(100, savingsRate * 100));

        // 2. 支出控制：看单笔最大支出占总支出的比例 (比例越低，分越高)
        double r2 = 80; // 默认值
        if (totalExpense > 0) {
            double maxOne = bills.stream().filter(b -> b.getType() == 2).mapToDouble(b -> b.getAmount().doubleValue())
                    .max().orElse(0);
            double concentration = maxOne / totalExpense;
            r2 = Math.max(10, 100 - (concentration * 100));
        }

        // 3. 风险偏好：看风险预警数
        List<Map<String, String>> riskAlerts = getRiskAlerts(userId);
        double r3 = Math.max(20, 100 - (riskAlerts.size() * 20));

        // 4. 成长潜力：综合近 30 天记账频率 (活跃度)
        long activeDays = bills.stream()
                .filter(b -> b.getBillDate().isAfter(LocalDate.now().minusDays(30)))
                .map(AccBill::getBillDate)
                .distinct()
                .count();
        double r4 = Math.min(100, (activeDays / 20.0) * 100);

        // 5. 资产健康：复用健康评分
        Map<String, Object> health = getFinancialHealth(userId, null, null);
        double r5 = ((Number) health.get("score")).doubleValue();

        Map<String, Object> profile = new HashMap<>();

        // 称号与评价
        if (savingsRate > 0.4) {
            profile.put("title", "稳健理财家 🛡️");
            profile.put("grade", "S");
            profile.put("description", "您拥有极强的储蓄意识和资产保护能力，财务结构非常稳固。");
        } else if (savingsRate > 0.1) {
            profile.put("title", "平衡生活家 ⚖️");
            profile.put("grade", "A");
            profile.put("description", "您在享受当下与储备未来之间找到了平衡，继续保持稳定的记账习惯。");
        } else {
            profile.put("title", "自由享乐派 🎸");
            profile.put("grade", "B");
            profile.put("description", "您更倾向于通过消费提升生活质量，建议适当增加紧急预备金。");
        }

        profile.put("savingsRate", String.format("%.1f%%", Math.max(0, savingsRate * 100)));
        profile.put("netWorth", String.format("%,.0f", netWorth));
        profile.put("radarData", Arrays.asList((int) r1, (int) r2, (int) r3, (int) r4, (int) r5));

        // 动态标签
        List<String> tags = new ArrayList<>();
        if (savingsRate > 0.5)
            tags.add("储蓄达人");
        if (activeDays > 25)
            tags.add("专业记账员");
        if (r3 > 80)
            tags.add("风险规避者");
        if (netWorth > 10000)
            tags.add("财富积累中");
        if (tags.isEmpty())
            tags.add("财务探索者");
        profile.put("tags", tags);

        // 针对性建议
        List<String> suggestions = new ArrayList<>();
        if (savingsRate < 0.2)
            suggestions.add("您的储蓄率偏低，建议开启‘先储蓄后消费’模式。");
        if (r2 < 50)
            suggestions.add("单笔支出占比较高，建议审视是否存在冲动消费情况。");
        if (activeDays < 10)
            suggestions.add("记账频率较低，建议每天固定时间记录流水以防遗漏。");
        if (suggestions.size() < 3) {
            suggestions.add("目前的资产净值为 " + profile.get("netWorth") + "，建议根据此规模规划长期投资。");
            suggestions.add("您的财务评分等级为 " + profile.get("grade") + "，在同龄人中处于领先水平。");
        }
        profile.put("suggestions", suggestions.subList(0, Math.min(3, suggestions.size())));

        return profile;
    }

    @Override
    public List<Map<String, Object>> getFinancialTimeline(Long userId) {
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.orderByDesc(AccBill::getBillDate);
        List<AccBill> bills = this.list(wrapper);

        Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AccCategory::getId, AccCategory::getName));

        return bills.stream()
                .map(b -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", b.getBillDate());
                    item.put("amount", b.getAmount());
                    item.put("category", categoryMap.getOrDefault(b.getCategoryId(), "其他"));
                    item.put("isKeyInfo", b.getAmount() != null && b.getAmount().doubleValue() > 500);
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getRiskAlerts(Long userId) {
        List<Map<String, String>> alerts = new ArrayList<>();
        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        wrapper.eq(AccBill::getType, 2);
        wrapper.gt(AccBill::getAmount, 1000);
        long largeBills = this.count(wrapper);

        if (largeBills > 0) {
            Map<String, String> alert = new HashMap<>();
            alert.put("level", "warning");
            alert.put("title", "大额支出预警");
            alert.put("desc", "检测到 " + largeBills + " 笔超过 1000 元的支出，请确认是否为本人操作。");
            alerts.add(alert);
        }
        return alerts;
    }

    @Override
    public Map<String, Object> getGoalTracking(Long userId) {
        Map<String, Object> res = new HashMap<>();

        AccFinancialGoal goal = goalMapper.selectOne(new LambdaQueryWrapper<AccFinancialGoal>()
                .eq(AccFinancialGoal::getUserId, userId)
                .orderByDesc(AccFinancialGoal::getId)
                .last("LIMIT 1"));

        double target = (goal != null && goal.getTargetAmount() != null) ? goal.getTargetAmount().doubleValue()
                : 50000.0;
        String goalName = (goal != null && goal.getGoalName() != null) ? goal.getGoalName() : "年度储蓄计划";
        String deadlineStr = (goal != null && goal.getTargetDate() != null) ? goal.getTargetDate().toString()
                : LocalDate.now().plusYears(1).toString();
        LocalDate deadline = LocalDate.parse(deadlineStr);

        LambdaQueryWrapper<AccBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccBill::getUserId, userId);
        List<AccBill> bills = this.list(wrapper);

        double income = bills.stream().filter(b -> b.getType() == 1 && b.getAmount() != null)
                .mapToDouble(b -> b.getAmount().doubleValue()).sum();
        double expense = bills.stream().filter(b -> b.getType() == 2 && b.getAmount() != null)
                .mapToDouble(b -> b.getAmount().doubleValue()).sum();
        double currentSaved = Math.max(0, income - expense);

        long daysLeft = deadline.toEpochDay() - LocalDate.now().toEpochDay();
        double dailyNeeds = daysLeft <= 0 ? 0 : (target - currentSaved) / daysLeft;

        res.put("goalName", goalName);
        res.put("targetAmount", target);
        res.put("currentSaved", currentSaved);
        res.put("progress", target == 0 ? 0 : Math.min(100, (currentSaved / target) * 100));
        res.put("deadline", deadlineStr);
        res.put("estimatedDays", Math.max(0, daysLeft));
        res.put("dailyNeeds", String.format("%.2f", Math.max(0, dailyNeeds)));
        res.put("encouragement", currentSaved >= target ? "恭喜您已达成目标！" : "坚持就是胜利！继续保持这份克制与专注！");

        return res;
    }

    @Override
    public boolean updateGoal(Long userId, Double targetAmount, String deadline) {
        AccFinancialGoal goal = new AccFinancialGoal();
        goal.setUserId(userId);
        goal.setTargetAmount(new BigDecimal(targetAmount));
        goal.setTargetDate(LocalDate.parse(deadline.substring(0, 10))); // Handle ISO string
        goal.setGoalName("我的财务目标"); // Default or we can add it optionally
        return goalMapper.insert(goal) > 0;
    }
}
