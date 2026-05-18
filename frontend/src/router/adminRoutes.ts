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
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'Grid',
          tabClosable: false,
          keepAlive: true,
          componentName: 'DashboardPage',
        },
      },
      {
        path: 'userManage',
        name: '用户管理',
        component: () => import('@/pages/admin/UserManagePage.vue'),
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'User',
          keepAlive: true,
          componentName: 'UserManagePage',
        },
      },
      {
        path: 'posts',
        name: '文章管理',
        component: () => import('@/pages/admin/PostManagePage.vue'),
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'Document',
          keepAlive: true,
          componentName: 'PostManagePage',
        },
      },
      {
        path: 'posts/new',
        name: '创建文章',
        component: () => import('@/pages/admin/PostEditPage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin', menuVisible: false },
      },
      {
        path: 'posts/:id/edit',
        name: '编辑文章',
        component: () => import('@/pages/admin/PostEditPage.vue'),
        meta: { requiresAuth: true, requiredRole: 'admin', menuVisible: false },
      },
      {
        path: 'tags',
        name: '标签管理',
        component: () => import('@/pages/admin/TagManagePage.vue'),
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'PriceTag',
          keepAlive: true,
          componentName: 'TagManagePage',
        },
      },
      {
        path: 'comments',
        name: '评论管理',
        component: () => import('@/pages/admin/CommentManagePage.vue'),
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'ChatDotSquare',
          keepAlive: true,
          componentName: 'CommentManagePage',
        },
      },
      {
        path: 'attachments',
        name: '附件库',
        component: () => import('@/pages/admin/AttachmentManagePage.vue'),
        meta: {
          requiresAuth: true,
          requiredRole: 'admin',
          icon: 'Folder',
          keepAlive: true,
          componentName: 'AttachmentManagePage',
        },
      },
    ],
  },
]

export default adminRoutes
