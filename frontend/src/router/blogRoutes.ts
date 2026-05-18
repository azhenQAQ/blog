import type { RouteRecordRaw } from 'vue-router'

const blogRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/BlogLayout.vue'),
    children: [
      {
        path: '',
        name: '主页',
        component: () => import('@/pages/blog/HomePage.vue'),
        meta: { title: '主页' },
      },
      {
        path: 'posts/:id',
        name: '文章详情',
        component: () => import('@/pages/blog/PostDetailPage.vue'),
        meta: { title: '文章详情' },
      },
      {
        path: 'guestbook',
        name: '留言板',
        component: () => import('@/pages/blog/GuestbookPage.vue'),
        meta: { title: '留言板' },
      },
      {
        path: 'tools',
        name: '奇思妙想',
        component: () => import('@/pages/blog/ToolHubPage.vue'),
        meta: { title: '奇思妙想' },
      },
    ],
  },
]

export default blogRoutes
