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
  // C端应用（会员端）
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
      {
        path: 'edit-profile',
        name: 'EditProfile',
        component: () => import('@/views/EditProfile.vue'),
        meta: { title: '编辑资料', showTabBar: false, requiresAuth: true },
      },
    ],
  },
  // 管理员后台
  {
    path: '/admin',
    component: () => import('@/components/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { roles: ['ADMIN'] },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/AdminDashboard.vue'),
        meta: { title: '管理仪表盘', roles: ['ADMIN'] },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] },
      },
      {
        path: 'coaches',
        name: 'AdminCoaches',
        component: () => import('@/views/admin/AdminCoaches.vue'),
        meta: { title: '教练管理', roles: ['ADMIN'] },
      },
      {
        path: 'courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/AdminCourses.vue'),
        meta: { title: '课程管理', roles: ['ADMIN'] },
      },
    ],
  },
  // 教练后台
  {
    path: '/coach',
    component: () => import('@/components/CoachLayout.vue'),
    redirect: '/coach/dashboard',
    meta: { roles: ['COACH'] },
    children: [
      {
        path: 'dashboard',
        name: 'CoachDashboard',
        component: () => import('@/views/coach/CoachDashboard.vue'),
        meta: { title: '教练仪表盘', roles: ['COACH'] },
      },
      {
        path: 'schedule',
        name: 'CoachSchedule',
        component: () => import('@/views/coach/CoachSchedule.vue'),
        meta: { title: '日程管理', roles: ['COACH'] },
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

  // 需要登录但未登录 → 跳转登录页
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录访问登录页 → 根据角色跳转
  if (to.name === 'Login' && userStore.isLoggedIn()) {
    if (userStore.isAdmin) next({ name: 'AdminDashboard' })
    else if (userStore.isCoach) next({ name: 'CoachDashboard' })
    else next({ name: 'Home' })
    return
  }

  // 角色权限校验
  const requiredRoles = to.meta.roles as string[] | undefined
  if (requiredRoles && requiredRoles.length > 0) {
    if (!userStore.isLoggedIn()) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    const hasRole = requiredRoles.includes(userStore.role)
    if (!hasRole) {
      // 跳转到对应角色的首页
      if (userStore.isAdmin) next({ name: 'AdminDashboard' })
      else if (userStore.isCoach) next({ name: 'CoachDashboard' })
      else next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
