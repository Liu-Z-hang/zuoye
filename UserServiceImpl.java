package com.example.studyroomserver.service.impl;

import com.example.studyroomserver.common.utils.JwtUtil;
import com.example.studyroomserver.entity.User;
import com.example.studyroomserver.mapper.UserMapper;
import com.example.studyroomserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public User register(String username, String password, String phone) {
        if (userMapper.selectByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole("USER");
        user.setBalance(0.0);
        
        userMapper.insert(user);
        return user;
    }

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户名不存在");
        }
        if (!password.equals(user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        return jwtUtil.generateToken(user.getId());
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public void recharge(Long userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        user.setBalance(user.getBalance() + amount);
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deductBalance(Long userId, Double amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (user.getBalance() < amount) {
            throw new RuntimeException("余额不足");
        }
        user.setBalance(user.getBalance() - amount);
        userMapper.updateById(user);
    }
}