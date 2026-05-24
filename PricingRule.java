package com.example.studyroomserver.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@TableName("pricing_rule")
public class PricingRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal pricePerHour;

    private Integer priority;

    @TableLogic
    private Integer deleted;
}