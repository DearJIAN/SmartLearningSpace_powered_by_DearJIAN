package com.smartcampus.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.smartcampus.common.Result;
import com.smartcampus.entity.LFLostItem;
import com.smartcampus.service.LFLostItemService;
import com.smartcampus.service.SysClassroomService;
import com.smartcampus.service.VisualStatsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 失物招领Controller
 */
@RestController
@RequestMapping("/api/lost-found")
@CrossOrigin // 确保跨域请求正常
public class LFLostItemController {
    
    @Autowired
    private LFLostItemService lostItemService;
    
    @Autowired
    private VisualStatsLogService visualStatsLogService;
    
    @Autowired
    private SysClassroomService classroomService;
    
    /**
     * 获取失物招领列表（支持分页）
     * @param roomId 教室ID
     * @param itemType 物品类型
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 当前页码（默认1）
     * @param pageSize 每页记录数（默认15）
     * @return 失物招领列表
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getLostItemList(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer pageSize) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("itemType", itemType);
        params.put("status", status);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("page", page);
        params.put("pageSize", pageSize);
        
        List<LFLostItem> lostItemList = lostItemService.getLostItemList(params);
        // 计算总记录数（不考虑分页）
        Map<String, Object> countParams = new HashMap<>(params);
        countParams.remove("page");
        countParams.remove("pageSize");
        long total = lostItemService.getLostItemList(countParams).size();
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", lostItemList);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        
        return Result.success(result);
    }
    
    /**
     * 构建查询条件
     * @param params 查询参数
     * @return QueryWrapper
     */
    private com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LFLostItem> getQueryWrapper(Map<String, Object> params) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LFLostItem> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
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
        
        return queryWrapper;
    }
    
    /**
     * 获取失物招领详情
     * @param id 失物招领ID
     * @return 失物招领详情
     */
    @GetMapping("/{id}")
    public Result<LFLostItem> getLostItemById(@PathVariable Long id) {
        LFLostItem lostItem = lostItemService.getLostItemById(id);
        if (lostItem != null) {
            return Result.success(lostItem);
        }
        return Result.error("失物招领记录不存在");
    }
    
    /**
     * 更新失物招领状态
     * @param id 失物招领ID
     * @param status 新状态（0-未认领，1-已认领）
     * @param quantity 认领数量，默认全部
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<String> updateLostItemStatus(@PathVariable Long id, @RequestParam Integer status, @RequestParam(required = false) Integer quantity) {
        if (status != 0 && status != 1) {
            return Result.error("无效的状态值，只能是0或1");
        }
        
        boolean updated;
        if (status == 1 && quantity != null && quantity > 0) {
            // 部分认领，更新数量
            updated = lostItemService.updateLostItemQuantity(id, quantity);
        } else {
            // 全部认领，更新状态
            updated = lostItemService.updateLostItemStatus(id, status);
        }
        
        if (updated) {
            return Result.success("状态更新成功");
        }
        return Result.error("状态更新失败，记录不存在");
    }
    
    /**
     * 接收YOLO检测结果并处理
     * 核心业务逻辑：接收YOLO检测结果，生成或更新失物招领记录
     * @param detectionResult YOLO检测结果
     * @return 处理结果
     */
    @PostMapping("/yolo-detection")
    public Result<String> receiveYoloDetection(@RequestBody YoloDetectionResult detectionResult) {
        try {
            // 获取教室名称
            com.smartcampus.entity.SysClassroom classroom = classroomService.getById(detectionResult.getRoomId());
            if (classroom == null) {
                return Result.error("教室不存在");
            }
            String roomName = classroom.getRoomName();
            
            // 生成或更新失物招领记录（支持数量）
            boolean generated = lostItemService.autoGenerateLostItem(
                    detectionResult.getRoomId(),
                    roomName,
                    detectionResult.getItemType(),
                    detectionResult.getItemCount()
            );
            
            if (generated) {
                return Result.success("已生成或更新失物招领记录");
            } else {
                return Result.error("生成失物招领记录失败");
            }
        } catch (Exception e) {
            return Result.error("处理YOLO检测结果失败：" + e.getMessage());
        }
    }
    
    /**
     * 模拟生成失物招领记录（用于测试）
     * @return 操作结果
     */
    @PostMapping("/generate-test")
    public Result<String> generateTestLostItem() {
        // 模拟生成一条失物招领记录
        boolean generated = lostItemService.autoGenerateLostItem(
                101L, // 教室ID
                "101教室", // 教室名称
                "书包" // 物品类型
        );
        
        if (generated) {
            return Result.success("测试失物招领记录生成成功");
        } else {
            return Result.error("测试失物招领记录生成失败");
        }
    }
    
    /**
     * 清空所有失物招领记录
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllLostItems() {
        boolean cleared = lostItemService.clearAllLostItems();
        if (cleared) {
            return Result.success("所有失物招领记录已清空");
        } else {
            return Result.error("清空失物招领记录失败");
        }
    }
    
    /**
     * YOLO检测结果DTO
     */
    private static class YoloDetectionResult {
        private Long roomId;
        private String itemType;
        private Integer itemCount;
        private Double confidence;
        private Long timestamp;
        
        // getter and setter
        public Long getRoomId() {
            return roomId;
        }
        
        public void setRoomId(Long roomId) {
            this.roomId = roomId;
        }
        
        public String getItemType() {
            return itemType;
        }
        
        public void setItemType(String itemType) {
            this.itemType = itemType;
        }
        
        public Integer getItemCount() {
            return itemCount;
        }
        
        public void setItemCount(Integer itemCount) {
            this.itemCount = itemCount;
        }
        
        public Double getConfidence() {
            return confidence;
        }
        
        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
