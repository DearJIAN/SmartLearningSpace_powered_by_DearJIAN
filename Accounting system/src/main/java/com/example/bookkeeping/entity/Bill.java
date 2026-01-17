package com.example.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 补全引用
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat; // 新增引用

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("bill")
public class Bill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private Integer type; // 1: 收入, 2: 支出
    private Long categoryId;

    // 👇 新增注解：指定表单传入日期的格式
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    private String remark;
    
    @TableField(exist = false)
    private String categoryName;
}
