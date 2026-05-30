# l2d-widget 文档

> 来源: https://l2d-widget.hacxy.cn
> 抓取时间: 2026-05-30

---

## 介绍

l2d-widget 是一个轻量级的 Live2D 网页挂件库，基于 [l2d](https://github.com/hacxy/l2d) 构建。

一行代码就能在任意网页上放一个 Live2D 模型，同时带上这些交互能力：

- **悬浮菜单** — 鼠标移上去就出现操作按钮，想加自定义按钮也行
- **提示气泡** — 欢迎语、循环消息、打字动画，还能驱动嘴型
- **多模型切换** — 给个数组，用户就能在模型之间随意切
- **状态栏** — 加载进度、休眠唤醒，一目了然
- **入场动画** — 滑入或淡入，两种过渡效果

整个库大约 500 行代码，不依赖任何 UI 框架，纯原生 DOM 实现。

---

## 安装

### 包管理器

用你习惯的包管理器装就行：

```bash
# npm
npm install l2d-widget

# pnpm
pnpm add l2d-widget

# yarn
yarn add l2d-widget
```

### CDN

不想装包？直接用 `<script>` 标签引入也可以，全局变量是 `L2D_WIDGET`：

```html
<script src="https://unpkg.com/l2d-widget/dist/index.min.js"></script>
<script>
  L2D_WIDGET.createWidget({
    model: {
      path: 'https://model.hacxy.cn/cat-black/model.json',
    },
  })
</script>
```

---

## 快速开始

### 最小示例

```ts
import { createWidget } from 'l2d-widget';

const widget = createWidget({
  model: {
    path: 'https://model.hacxy.cn/cat-black/model.json',
  },
});
```

就这几行，页面左下角就会出现一个 Live2D 模型，默认配置全部就绪。

### 背后发生了什么？

1. 往页面里插了一个固定定位的 canvas
2. 从指定路径加载 Live2D 模型并渲染
3. 加载完成后模型滑入登场
4. 鼠标悬浮到模型上，右侧弹出操作菜单
5. 模型上方定时冒出提示气泡

### 改个位置试试

```ts
createWidget({
  model: {
    path: 'https://model.hacxy.cn/cat-black/model.json',
  },
  position: 'bottom-right', // 右下角
  size: 400, // canvas 尺寸 400px
});
```

---

## 位置与动画

### 位置

`position` 决定挂件固定在页面的哪个角落——`'bottom-left'`（默认）或 `'bottom-right'`：

```ts
createWidget({
  model: { path: '/models/model.json' },
  position: 'bottom-right',
});
```

### 尺寸

`size` 设置 canvas 大小，默认 `300`。传数字就是正方形，传对象可以分别指定宽高：

```ts
// 正方形 400x400
createWidget({
  model: { path: '/models/model.json' },
  size: 400,
});

// 宽高分别指定
createWidget({
  model: { path: '/models/model.json' },
  size: { width: 350, height: 500 },
});
```

### 动画类型

`transitionType` 设置入场和退场的过渡效果。`'slide'`（默认）是滑入滑出，`'fade'` 是淡入淡出：

```ts
createWidget({
  model: { path: '/models/model.json' },
  transitionType: 'fade',
  transitionDuration: 1000, // 动画时长 1000ms（默认 1500）
});
```

### 主题色

`primaryColor` 统一控制菜单、状态栏、提示气泡这些 UI 元素的颜色，一处配置全局生效：

```ts
createWidget({
  model: { path: '/models/model.json' },
  primaryColor: 'rgba(255, 130, 130, 0.9)', // 粉红色主题
});
```

---

## 菜单定制

鼠标移到模型上，旁边会弹出一组圆形按钮——这就是菜单。

### 默认菜单

开箱自带三个按钮：

- **切换模型** — 只有传了多个模型时才会出现
- **休眠** — 把模型藏起来，显示休眠状态栏
- **关于** — 跳转到项目 GitHub 页面

### 追加菜单项

想在默认菜单后面加几个自定义按钮？用 `menus.extraItems`：

```ts
createWidget({
  model: { path: '/models/model.json' },
  menus: {
    extraItems: [
      {
        icon: 'mdi:emoticon-happy-outline',
        label: '播放动作',
        onClick(widget) {
          const motions = widget.l2d.getMotions();
          const groups = Object.keys(motions);
          if (groups.length > 0) {
            widget.l2d.playMotion(groups[0]!);
          }
        },
      },
    ],
  },
});
```

### 完全替换菜单

如果默认按钮不合适，用 `menus.items` 整个换掉（设了 `items` 后 `extraItems` 就不生效了）：

```ts
createWidget({
  model: { path: '/models/model.json' },
  menus: {
    items: [
      {
        icon: 'mdi:bed',
        label: '休眠',
        onClick(widget) {
          widget.sleep();
        },
      },
    ],
  },
});
```

### 菜单对齐

`menus.align` 决定菜单出现在 canvas 的哪一侧，默认 `'right'`：

```ts
createWidget({
  model: { path: '/models/model.json' },
  menus: { align: 'left' },
});
```

### 图标

`icon` 用的是 [Iconify](https://icon-sets.iconify.design/) 图标名，格式 `"prefix:name"`，比如 `"mdi:home"`。图标会通过 Iconify API 远程加载并缓存到本地。

加载失败的话，按钮会退回显示 `label` 文本，不会空着。

---

## 提示气泡

模型上方那个会冒话的小气泡，可以用来展示欢迎语、循环消息，还能配打字动画。

### 基础配置

每个模型的 `tips` 是独立配置的：

```ts
createWidget({
  model: {
    path: '/models/model.json',
    tips: {
      welcomeMessage: ['欢迎来访！', '好久不见！'],
      messages: ['记得多休息哦～', '有什么可以帮你的吗？'],
      duration: 3000, // 每条显示 3 秒（默认）
      interval: 5000, // 每 5 秒切换一条（默认）
    },
  },
});
```

### 位置偏移

气泡位置不太对？用 `offset` 微调一下：

```ts
const options = {
  tips: {
    offset: { x: 20, y: -10 }, // 向右 20px，向上 10px
  },
};
```

### 打字动画

开启 `typing` 后，提示文字会一个字一个字地"打"出来：

```ts
const options = {
  tips: {
    typing: {
      speed: 100, // 每字 100ms（默认）
    },
  },
};
```

### 嘴型同步

如果你的模型支持嘴型参数，打字的时候还能让模型跟着「说话」：

```ts
const options = {
  tips: {
    typing: {
      param: 'PARAM_MOUTH_OPEN_Y', // 嘴型参数名
      speed: 200,
      minValue: 0, // 嘴型开合最小值（0~1）
      maxValue: 1, // 嘴型开合最大值（0~1）
    },
  },
};
```

参数名因模型而异，常见的有 `PARAM_MOUTH_OPEN_Y`、`ParamMouthOpenY` 等，具体看模型文件里的定义。

---

## 多模型切换

### 配置多个模型

把 `model` 从对象改成数组，就自动开启了多模型切换：

```ts
createWidget({
  model: [
    { path: '/models/cat-black/model.json' },
    { path: '/models/cat-white/model.json' },
  ],
});
```

菜单里会自动多出一个"切换模型"按钮，不用额外配。

### 独立配置

每个模型可以有自己的缩放、位置偏移、音量和提示气泡：

```ts
createWidget({
  model: [
    {
      path: '/models/cat-black/model.json',
      scale: 1.2,
      tips: {
        welcomeMessage: ['我是黑猫！'],
      },
    },
    {
      path: '/models/cat-white/model.json',
      offset: [0.5, 0],
      tips: {
        welcomeMessage: ['我是白猫！'],
      },
    },
  ],
});
```

### 编程式切换

除了用户点菜单切换，你也可以在代码里调 `widget.switchModel(index)`：

```ts
const widget = createWidget({
  model: [
    { path: '/models/cat-black/model.json' },
    { path: '/models/cat-white/model.json' },
  ],
});

// 切换到第二个模型
await widget.switchModel(1);
```

切换过程是完整的：退场动画 -> 销毁旧模型 -> 重建新模型 -> 入场动画。

---

## Widget 实例方法

`createWidget()` 返回一个 `Widget` 对象，提供以下方法和属性：

| 方法/属性 | 说明 |
|-----------|------|
| `l2d` | 底层 `l2d` 实例 |
| `switchModel(index)` | 切换到指定索引的模型 |
| `sleep()` | 隐藏模型，点击状态栏可唤醒 |
| `destroy()` | 销毁 widget 并释放资源 |

---

## 示例代码

### 完整配置示例

```ts
import { createWidget } from 'l2d-widget';

const widget = createWidget({
  // 模型配置
  model: [
    {
      path: 'https://model.hacxy.cn/cat-black/model.json',
      scale: 1.0,
      tips: {
        welcomeMessage: ['欢迎来到我的博客！', '今天也要开心哦～'],
        messages: ['休息一下吧～', '记得喝水喵！', '代码写累了吗？'],
        duration: 4000,
        interval: 8000,
        typing: {
          speed: 100,
          param: 'PARAM_MOUTH_OPEN_Y',
        },
      },
    },
  ],

  // 位置配置
  position: 'bottom-right',
  size: 300,

  // 动画配置
  transitionType: 'slide',
  transitionDuration: 1500,

  // 主题色
  primaryColor: 'rgba(100, 149, 237, 0.9)',

  // 菜单配置
  menus: {
    align: 'right',
    extraItems: [
      {
        icon: 'mdi:heart',
        label: '点赞',
        onClick(widget) {
          console.log('点赞成功！');
        },
      },
    ],
  },
});

// 编程式控制
// widget.sleep(); // 休眠
// widget.switchModel(0); // 切换模型
// widget.destroy(); // 销毁
```

---

## Vue 3 集成示例

```vue
<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { createWidget } from 'l2d-widget'
import type { Widget } from 'l2d-widget'

let widget: Widget | null = null

onMounted(() => {
  widget = createWidget({
    model: {
      path: 'https://model.hacxy.cn/cat-black/model.json',
      tips: {
        welcomeMessage: ['欢迎来访！'],
        messages: ['记得多休息哦～'],
      },
    },
  })
})

onUnmounted(() => {
  widget?.destroy()
  widget = null
})
</script>

<template>
  <!-- Live2D 看板娘组件，无需额外 DOM -->
</template>
```

---

## 相关链接

- **项目地址**: https://github.com/hacxy/l2d-widget
- **在线文档**: https://l2d-widget.hacxy.cn
- **l2d 核心库**: https://github.com/hacxy/l2d
- **模型资源**: https://model.hacxy.cn
- **图标库**: https://icon-sets.iconify.design/
