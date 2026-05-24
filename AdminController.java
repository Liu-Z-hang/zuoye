package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.OrderInfo;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.service.OrderService;
import com.example.studyroomserver.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无权限");
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        User user = userService.getByUsername(username);
        
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无管理员权限");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    }

    @GetMapping("/orders")
    public Result<List<OrderInfo>> getAllOrders(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(orderService.listAll());
    }

    @GetMapping("/revenue")
    public Result<Map<String, Object>> getRevenueStatistics(HttpServletRequest request,
                                                            @RequestParam(required = false) String date) {
        checkAdmin(request);
        return Result.success(orderService.getRevenueStatistics(date));
    }

    @GetMapping("/seat-ranking")
    public Result<Map<String, Object>> getSeatUsageRanking(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(orderService.getSeatUsageRanking());
    }

    @GetMapping("/user-ranking")
    public Result<Map<String, Object>> getUserConsumptionRanking(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(orderService.getUserConsumptionRanking());
    }
}