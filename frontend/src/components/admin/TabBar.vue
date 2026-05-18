<script setup lang="ts">
import { reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminTabsStore } from '@/stores/adminTabs'

const router = useRouter()
const tabStore = useAdminTabsStore()

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  targetPath: '',
})

const closableTabsCount = computed(() => tabStore.tabs.filter((t) => t.closable).length)

function handleTabClick(path: string) {
  tabStore.setActiveTab(path)
  router.push(path)
}

function handleClose(path: string, e: MouseEvent) {
  e.stopPropagation()
  const targetPath = tabStore.closeTab(path)
  if (targetPath) router.push(targetPath)
}

function handleContextMenu(path: string, e: MouseEvent) {
  e.preventDefault()
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.targetPath = path
}

function hideContextMenu() {
  contextMenu.visible = false
}

onMounted(() => {
  document.addEventListener('click', hideContextMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', hideContextMenu)
})

function doCloseOthers() {
  const targetPath = tabStore.closeOthers(contextMenu.targetPath)
  if (targetPath) router.push(targetPath)
  hideContextMenu()
}

function doCloseRight() {
  const targetPath = tabStore.closeRight(contextMenu.targetPath)
  if (targetPath) router.push(targetPath)
  hideContextMenu()
}

function doCloseAll() {
  const targetPath = tabStore.closeAll()
  router.push(targetPath)
  hideContextMenu()
}
</script>

<template>
  <div class="tab-bar" @click="hideContextMenu">
    <div
      v-for="tab in tabStore.tabs"
      :key="tab.path"
      :class="['tab-item', { active: tab.path === tabStore.activeTabPath }]"
      @click="handleTabClick(tab.path)"
      @contextmenu="handleContextMenu(tab.path, $event)"
    >
      <span class="tab-title">{{ tab.title }}</span>
      <button
        v-if="tab.closable"
        class="tab-close"
        @click="handleClose(tab.path, $event)"
      >&times;</button>
    </div>

    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="tab-context-menu"
        :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
        @click.stop
      >
        <div class="menu-item" @click="doCloseOthers">关闭其他</div>
        <div class="menu-item" @click="doCloseRight">关闭右侧</div>
        <div
          :class="['menu-item', { disabled: closableTabsCount <= 1 }]"
          @click="closableTabsCount > 1 && doCloseAll()"
        >关闭全部</div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.tab-bar {
  display: flex;
  align-items: flex-end;
  height: 36px;
  padding: 0 8px;
  background: var(--el-bg-color-overlay, #fff);
  border-bottom: 1px solid var(--el-border-color-light, #e4e7ed);
  overflow-x: auto;
  overflow-y: hidden;
  gap: 2px;
  flex-shrink: 0;
}

.tab-bar::-webkit-scrollbar {
  height: 3px;
}

.tab-bar::-webkit-scrollbar-thumb {
  background: var(--el-border-color-light, #e4e7ed);
  border-radius: 2px;
}

.tab-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  height: 30px;
  min-width: 100px;
  max-width: 180px;
  padding: 0 10px;
  border-radius: 6px 6px 0 0;
  background: var(--el-fill-color-light, #f5f7fa);
  color: var(--el-text-color-secondary, #909399);
  font-size: 0.88em;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  transition: background 0.15s, color 0.15s;
  border: 1px solid transparent;
  flex-shrink: 0;
}

.tab-item:hover {
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-text-color-primary, #303133);
}

.tab-item.active {
  background: var(--el-bg-color-overlay, #fff);
  color: var(--el-color-primary, #409eff);
  border-color: var(--el-border-color-light, #e4e7ed);
  border-bottom-color: transparent;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--el-color-primary, #409eff);
}

.tab-title {
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.tab-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border: none;
  border-radius: 3px;
  background: transparent;
  color: inherit;
  font-size: 0.85em;
  line-height: 1;
  cursor: pointer;
  flex-shrink: 0;
  padding: 0;
  opacity: 0.6;
  transition: opacity 0.15s, background 0.15s, color 0.15s;
}

.tab-close:hover {
  opacity: 1;
  background: var(--el-color-danger-light-3, #f89898);
  color: #fff;
}

.tab-context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 120px;
  padding: 4px 0;
  background: var(--el-bg-color-overlay, #fff);
  border: 1px solid var(--el-border-color-light, #e4e7ed);
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.menu-item {
  padding: 6px 16px;
  font-size: 0.88em;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  user-select: none;
  transition: background 0.15s, color 0.15s;
}

.menu-item:hover {
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
}

.menu-item.disabled {
  color: var(--el-text-color-placeholder, #c0c4cc);
  cursor: not-allowed;
}

.menu-item.disabled:hover {
  background: transparent;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
</style>
