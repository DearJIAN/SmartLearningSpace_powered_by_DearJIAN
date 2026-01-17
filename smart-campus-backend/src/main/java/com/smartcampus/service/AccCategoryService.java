package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.AccCategory;

import java.util.List;

/**
 * 记账分类服务接口
 */
public interface AccCategoryService extends IService<AccCategory> {

    /**
     * 获取用户可见的所有分类（包含系统预置 + 用户自定义）
     * 
     * @param userId 用户ID
     * @param type   类型：1=收入，2=支出，null=全部
     * @return 分类列表
     */
    List<AccCategory> getUserCategories(Long userId, Integer type);

    /**
     * 为新用户初始化默认分类
     * 
     * @param userId 用户ID
     */
    void initDefaultCategories(Long userId);
}
