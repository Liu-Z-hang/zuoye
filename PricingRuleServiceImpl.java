package com.example.studyroomserver.service.impl;

import com.example.studyroomserver.entity.PricingRule;
import com.example.studyroomserver.mapper.PricingRuleMapper;
import com.example.studyroomserver.service.PricingRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingRuleServiceImpl implements PricingRuleService {

    private final PricingRuleMapper pricingRuleMapper;

    @Override
    public List<PricingRule> listAll() {
        return pricingRuleMapper.selectOrderByPriority();
    }

    @Override
    public PricingRule getById(Long id) {
        return pricingRuleMapper.selectById(id);
    }

    @Override
    @Transactional
    public PricingRule create(PricingRule rule) {
        pricingRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public PricingRule update(PricingRule rule) {
        PricingRule existing = pricingRuleMapper.selectById(rule.getId());
        if (existing == null) {
            throw new RuntimeException("定价规则不存在");
        }
        pricingRuleMapper.updateById(rule);
        return rule;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PricingRule rule = pricingRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("定价规则不存在");
        }
        pricingRuleMapper.deleteById(id);
    }

    @Override
    public BigDecimal calculatePrice(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("开始时间不能晚于结束时间");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDateTime currentTime = startTime;

        while (currentTime.isBefore(endTime)) {
            PricingRule rule = matchRule(currentTime);
            if (rule == null) {
                throw new RuntimeException("未找到匹配的定价规则");
            }

            LocalDateTime ruleEndTime = currentTime.toLocalDate().atTime(rule.getEndTime());
            if (ruleEndTime.isAfter(endTime)) {
                ruleEndTime = endTime;
            }

            long minutes = Duration.between(currentTime, ruleEndTime).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.CEILING);
            BigDecimal segmentPrice = hours.multiply(rule.getPricePerHour());
            totalPrice = totalPrice.add(segmentPrice);

            currentTime = ruleEndTime;
        }

        return totalPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingRule matchRule(LocalDateTime time) {
        LocalTime currentTime = time.toLocalTime();
        List<PricingRule> rules = pricingRuleMapper.selectOrderByPriority();

        for (PricingRule rule : rules) {
            LocalTime ruleStart = rule.getStartTime();
            LocalTime ruleEnd = rule.getEndTime();

            if (ruleStart.isBefore(ruleEnd)) {
                if (!currentTime.isBefore(ruleStart) && currentTime.isBefore(ruleEnd)) {
                    return rule;
                }
            } else {
                if (!currentTime.isBefore(ruleStart) || currentTime.isBefore(ruleEnd)) {
                    return rule;
                }
            }
        }
        return null;
    }
}