package com.example.studyroomserver.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long seatId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private BigDecimal totalPrice;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    @TableLogic
    private Integer deleted;
}