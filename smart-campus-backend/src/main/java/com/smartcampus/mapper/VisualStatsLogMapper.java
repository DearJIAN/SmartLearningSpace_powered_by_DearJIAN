package com.smartcampus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartcampus.entity.VisualStatsLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 视觉数据日志Mapper接口
 */
@Mapper
public interface VisualStatsLogMapper extends BaseMapper<VisualStatsLog> {

    /**
     * 查询最近24小时按小时分组的平均专注度
     * 
     * @return 包含 time_point 和 avg_focus 的结果列表
     */
    @Select("SELECT DATE_FORMAT(create_time, '%H:00') AS time_point, " +
            "AVG(focus_index) AS avg_focus " +
            "FROM visual_stats_log " +
            "WHERE room_id = #{roomId} AND create_time >= NOW() - INTERVAL 24 HOUR " +
            "GROUP BY DATE_FORMAT(create_time, '%H:00') " +
            "ORDER BY time_point")
    List<Map<String, Object>> selectTrendLast24Hours(Long roomId);

    /**
     * 获取最新的一条记录的人数
     */
    @Select("SELECT person_count FROM visual_stats_log WHERE room_id = #{roomId} ORDER BY create_time DESC LIMIT 1")
    Integer selectLatestPersonCount(Long roomId);

    /**
     * 计算今日平均专注度 (create_time >= 当天0点)
     */
    @Select("SELECT AVG(focus_index) FROM visual_stats_log WHERE room_id = #{roomId} AND create_time >= CURDATE()")
    Double selectTodayAvgFocus(Long roomId);
}
