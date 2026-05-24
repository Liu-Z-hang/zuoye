package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.Area;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.service.AreaService;
import com.example.studyroomserver.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/area")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;
    private final UserService userService;

    @GetMapping
    public Result<List<Area>> listAll() {
        return Result.success(areaService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Area> getById(@PathVariable Long id) {
        return Result.success(areaService.getById(id));
    }

    @PostMapping
    public Result<Area> create(HttpServletRequest request, @RequestBody Area area) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        return Result.success(areaService.create(area));
    }

    @PutMapping("/{id}")
    public Result<Area> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Area area) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        area.setId(id);
        return Result.success(areaService.update(area));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        areaService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<Area> updateStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam String status) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        areaService.updateStatus(id, status);
        return Result.success(areaService.getById(id));
    }
}