package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.Seat;

import java.util.List;

public interface SeatService {

    List<Seat> listAll();

    List<Seat> listByArea(Long areaId);

    List<Seat> listAvailable(Long areaId);

    Seat getById(Long id);

    Seat create(Seat seat);

    Seat update(Seat seat);

    void delete(Long id);

    void updateStatus(Long id, String status);
    
    long count();
}