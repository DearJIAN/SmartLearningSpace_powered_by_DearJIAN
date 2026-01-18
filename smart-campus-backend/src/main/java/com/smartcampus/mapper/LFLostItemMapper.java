package com.smartcampus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.entity.LFLostItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 失物招领Mapper接口
 * 对应数据库表：lf_lost_item
 */
@Mapper
public interface LFLostItemMapper extends BaseMapper<LFLostItem> {
    
    /**
     * 清空表并重置自增ID
     */
    void truncateTable();
}
