package com.smartcampus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 失物招领实体类
 * 对应数据库表：lf_lost_item
 */
@Data
@TableName("lf_lost_item")
public class LFLostItem {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 教室ID
     */
    private Long roomId;
    
    /**
     * 教室名称
     */
    private String roomName;
    
    /**
     * 物品类型（书包/水杯/雨伞/电子设备等）
     */
    private String itemType;
    
    /**
     * 物品数量
     */
    private Integer itemCount;
    
    /**
     * 发现时间
     */
    private Date foundTime;
    
    /**
     * 状态：0-未认领，1-已认领
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 更新时间
     */
    private Date updateTime;
}
