package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.AccFinancialGoal;
import com.smartcampus.mapper.AccFinancialGoalMapper;
import com.smartcampus.service.AccFinancialGoalService;
import org.springframework.stereotype.Service;

/**
 * 财务目标服务实现
 */
@Service
public class AccFinancialGoalServiceImpl extends ServiceImpl<AccFinancialGoalMapper, AccFinancialGoal>
        implements AccFinancialGoalService {

    // 基础 CRUD 继承自 ServiceImpl
}
