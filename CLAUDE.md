# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

个人博客全栈项目，前后端分离部署。

| 层           | 技术栈                                                                   | 入口/配置                                                    | 专属指南             |
| ------------ | ------------------------------------------------------------------------ | ------------------------------------------------------------ | -------------------- |
| **backend**  | Spring Boot 3.5 + Java 21 + Maven + MySQL + MyBatis-Plus + Sa-Token      | `backend/src/main/java/com/blog/backend/AppApplication.java` | `backend/CLAUDE.md`  |
| **frontend** | Vue 3 (Composition API) + TypeScript 6 + Vite 8 + Pinia 3 + Vue Router 5 | `frontend/src/main.ts`、`frontend/vite.config.ts`            | `frontend/CLAUDE.md` |

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

## 数据库变更规则

- **已有表的字段变更（新增/修改）必须使用 `ALTER TABLE` 语句追加在 `create.sql` 文件末尾**，禁止修改原有的 `CREATE TABLE` 语句

## 禁止事项

- **禁止**主动执行构建/编译/打包命令（Maven、npm run build 等）
- **禁止**主动执行 `npm install` 安装依赖
- **禁止**主动新增、修改、执行测试用例
- 以上操作仅在用户明确要求时方可执行

## Agent Teams 协作

当任务同时跨越前后端或涉及多个独立子步骤时，优先使用 Agent Teams 并行分工，减少串行等待：

- `TeamCreate` 创建团队 → `Agent` 工具 spawn 子代理（如前后端各分派一个，或搜索/实现分离）
- `TaskCreate` / `TaskUpdate` 分配并追踪子任务进度
- 完工后 `TeamDelete` 清理团队资源

适用场景：前后端并行开发、跨模块重构、多文件批量修改、独立子任务可并行执行等。

## 其他

- 临时文件存放于各子项目的 `tmp` 目录
```
