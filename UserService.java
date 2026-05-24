package com.example.studyroomserver.service;

import com.example.studyroomserver.entity.User;

public interface UserService {

    User register(String username, String password, String phone);

    String login(String username, String password);

    User getById(Long id);

    User getByUsername(String username);

    void recharge(Long userId, Double amount);

    void update(User user);

    void deductBalance(Long userId, Double amount);
}