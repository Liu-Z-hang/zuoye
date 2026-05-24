package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.PricingRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PricingRuleService {

    List<PricingRule> listAll();

    PricingRule getById(Long id);

    PricingRule create(PricingRule rule);

    PricingRule update(PricingRule rule);

    void delete(Long id);

    BigDecimal calculatePrice(LocalDateTime startTime, LocalDateTime endTime);

    PricingRule matchRule(LocalDateTime time);
}