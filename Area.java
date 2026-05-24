package com.example.studyroomserver.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("area")
public class Area {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String status;   // ENABLE / DISABLE
    @TableLogic
    private Integer deleted;
}