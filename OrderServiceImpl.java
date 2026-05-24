package com.example.studyroomserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studyroomserver.entity.*;
import com.example.studyroomserver.mapper.OrderInfoMapper;
import com.example.studyroomserver.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderInfoMapper orderInfoMapper;
    private final SeatService seatService;
    private final UserService userService;
    private final PricingRuleService pricingRuleService;
    private final UsageRecordService usageRecordService;

    @Override
    @Transactional
    public OrderInfo createOrder(Long userId, Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        if (!"AVAILABLE".equals(seat.getStatus())) {
            throw new RuntimeException("座位不可用");
        }

        List<OrderInfo> overlappingOrders = orderInfoMapper.selectOverlappingOrders(seatId, startTime, endTime);
        if (!overlappingOrders.isEmpty()) {
            throw new RuntimeException("该时段座位已被预约");
        }

        BigDecimal totalPrice = calculatePrice(seatId, startTime, endTime);

        User user = userService.getById(userId);
        if (user.getBalance() < totalPrice.doubleValue()) {
            throw new RuntimeException("余额不足");
        }

        userService.deductBalance(userId, totalPrice.doubleValue());

        seatService.updateStatus(seatId, "OCCUPIED");

        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setSeatId(seatId);
        order.setStartTime(startTime);
        order.setEndTime(endTime);
        order.setTotalPrice(totalPrice);
        order.setStatus("PAID");
        order.setCreateTime(LocalDateTime.now());
        order.setPayTime(LocalDateTime.now());

        orderInfoMapper.insert(order);
        return order;
    }

    @Override
    public OrderInfo getById(Long id) {
        return orderInfoMapper.selectById(id);
    }

    @Override
    public List<OrderInfo> listByUser(Long userId) {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderInfo::getUserId, userId)
                .orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectList(queryWrapper);
    }

    @Override
    public List<OrderInfo> listBySeat(Long seatId) {
        return orderInfoMapper.selectBySeatId(seatId);
    }

    @Override
    public List<OrderInfo> listAll() {
        LambdaQueryWrapper<OrderInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(OrderInfo::getCreateTime);
        return orderInfoMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public OrderInfo cancelOrder(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus()) && !"USING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许取消");
        }

        if ("USING".equals(order.getStatus())) {
            seatService.updateStatus(order.getSeatId(), "AVAILABLE");
        }

        userService.recharge(order.getUserId(), order.getTotalPrice().doubleValue());

        order.setStatus("CANCELED");
        orderInfoMapper.updateById(order);
        return order;
    }

    @Override
    @Transactional
    public OrderInfo checkIn(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许签到");
        }

        order.setStatus("USING");
        orderInfoMapper.updateById(order);

        UsageRecord record = new UsageRecord();
        record.setOrderId(id);
        record.setActualStartTime(LocalDateTime.now());
        usageRecordService.create(record);

        return order;
    }

    @Override
    @Transactional
    public OrderInfo checkOut(Long id) {
        OrderInfo order = orderInfoMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!"USING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许签退");
        }

        User user = userService.getById(order.getUserId());

        List<UsageRecord> records = usageRecordService.listByOrder(id);
        if (records.isEmpty()) {
            throw new RuntimeException("未找到使用记录");
        }

        UsageRecord record = records.get(0);
        LocalDateTime actualEndTime = LocalDateTime.now();
        record.setActualEndTime(actualEndTime);

        long minutes = Duration.between(order.getEndTime(), actualEndTime).toMinutes();
        BigDecimal overtimeFee = BigDecimal.ZERO;

        if (minutes > 0) {
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.CEILING);
            PricingRule rule = pricingRuleService.matchRule(actualEndTime);
            if (rule != null) {
                overtimeFee = hours.multiply(rule.getPricePerHour()).setScale(2, RoundingMode.HALF_UP);
            }
            record.setOvertimeFee(overtimeFee);
            if (user.getBalance() >= overtimeFee.doubleValue()) {
                userService.deductBalance(order.getUserId(), overtimeFee.doubleValue());
            }
        }

        usageRecordService.update(record);

        order.setStatus("FINISHED");
        orderInfoMapper.updateById(order);

        seatService.updateStatus(order.getSeatId(), "AVAILABLE");

        return order;
    }

    @Override
    public BigDecimal calculatePrice(Long seatId, LocalDateTime startTime, LocalDateTime endTime) {
        Seat seat = seatService.getById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }

        BigDecimal basePrice = pricingRuleService.calculatePrice(startTime, endTime);
        BigDecimal priceFactor = seat.getPriceFactor() != null ? seat.getPriceFactor() : BigDecimal.ONE;

        return basePrice.multiply(priceFactor).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public Map<String, Object> getRevenueStatistics(String date) {
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.plusDays(1).atStartOfDay();

        LambdaQueryWrapper<OrderInfo> todayQuery = new LambdaQueryWrapper<>();
        todayQuery.ge(OrderInfo::getCreateTime, startOfDay)
                .lt(OrderInfo::getCreateTime, endOfDay)
                .ne(OrderInfo::getStatus, "CANCELLED");

        List<OrderInfo> todayOrders = orderInfoMapper.selectList(todayQuery);
        BigDecimal todayRevenue = todayOrders.stream()
                .map(OrderInfo::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<OrderInfo> allQuery = new LambdaQueryWrapper<>();
        allQuery.ne(OrderInfo::getStatus, "CANCELLED");

        List<OrderInfo> allOrders = orderInfoMapper.selectList(allQuery);
        BigDecimal totalRevenue = allOrders.stream()
                .map(OrderInfo::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalSeats = seatService.count();
        long usedSeats = allOrders.stream()
                .filter(o -> !"FINISHED".equals(o.getStatus()) && !"CANCELLED".equals(o.getStatus()))
                .map(OrderInfo::getSeatId)
                .distinct()
                .count();

        double seatUsageRate = totalSeats > 0 ? (double) usedSeats / totalSeats : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("date", queryDate.toString());
        result.put("totalRevenue", totalRevenue);
        result.put("todayRevenue", todayRevenue);
        result.put("totalOrders", allOrders.size());
        result.put("todayOrders", todayOrders.size());
        result.put("seatUsageRate", seatUsageRate);

        return result;
    }

    @Override
    public Map<String, Object> getSeatUsageRanking() {
        List<OrderInfo> orders = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .ne(OrderInfo::getStatus, "CANCELLED"));

        Map<Long, Long> seatUsageMap = new HashMap<>();
        for (OrderInfo order : orders) {
            seatUsageMap.merge(order.getSeatId(), 1L, Long::sum);
        }

        List<Map<String, Object>> ranking = new ArrayList<>();
        seatUsageMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    Seat seat = seatService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("seatId", entry.getKey());
                    item.put("seatNumber", seat != null ? seat.getSeatNumber() : "未知");
                    item.put("usageCount", entry.getValue());
                    ranking.add(item);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        return result;
    }

    @Override
    public Map<String, Object> getUserConsumptionRanking() {
        List<OrderInfo> orders = orderInfoMapper.selectList(new LambdaQueryWrapper<OrderInfo>()
                .ne(OrderInfo::getStatus, "CANCELLED"));

        Map<Long, BigDecimal> userConsumptionMap = new HashMap<>();
        for (OrderInfo order : orders) {
            userConsumptionMap.merge(order.getUserId(),
                    order.getTotalPrice(), BigDecimal::add);
        }

        List<Map<String, Object>> ranking = new ArrayList<>();
        userConsumptionMap.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    User user = userService.getById(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("userId", entry.getKey());
                    item.put("username", user != null ? user.getUsername() : "未知");
                    item.put("consumption", entry.getValue());
                    ranking.add(item);
                });

        Map<String, Object> result = new HashMap<>();
        result.put("ranking", ranking);
        return result;
    }
}