package com.example.studyroomserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyroomserver.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    List<OrderInfo> selectByUserId(@Param("userId") Long userId);

    List<OrderInfo> selectBySeatId(@Param("seatId") Long seatId);

    List<OrderInfo> selectOverlappingOrders(
            @Param("seatId") Long seatId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}