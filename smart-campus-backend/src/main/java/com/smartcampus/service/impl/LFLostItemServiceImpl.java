package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.LFLostItem;
import com.smartcampus.mapper.LFLostItemMapper;
import com.smartcampus.service.LFLostItemService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 失物招领Service实现类
 */
@Service
public class LFLostItemServiceImpl extends ServiceImpl<LFLostItemMapper, LFLostItem> implements LFLostItemService {
    
    @Override
    public List<LFLostItem> getLostItemList(Map<String, Object> params) {
        // 创建查询条件
        QueryWrapper<LFLostItem> queryWrapper = new QueryWrapper<>();
        
        // 根据参数构建查询条件
        if (params.containsKey("roomId") && params.get("roomId") != null) {
            queryWrapper.eq("room_id", params.get("roomId"));
        }
        
        if (params.containsKey("itemType") && params.get("itemType") != null) {
            queryWrapper.eq("item_type", params.get("itemType"));
        }
        
        if (params.containsKey("status") && params.get("status") != null) {
            queryWrapper.eq("status", params.get("status"));
        }
        
        if (params.containsKey("startTime") && params.get("startTime") != null) {
            queryWrapper.ge("found_time", params.get("startTime"));
        }
        
        if (params.containsKey("endTime") && params.get("endTime") != null) {
            queryWrapper.le("found_time", params.get("endTime"));
        }
        
        // 按发现时间倒序排列
        queryWrapper.orderByDesc("found_time");
        
        // 处理分页
        if (params.containsKey("page") && params.containsKey("pageSize")) {
            Integer page = (Integer) params.get("page");
            Integer pageSize = (Integer) params.get("pageSize");
            Integer offset = (page - 1) * pageSize;
            return list(queryWrapper.last("LIMIT " + offset + ", " + pageSize));
        }
        
        // 不分页，返回所有记录
        return list(queryWrapper);
    }
    
    @Override
    public LFLostItem getLostItemById(Long id) {
        return baseMapper.selectById(id);
    }
    
    @Override
    public boolean updateLostItemStatus(Long id, Integer status) {
        LFLostItem lostItem = new LFLostItem();
        lostItem.setId(id);
        lostItem.setStatus(status);
        return updateById(lostItem);
    }
    
    @Override
    public boolean updateLostItemQuantity(Long id, Integer quantity) {
        // 先查询当前记录
        LFLostItem lostItem = getById(id);
        if (lostItem == null) {
            return false;
        }
        
        // 计算剩余数量
        int remaining = lostItem.getItemCount() - quantity;
        if (remaining < 0) {
            return false; // 认领数量不能大于当前数量
        }
        
        if (remaining == 0) {
            // 如果全部认领，直接更新状态为已认领
            lostItem.setStatus(1);
            lostItem.setItemCount(0);
        } else {
            // 部分认领，更新剩余数量，状态仍为未认领
            lostItem.setItemCount(remaining);
        }
        
        return updateById(lostItem);
    }
    
    @Override
    public boolean autoGenerateLostItem(Long roomId, String roomName, String itemType) {
        return autoGenerateLostItem(roomId, roomName, itemType, 1);
    }
    
    /**
     * 自动生成失物招领记录（支持数量参数）
     * @param roomId 教室ID
     * @param roomName 教室名称
     * @param itemType 物品类型
     * @param itemCount 物品数量
     * @return 是否生成成功
     */
    public boolean autoGenerateLostItem(Long roomId, String roomName, String itemType, Integer itemCount) {
        // 查询相同教室和物品类型的未认领记录
        QueryWrapper<LFLostItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId)
                   .eq("item_type", itemType)
                   .eq("status", 0)
                   .orderByDesc("found_time");
        
        LFLostItem existingItem = getOne(queryWrapper);
        
        if (existingItem != null) {
            // 更新现有记录的数量和发现时间
            existingItem.setItemCount(itemCount);
            existingItem.setFoundTime(new Date());
            return updateById(existingItem);
        } else {
            // 创建新记录
            LFLostItem lostItem = new LFLostItem();
            lostItem.setRoomId(roomId);
            lostItem.setRoomName(roomName);
            lostItem.setItemType(itemType);
            lostItem.setItemCount(itemCount);
            lostItem.setFoundTime(new Date());
            lostItem.setStatus(0); // 初始状态：未认领
            
            return save(lostItem);
        }
    }
    
    @Override
    public boolean clearAllLostItems() {
        // 使用TRUNCATE TABLE语句清空表并重置自增ID
        baseMapper.truncateTable();
        return true;
    }
}
