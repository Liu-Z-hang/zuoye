package com.example.studyroomserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.studyroomserver.entity.PricingRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PricingRuleMapper extends BaseMapper<PricingRule> {

    List<PricingRule> selectOrderByPriority();
}