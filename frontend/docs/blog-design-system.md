# 博客端设计系统

> 风格方向：**新粗野主义印刷风 (Neo-Brutalist Print)**
> 版本：v1，最后更新：2026-05-18

## 布局架构

```
BlogLayout
├── ParticleBg          # 固定 Canvas 背景（几何印刷块漂浮）
├── NavBar              # 固定顶部导航（60px 高）
├── .main-container     # flex 容器（max-width: 1350px）
│   ├── .content-area   # 左侧内容区（flex: 1）
│   │   └── <router-view />
│   └── .sidebar-area   # 右侧边栏（26%, 260px-290px）
│       └── SideBar
└── FloatingToolbar     # 右下角浮动按钮组（主题切换 + 回到顶部）
```

- 内容宽度由 BlogLayout 的 `.main-container` 统一控制，页面组件**禁止**自行设置 `max-width`
- 移动端（≤900px）侧边栏置于内容下方 `position: static`
- 侧边栏桌面端使用 `position: sticky; top: var(--nav-height)`

## 色彩系统

### 设计理念

印刷品感的硬派配色：暖白新闻纸 × 纯黑油墨 × 警示橙 × 明黄荧光笔。

### CSS 变量一览

| 变量 | 用途 | light 值 | dark 值 |
|---|---|---|---|
| `--page-bg` | 页面底色 | `#f4f0e6` | `#111111` |
| `--card-bg` | 卡片背景 | `#ffffff` | `#1a1a1a` |
| `--shadow-color` | 阴影/边框色 | `#1a1a1a` | `#555` |
| `--text-main` | 正文 | `#2a2a2a` | `#d0d0d0` |
| `--text-strong` | 标题/强调 | `#000000` | `#ffffff` |
| `--text-muted` | 辅助文字 | `#777` | `#999` |
| `--accent` | 主强调色 | `#ff5722` | `#ff5722` |
| `--accent-yellow` | 高亮色（标签/标题底） | `#ffd600` | `#ffd600` |
| `--accent-secondary` | 辅强调色 | `#00bcd4` | `#00bcd4` |
| `--border-color` | 通用边框 | `#1a1a1a` | `#555` |

### 硬阴影与边框

```
--radius-card: 0px           # 全直角，无圆角
--card-border: 3px solid #1a1a1a   # 粗边框
--card-shadow: 6px 6px 0 #1a1a1a  # 硬阴影（无模糊）
--card-hover-shadow: 8px 8px 0 #1a1a1a
```

## 字体系统

| 用途 | 字体栈 | 加载方式 |
|---|---|---|
| 标题/导航 | `Oswald, Noto Sans SC, PingFang SC, Microsoft YaHei, sans-serif` | `@fontsource/oswald` (npm) |
| 正文 | `Inter, PingFang SC, Microsoft YaHei, sans-serif` | `@fontsource/inter` (npm) |
| 代码 | `JetBrains Mono, Fira Code, Consolas, monospace` | 系统自带 |

- Oswald 仅支持拉丁字符，中文字符自动回落至 `Noto Sans SC` / `PingFang SC`
- 标题统一 `text-transform: uppercase` + `letter-spacing: 0.02em~0.06em`
- 正文 `line-height: 1.8`，字号 `16px`

## 组件风格约定

### 卡片

- 白底/暗底 + 3px 黑边框 + 6px 6px 0 硬阴影
- hover 时 `translate(-2px, -2px)` + 阴影加大
- 无圆角，无渐变

### 按钮

- 主按钮：黑底白字 + 黄硬阴影 `4px 4px 0 var(--accent-yellow)`
- hover 时阴影缩小 + `translate(2px, 2px)`（按入感）
- 字体 Oswald、大写

### 标签

- 黄底黑字 `var(--accent-yellow)` + `#1a1a1a`
- hover 变为橙底白字 `var(--accent)`
- 无圆角

### 表单输入

- 3px 实线边框 `var(--shadow-color)`
- focus 时边框变色 `var(--accent)`
- 无圆角，无内阴影

### 分割线

- 3px 实线 `var(--shadow-color)`（替代原 1px 细线）

## 动效规范

- **transition**: `0.1s~0.15s ease`（快速、干脆）
- **hover 卡片**: `translate(-2px, -2px)` 偏移 + 硬阴影变化
- **hover 按钮**: `translate(2px, 2px)` 按入感
- 不使用缓动曲线，不使用模糊过渡

## 粒子背景

`ParticleBg.vue` — Canvas 绘制慢速漂浮的几何图形块（方块、圆、十字），模拟印刷车间散落的铅字块：

- light 模式：`#1a1a1a` 填充 + `#ff5722` 描边
- dark 模式：`#ffd600` 填充 + `#ff5722` 描边
- 透明度 0.03~0.09，极低调

## 响应式断点

- **唯一断点**: `@media (max-width: 900px)`
- 导航栏变汉堡菜单
- 内容/侧边栏变为纵向排列
- 卡片封面图缩小、取消左右交替布局
- 字号等比缩小

## 主题切换

- `data-theme="light|dark"` 挂载于 `<html>` 元素
- 首次加载检测 `prefers-color-scheme` 系统偏好
- FloatingToolbar 提供切换按钮
- 所有组件通过 CSS 变量自动适配，无需 JS 判断
