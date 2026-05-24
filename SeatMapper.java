package com.example.studyroomserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyroomserver.entity.Seat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeatMapper extends BaseMapper<Seat> {

    List<Seat> selectByAreaId(@Param("areaId") Long areaId);

    List<Seat> selectAvailableSeats(@Param("areaId") Long areaId);
}