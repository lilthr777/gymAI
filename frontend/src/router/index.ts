import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', showTabBar: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', showTabBar: false },
  },
  {
    path: '/',
    component: () => import('@/components/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', showTabBar: true, requiresAuth: true },
      },
      {
        path: 'courses',
        name: 'CourseBrowse',
        component: () => import('@/views/CourseBrowse.vue'),
        meta: { title: '课程', showTabBar: true },
      },
      {
        path: 'courses/:id',
        name: 'CourseDetail',
        component: () => import('@/views/CourseDetail.vue'),
        meta: { title: '课程详情', showTabBar: false },
      },
      {
        path: 'coaches',
        name: 'CoachBrowse',
        component: () => import('@/views/CoachBrowse.vue'),
        meta: { title: '教练', showTabBar: true },
      },
      {
        path: 'coaches/:id',
        name: 'CoachDetail',
        component: () => import('@/views/CoachDetail.vue'),
        meta: { title: '教练详情', showTabBar: false },
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/AiChat.vue'),
        meta: { title: 'AI助手', showTabBar: true, requiresAuth: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '我的', showTabBar: true, requiresAuth: true },
      },
      {
        path: 'my-courses',
        name: 'MyCourses',
        component: () => import('@/views/MyCourses.vue'),
        meta: { title: '我的课程', showTabBar: false, requiresAuth: true },
      },
      {
        path: 'my-checkins',
        name: 'MyCheckins',
        component: () => import('@/views/MyCheckins.vue'),
        meta: { title: '签到记录', showTabBar: false, requiresAuth: true },
      },
      {
        path: 'card',
        name: 'CardPage',
        component: () => import('@/views/CardPage.vue'),
        meta: { title: '会员卡', showTabBar: false, requiresAuth: true },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || ''} - gymAI`

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else if (to.name === 'Login' && userStore.isLoggedIn()) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
