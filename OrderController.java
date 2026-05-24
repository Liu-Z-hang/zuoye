package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.OrderInfo;
import com.example.studyroomserver.service.OrderService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderInfo> create(HttpServletRequest request,
                                    @RequestParam Long seatId,
                                    @RequestParam String startTime,
                                    @RequestParam String endTime) {
        Long userId = (Long) request.getAttribute("userId");
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        OrderInfo order = orderService.createOrder(userId, seatId, start, end);
        return Result.success(order);
    }

    @GetMapping("/{id}")
    public Result<OrderInfo> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @GetMapping("/my")
    public Result<List<OrderInfo>> getMyOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(orderService.listByUser(userId));
    }

    @PostMapping("/{id}/cancel")
    public Result<OrderInfo> cancel(@PathVariable Long id) {
        return Result.success(orderService.cancelOrder(id));
    }

    @PostMapping("/{id}/checkin")
    public Result<OrderInfo> checkIn(@PathVariable Long id) {
        return Result.success(orderService.checkIn(id));
    }

    @PostMapping("/{id}/checkout")
    public Result<OrderInfo> checkOut(@PathVariable Long id) {
        return Result.success(orderService.checkOut(id));
    }

    @GetMapping("/price")
    public Result<Map<String, Object>> calculatePrice(@RequestParam Long seatId,
                                                      @RequestParam String startTime,
                                                      @RequestParam String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        BigDecimal price = orderService.calculatePrice(seatId, start, end);
        
        Map<String, Object> result = new HashMap<>();
        result.put("price", price);
        result.put("seatId", seatId);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        
        return Result.success(result);
    }
}