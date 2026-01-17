package com.example.bookkeeping.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookkeeping.entity.Bill;
import com.example.bookkeeping.entity.Budget; // 新增引用
import com.example.bookkeeping.entity.Category;
import com.example.bookkeeping.mapper.BillMapper;
import com.example.bookkeeping.mapper.CategoryMapper;
import com.example.bookkeeping.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
// 补全引用
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BillServiceImpl extends ServiceImpl<BillMapper, Bill> implements BillService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private com.example.bookkeeping.mapper.BudgetMapper budgetMapper;

    @Override
    public Page<Bill> getBillPage(Page<Bill> page, Long userId, Long categoryId, String startDate, String endDate) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        // 🔐 关键点：这里强制限制了只能查自己的 user_id
        wrapper.eq(Bill::getUserId, userId);

        if (categoryId != null) wrapper.eq(Bill::getCategoryId, categoryId);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);

        wrapper.orderByDesc(Bill::getBillDate);

        Page<Bill> result = this.page(page, wrapper);

        // 填充分类名称
        result.getRecords().forEach(bill -> {
            Category cat = categoryMapper.selectById(bill.getCategoryId());
            if (cat != null) bill.setCategoryName(cat.getName());
        });
        return result;
    }

    @Override
    public List<Category> getAllCategories() {
        // 👇 修改：明确只查询系统公共分类 (userId = 0)
        // 这样可以防止未来开发“用户自定义分类”时，错误的把 A 用户的分类名显示给 B 用户
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().eq(Category::getUserId, 0));
    }

    // 👇 核心修复：确保使用真实词库，绝不生成 "模拟数据-xxx"
    @Override
    public void generateRandomBills(Long userId, int count) {
        List<Category> categories = categoryMapper.selectList(null);
        if (categories.isEmpty()) return;

        List<Bill> bills = new ArrayList<>();
        Random random = new Random();

        // 👇 用于记录本次生成中已包含薪资的月份，防止单月多次发薪
        Set<String> salaryMonths = new HashSet<>();

        // 真实生活场景备注库
        List<String> foodRemarks = Arrays.asList("黄焖鸡米饭", "肯德基疯狂星期四", "楼下便利店", "超市买菜", "同事聚餐AA", "一点点奶茶", "早餐包子", "百果园", "烧烤");
        List<String> trafficRemarks = Arrays.asList("滴滴打车", "地铁充值", "加油", "停车费", "哈啰单车", "汽车保养");
        List<String> shoppingRemarks = Arrays.asList("淘宝衣服", "京东", "屈臣氏", "买书", "数码配件", "优衣库");
        List<String> housingRemarks = Arrays.asList("房租", "电费", "水费", "物业费", "宽带");
        List<String> entRemarks = Arrays.asList("电影票", "腾讯视频", "Steam", "KTV", "密室");
        List<String> incomeRemarks = Arrays.asList("兼职", "闲鱼", "报销", "理财收益"); // 移除"工资"，单独处理
        List<String> otherRemarks = Arrays.asList("红包", "打印", "买药", "理发", "送礼");

        for (int i = 0; i < count; i++) {
            Bill bill = new Bill();
            bill.setUserId(userId);

            Category cat = categories.get(random.nextInt(categories.size()));

            // 生成随机日期
            LocalDate date = LocalDate.now().minusDays(random.nextInt(60));
            bill.setBillDate(date);

            // 👇 逻辑修正：针对 "工资" 做特殊处理
            if (cat.getName().contains("工资")) {
                String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                // 如果本月已经发过工资，或者随机到了未来的日期（不太合理），就换成普通收入类别
                if (salaryMonths.contains(monthKey)) {
                    // 换成"兼职"或其他收入，避免重复发薪
                    cat = categories.stream().filter(c -> c.getType() == 1 && !c.getName().contains("工资")).findFirst().orElse(cat);
                } else {
                    // 标记本月已发薪
                    salaryMonths.add(monthKey);
                    // 强制发薪日为每月 10 号
                    bill.setBillDate(date.withDayOfMonth(10));
                }
            }

            bill.setCategoryId(cat.getId());
            bill.setType(cat.getType());

            BigDecimal amount;
            String remark;
            String catName = cat.getName();

            // 智能匹配金额和备注（缩小金额范围，降低大额出现概率）
            if (cat.getType() == 1) { // 收入
                if (catName.contains("工资")) {
                    // 工资限制在较低范围，避免生成过大金额
                    amount = BigDecimal.valueOf(200 + random.nextInt(300)); // 200 - 499
                    // 备注动态关联月份
                    remark = bill.getBillDate().getMonthValue() + "月薪资";
                } else if (catName.contains("奖金")) {
                    // 奖金一般较小，偶发性较大金额概率降低
                    if (random.nextDouble() < 0.05) {
                        amount = BigDecimal.valueOf(300 + random.nextInt(200)); // 小概率 300 - 499
                    } else {
                        amount = BigDecimal.valueOf(50 + random.nextInt(150)); // 50 - 199
                    }
                    remark = "项目奖金";
                } else {
                    // 普通收入以较小金额为主，少量较大概率
                    if (random.nextDouble() < 0.05) {
                        amount = BigDecimal.valueOf(200 + random.nextInt(300)); // 偶发较高
                    } else {
                        amount = BigDecimal.valueOf(20 + random.nextInt(180)); // 20 - 199
                    }
                    remark = incomeRemarks.get(random.nextInt(incomeRemarks.size()));
                }
            } else { // 支出
                if (catName.contains("餐饮") || catName.contains("吃")) {
                    amount = BigDecimal.valueOf(5 + random.nextInt(80)); // 5 - 84
                    remark = foodRemarks.get(random.nextInt(foodRemarks.size()));
                } else if (catName.contains("交通") || catName.contains("行")) {
                    amount = BigDecimal.valueOf(1 + random.nextInt(50)); // 1 - 50
                    remark = trafficRemarks.get(random.nextInt(trafficRemarks.size()));
                } else if (catName.contains("房") || catName.contains("住")) {
                    // 房租/水电等支出偏高但仍限制在几百内，较大值概率降低
                    if (random.nextDouble() < 0.1) {
                        amount = BigDecimal.valueOf(300 + random.nextInt(200)); // 小概率 300 - 499
                    } else {
                        amount = BigDecimal.valueOf(150 + random.nextInt(150)); // 150 - 299
                    }
                    remark = housingRemarks.get(random.nextInt(housingRemarks.size()));
                } else if (catName.contains("购") || catName.contains("买")) {
                    if (random.nextDouble() < 0.05) {
                        amount = BigDecimal.valueOf(200 + random.nextInt(300)); // 偶发较高
                    } else {
                        amount = BigDecimal.valueOf(20 + random.nextInt(180)); // 20 - 199
                    }
                    remark = shoppingRemarks.get(random.nextInt(shoppingRemarks.size()));
                } else if (catName.contains("娱乐") || catName.contains("玩")) {
                    amount = BigDecimal.valueOf(10 + random.nextInt(140)); // 10 - 149
                    remark = entRemarks.get(random.nextInt(entRemarks.size()));
                } else {
                    amount = BigDecimal.valueOf(5 + random.nextInt(100)); // 5 - 104
                    remark = otherRemarks.get(random.nextInt(otherRemarks.size()));
                }
            }

            bill.setAmount(amount);
            bill.setRemark(remark);
            bills.add(bill);
        }

        this.saveBatch(bills);
    }

    // 👇 新增：统计接口实现
    @Override
    public Map<String, Object> getBillStats(Long userId, Long categoryId, String startDate, String endDate) {
        // 构建查询条件 (与列表查询一致)
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        if (categoryId != null) wrapper.eq(Bill::getCategoryId, categoryId);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);

        List<Bill> list = this.list(wrapper);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Bill bill : list) {
            if (bill.getType() == 1) {
                totalIncome = totalIncome.add(bill.getAmount());
            } else if (bill.getType() == 2) {
                totalExpense = totalExpense.add(bill.getAmount());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("income", totalIncome);
        stats.put("expense", totalExpense);
        stats.put("balance", totalIncome.subtract(totalExpense));
        return stats;
    }

    @Override
    public void clearBills(Long userId) {
        this.remove(new LambdaQueryWrapper<Bill>().eq(Bill::getUserId, userId));
    }

    @Override
    public Map<String, Object> getAnalysisData(Long userId, String startDate, String endDate) {
        // 1. 获取用户所有账单，支持日期筛选
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);
        wrapper.orderByAsc(Bill::getBillDate); // 按时间正序

        List<Bill> bills = this.list(wrapper);
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> catNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName));

        // 2. 准备数据容器
        Map<String, BigDecimal> incomePie = new HashMap<>();
        Map<String, BigDecimal> expensePie = new HashMap<>();

        // 月趋势 (K:月份yyyy-MM, V:[收入, 支出])
        Map<String, BigDecimal[]> monthlyTrend = new TreeMap<>();

        // 👇 新增：日趋势 (K:日期yyyy-MM-dd, V:[收入, 支出])
        Map<String, BigDecimal[]> dailyTrend = new TreeMap<>();

        DateTimeFormatter monthDf = DateTimeFormatter.ofPattern("yyyy-MM");
        DateTimeFormatter dayDf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Bill b : bills) {
            if (b.getBillDate() == null) continue;
            String catName = catNameMap.getOrDefault(b.getCategoryId(), "其他");
            String month = b.getBillDate().format(monthDf);
            String day = b.getBillDate().format(dayDf);

            // 初始化
            monthlyTrend.putIfAbsent(month, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            dailyTrend.putIfAbsent(day, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});

            if (b.getType() == 1) { // 收入
                incomePie.merge(catName, b.getAmount(), BigDecimal::add);
                monthlyTrend.get(month)[0] = monthlyTrend.get(month)[0].add(b.getAmount());
                dailyTrend.get(day)[0] = dailyTrend.get(day)[0].add(b.getAmount());
            } else { // 支出
                expensePie.merge(catName, b.getAmount(), BigDecimal::add);
                monthlyTrend.get(month)[1] = monthlyTrend.get(month)[1].add(b.getAmount());
                dailyTrend.get(day)[1] = dailyTrend.get(day)[1].add(b.getAmount());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("incomePie", incomePie);
        result.put("expensePie", expensePie);
        result.put("monthly", monthlyTrend);
        result.put("daily", dailyTrend); // 返回日数据
        return result;
    }

    @Override
    public Map<String, Object> getTreeMapData(Long userId, String startDate, String endDate) {
        // 1. 查询数据
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);
        wrapper.orderByDesc(Bill::getBillDate);
        List<Bill> bills = this.list(wrapper);

        // 获取分类映射
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> catNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName));

        // 2. 构建树结构
        // 根节点
        Map<String, Object> root = new HashMap<>();
        root.put("name", "账本资金流向");
        root.put("symbol", "image://https://cdn-icons-png.flaticon.com/512/2933/2933116.png"); // 根图标
        root.put("symbolSize", 30);
        List<Map<String, Object>> rootChildren = new ArrayList<>();

        // 二级节点：收入 & 支出
        Map<String, Object> incomeNode = new HashMap<>();
        incomeNode.put("name", "收入流入");
        incomeNode.put("itemStyle", Collections.singletonMap("color", "#198754"));
        incomeNode.put("label", Collections.singletonMap("fontSize", 16));
        List<Map<String, Object>> incomeChildren = new ArrayList<>(); // 收入下的分类列表

        Map<String, Object> expenseNode = new HashMap<>();
        expenseNode.put("name", "支出流向");
        expenseNode.put("itemStyle", Collections.singletonMap("color", "#dc3545"));
        expenseNode.put("label", Collections.singletonMap("fontSize", 16));
        List<Map<String, Object>> expenseChildren = new ArrayList<>(); // 支出下的分类列表

        // 辅助Map：用于按分类归组
        Map<String, List<Map<String, Object>>> incomeCatGroups = new HashMap<>();
        Map<String, List<Map<String, Object>>> expenseCatGroups = new HashMap<>();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("MM-dd");

        for (Bill bill : bills) {
            // 🔍 修复：增加非空判断，跳过脏数据
            if (bill.getBillDate() == null || bill.getAmount() == null) continue;

            String catName = catNameMap.getOrDefault(bill.getCategoryId(), "其他");
            int type = bill.getType();

            // 构建叶子节点 (每一笔账单)
            Map<String, Object> leafNode = new HashMap<>();
            // 显示格式：[12-05] 备注 : 金额
            leafNode.put("name", "[" + bill.getBillDate().format(df) + "] " + bill.getRemark() + " : " + bill.getAmount());
            leafNode.put("value", bill.getAmount());
            leafNode.put("symbol", "circle"); // 叶子节点用小圆点
            leafNode.put("symbolSize", 6);

            if (type == 1) {
                incomeCatGroups.computeIfAbsent(catName, k -> new ArrayList<>()).add(leafNode);
            } else {
                expenseCatGroups.computeIfAbsent(catName, k -> new ArrayList<>()).add(leafNode);
            }
        }

        // 组装分类节点
        incomeCatGroups.forEach((catName, children) -> {
            Map<String, Object> catNode = new HashMap<>();
            catNode.put("name", catName);
            catNode.put("children", children);
            catNode.put("symbol", "rect"); // 分类节点用方块
            catNode.put("symbolSize", 10);
            incomeChildren.add(catNode);
        });

        expenseCatGroups.forEach((catName, children) -> {
            Map<String, Object> catNode = new HashMap<>();
            catNode.put("name", catName);
            catNode.put("children", children);
            catNode.put("symbol", "rect");
            catNode.put("symbolSize", 10);
            expenseChildren.add(catNode);
        });

        incomeNode.put("children", incomeChildren);
        expenseNode.put("children", expenseChildren);

        rootChildren.add(expenseNode); // 支出放上面或下面
        rootChildren.add(incomeNode);
        root.put("children", rootChildren);

        return root;
    }

    @Override
    public List<Map<String, Object>> getCalendarData(Long userId, Integer type, Long categoryId, String startStr, String endStr) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);

        // 前端日历插件会自动传 start/end 参数来获取当前视图的范围
        if (StringUtils.hasText(startStr)) wrapper.ge(Bill::getBillDate, startStr);
        if (StringUtils.hasText(endStr)) wrapper.le(Bill::getBillDate, endStr);
        if (type != null) wrapper.eq(Bill::getType, type);
        if (categoryId != null) wrapper.eq(Bill::getCategoryId, categoryId);

        List<Bill> bills = this.list(wrapper);
        List<Category> categories = categoryMapper.selectList(null);
        Map<Long, String> catNameMap = categories.stream().collect(Collectors.toMap(Category::getId, Category::getName));

        List<Map<String, Object>> events = new ArrayList<>();

        for (Bill bill : bills) {
            Map<String, Object> event = new HashMap<>();
            String catName = catNameMap.getOrDefault(bill.getCategoryId(), "其他");

            // 构建标题：[餐饮] -15.00
            String symbol = bill.getType() == 1 ? "+" : "-";
            event.put("title", "[" + catName + "] " + symbol + bill.getAmount());

            // 日期 ISO 格式
            event.put("start", bill.getBillDate().toString());

            // 颜色区分：收入绿色，支出红色
            if (bill.getType() == 1) {
                event.put("backgroundColor", "#198754"); // success color
                event.put("borderColor", "#198754");
            } else {
                event.put("backgroundColor", "#dc3545"); // danger color
                event.put("borderColor", "#dc3545");
            }

            // 扩展属性，用于点击弹窗显示详情
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
    public Map<String, Map<String, Object>> getCalendarHeatMap(Long userId, String startStr, String endStr, Integer type, Long categoryId) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        if (StringUtils.hasText(startStr)) wrapper.ge(Bill::getBillDate, startStr);
        if (StringUtils.hasText(endStr)) wrapper.le(Bill::getBillDate, endStr);
        if (type != null) wrapper.eq(Bill::getType, type);
        if (categoryId != null) wrapper.eq(Bill::getCategoryId, categoryId);

        List<Bill> bills = this.list(wrapper);

        // K: 日期(yyyy-MM-dd), V: {income: x, expense: y, balance: z}
        Map<String, Map<String, Object>> resultMap = new HashMap<>();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Bill bill : bills) {
            String dateKey = bill.getBillDate().format(df);
            resultMap.putIfAbsent(dateKey, new HashMap<>());
            Map<String, Object> dayStats = resultMap.get(dateKey);

            BigDecimal income = (BigDecimal) dayStats.getOrDefault("income", BigDecimal.ZERO);
            BigDecimal expense = (BigDecimal) dayStats.getOrDefault("expense", BigDecimal.ZERO);

            if (bill.getType() == 1) {
                income = income.add(bill.getAmount());
            } else {
                expense = expense.add(bill.getAmount());
            }

            dayStats.put("income", income);
            dayStats.put("expense", expense);
            // balance = income - expense
            dayStats.put("balance", income.subtract(expense));
        }

        return resultMap;
    }

    // --- 📊 企业级报表扩展实现 ---

    // 1. 支出 TOP5 分类排行
    @Override
    public List<Map<String, Object>> getExpenditureTop5(Long userId, String startDate, String endDate) {
        if (userId == null) return Collections.emptyList(); // 参数保护

        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        wrapper.eq(Bill::getType, 2); // 支出
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);

        List<Bill> bills = this.list(wrapper);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        // 按分类ID分组求和
        Map<Long, BigDecimal> grouped = bills.stream()
                .filter(b -> b.getCategoryId() != null && b.getAmount() != null) // 🔍 修复：过滤 null 金额
                .collect(Collectors.groupingBy(
                        Bill::getCategoryId,
                        Collectors.reducing(BigDecimal.ZERO, Bill::getAmount, BigDecimal::add)
                ));

        return grouped.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // 降序
                .limit(5)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", catMap.getOrDefault(e.getKey(), "其他"));
                    map.put("amount", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // 2. 收入 / 支出 同比 / 环比分析
    @Override
    public Map<String, Object> getYoYAnalysis(Long userId, String startDateStr, String endDateStr) {
        // 解析日期，若无则默认本月
        LocalDate start, end;
        try {
            start = StringUtils.hasText(startDateStr) ? LocalDate.parse(startDateStr) : LocalDate.now().withDayOfMonth(1);
            end = StringUtils.hasText(endDateStr) ? LocalDate.parse(endDateStr) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        // 计算上个周期 (长度相同)
        long days = end.toEpochDay() - start.toEpochDay();
        LocalDate prevStart = start.minusDays(days + 1);
        LocalDate prevEnd = end.minusDays(days + 1);

        Map<String, BigDecimal> current = getSumByType(userId, start, end);
        Map<String, BigDecimal> previous = getSumByType(userId, prevStart, prevEnd);

        Map<String, Object> result = new HashMap<>();
        result.put("current", current);
        result.put("previous", previous);

        Map<String, Object> rates = new HashMap<>();
        rates.put("incomeRate", calculateRate(current.get("income"), previous.get("income")));
        rates.put("expenseRate", calculateRate(current.get("expense"), previous.get("expense")));
        result.put("rate", rates);

        return result;
    }

    private Map<String, BigDecimal> getSumByType(Long userId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        wrapper.ge(Bill::getBillDate, start);
        wrapper.le(Bill::getBillDate, end);
        List<Bill> bills = this.list(wrapper);

        // 🔍 修复：过滤 null 金额
        BigDecimal income = bills.stream().filter(b -> b.getType() == 1 && b.getAmount() != null)
                .map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal expense = bills.stream().filter(b -> b.getType() == 2 && b.getAmount() != null)
                .map(Bill::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> map = new HashMap<>();
        map.put("income", income);
        map.put("expense", expense);
        return map;
    }

    private Double calculateRate(BigDecimal curr, BigDecimal prev) {
         if (prev.compareTo(BigDecimal.ZERO) == 0) return (curr.compareTo(BigDecimal.ZERO) > 0) ? 1.0 : 0.0;
         return curr.subtract(prev).divide(prev, 4, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    // 3. 消费频率分析
    @Override
    public List<Map<String, Object>> getConsumptionFrequency(Long userId, String startDate, String endDate) {
         LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        wrapper.eq(Bill::getType, 2);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);

        List<Bill> bills = this.list(wrapper);
        Map<Long, String> catMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        Map<Long, Long> counts = bills.stream()
                .filter(b -> b.getCategoryId() != null)
                .collect(Collectors.groupingBy(Bill::getCategoryId, Collectors.counting()));

        return counts.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("category", catMap.getOrDefault(e.getKey(), "其他"));
                    map.put("count", e.getValue());
                    return map;
                })
                .sorted((a,b) -> ((Long)b.get("count")).compareTo((Long)a.get("count")))
                .collect(Collectors.toList());
    }

    // 4. 星期消费分布
    @Override
    public Map<String, BigDecimal> getWeeklyConsumption(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        wrapper.eq(Bill::getType, 2);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);

        List<Bill> bills = this.list(wrapper);

        Map<String, BigDecimal> result = new LinkedHashMap<>();
        String[] weeks = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for(String w : weeks) result.put(w, BigDecimal.ZERO);

        for (Bill bill : bills) {
             // 🔍 修复：增加 date 和 amount 的非空校验
             if(bill.getBillDate() == null || bill.getAmount() == null) continue;

             String dayStr = bill.getBillDate().getDayOfWeek().toString(); // MONDAY
             String shortKey = dayStr.substring(0, 1) + dayStr.substring(1, 3).toLowerCase(); // Mon
             result.merge(shortKey, bill.getAmount(), BigDecimal::add);
        }
        return result;
    }

    // 5. 预算消耗趋势
    @Override
    public Map<String, Object> getBudgetBurnTrend(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<Bill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bill::getUserId, userId);
        wrapper.eq(Bill::getType, 2);
        if (StringUtils.hasText(startDate)) wrapper.ge(Bill::getBillDate, startDate);
        if (StringUtils.hasText(endDate)) wrapper.le(Bill::getBillDate, endDate);
        wrapper.orderByAsc(Bill::getBillDate);
        List<Bill> bills = this.list(wrapper);

        String month = StringUtils.hasText(startDate) ? startDate.substring(0, 7) : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 防御性编程：如果是预算表不存在或查询报错，默认预算为 0
        BigDecimal totalBudget = BigDecimal.ZERO;
        try {
            LambdaQueryWrapper<Budget> budgetWrapper = new LambdaQueryWrapper<>();
            budgetWrapper.eq(Budget::getUserId, userId);
            budgetWrapper.eq(Budget::getMonth, month);
            Budget budgetObj = budgetMapper.selectOne(budgetWrapper);
            if (budgetObj != null) totalBudget = budgetObj.getTotalBudget();
        } catch (Exception e) {
            System.err.println("⚠️ 警告：查询预算失败，可能是表不存在或 SQL 错误: " + e.getMessage());
        }

        // 每日累积
        List<Map<String, Object>> dailyUsed = new ArrayList<>();
        BigDecimal accum = BigDecimal.ZERO;
        Map<String, BigDecimal> daySumMap = new TreeMap<>();

        for(Bill b : bills) {
            if (b.getBillDate() == null || b.getAmount() == null) continue; // 🔍 修复：增加 amount 检查
            String day = b.getBillDate().toString();
            daySumMap.merge(day, b.getAmount(), BigDecimal::add);
        }

        for(Map.Entry<String, BigDecimal> entry : daySumMap.entrySet()) {
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

    // 6. 预算风险
    @Override
    public Map<String, Object> getBudgetRisk(Long userId, String startDate, String endDate) {
         Map<String, Object> trend = getBudgetBurnTrend(userId, startDate, endDate);
         BigDecimal budget = (BigDecimal) trend.get("budget");
         List<Map<String, Object>> daily = (List<Map<String, Object>>) trend.get("dailyUsed");

         BigDecimal used = BigDecimal.ZERO;
         if (!daily.isEmpty()) {
             used = (BigDecimal) daily.get(daily.size() - 1).get("used");
         }

         Map<String, Object> result = new HashMap<>();
         result.put("total", budget);
         result.put("used", used);
         if (budget != null && budget.compareTo(BigDecimal.ZERO) > 0) {
             result.put("rate", used.divide(budget, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal(100)));
         } else {
             result.put("rate", BigDecimal.ZERO);
         }
         return result;
    }

    // 7. 健康指数
    @Override
    public Map<String, Object> getFinancialHealth(Long userId, String startDate, String endDate) {
        // 防止日期解析异常
        LocalDate start, end;
        try {
            start = StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : LocalDate.now().withDayOfMonth(1);
            end = StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        }

        Map<String, BigDecimal> sums = getSumByType(userId, start, end);

        BigDecimal income = sums.get("income");
        BigDecimal expense = sums.get("expense");

        double savingRate = 0;
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            savingRate = income.subtract(expense).divide(income, 4, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        List<Map<String, Object>> top5 = getExpenditureTop5(userId, startDate, endDate);
        double expenseFocus = 0;
        if (!top5.isEmpty() && expense.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal top1 = (BigDecimal) top5.get(0).get("amount");
            expenseFocus = top1.divide(expense, 4, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        double balance = 0.5;
        if (income.add(expense).compareTo(BigDecimal.ZERO) > 0) {
            // 平衡度：支出/收入 接近 0.6 为佳？这里做个简单归一化
            balance = income.divide(income.add(expense), 4, java.math.RoundingMode.HALF_UP).doubleValue();
        }

        Map<String, Object> res = new HashMap<>();
        res.put("savingRate", Math.max(0, savingRate));
        res.put("expenseFocus", expenseFocus);
        res.put("balance", balance);
        res.put("stability", 0.7); // 暂定常量
        return res;
    }
}
