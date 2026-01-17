package com.example.bookkeeping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookkeeping.entity.Bill;
import com.example.bookkeeping.entity.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map; // 补全引用

public interface BillService extends IService<Bill> {
    Page<Bill> getBillPage(Page<Bill> page, Long userId, Long categoryId, String startDate, String endDate);
    List<Category> getAllCategories();
    
    // 生成随机账单
    void generateRandomBills(Long userId, int count);
    
    // 清空账单
    void clearBills(Long userId);

    // 👇 新增：获取当前条件下的收支统计 (总收、总支、结余)
    Map<String, Object> getBillStats(Long userId, Long categoryId, String startDate, String endDate);

    // 👇 修改：增加日期范围 filter
    Map<String, Object> getAnalysisData(Long userId, String startDate, String endDate);

    // 👇 新增：获取树状流向图数据
    Map<String, Object> getTreeMapData(Long userId, String startDate, String endDate);

    // 👇 新增：获取日历视图数据
    List<Map<String, Object>> getCalendarData(Long userId, Integer type, Long categoryId, String startStr, String endStr);

    // 👇 新增：获取日历热力图数据 (按天聚合)
    Map<String, Map<String, Object>> getCalendarHeatMap(Long userId, String startStr, String endStr, Integer type, Long categoryId);

    // --- 📊 企业级报表扩展接口 ---

    // 1. 支出 TOP5 分类排行
    List<Map<String, Object>> getExpenditureTop5(Long userId, String startDate, String endDate);

    // 2. 收入 / 支出 同比 / 环比分析
    Map<String, Object> getYoYAnalysis(Long userId, String startDate, String endDate);

    // 3. 消费频率分析（次数维度）
    List<Map<String, Object>> getConsumptionFrequency(Long userId, String startDate, String endDate);

    // 4. 星期消费分布（行为模式）
    Map<String, BigDecimal> getWeeklyConsumption(Long userId, String startDate, String endDate);

    // 5. 预算消耗趋势图
    Map<String, Object> getBudgetBurnTrend(Long userId, String startDate, String endDate);

    // 6. 预算风险区间图
    Map<String, Object> getBudgetRisk(Long userId, String startDate, String endDate);

    // 7. 综合财务健康指数
    Map<String, Object> getFinancialHealth(Long userId, String startDate, String endDate);
}
