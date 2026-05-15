import type { RouteRecordRaw } from 'vue-router'

const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: '控制台',
        component: () => import('@/pages/admin/DashboardPage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'userManage',
        name: '用户管理',
        component: () => import('@/pages/admin/UserManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'posts',
        name: '文章管理',
        component: () => import('@/pages/admin/PostManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'comments',
        name: '评论管理',
        component: () => import('@/pages/admin/CommentManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
    ],
  },
]

export default adminRoutes
