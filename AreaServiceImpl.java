package com.example.studyroomserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.studyroomserver.entity.Area;
import com.example.studyroomserver.mapper.AreaMapper;
import com.example.studyroomserver.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaServiceImpl implements AreaService {

    private final AreaMapper areaMapper;

    @Override
    public List<Area> listAll() {
        LambdaQueryWrapper<Area> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Area::getId);
        return areaMapper.selectList(queryWrapper);
    }

    @Override
    public Area getById(Long id) {
        return areaMapper.selectById(id);
    }

    @Override
    @Transactional
    public Area create(Area area) {
        area.setStatus("ENABLE");
        areaMapper.insert(area);
        return area;
    }

    @Override
    @Transactional
    public Area update(Area area) {
        Area existing = areaMapper.selectById(area.getId());
        if (existing == null) {
            throw new RuntimeException("区域不存在");
        }
        areaMapper.updateById(area);
        return area;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Area area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        areaMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Area area = areaMapper.selectById(id);
        if (area == null) {
            throw new RuntimeException("区域不存在");
        }
        area.setStatus(status);
        areaMapper.updateById(area);
    }
}