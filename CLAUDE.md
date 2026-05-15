# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

个人博客全栈项目，前后端分离部署。

| 层 | 技术栈 | 入口/配置 | 专属指南 |
|---|---|---|---|
| **backend** | Spring Boot 3.5 + Java 21 + Maven + MySQL + MyBatis-Plus + Sa-Token | `backend/src/main/java/com/blog/backend/AppApplication.java` | `backend/CLAUDE.md` |
| **frontend** | Vue 3 (Composition API) + TypeScript 6 + Vite 8 + Pinia 3 + Vue Router 5 | `frontend/src/main.ts`、`frontend/vite.config.ts` | `frontend/CLAUDE.md` |

## 系统架构

```
前端 (Vite dev server :5173)
  ├── 博客端 (/)              → BlogLayout（导航栏 + 侧边栏 + 粒子背景）
  │   ├── /                   → 主页 (HomePage)
  │   ├── /user/login         → 登录
  │   ├── /user/register      → 注册
  │   ├── /posts              → 文章列表
  │   └── /posts/:id          → 文章详情
  ├── 管理端 (/admin/*)       → 独立页面，无公共布局
  │   ├── /admin/dashboard    → 控制台
  │   ├── /admin/userManage   → 用户管理
  │   ├── /admin/posts        → 文章管理
  │   └── /admin/comments     → 评论管理
  │
  └── API 请求 ─────────────────→ 后端 (Spring Boot :8123/api)
                                    ├── Controller → Service(interface) → ServiceImpl → Mapper → DB
                                    ├── 认证：Sa-Token（StpUtil）
                                    ├── 权限：@AuthCheck 注解 + AuthInterceptor AOP 切面
                                    └── 响应：BaseResponse<T>（code=0 成功，其他见 ErrorCode）
```

## 数据库

MySQL 数据库名 `blog`，建表脚本 `backend/sql/create.sql`，共 5 张表：

| 表 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户表 | id, username, password, nickname, role(admin/user), deleted_at |
| `article` | 文章表 | id, title, content(Markdown), status(1发布/0草稿), user_id(FK→user) |
| `tag` | 标签表 | id, name, color, deleted_at |
| `article_tag` | 文章标签多对多关联 | article_id, tag_id (联合唯一) |
| `friend_link` | 友链表 | id, name, url, logo, sort_order, status |

所有表使用 MyBatis-Plus 逻辑删除（`deleted_at`），配置了驼峰命名自动映射。

## 运行命令

以下为手动命令，仅用户明确要求时执行：

**后端：**
```sh
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local  # 启动（本地开发）
./mvnw clean compile    # 编译
./mvnw clean package -DskipTests  # 打包
```

**前端：**
```sh
cd frontend
npm run dev      # 启动开发服务器
npm run build    # 生产构建（含类型检查）
npm run lint     # oxlint + ESLint 双检查
```

## 配置文件一览

| 文件 | 用途 |
|---|---|
| `backend/src/main/resources/application.yml` | 公共配置，默认激活 `local` profile |
| `backend/src/main/resources/application-local.yml` | 本地开发（mysql://localhost:3306/blog） |
| `backend/src/main/resources/application-prod.yml` | 生产配置（含 Redis 连接） |
| `frontend/.prettierrc.json` | 格式化规则（无分号、单引号、100 字符宽） |
| `frontend/.oxlintrc.json` | oxlint 快速检查配置 |
| `frontend/eslint.config.ts` | ESLint Vue/TS 规则 |

## 关键设计约定

- 后端 API 基础路径：`http://localhost:8123/api`，Knife4j 文档：`/api/doc.html`
- 统一响应 `BaseResponse<T>`（code=0 成功），错误通过 `BusinessException` + `ErrorCode` 枚举抛出
- `@AuthCheck(mustRole = "admin")` 注解自动校验登录 + 管理员角色（AOP 切入）
- DTO（入参）/ Entity（数据库映射）/ VO（出参脱敏）三层分离
- 前端 CSS 主题系统：`data-theme="light|dark"` 切换，初始化时检测系统偏好
- 前端路径别名 `@` → `./src`

## 禁止事项

- **禁止**主动执行构建/编译/打包命令（Maven、npm run build 等）
- **禁止**主动执行 `npm install` 安装依赖
- **禁止**主动新增、修改、执行测试用例
- 以上操作仅在用户明确要求时方可执行

## 其他

- 临时文件存放于各子项目的 `tmp` 目录
