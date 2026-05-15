import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import blogRoutes from './blogRoutes'
import adminRoutes from './adminRoutes'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  ...blogRoutes,
  ...adminRoutes,
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

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()

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
