# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 技术栈

- **框架**: Vue 3 (Composition API + `<script setup>`)
- **语言**: TypeScript (~6.0)
- **构建**: Vite 8
- **路由**: Vue Router 5 (createWebHistory)
- **状态管理**: Pinia 3
- **UI 组件库**: Element Plus（仅限 admin 端使用，blog 端禁止引入）
- **代码规范**: oxlint + ESLint + Prettier

## 项目架构

```text
src/
├── main.ts              # 入口：初始化 Pinia/Router，加载主题
├── App.vue              # 根组件
├── router/              # 路由定义（blogRoutes + adminRoutes 合并）
├── layouts/             # 布局组件
├── components/          # 可复用组件（按域分组，如 blog/）
├── pages/               # 页面组件（按域分组，blog/ + admin/）
├── stores/              # Pinia 状态管理
└── styles/              # 全局样式
```

## 架构要点

### 路由结构

路由分为两个模块，在 `src/router/index.ts` 合并：

- **blogRoutes**: 位于 BlogLayout 的子路由下，共享导航栏和侧边栏布局
- **adminRoutes**: 独立路由，直接加载管理页面

### 主题系统

项目存在两套独立的主题体系，**禁止混用**：

- **blog 博客端**：基于 `theme.css` 中的自定义 CSS 变量，通过 `data-theme` 属性在 `light`/`dark` 间切换
  - 入口初始化时检测系统偏好
  - NavBar 提供切换按钮 + 监听系统主题变化
  - `theme.css` 定义所有博客专用主题变量（页面背景、卡片、文字、边框等）
  - 完整设计系统文档见 [docs/blog-design-system.md](docs/blog-design-system.md)（布局、色彩、字体、组件风格、动效规范）
- **admin 管理端**：使用 Element Plus 内置 CSS 变量体系（`--el-*`），暗色模式由 Element Plus 自动适配

### 命名规范

- 路由 name 使用中文（如 `'主页'`, `'文章详情'`）
- 组件使用 PascalCase 文件名
- 页面文件使用 PascalCase 并以 `Page` 后缀
- `<script setup lang="ts">` 是标准写法
- 路径别名 `@` → `./src`

### 布局规范

- **blog 端页面禁止单独设置 `max-width`**，内容宽度由 BlogLayout 的 `.main-container` 统一控制（桌面端 `max-width: var(--content-max-width)`，移动端自适应）
- 页面组件只需关注自身内容的排列，无需包裹多余的宽度容器

### 代码风格

- 无分号（semi: false）
- 单引号（singleQuote: true）
- 每行最大 100 字符（printWidth: 100）
- 双 lint 系统：oxlint 快速检查 + ESLint Vue/TS 规则

### 响应式设计（仅限 blog 博客端 — admin 端无需考虑）

- **blog 端**所有 UI 改动需确保桌面端和移动端（≤900px）均正常显示
- 参考 BlogLayout 中的 `@media (max-width: 900px)` 断点，为改动补充对应响应式样式
- 优先使用弹性布局（flex/grid）和相对单位，避免固定宽高
- 移动端下侧边栏应变为 `position: static` 并置于内容下方（参考已有模式）
- **admin 管理端禁止编写响应式适配代码**（如 `@media`、`isMobile`、`resize` 监听等），Element Plus 组件自带桌面端适配

### Element Plus 组件库使用规范

- **admin 管理端**：优先使用 Element Plus 组件开发，减少重复造轮子
- **blog 博客端**：**严格禁止**引入 Element Plus，保持博客端零组件库依赖，样式完全自控
- 全局已按需引入，开发时直接在组件中使用即可，无需单独 import
- **admin 端无需自行实现暗色模式切换**，Element Plus 组件自带日间/夜间配色，直接使用其内置 CSS 变量（`--el-*`）即可自动适配
- **admin 端禁止编写响应式代码**（`@media`、`resize` 监听、`isMobile` 判断等），Element Plus 组件自身已处理桌面端适配

### CSS 变量使用规范（两套体系，泾渭分明）

| 端 | 变量体系 | 来源文件 | 示例 |
| --- | --- | --- | --- |
| **blog 博客端** | 自定义 CSS 变量 | `src/styles/theme.css` | `var(--card-bg)`, `var(--text-main)`, `var(--accent)` |
| **admin 管理端** | Element Plus 内置变量 | Element Plus 全局样式 | `var(--el-bg-color-overlay)`, `var(--el-text-color-secondary)`, `var(--el-border-color-light)` |

- admin 端**禁止**引用 `theme.css` / `form.css` 中的自定义变量，两套变量互不混用
- admin 端暗色模式由 Element Plus 自身的 CSS 变量体系自动适配，无需额外配置

### 奇思妙想模块规范

- **路由**：所有工具必须注册为顶层独立路由（在 `router/index.ts` 中，非 BlogLayout 子路由），以新标签页/独立页面形式打开
- **样式**：所有工具页面的样式必须写在对应 `.vue` 文件的 `<style scoped>` 内部，禁止引入外部样式文件
- 工具页面需自包含完整布局（Header + 内容区），参考 `ImageProcessPage.vue` 的模式

## 禁止事项

- 禁止执行 `npm run dev`、`npm run build`、`npm run lint` 等 npm 脚本命令，这些由用户手动执行
- 禁止执行 `npm install` 安装依赖（除非用户明确要求）
