import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { RouteRecordRaw } from 'vue-router'
import adminRoutes from '@/router/adminRoutes'

interface TabItem {
  path: string
  title: string
  closable: boolean
}

const ADMIN_PREFIX = '/admin'

function getAdminChildren(): RouteRecordRaw[] {
  const parent = adminRoutes.find((r) => r.path === ADMIN_PREFIX)
  return parent?.children ?? []
}

function getMenuRoutes(): RouteRecordRaw[] {
  return getAdminChildren().filter((r) => r.meta?.menuVisible !== false)
}

/** 将路由 pattern（含 :param）转为正则匹配 */
function matchRoutePattern(pattern: string, path: string): boolean {
  const regexStr = pattern.replace(/:\w+/g, '[^/]+')
  return new RegExp('^' + regexStr + '$').test(path)
}

/** 在所有 admin 子路由中查找匹配的路由记录 */
function findAdminRoute(path: string): RouteRecordRaw | undefined {
  return getAdminChildren().find((r) => {
    const fullPath = ADMIN_PREFIX + '/' + r.path
    return fullPath === path || matchRoutePattern(fullPath, path)
  })
}

/**
 * 解析当前路径对应的 Tab 路径。
 * - 若路径匹配到某个 admin 子路由（含非菜单路由如编辑页），返回路径本身作为独立 Tab
 * - 否则回退到父级菜单路由
 */
function resolveTabPath(currentPath: string): string | null {
  // 优先检查是否匹配独立子路由
  if (findAdminRoute(currentPath)) return currentPath
  // 回退：查找父级菜单路由
  for (const r of getMenuRoutes()) {
    const fullPath = ADMIN_PREFIX + '/' + r.path
    if (currentPath.startsWith(fullPath + '/')) return fullPath
  }
  return null
}

/** 解析路径对应的菜单路由路径，用于侧边栏高亮 */
function resolveMenuPath(path: string): string | null {
  for (const r of getMenuRoutes()) {
    const fullPath = ADMIN_PREFIX + '/' + r.path
    if (path === fullPath) return fullPath
    if (path.startsWith(fullPath + '/')) return fullPath
  }
  return null
}

function buildInitialTabs(): TabItem[] {
  const dashboard = getMenuRoutes().find((r) => r.path === 'dashboard')
  const title = (dashboard?.meta?.menuTitle as string) || (dashboard?.name as string) || '控制台'
  return [{ path: '/admin/dashboard', title, closable: false }]
}

export { resolveTabPath, resolveMenuPath, getAdminChildren, getMenuRoutes, findAdminRoute }

export const useAdminTabsStore = defineStore('adminTabs', () => {
  const tabs = ref<TabItem[]>(buildInitialTabs())
  const activeTabPath = ref('/admin/dashboard')

  function openTab(path: string) {
    // 已存在则直接激活
    if (tabs.value.some((t) => t.path === path)) {
      activeTabPath.value = path
      return
    }

    const matched = findAdminRoute(path)
    if (!matched) return

    const title =
      (matched.meta?.tabTitle as string) ||
      (matched.meta?.menuTitle as string) ||
      (matched.name as string) ||
      path
    const closable = (matched.meta?.tabClosable as boolean) ?? true
    tabs.value.push({ path, title, closable })
    activeTabPath.value = path
  }

  function setActiveTab(path: string) {
    activeTabPath.value = path
  }

  function closeTab(path: string): string | null {
    const idx = tabs.value.findIndex((t) => t.path === path)
    if (idx === -1) return null
    if (!tabs.value[idx].closable) return null

    tabs.value.splice(idx, 1)

    if (activeTabPath.value !== path) return null

    const next = tabs.value[idx] ?? tabs.value[idx - 1]
    if (next) {
      activeTabPath.value = next.path
      return next.path
    }
    return null
  }

  function closeOthers(path: string): string | null {
    tabs.value = tabs.value.filter((t) => !t.closable || t.path === path)
    if (!tabs.value.some((t) => t.path === activeTabPath.value)) {
      activeTabPath.value = path
      return path
    }
    return null
  }

  function closeRight(path: string): string | null {
    const idx = tabs.value.findIndex((t) => t.path === path)
    if (idx === -1) return null
    tabs.value = tabs.value.filter((t, i) => !t.closable || i <= idx)
    if (!tabs.value.some((t) => t.path === activeTabPath.value)) {
      activeTabPath.value = path
      return path
    }
    return null
  }

  function closeAll(): string {
    tabs.value = tabs.value.filter((t) => !t.closable)
    activeTabPath.value = '/admin/dashboard'
    return '/admin/dashboard'
  }

  function reset() {
    tabs.value = buildInitialTabs()
    activeTabPath.value = '/admin/dashboard'
  }

  return {
    tabs,
    activeTabPath,
    openTab,
    setActiveTab,
    closeTab,
    closeOthers,
    closeRight,
    closeAll,
    reset,
  }
})
