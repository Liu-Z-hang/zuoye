-- 共享自习室座位预约与计费管理系统数据库脚本

CREATE DATABASE IF NOT EXISTS studyroom DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE studyroom;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    phone VARCHAR(20) COMMENT '手机号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 区域表
CREATE TABLE IF NOT EXISTS area (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '区域ID',
    name VARCHAR(50) NOT NULL COMMENT '区域名称',
    description VARCHAR(200) COMMENT '区域描述',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLE' COMMENT '状态：ENABLE/DISABLE',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='区域表';

-- 座位表
CREATE TABLE IF NOT EXISTS seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    area_id BIGINT NOT NULL COMMENT '所属区域ID',
    seat_number VARCHAR(20) NOT NULL COMMENT '座位号',
    attributes TEXT COMMENT '座位属性（JSON格式）',
    price_factor DECIMAL(3, 2) NOT NULL DEFAULT 1.00 COMMENT '价格系数',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE/OCCUPIED/MAINTENANCE',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_area_id (area_id),
    INDEX idx_status (status),
    INDEX idx_seat_number (seat_number),
    FOREIGN KEY (area_id) REFERENCES area(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='座位表';

-- 定价规则表
CREATE TABLE IF NOT EXISTS pricing_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    price_per_hour DECIMAL(10, 2) NOT NULL COMMENT '每小时价格',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定价规则表';

-- 订单表
CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    start_time DATETIME NOT NULL COMMENT '预约开始时间',
    end_time DATETIME NOT NULL COMMENT '预约结束时间',
    total_price DECIMAL(10, 2) NOT NULL COMMENT '总价格',
    status VARCHAR(20) NOT NULL DEFAULT 'PAID' COMMENT '状态：PAID/USING/FINISHED/CANCELLED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    pay_time DATETIME COMMENT '支付时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 使用记录表
CREATE TABLE IF NOT EXISTS usage_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    actual_start_time DATETIME COMMENT '实际签到时间',
    actual_end_time DATETIME COMMENT '实际签退时间',
    overtime_fee DECIMAL(10, 2) DEFAULT 0.00 COMMENT '超时费用',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
    INDEX idx_order_id (order_id),
    FOREIGN KEY (order_id) REFERENCES order_info(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='使用记录表';

-- 插入测试数据

-- 插入管理员账户
INSERT INTO user (username, password, role, balance, phone) VALUES 
('admin', 'admin123', 'ADMIN', 1000.00, '13800138000');

-- 插入测试用户
INSERT INTO user (username, password, role, balance, phone) VALUES 
('student1', '123456', 'USER', 200.00, '13800138001'),
('student2', '123456', 'USER', 150.00, '13800138002');

-- 插入区域
INSERT INTO area (name, description, status) VALUES 
('A区', '安静学习区', 'ENABLE'),
('B区', '讨论区', 'ENABLE'),
('C区', '休闲区', 'ENABLE');

-- 插入座位
INSERT INTO seat (area_id, seat_number, attributes, price_factor, status) VALUES 
(1, 'A001', '{"window":true,"power":true}', 1.0, 'AVAILABLE'),
(1, 'A002', '{"window":true,"power":true}', 1.0, 'AVAILABLE'),
(1, 'A003', '{"window":false,"power":true}', 1.0, 'AVAILABLE'),
(2, 'B001', '{"window":false,"power":true}', 1.2, 'AVAILABLE'),
(2, 'B002', '{"window":false,"power":true}', 1.2, 'AVAILABLE'),
(3, 'C001', '{"window":true,"power":false}', 0.8, 'AVAILABLE'),
(3, 'C002', '{"window":true,"power":false}', 0.8, 'AVAILABLE');

-- 插入定价规则
INSERT INTO pricing_rule (start_time, end_time, price_per_hour, priority) VALUES 
('08:00:00', '12:00:00', 5.00, 1),
('12:00:00', '18:00:00', 8.00, 2),
('18:00:00', '22:00:00', 10.00, 3),
('22:00:00', '08:00:00', 3.00, 4);