package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.Area;

import java.util.List;

public interface AreaService {

    List<Area> listAll();

    Area getById(Long id);

    Area create(Area area);

    Area update(Area area);

    void delete(Long id);

    void updateStatus(Long id, String status);
}