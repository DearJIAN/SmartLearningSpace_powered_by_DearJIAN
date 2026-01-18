package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.LFLostItem;

import java.util.List;
import java.util.Map;

/**
 * 失物招领Service接口
 */
public interface LFLostItemService extends IService<LFLostItem> {
    
    /**
     * 获取失物招领列表，支持筛选
     * @param params 筛选条件（教室ID、物品类型、状态、时间范围）
     * @return 失物招领列表
     */
    List<LFLostItem> getLostItemList(Map<String, Object> params);
    
    /**
     * 根据ID获取失物招领详情
     * @param id 失物招领ID
     * @return 失物招领详情
     */
    LFLostItem getLostItemById(Long id);
    
    /**
     * 更新失物招领状态
     * @param id 失物招领ID
     * @param status 新状态（0-未认领，1-已认领）
     * @return 更新结果
     */
    boolean updateLostItemStatus(Long id, Integer status);
    
    /**
     * 部分认领，更新物品数量
     * @param id 失物招领ID
     * @param quantity 认领数量
     * @return 更新结果
     */
    boolean updateLostItemQuantity(Long id, Integer quantity);
    
    /**
     * 自动生成失物招领记录
     * @param roomId 教室ID
     * @param roomName 教室名称
     * @param itemType 物品类型
     * @return 生成结果
     */
    boolean autoGenerateLostItem(Long roomId, String roomName, String itemType);
    
    /**
     * 自动生成失物招领记录（支持数量参数）
     * @param roomId 教室ID
     * @param roomName 教室名称
     * @param itemType 物品类型
     * @param itemCount 物品数量
     * @return 生成结果
     */
    boolean autoGenerateLostItem(Long roomId, String roomName, String itemType, Integer itemCount);
    
    /**
     * 清空所有失物招领记录
     * @return 操作结果
     */
    boolean clearAllLostItems();
}
