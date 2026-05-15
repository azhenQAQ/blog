<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)

const navItems = [
  { label: '主页', path: '/' },
  { label: '文章', path: '/posts' },
  // { label: '留言板', path: '/posts' },
  // { label: '友人帐', path: '/posts' },
]

const isActive = (path: string) => route.path === path

function navigate(path: string) {
  router.push(path)
  menuOpen.value = false
}

const isDark = ref(document.documentElement.getAttribute('data-theme') !== 'light')

function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.dataset.theme = isDark.value ? 'dark' : 'light'
}

// 监听系统主题变化
const mediaQuery = globalThis.matchMedia('(prefers-color-scheme: dark)')

function handleSystemThemeChange(e: MediaQueryListEvent) {
  isDark.value = e.matches
  document.documentElement.dataset.theme = e.matches ? 'dark' : 'light'
}

mediaQuery.addEventListener('change', handleSystemThemeChange)

onUnmounted(() => {
  mediaQuery.removeEventListener('change', handleSystemThemeChange)
})

</script>

<template>
  <nav class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="nav-brand">废话回收站</router-link>

      <div class="nav-menu" :class="{ open: menuOpen }">
        <a
          v-for="item in navItems"
          :key="item.label"
          :class="{ active: isActive(item.path) }"
          @click="navigate(item.path)"
        >
          {{ item.label }}
        </a>
      </div>

      <div class="nav-actions">
        <button class="theme-toggle" @click="toggleTheme" :title="isDark ? '切换亮色' : '切换暗色'">
          <span v-if="isDark">☀</span>
          <span v-else>☾</span>
        </button>

        <button class="menu-toggle" @click="menuOpen = !menuOpen">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--nav-height);
  background: var(--nav-bg);
  box-shadow: var(--nav-shadow);
  backdrop-filter: blur(8px);
  z-index: 100;
}

.nav-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 24px;
}

.nav-brand {
  font-size: 1.3em;
  font-weight: 700;
  color: var(--text-strong);
  text-decoration: none;
  white-space: nowrap;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 48px;
}

.nav-menu a {
  position: relative;
  padding: 6px 14px;
  color: var(--text-main);
  text-decoration: none;
  font-size: 0.95em;
  cursor: pointer;
  transition: color 0.2s;
}

.nav-menu a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: var(--accent);
  transition: width 0.2s;
  border-radius: 1px;
}

.nav-menu a:hover,
.nav-menu a.active {
  color: var(--accent);
}

.nav-menu a:hover::after,
.nav-menu a.active::after {
  width: 60%;
}

.nav-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}

.theme-toggle {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: 50%;
  font-size: 1.1em;
  cursor: pointer;
  color: var(--text-main);
  transition: background 0.2s;
}

.theme-toggle:hover {
  background: var(--accent-bg);
  color: var(--accent);
}

.menu-toggle {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
}

.menu-toggle span {
  display: block;
  width: 20px;
  height: 2px;
  background: var(--text-main);
  border-radius: 1px;
  transition: all 0.2s;
}

@media (max-width: 900px) {
  .nav-menu {
    display: none;
    position: fixed;
    top: var(--nav-height);
    left: 0;
    right: 0;
    background: var(--card-bg);
    flex-direction: column;
    padding: 12px;
    box-shadow: var(--nav-shadow);
    gap: 4px;
  }

  .nav-menu.open {
    display: flex;
  }

  .nav-menu a {
    padding: 12px 16px;
    width: 100%;
    border-radius: var(--radius-card);
  }

  .nav-menu a:hover {
    background: var(--accent-bg);
  }

  .menu-toggle {
    display: flex;
  }
}
</style>
