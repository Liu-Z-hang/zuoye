package com.example.studyroomserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyroomserver.entity.UsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UsageRecordMapper extends BaseMapper<UsageRecord> {

    List<UsageRecord> selectByOrderId(@Param("orderId") Long orderId);

    List<UsageRecord> selectByUserId(@Param("userId") Long userId);
}