package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.UsageRecord;

import java.util.List;

public interface UsageRecordService {

    UsageRecord getById(Long id);

    List<UsageRecord> listByOrder(Long orderId);

    List<UsageRecord> listByUser(Long userId);

    UsageRecord create(UsageRecord record);

    UsageRecord update(UsageRecord record);
}