<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
import type { AdminDashboard, DashboardStats } from '@/types'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const dashboard = ref<AdminDashboard>({
  totalUsers: 0, totalCourses: 0, totalCoaches: 0, todayCheckins: 0, monthlyRevenue: 0,
})
const stats = ref<DashboardStats>({ enrollmentTrend: [], revenueByMonth: [] })
const loading = ref(true)

const statCards = [
  { label: '用户总数', value: () => dashboard.value.totalUsers, color: '#0071e3' },
  { label: '课程总数', value: () => dashboard.value.totalCourses, color: '#34c759' },
  { label: '教练总数', value: () => dashboard.value.totalCoaches, color: '#ff9500' },
  { label: '今日签到', value: () => dashboard.value.todayCheckins, color: '#af52de' },
]

const enrollmentOption = ref({})
const revenueOption = ref({})

function buildCharts() {
  const months = stats.value.enrollmentTrend.map(e => e.month.substring(5))
  enrollmentOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months, boundaryGap: false },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '报名人次', type: 'line', smooth: true,
      data: stats.value.enrollmentTrend.map(e => e.count),
      areaStyle: { color: 'rgba(0,113,227,0.1)' },
      lineStyle: { color: '#0071e3', width: 2 },
      itemStyle: { color: '#0071e3' },
    }],
  }
  revenueOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [{
      name: '收入(元)', type: 'bar',
      data: stats.value.revenueByMonth.map(e => e.amount),
      itemStyle: { color: '#34c759', borderRadius: [4, 4, 0, 0] },
    }],
  }
}

onMounted(async () => {
  try {
    const [dashRes, statsRes] = await Promise.all([adminApi.dashboard(), adminApi.stats()])
    if (dashRes.data) dashboard.value = dashRes.data
    if (statsRes.data) {
      stats.value = statsRes.data
      buildCharts()
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="admin-dashboard" v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-info">
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value" :style="{ color: card.color }">{{ card.value() }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 本月收入 -->
    <el-card shadow="never" class="revenue-card" style="margin-top: 20px">
      <div style="font-size: 14px; color: var(--text-secondary, #86868b)">本月收入</div>
      <div style="font-size: 32px; font-weight: 700; color: #34c759; margin-top: 4px">
        ¥{{ dashboard.monthlyRevenue }}
      </div>
    </el-card>

    <!-- 图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :xs="24" :sm="12">
        <el-card shadow="never">
          <template #header><span>近6月报名趋势</span></template>
          <v-chart :option="enrollmentOption" style="height: 260px" autoresize />
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="never">
          <template #header><span>近6月收入趋势</span></template>
          <v-chart :option="revenueOption" style="height: 260px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped lang="scss">
.admin-dashboard {
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
  .revenue-card :deep(.el-card__body) { padding: 20px 24px; }
}
</style>
