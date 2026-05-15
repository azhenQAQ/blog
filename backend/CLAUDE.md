# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目简介

基于 **Spring Boot 3.5 + Java 21 + MySQL 5.7** 的个人博客后端项目。Maven 构建，MySQL 存储，MyBatis-Plus ORM，Sa-Token 认证授权。

## 配置文件

| 文件 | 用途 |
|------|------|
| `application.yml` | 公共配置（默认激活 `local` profile） |
| `application-local.yml` | 本地开发配置 |
| `application-prod.yml` | 生产配置 |

## API 约定

- 基础路径：`http://localhost:8123/api`
- 统一响应格式：`BaseResponse<T>`（code / data / message）
- 统一错误码：`ErrorCode` 枚举
- 业务异常：`BusinessException` → `GlobalExceptionHandler` 统一处理
- API 文档：Knife4j，启动后访问 `/api/doc.html`

## 禁止事项

- **禁止**主动执行 Maven 构建/编译/打包命令
- **禁止**新增、修改或执行测试用例
- 以上操作仅在用户明确要求时才可执行

## 目录结构

```
src/main/java/com/blog/backend/
├── AppApplication.java           # 主入口
├── annotation/                   # 自定义注解（AuthCheck）
├── aop/                          # AOP 切面（权限校验拦截器）
├── common/                       # 通用响应包装与工具（BaseResponse, ResultUtils）
├── config/                       # 配置（MyBatis-Plus 分页, Sa-Token 权限接口）
├── constant/                     # 常量定义
├── controller/                   # REST 控制器
├── exception/                    # 异常处理（BusinessException, ErrorCode, GlobalExceptionHandler）
├── mapper/                       # MyBatis-Plus Mapper
├── model/
│   ├── dto/                      # 请求 DTO（按业务模块分包）
│   ├── entity/                   # 数据库实体
│   ├── enums/                    # 枚举
│   └── vo/                       # 响应 VO（脱敏视图，按业务模块分包）
└── service/
    ├── *.java                    # 服务接口
    └── impl/                     # 服务实现
src/main/resources/
├── application.yml / -local.yml / -prod.yml
└── mapper/                       # MyBatis XML 映射
```

## 分层调用链

```
Controller → Service(interface) → ServiceImpl → Mapper(BaseMapper) → DB
```

## 其他
- 临时文件，如 脚本、图片等存放在 `tmp` 目录下