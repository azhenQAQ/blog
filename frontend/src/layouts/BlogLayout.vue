<script setup lang="ts">
import NavBar from '@/components/blog/NavBar.vue'
import SideBar from '@/components/blog/SideBar.vue'
import ParticleBg from '@/components/blog/ParticleBg.vue'
import FloatingToolbar from '@/components/blog/FloatingToolbar.vue'
</script>

<template>
  <div class="blog-layout">
    <ParticleBg />
    <NavBar />
    <div class="main-container">
      <main class="content-area">
        <router-view />
      </main>
      <aside class="sidebar-area">
        <SideBar />
      </aside>
    </div>
    <FloatingToolbar />
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
  position: sticky;
  top: var(--nav-height);
  align-self: flex-start;
  max-height: calc(100vh - var(--nav-height));
  overflow-y: auto;
}

@media (max-width: 900px) {
  .main-container {
    flex-direction: column;
    padding: calc(var(--nav-height) + 16px) 12px 24px;
  }

  .sidebar-area {
    width: 100%;
    min-width: unset;
    max-width: unset;
    position: static;
    max-height: none;
    overflow-y: visible;
  }
}
</style>
