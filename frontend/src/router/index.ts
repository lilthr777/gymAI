import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '仪表盘', icon: 'Odometer', requiresAuth: true },
      },
      {
        path: 'members',
        name: 'MemberList',
        component: () => import('@/views/MemberList.vue'),
        meta: { title: '会员管理', icon: 'User', requiresAuth: true },
      },
      {
        path: 'coaches',
        name: 'CoachList',
        component: () => import('@/views/CoachList.vue'),
        meta: { title: '教练管理', icon: 'Avatar', requiresAuth: true },
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Calendar', requiresAuth: true },
      },
      {
        path: 'checkins',
        name: 'CheckinList',
        component: () => import('@/views/CheckinList.vue'),
        meta: { title: '签到记录', icon: 'Check', requiresAuth: true },
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { title: 'AI 助手', icon: 'ChatDotRound', requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || 'gymAI'} - 智能健身房管理系统`

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.isLoggedIn()) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
