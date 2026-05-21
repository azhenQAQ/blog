<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { useMusicStore } from '@/stores/music'
import { useConfirm } from '@/composables/useConfirm'
import { useSnackbar } from '@/composables/useSnackbar'
import SvgIcon from '@/components/common/SvgIcon.vue'

const themeStore = useThemeStore()
const userStore = useUserStore()
const musicStore = useMusicStore()
const { showConfirm } = useConfirm()
const { showMessage } = useSnackbar()

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

async function handleLogout() {
  const confirmed = await showConfirm({
    title: '退出登录',
    message: '确定退出当前账号吗？',
  })
  if (!confirmed) return

  try {
    await userStore.logout()
    showMessage({ type: 'success', message: '退出成功' })
  } catch (e) {
    const msg = e instanceof Error ? e.message : '退出失败，请重试'
    showMessage({ type: 'error', message: msg })
  }
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
      <button
        class="tool-btn"
        :title="musicStore.autoPlayEnabled ? '关闭自动播放' : '开启自动播放'"
        @click="musicStore.toggleAutoPlay()"
      >
        <SvgIcon :name="musicStore.autoPlayEnabled ? 'pause' : 'play'" :size="16" />
      </button>
      <button
        v-if="userStore.isLogin"
        class="tool-btn"
        title="退出登录"
        @click="handleLogout"
      >
        ⏻
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
  gap: 10px;
  z-index: 200;
}

/* 触发按钮 */
.trigger-btn {
  width: 44px;
  height: 44px;
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 4px 4px 0 var(--shadow-color);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.1s ease;
  padding: 0;
  overflow: hidden;
}

.trigger-btn:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 var(--shadow-color);
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
  gap: 10px;
  opacity: 0;
  transform: translateY(8px);
  pointer-events: none;
  transition:
    opacity 0.15s ease,
    transform 0.15s ease;
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
  background: var(--card-bg);
  border: 3px solid var(--shadow-color);
  box-shadow: 3px 3px 0 var(--shadow-color);
  color: var(--text-main);
  font-family: var(--font-heading);
  font-size: 1.1em;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.1s ease;
}

.tool-btn img {
  width: 18px;
  height: 18px;
  display: block;
  pointer-events: none;
}

.tool-btn:hover {
  background: var(--accent-yellow);
  box-shadow: 5px 5px 0 var(--shadow-color);
  transform: translate(-2px, -2px);
}

[data-theme='dark'] .tool-btn:hover {
  background: var(--accent);
  color: #fff;
}

/* 移动端 ≤900px */
@media (max-width: 900px) {
  .floating-toolbar {
    bottom: 20px;
    right: 6px;
    gap: 8px;
  }

  .trigger-btn {
    width: 38px;
    height: 38px;
    box-shadow: 3px 3px 0 var(--shadow-color);
  }

  .trigger-btn img {
    width: 18px;
    height: 18px;
  }

  .tool-btn {
    width: 34px;
    height: 34px;
    box-shadow: 2px 2px 0 var(--shadow-color);
  }

  .tool-btn img {
    width: 15px;
    height: 15px;
  }

  .toolbar-buttons {
    gap: 8px;
  }
}
</style>
