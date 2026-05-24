<<<<<<< HEAD
﻿# 共享自习室座位预约与计费管理系统

本仓库包含一个 Spring Boot 后端和两个 Vue 前端，用于共享自习室座位预约、计费、签到签退和后台管理。

## 项目结构

```text
study-room-server   后端服务，包含接口、业务逻辑、数据库脚本
study-room-admin    管理员端前端，包含用户、区域、座位、订单、计费管理页面
study-room-client   用户端前端，包含登录、座位浏览、预约、订单、个人中心页面
docs                结对计划、需求分解和角色互换记录
```

## 数据库

数据库脚本位于：

```text
study-room-server/src/main/resources/schema.sql
```

导入后会创建 `studyroom` 数据库。后端数据库连接配置位于：

```text
study-room-server/src/main/resources/application.yaml
```

如本机 MySQL 用户名或密码不同，请修改其中的 `username` 和 `password`。

## 启动方式

后端：

```bash
cd study-room-server
mvn spring-boot:run
```

管理员端：

```bash
cd study-room-admin
npm install
npm run dev
```

用户端：

```bash
cd study-room-client
npm install
npm run dev
```

## 协作方式

本项目采用 `main -> develop -> feature/*` 的 Git Flow 简化流程。所有功能分支从 `develop` 拉出，通过 Pull Request 合并回 `develop`，阶段稳定后再由 `develop` 合并到 `main`。
=======
# zuoye
>>>>>>> 2071069da79d4bad9b162e556b7cb70f45b415d8
