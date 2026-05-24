package com.example.studyroomserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("seat")
public class Seat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long areaId;

    private String seatNumber;

    private String attributes; // JSON字符串

    private BigDecimal priceFactor;

    private String status;

    @TableLogic
    private Integer deleted;
}