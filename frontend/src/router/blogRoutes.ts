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
      },
      {
        path: 'posts/:id',
        name: '文章详情',
        component: () => import('@/pages/blog/PostDetailPage.vue'),
      },
      {
        path: 'guestbook',
        name: '留言板',
        component: () => import('@/pages/blog/GuestbookPage.vue'),
      },
    ],
  },
]

export default blogRoutes
