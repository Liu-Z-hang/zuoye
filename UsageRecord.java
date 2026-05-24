package com.example.studyroomserver.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("usage_record")
public class UsageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private BigDecimal overtimeFee;
}