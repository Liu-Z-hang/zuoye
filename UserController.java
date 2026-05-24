package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestParam String username, @RequestParam String password, @RequestParam(required = false) String phone) {
        User user = userService.register(username, password, phone);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        User user = userService.getByUsername(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.success(result);
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @PostMapping("/recharge")
    public Result<User> recharge(HttpServletRequest request, @RequestParam Double amount) {
        Long userId = (Long) request.getAttribute("userId");
        userService.recharge(userId, amount);
        User user = userService.getById(userId);
        return Result.success(user);
    }

    @PutMapping("/update")
    public Result<User> update(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        userService.update(user);
        return Result.success(userService.getById(userId));
    }
}