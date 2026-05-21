<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import MusicPlayer from '@/components/blog/MusicPlayer.vue'

const router = useRouter()
const route = useRoute()
const menuOpen = ref(false)
const navbarRef = ref<HTMLElement | null>(null)

const navItems = [
  { label: '主页', path: '/' },
  { label: '留言板', path: '/guestbook' },
  { label: '奇思妙想', path: '/tools' },
]

const isActive = (path: string) => route.path === path

function navigate(path: string) {
  router.push(path)
  menuOpen.value = false
}

function onDocumentClick(e: MouseEvent) {
  if (!menuOpen.value) return
  if (navbarRef.value && !navbarRef.value.contains(e.target as Node)) {
    menuOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', onDocumentClick))
onUnmounted(() => document.removeEventListener('click', onDocumentClick))
</script>

<template>
  <nav ref="navbarRef" class="navbar">
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
        <MusicPlayer />
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
  border-bottom: 3px solid var(--shadow-color);
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
  font-family: var(--font-heading);
  font-size: 1.4em;
  font-weight: 700;
  text-decoration: none;
  white-space: nowrap;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  background: var(--accent-yellow);
  color: #1a1a1a;
  padding: 4px 12px;
}

[data-theme='dark'] .nav-brand {
  color: #1a1a1a;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 0;
  margin-left: 48px;
}

.nav-menu a {
  position: relative;
  padding: 8px 18px;
  color: var(--text-main);
  text-decoration: none;
  font-family: var(--font-heading);
  font-size: 1em;
  font-weight: 500;
  letter-spacing: 0.03em;
  cursor: pointer;
  transition: background 0.1s ease, color 0.1s ease;
  text-transform: uppercase;
}

.nav-menu a:hover,
.nav-menu a.active {
  background: var(--accent);
  color: #fff;
}

.nav-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-toggle {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: none;
  border: 2px solid var(--shadow-color);
  cursor: pointer;
  padding: 8px;
}

.menu-toggle span {
  display: block;
  width: 20px;
  height: 2.5px;
  background: var(--shadow-color);
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
    padding: 0;
    border-bottom: 3px solid var(--shadow-color);
    gap: 0;
  }

  .nav-menu.open {
    display: flex;
  }

  .nav-menu a {
    padding: 14px 20px;
    width: 100%;
    border-bottom: 1px solid var(--border-color);
  }

  .menu-toggle {
    display: flex;
  }
}
</style>
