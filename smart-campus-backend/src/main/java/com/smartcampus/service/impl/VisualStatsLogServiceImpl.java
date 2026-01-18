package com.smartcampus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartcampus.entity.VisualStatsLog;
import com.smartcampus.mapper.VisualStatsLogMapper;
import com.smartcampus.service.VisualStatsLogService;
import org.springframework.stereotype.Service;

/**
 * 视觉数据日志Service实现类
 */
@Service
public class VisualStatsLogServiceImpl extends ServiceImpl<VisualStatsLogMapper, VisualStatsLog> implements VisualStatsLogService {
    
    @Override
    public Integer getCurrentPersonCount(Long roomId) {
        // 调用mapper获取最新的人数记录
        return baseMapper.selectLatestPersonCount(roomId);
    }
}
