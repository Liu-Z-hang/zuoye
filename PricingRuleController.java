package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.PricingRule;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.service.PricingRuleService;
import com.example.studyroomserver.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricing")
@RequiredArgsConstructor
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;
    private final UserService userService;

    @GetMapping
    public Result<List<PricingRule>> listAll() {
        return Result.success(pricingRuleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<PricingRule> getById(@PathVariable Long id) {
        return Result.success(pricingRuleService.getById(id));
    }

    @PostMapping
    public Result<PricingRule> create(HttpServletRequest request, @RequestBody PricingRule rule) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        return Result.success(pricingRuleService.create(rule));
    }

    @PutMapping("/{id}")
    public Result<PricingRule> update(HttpServletRequest request, @PathVariable Long id, @RequestBody PricingRule rule) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        rule.setId(id);
        return Result.success(pricingRuleService.update(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        pricingRuleService.delete(id);
        return Result.success(null);
    }
}