import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import blogRoutes from './blogRoutes'
import adminRoutes from './adminRoutes'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  ...blogRoutes,
  ...adminRoutes,
  {
    path: '/tools/image-process',
    name: '图片处理',
    component: () => import('@/pages/tools/ImageProcessPage.vue'),
    meta: { title: '图片处理' },
  },
  {
    path: '/tools/json-formatter',
    name: 'JSON 格式化',
    component: () => import('@/pages/tools/JsonFormatterPage.vue'),
    meta: { title: 'JSON 格式化' },
  },
  {
    path: '/tools/translate',
    name: '翻译',
    component: () => import('@/pages/blog/tools/TranslatePage.vue'),
    meta: { title: '翻译' },
  },
  {
    path: '/login',
    name: '登录',
    component: () => import('@/pages/blog/LoginPage.vue'),
  },
  {
    path: '/register',
    name: '注册',
    component: () => import('@/pages/blog/RegisterPage.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

const SITE_NAME = '废话回收站'

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

  const pageTitle = to.meta?.title as string | undefined
  document.title = pageTitle ? `${pageTitle} - ${SITE_NAME}` : SITE_NAME

  const requiresAuth = to.matched.some((r) => r.meta?.requiresAuth === true)
  const requiredRole = to.matched.find((r) => r.meta?.requiredRole)?.meta?.requiredRole as
    | string
    | undefined

  if (requiresAuth && !userStore.isLogin) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  if (requiredRole && !userStore.isAdminUser) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
