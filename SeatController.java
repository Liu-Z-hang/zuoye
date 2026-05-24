package com.example.studyroomserver.controller;

import com.example.studyroomserver.common.Result;
import com.example.studyroomserver.entity.Seat;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.service.SeatService;
import com.example.studyroomserver.service.UserService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final UserService userService;

    @GetMapping
    public Result<List<Seat>> listAll() {
        return Result.success(seatService.listAll());
    }

    @GetMapping("/area/{areaId}")
    public Result<List<Seat>> listByArea(@PathVariable Long areaId) {
        return Result.success(seatService.listByArea(areaId));
    }

    @GetMapping("/available")
    public Result<List<Seat>> listAvailable(@RequestParam(required = false) Long areaId) {
        return Result.success(seatService.listAvailable(areaId));
    }

    @GetMapping("/{id}")
    public Result<Seat> getById(@PathVariable Long id) {
        return Result.success(seatService.getById(id));
    }

    @PostMapping
    public Result<Seat> create(HttpServletRequest request, @RequestBody Seat seat) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        return Result.success(seatService.create(seat));
    }

    @PutMapping("/{id}")
    public Result<Seat> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Seat seat) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        seat.setId(id);
        return Result.success(seatService.update(seat));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        seatService.delete(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/status")
    public Result<Seat> updateStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam String status) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (!"ADMIN".equals(user.getRole())) {
            return Result.error(403, "无权限");
        }
        seatService.updateStatus(id, status);
        return Result.success(seatService.getById(id));
    }
}