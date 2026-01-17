package com.example.bookkeeping.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("financial_goal")
public class FinancialGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal targetAmount;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;
    
    private LocalDateTime createdAt;
}
