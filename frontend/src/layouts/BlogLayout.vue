<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/blog/NavBar.vue'
import SideBar from '@/components/blog/SideBar.vue'
import ParticleBg from '@/components/blog/ParticleBg.vue'
import FloatingToolbar from '@/components/blog/FloatingToolbar.vue'
import SnackbarContainer from '@/components/blog/SnackbarContainer.vue'
import ConfirmDialog from '@/components/blog/ConfirmDialog.vue'
import Live2DWidget from '@/components/blog/Live2DWidget.vue'

const route = useRoute()
const mobileSidebarOpen = ref(false)

function toggleSidebar() {
  mobileSidebarOpen.value = !mobileSidebarOpen.value
}

watch(
  () => route.fullPath,
  () => {
    mobileSidebarOpen.value = false
  },
)
</script>

<template>
  <div class="blog-layout">
    <ParticleBg />
    <NavBar />
    <button class="sidebar-toggle" @click="toggleSidebar" />
    <div v-if="mobileSidebarOpen" class="sidebar-overlay" @click="mobileSidebarOpen = false" />
    <div class="main-container">
      <main class="content-area">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>
      <aside class="sidebar-area" :class="{ open: mobileSidebarOpen }">
        <div class="sidebar-sticky-wrap">
          <SideBar />
        </div>
      </aside>
    </div>
    <FloatingToolbar />
    <SnackbarContainer />
    <ConfirmDialog />
    <Live2DWidget />
  </div>
</template>

<style scoped>
.blog-layout {
  min-height: 100vh;
  background: var(--page-bg);
  font-family: var(--font-main);
  font-size: var(--font-size);
  line-height: var(--line-height);
  color: var(--text-main);
  position: relative;
}

.main-container {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: calc(var(--nav-height) + 24px) 24px 40px;
  display: flex;
  gap: 24px;
  position: relative;
  z-index: 1;
  min-height: 100vh;
}

.content-area {
  flex: 1;
  min-width: 0;
}

.sidebar-area {
  width: var(--sidebar-width);
  min-width: var(--sidebar-min-width);
  max-width: var(--sidebar-max-width);
  flex-shrink: 0;
}

.sidebar-sticky-wrap {
  position: sticky;
  top: var(--nav-height);
  max-height: calc(100vh - var(--nav-height));
  overflow-y: auto;
}

.sidebar-toggle,
.sidebar-overlay {
  display: none;
}

@media (max-width: 900px) {
  .main-container {
    flex-direction: column;
    padding: calc(var(--nav-height) + 16px) 12px 24px;
  }

  .sidebar-area {
    position: fixed;
    top: 0;
    right: -100%;
    width: 85vw;
    max-width: 320px;
    min-width: unset;
    height: 100vh;
    z-index: 250;
    background: var(--page-bg);
    transition: right 0.3s ease;
    overflow-y: auto;
  }

  .sidebar-area.open {
    right: 0;
    box-shadow: -4px 0 20px rgba(0, 0, 0, 0.35);
  }

  .sidebar-sticky-wrap {
    position: static;
    max-height: none;
    overflow-y: visible;
    padding: var(--nav-height) 16px 24px;
  }

  .sidebar-toggle {
    display: flex;
    align-items: center;
    justify-content: center;
    position: fixed;
    top: calc(var(--nav-height) + 12px);
    right: 12px;
    width: 36px;
    height: 36px;
    z-index: 300;
    background: var(--card-bg);
    border: var(--card-border);
    cursor: pointer;
    box-shadow: var(--card-shadow);
    transition: box-shadow 0.1s ease, transform 0.1s ease;
  }

  .sidebar-toggle::before {
    content: '☰';
    font-family: var(--font-heading);
    font-size: 1.2em;
    color: var(--text-strong);
  }

  .sidebar-toggle:hover {
    box-shadow: var(--card-hover-shadow);
    transform: translate(-1px, -1px);
  }

  .sidebar-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: 249;
  }
}
</style>
