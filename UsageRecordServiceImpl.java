package com.example.studyroomserver.service.impl;

import com.example.studyroomserver.entity.UsageRecord;
import com.example.studyroomserver.mapper.UsageRecordMapper;
import com.example.studyroomserver.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageRecordServiceImpl implements UsageRecordService {

    private final UsageRecordMapper usageRecordMapper;

    @Override
    public UsageRecord getById(Long id) {
        return usageRecordMapper.selectById(id);
    }

    @Override
    public List<UsageRecord> listByOrder(Long orderId) {
        return usageRecordMapper.selectByOrderId(orderId);
    }

    @Override
    public List<UsageRecord> listByUser(Long userId) {
        return usageRecordMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public UsageRecord create(UsageRecord record) {
        usageRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public UsageRecord update(UsageRecord record) {
        UsageRecord existing = usageRecordMapper.selectById(record.getId());
        if (existing == null) {
            throw new RuntimeException("使用记录不存在");
        }
        usageRecordMapper.updateById(record);
        return record;
    }
}