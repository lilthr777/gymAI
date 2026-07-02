<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { coachBackendApi } from '@/api'
import type { AdminDashboard } from '@/types'

const dashboard = ref<AdminDashboard>({
  totalUsers: 0, totalCourses: 0, totalCoaches: 0, todayCheckins: 0, monthlyRevenue: 0,
})
const loading = ref(true)

const statCards = [
  { label: '课程总数', value: () => dashboard.value.totalCourses, color: '#0071e3' },
  { label: '学员总数', value: () => dashboard.value.totalUsers, color: '#34c759' },
  { label: '今日签到', value: () => dashboard.value.todayCheckins, color: '#ff9500' },
]

onMounted(async () => {
  try {
    const res = await coachBackendApi.dashboard()
    if (res.data) dashboard.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="coach-dashboard" v-loading="loading">
    <h2 style="font-size: 22px; font-weight: 700; margin: 0 0 20px 0">欢迎回来，教练</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8" v-for="card in statCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value" :style="{ color: card.color }">{{ card.value() }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-card shadow="never" style="margin-top: 24px">
      <template #header><span>快捷操作</span></template>
      <el-row :gutter="16">
        <el-col :xs="24" :sm="8">
          <router-link to="/coach/schedule" class="quick-link">
            <div class="quick-card" style="background: rgba(0,113,227,0.06)">
              <span style="font-weight: 600">查看日程</span>
              <span style="font-size: 12px; color: var(--text-secondary)">查看我的排课和学员</span>
            </div>
          </router-link>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.coach-dashboard {
  .stat-card {
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 20px;
    }
    .stat-info {
      .stat-label { font-size: 13px; color: var(--text-secondary, #86868b); }
      .stat-value { font-size: 28px; font-weight: 700; margin-top: 2px; }
    }
  }
  .quick-link { text-decoration: none; color: inherit; }
  .quick-card {
    display: flex; align-items: center; justify-content: center;
    padding: 20px 24px; border-radius: 12px;
    cursor: pointer;
    transition: transform 0.2s;
    &:hover { transform: translateY(-2px); }
  }
}
</style>
