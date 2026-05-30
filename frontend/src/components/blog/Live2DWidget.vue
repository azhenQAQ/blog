<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { createWidget } from 'l2d-widget'
import type { Widget } from 'l2d-widget'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

let widget: Widget | null = null

onMounted(() => {
  widget = createWidget({
    // 模型配置 - 使用官方提供的示例模型
    model: {
      path: 'https://model.hacxy.cn/cat-black/model.json',
      tips: {
        welcomeMessage: ['欢迎来到博客！', '今天也要开心哦～'],
        messages: ['休息一下吧～', '记得喝水喵！', '代码写累了吗？'],
        duration: 4000,
        interval: 8000,
        typing: {
          speed: 100, // 打字动画速度
        },
      },
    },

    // 位置：左下角，与右侧 FloatingToolbar 不冲突
    position: 'bottom-left',

    // 尺寸：适中大小
    size: { width: 250, height: 350 },

    // 入场动画：淡入效果
    transitionType: 'fade',
    transitionDuration: 1000,

    // 主题色：使用博客的强调色
    primaryColor: 'rgba(255, 87, 34, 0.9)',

    // 菜单配置
    menus: {
      align: 'right',
      extraItems: [
        {
          icon: 'mdi:arrow-up-bold',
          label: '回到顶部',
          onClick() {
            window.scrollTo({ top: 0, behavior: 'smooth' })
          },
        },
        {
          icon: themeStore.isDark ? 'mdi:weather-sunny' : 'mdi:weather-night',
          label: themeStore.isDark ? '亮色模式' : '暗色模式',
          onClick() {
            themeStore.toggleTheme()
          },
        },
      ],
    },
  })
})

onUnmounted(() => {
  // 组件销毁时清理资源，避免内存泄漏喵～
  widget?.destroy()
  widget = null
})
</script>

<template>
  <!-- Live2D 看板娘组件，无需额外 DOM，l2d-widget 会自动创建 canvas -->
</template>
