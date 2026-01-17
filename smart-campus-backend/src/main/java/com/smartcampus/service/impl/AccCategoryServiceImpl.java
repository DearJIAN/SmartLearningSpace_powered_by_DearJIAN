package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.AccCategory;
import com.smartcampus.mapper.AccCategoryMapper;
import com.smartcampus.service.AccCategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 记账分类服务实现
 */
@Service
public class AccCategoryServiceImpl extends ServiceImpl<AccCategoryMapper, AccCategory> implements AccCategoryService {

    @Override
    public List<AccCategory> getUserCategories(Long userId, Integer type) {
        LambdaQueryWrapper<AccCategory> wrapper = new LambdaQueryWrapper<>();
        // user_id=0 为系统预置分类，user_id=userId 为用户自定义分类
        wrapper.in(AccCategory::getUserId, 0L, userId);

        // 如果指定了类型，则筛选
        if (type != null) {
            wrapper.eq(AccCategory::getType, type);
        }

        wrapper.orderByAsc(AccCategory::getType, AccCategory::getId);
        List<AccCategory> list = this.list(wrapper);

        // 如果是该用户的第一次访问（没有任何分类），则自动初始化并重新查询
        if (list.isEmpty() && type == null) {
            initDefaultCategories(userId);
            return this.getUserCategories(userId, null);
        }

        return list;
    }

    @Override
    public void initDefaultCategories(Long userId) {
        // 创建默认收入分类
        String[] incomeCategories = { "工资", "奖金", "投资收益", "其他收入" };
        for (String name : incomeCategories) {
            AccCategory category = new AccCategory();
            category.setUserId(userId);
            category.setName(name);
            category.setType(1); // 收入
            this.save(category);
        }

        // 创建默认支出分类
        String[] expenseCategories = { "餐饮美食", "购物消费", "交通出行", "生活缴费", "娱乐休闲", "医疗健康", "学习教育", "其他支出" };
        for (String name : expenseCategories) {
            AccCategory category = new AccCategory();
            category.setUserId(userId);
            category.setName(name);
            category.setType(2); // 支出
            this.save(category);
        }
    }
}
