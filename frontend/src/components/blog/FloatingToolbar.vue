<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const expanded = ref(false)
const toolbarRef = ref<HTMLElement | null>(null)

function toggleExpand() {
  expanded.value = !expanded.value
}

function collapse() {
  expanded.value = false
}

// 鼠标移出 1 秒后自动收起
let leaveTimer: ReturnType<typeof setTimeout> | null = null

function onMouseEnter() {
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
}

function onMouseLeave() {
  if (leaveTimer) clearTimeout(leaveTimer)
  leaveTimer = setTimeout(() => {
    collapse()
  }, 1000)
}

// 点击外部区域收起
function onDocumentClick(e: MouseEvent) {
  if (!expanded.value) return
  if (toolbarRef.value && !toolbarRef.value.contains(e.target as Node)) {
    collapse()
  }
}

// 回到顶部
const showBackToTop = ref(false)
let scrollTimer: ReturnType<typeof setTimeout> | null = null

function onScroll() {
  if (scrollTimer) return
  scrollTimer = setTimeout(() => {
    showBackToTop.value = window.scrollY > 300
    scrollTimer = null
  }, 100)
}

function scrollToTop() {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  document.addEventListener('click', onDocumentClick)
  showBackToTop.value = window.scrollY > 300
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick)
  window.removeEventListener('scroll', onScroll)
  if (leaveTimer) clearTimeout(leaveTimer)
  if (scrollTimer) clearTimeout(scrollTimer)
})
</script>

<template>
  <div
    ref="toolbarRef"
    class="floating-toolbar"
    :class="{ expanded }"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
  >
    <div class="toolbar-buttons">
      <button v-if="showBackToTop" class="tool-btn" title="回到顶部" @click="scrollToTop">↑</button>
      <button
        class="tool-btn"
        :title="themeStore.isDark ? '切换亮色模式' : '切换暗色模式'"
        @click="themeStore.toggleTheme()"
      >
        <img
          :src="themeStore.isDark ? '/images/day-mode.png' : '/images/night-mode.png'"
          alt="主题切换"
        />
      </button>
    </div>
    <button class="trigger-btn" @click="toggleExpand" title="展开工具栏">
      <img
        :src="themeStore.isDark ? '/images/setting-white.png' : '/images/setting-black.png'"
        alt="菜单"
      />
    </button>
  </div>
</template>

<style scoped>
.floating-toolbar {
  position: fixed;
  bottom: calc(var(--nav-height) + 20px);
  right: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 200;
}

/* 触发按钮 */
.trigger-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition:
    transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
    box-shadow 0.3s ease;
  padding: 0;
  overflow: hidden;
}

.trigger-btn:hover {
  transform: scale(1.1);
  box-shadow: var(--card-hover-shadow);
}

.trigger-btn img {
  width: 22px;
  height: 22px;
  display: block;
  pointer-events: none;
}

/* 工具按钮容器 */
.toolbar-buttons {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  opacity: 0;
  transform: translateY(8px);
  pointer-events: none;
  transition:
    opacity 0.25s ease,
    transform 0.25s ease;
}

.expanded .toolbar-buttons {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

/* 工具按钮 */
.tool-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--card-bg);
  border: var(--card-border);
  box-shadow: var(--card-shadow);
  color: var(--text-main);
  font-size: 1.1em;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.tool-btn img {
  width: 18px;
  height: 18px;
  display: block;
  pointer-events: none;
}

.tool-btn:hover {
  background: var(--accent-bg);
  box-shadow: var(--card-hover-shadow);
}

/* 移动端 ≤900px */
@media (max-width: 900px) {
  .floating-toolbar {
    bottom: 20px;
    right: 6px;
    gap: 6px;
  }

  .trigger-btn {
    width: 38px;
    height: 38px;
  }

  .trigger-btn img {
    width: 18px;
    height: 18px;
  }

  .tool-btn {
    width: 34px;
    height: 34px;
  }

  .tool-btn img {
    width: 15px;
    height: 15px;
  }

  .toolbar-buttons {
    gap: 6px;
  }
}
</style>
