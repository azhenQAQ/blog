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
        path: 'posts/new',
        name: '创建文章',
        component: () => import('@/pages/admin/PostEditPage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'posts/:id/edit',
        name: '编辑文章',
        component: () => import('@/pages/admin/PostEditPage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'tags',
        name: '标签管理',
        component: () => import('@/pages/admin/TagManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'comments',
        name: '评论管理',
        component: () => import('@/pages/admin/CommentManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
      {
        path: 'attachments',
        name: '附件库',
        component: () => import('@/pages/admin/AttachmentManagePage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin' },
      },
    ],
  },
]

export default adminRoutes
