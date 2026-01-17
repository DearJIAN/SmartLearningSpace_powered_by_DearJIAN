package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.AccBill;

import java.util.List;
import java.util.Map;

/**
 * 账单记录服务接口
 */
public interface AccBillService extends IService<AccBill> {

        /**
         * 分页查询账单（带条件筛选）
         * 
         * @param page       分页对象
         * @param userId     用户ID
         * @param categoryId 分类ID（可选）
         * @param startDate  开始日期（可选）
         * @param endDate    结束日期（可选）
         * @param type       类型：1=收入，2=支出（可选）
         * @return 分页结果
         */
        Page<AccBill> getBillPage(Page<AccBill> page, Long userId, Long categoryId, String startDate, String endDate,
                        Integer type);

        /**
         * 获取账单统计数据（收入/支出/结余）
         * 
         * @param userId     用户ID
         * @param categoryId 分类ID（可选）
         * @param startDate  开始日期（可选）
         * @param endDate    结束日期（可选）
         * @return 统计数据 Map
         */
        Map<String, Object> getBillStats(Long userId, Long categoryId, String startDate, String endDate);

        /**
         * 随机生成账单（用于测试）
         * 
         * @param userId 用户ID
         * @param count  生成数量
         */
        void generateRandomBills(Long userId, Integer count);

        /**
         * 清空用户所有账单
         * 
         * @param userId 用户ID
         */
        void clearBills(Long userId);

        /**
         * 获取分析数据 (收支趋势、分类占比等)
         */
        Map<String, Object> getAnalysisData(Long userId, String startDate, String endDate);

        /**
         * 获取 TreeMap 资金流向图数据
         */
        Map<String, Object> getTreeMapData(Long userId, String startDate, String endDate);

        /**
         * 获取日历视图数据 (事件列表)
         */
        List<Map<String, Object>> getCalendarData(Long userId, Integer type, Long categoryId, String startStr,
                        String endStr);

        /**
         * 获取支出 TOP5 分类排行
         */
        List<Map<String, Object>> getExpenditureTop5(Long userId, String startDate, String endDate);

        /**
         * 收入 / 支出 同比 / 环比分析
         */
        Map<String, Object> getYoYAnalysis(Long userId, String startDate, String endDate);

        /**
         * 消费频率分析
         */
        List<Map<String, Object>> getConsumptionFrequency(Long userId, String startDate, String endDate);

        /**
         * 星期消费分布
         */
        Map<String, java.math.BigDecimal> getWeeklyConsumption(Long userId, String startDate, String endDate);

        /**
         * 预算消耗趋势图
         */
        Map<String, Object> getBudgetBurnTrend(Long userId, String startDate, String endDate);

        /**
         * 预算风险区间图
         */
        Map<String, Object> getBudgetRisk(Long userId, String startDate, String endDate);

        /**
         * 综合财务健康指数
         */
        Map<String, Object> getFinancialHealth(Long userId, String startDate, String endDate);

        /**
         * 获取洞察看板数据
         */
        Map<String, Object> getInsightDashboard(Long userId);

        /**
         * 获取财务画像
         */
        Map<String, Object> getFinancialProfile(Long userId);

        /**
         * 获取财务时间轴
         */
        List<Map<String, Object>> getFinancialTimeline(Long userId);

        /**
         * 获取风险预警
         */
        List<Map<String, String>> getRiskAlerts(Long userId);

        /**
         * 获取财务目标追踪
         */
        Map<String, Object> getGoalTracking(Long userId);

        /**
         * 更新财务目标
         */
        boolean updateGoal(Long userId, Double targetAmount, String deadline);

        /**
         * 获取账单列表（不分页，用于导出）
         */
        List<AccBill> getBillList(Long userId, Long categoryId, String startDate, String endDate, Integer type);
}
