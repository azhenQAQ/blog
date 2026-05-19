# 个人博客（http://blog.azhenna.top）

`本仓库99%的内容都是由AI生成，主要用于练习、提升自己的 vibe coding 能力`
`基于 **Spring Boot 3.5 + Vue 3 + MySQL** 的全栈个人博客系统，前后端分离部署`。

## 技术栈

| 层     | 技术                                                        | 入口                   |
| ------ | ----------------------------------------------------------- | ---------------------- |
| 后端   | Spring Boot 3.5 + Java 21 + Maven + MyBatis-Plus + Sa-Token | [backend/](backend/)   |
| 前端   | Vue 3 (Composition API) + TypeScript 6 + Vite 8 + Pinia 3   | [frontend/](frontend/) |
| 数据库 | MySQL                                                       | `backend/sql/`         |

## 环境要求

| 依赖    | 版本                      | 说明                                               |
| ------- | ------------------------- | -------------------------------------------------- |
| JDK     | 21                        | [pom.xml](backend/pom.xml) `<java.version>`        |
| Maven   | 3.6+                      | Spring Boot 3.5 构建                               |
| Node.js | `^20.19.0` 或 `>=22.12.0` | 见 [package.json](frontend/package.json) `engines` |
| MySQL   | 5.7+                      | 本地默认端口 3306                                  |

## 快速开始

### 1. 克隆项目

```bash
git clone git@github.com:azhenQAQ/blog.git blog
cd blog
```

### 2. 初始化数据库

登录 MySQL，创建数据库并执行建表脚本：

`backend\sql\create.sql`，`backend\sql\seed-data.sql`

> 脚本包含 7 张核心表（用户、文章、标签、评论等）和 20 条预置标签数据。

### 3. 配置数据库连接

编辑 [backend/src/main/resources/application-local.yml](backend/src/main/resources/application-local.yml)，修改数据库账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog
    username: root # 改为你的 MySQL 用户名
    password: 123456 # 改为你的 MySQL 密码
```

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动于 <http://localhost:8123/api>，API 文档访问 <http://localhost:8123/api/doc.html>。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问 <http://localhost:5173>，`/api` 请求自动代理到后端。

### 6. 初始用户

访问 <http://localhost:5173/login>，进行注册，注册后在数据库手动将 `user` 表的 `role` 字段改为 `admin`

## 项目结构

```text
blog/
├── backend/                     # Spring Boot 后端
│   ├── pom.xml                  # Maven 配置
│   ├── sql/
│   │   ├── create.sql           # 建表脚本
│   │   └── seed-data.sql        # 基础数据
│   └── src/main/
│       ├── java/com/blog/backend/
│       │   ├── AppApplication.java    # 启动类
│       │   ├── controller/            # REST 控制器
│       │   ├── service/               # 业务逻辑层
│       │   ├── mapper/                # MyBatis-Plus 映射
│       │   ├── model/                 # dto/entity/vo
│       │   └── config/                # 配置类
│       └── resources/
│           ├── application.yml        # 公共配置
│           ├── application-local.yml  # 本地开发配置
│           └── application-prod.yml   # 生产配置
├── frontend/                    # Vue 3 前端
│   ├── vite.config.ts           # Vite 配置（含代理）
│   └── src/
│       ├── main.ts              # 入口
│       ├── router/              # 路由定义
│       ├── pages/               # 页面组件（blog/ + admin/）
│       ├── layouts/             # 布局组件
│       ├── stores/              # Pinia 状态管理
│       └── styles/              # 全局样式 & 主题
├── deploy.sh                    # 后端生产部署脚本
└── deploy-frontend.sh           # 前端生产部署脚本
```

## 配置说明

| 配置项       | 本地开发         |
| ------------ | ---------------- |
| 后端端口     | 8123             |
| Context Path | `/api`           |
| 数据库用户   | root             |
| API 文档     | `/api/doc.html`  |
| 前端端口     | 5173 (Vite 默认) |

## 其他

- 后端统一响应格式 `BaseResponse<T>`（code=0 成功），错误通过 `BusinessException` 抛出
- `@AuthCheck(mustRole = "admin")` 注解校验管理员权限
- 前端双路由体系：`blogRoutes`（博客前台）+ `adminRoutes`（管理后台）
- 管理后台基于 Element Plus，博客前端零组件库依赖
