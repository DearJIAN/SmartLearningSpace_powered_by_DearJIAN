package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.AccBudget;

import java.util.Map;

/**
 * 月度预算服务接口
 */
public interface AccBudgetService extends IService<AccBudget> {

    /**
     * 获取预算状态
     * 
     * @param userId 用户ID
     * @param month  月份（YYYY-MM）
     * @return 预算状态 Map（total/used/remaining/progress/isOver）
     */
    Map<String, Object> getBudgetStatus(Long userId, String month);

    /**
     * 设置或更新月度预算
     * 
     * @param userId      用户ID
     * @param month       月份
     * @param totalBudget 总预算
     */
    void setBudget(Long userId, String month, java.math.BigDecimal totalBudget);
}
