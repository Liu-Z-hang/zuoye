package com.example.studyroomserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studyroomserver.entity.Seat;
import com.example.studyroomserver.mapper.SeatMapper;
import com.example.studyroomserver.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatMapper seatMapper;

    @Override
    public List<Seat> listAll() {
        LambdaQueryWrapper<Seat> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Seat::getAreaId).orderByAsc(Seat::getSeatNumber);
        return seatMapper.selectList(queryWrapper);
    }

    @Override
    public List<Seat> listByArea(Long areaId) {
        return seatMapper.selectByAreaId(areaId);
    }

    @Override
    public List<Seat> listAvailable(Long areaId) {
        if (areaId == null) {
            LambdaQueryWrapper<Seat> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Seat::getStatus, "AVAILABLE");
            return seatMapper.selectList(queryWrapper);
        }
        return seatMapper.selectAvailableSeats(areaId);
    }

    @Override
    public Seat getById(Long id) {
        return seatMapper.selectById(id);
    }

    @Override
    @Transactional
    public Seat create(Seat seat) {
        if (seat.getPriceFactor() == null) {
            seat.setPriceFactor(BigDecimal.ONE);
        }
        seat.setStatus("AVAILABLE");
        seatMapper.insert(seat);
        return seat;
    }

    @Override
    @Transactional
    public Seat update(Seat seat) {
        Seat existing = seatMapper.selectById(seat.getId());
        if (existing == null) {
            throw new RuntimeException("座位不存在");
        }
        seatMapper.updateById(seat);
        return seat;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        seatMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Seat seat = seatMapper.selectById(id);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        seat.setStatus(status);
        seatMapper.updateById(seat);
    }

    @Override
    public long count() {
        return seatMapper.selectCount(null);
    }
}