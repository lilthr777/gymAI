<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import BackendLayout from './BackendLayout.vue'
import { DataAnalysis, Calendar } from '@element-plus/icons-vue'

const route = useRoute()

const menuItems = [
  { path: '/coach/dashboard', title: '仪表盘', icon: DataAnalysis },
  { path: '/coach/schedule', title: '日程管理', icon: Calendar },
]

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/coach/dashboard': '教练仪表盘',
    '/coach/schedule': '日程管理',
  }
  return titles[route.path] || '教练后台'
})
</script>

<template>
  <BackendLayout :menu-items="menuItems" :title="pageTitle" />
</template>
