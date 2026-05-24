package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.OrderInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderService {

    OrderInfo createOrder(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime);

    OrderInfo getById(Long id);

    List<OrderInfo> listByUser(Long userId);

    List<OrderInfo> listBySeat(Long seatId);

    List<OrderInfo> listAll();

    OrderInfo cancelOrder(Long id);

    OrderInfo checkIn(Long id);

    OrderInfo checkOut(Long id);

    BigDecimal calculatePrice(Long seatId, LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> getRevenueStatistics(String date);

    Map<String, Object> getSeatUsageRanking();

    Map<String, Object> getUserConsumptionRanking();
}