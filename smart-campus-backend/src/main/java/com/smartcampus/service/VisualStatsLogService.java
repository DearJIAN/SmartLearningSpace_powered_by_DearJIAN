package com.smartcampus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartcampus.entity.VisualStatsLog;

/**
 * 视觉数据日志Service接口
 */
public interface VisualStatsLogService extends IService<VisualStatsLog> {
    
    /**
     * 获取教室当前人数
     * @param roomId 教室ID
     * @return 当前人数
     */
    Integer getCurrentPersonCount(Long roomId);
}
