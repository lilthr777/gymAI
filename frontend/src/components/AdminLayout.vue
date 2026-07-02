<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BackendLayout from './BackendLayout.vue'
import { DataAnalysis, User, UserFilled, Reading } from '@element-plus/icons-vue'

const route = useRoute()

const menuItems = [
  { path: '/admin/dashboard', title: '仪表盘', icon: DataAnalysis },
  { path: '/admin/users', title: '用户管理', icon: User },
  { path: '/admin/coaches', title: '教练管理', icon: UserFilled },
  { path: '/admin/courses', title: '课程管理', icon: Reading },
]

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin/dashboard': '管理仪表盘',
    '/admin/users': '用户管理',
    '/admin/coaches': '教练管理',
    '/admin/courses': '课程管理',
  }
  return titles[route.path] || '管理后台'
})
</script>

<template>
  <BackendLayout :menu-items="menuItems" :title="pageTitle" />
</template>
