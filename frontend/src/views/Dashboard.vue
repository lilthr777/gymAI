<template>
  <div class="dashboard-page">
    <!-- Stat Cards -->
    <div class="stat-grid">
      <div
        v-for="(card, i) in statCards"
        :key="card.label"
        class="stat-card"
        :class="card.accentClass"
        :style="{ animationDelay: `${0.1 + i * 0.1}s` }"
      >
        <div class="stat-ring"></div>
        <span class="stat-value" :class="{ pulse: true }">{{ card.value }}</span>
        <span class="stat-label">{{ card.label }}</span>
      </div>
    </div>

    <!-- Charts Row 1 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-card__header">
          <h3 class="chart-card__title">会员增长趋势</h3>
        </div>
        <div v-loading="loading" class="chart-card__body">
          <div ref="growthChartRef" class="chart-box"></div>
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-card__header">
          <h3 class="chart-card__title">课程类型分布</h3>
        </div>
        <div v-loading="loading" class="chart-card__body">
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </div>
    </div>

    <!-- Charts Row 2 -->
    <div class="charts-row">
      <div class="chart-card chart-card--full">
        <div class="chart-card__header">
          <h3 class="chart-card__title">本周签到统计</h3>
        </div>
        <div v-loading="loading" class="chart-card__body">
          <div ref="barChartRef" class="chart-box chart-box--large"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'
import { dashboardApi } from '@/api'
import { useChart } from '@/composables/useChart'
import type { ChartDataItem } from '@/types'

const loading = ref(false)

const stats = ref({
  totalMembers: 0,
  totalCoaches: 0,
  totalCourses: 0,
  todayCheckins: 0,
})

const memberGrowth = ref<ChartDataItem[]>([])
const courseDistribution = ref<ChartDataItem[]>([])
const weeklyCheckins = ref<ChartDataItem[]>([])

const statCards = computed(() => [
  { label: '会员总数', value: stats.value.totalMembers, accentClass: 'accent--cobalt' },
  { label: '教练总数', value: stats.value.totalCoaches, accentClass: 'accent--green' },
  { label: '课程总数', value: stats.value.totalCourses, accentClass: 'accent--amber' },
  { label: '今日签到', value: stats.value.todayCheckins, accentClass: 'accent--iron' },
])

const COBALT = '#3B6EB0'
const GREEN = '#5C9A4F'
const LEAD = '#6B6E73'
const STEEL = '#D9D5CE'
const DARK_BORDER = '#3A3D42'

const growthChartRef = ref<HTMLDivElement>()
const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()

const { initChart: initGrowthChart, updateChart: updateGrowthChart } = useChart(growthChartRef, {})
const { initChart: initPieChart, updateChart: updatePieChart } = useChart(pieChartRef, {})
const { initChart: initBarChart, updateChart: updateBarChart } = useChart(barChartRef, {})

const isDark = () => document.documentElement.classList.contains('dark')

const textColor = () => isDark() ? '#9B9EA3' : LEAD
const borderColor = () => isDark() ? DARK_BORDER : STEEL

const buildGrowthOption = (data: ChartDataItem[]) => ({
  tooltip: { trigger: 'axis' as const },
  grid: { left: '1%', right: '2%', bottom: '2%', top: '8%', containLabel: true },
  xAxis: {
    type: 'category' as const,
    boundaryGap: false,
    data: data.map((d) => d.name),
    axisLine: { lineStyle: { color: borderColor() } },
    axisTick: { show: false },
    axisLabel: { color: textColor(), fontSize: 12 },
  },
  yAxis: {
    type: 'value' as const,
    splitLine: { lineStyle: { color: borderColor(), type: 'dashed' as const } },
    axisLabel: { color: textColor(), fontSize: 12 },
  },
  series: [{
    name: '新增会员',
    type: 'line' as const,
    data: data.map((d) => d.value),
    smooth: true,
    symbol: 'circle' as const,
    symbolSize: 6,
    lineStyle: { color: COBALT, width: 2.5 },
    itemStyle: { color: COBALT },
    areaStyle: {
      color: {
        type: 'linear' as const, x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(59, 110, 176, 0.12)' },
          { offset: 1, color: 'rgba(59, 110, 176, 0.0)' },
        ],
      },
    },
  }],
})

const buildPieOption = (data: ChartDataItem[]) => ({
  tooltip: { trigger: 'item' as const },
  legend: { bottom: 0, textStyle: { color: textColor(), fontSize: 12 } },
  color: [COBALT, GREEN, '#D4953B', '#C4523D', '#5B8EC8', '#7AB86C'],
  series: [{
    name: '课程分布',
    type: 'pie' as const,
    radius: ['50%', '72%'],
    center: ['50%', '44%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 3, borderColor: isDark() ? '#25282C' : '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 13, fontWeight: 600 as const } },
    data: data.map((d) => ({ name: d.name, value: d.value })),
  }],
})

const buildBarOption = (data: ChartDataItem[]) => ({
  tooltip: { trigger: 'axis' as const },
  grid: { left: '1%', right: '2%', bottom: '2%', top: '8%', containLabel: true },
  xAxis: {
    type: 'category' as const,
    data: data.map((d) => d.name),
    axisTick: { alignWithLabel: true, show: false },
    axisLine: { lineStyle: { color: borderColor() } },
    axisLabel: { color: textColor(), fontSize: 12 },
  },
  yAxis: {
    type: 'value' as const,
    splitLine: { lineStyle: { color: borderColor(), type: 'dashed' as const } },
    axisLabel: { color: textColor(), fontSize: 12 },
  },
  series: [{
    name: '签到人数',
    type: 'bar' as const,
    data: data.map((d) => d.value),
    barWidth: '44%',
    itemStyle: {
      borderRadius: [4, 4, 0, 0] as any,
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: COBALT },
        { offset: 1, color: '#5B8EC8' },
      ]),
    },
  }],
})

// Dynamic import for echarts graphic in bar
import * as echarts from 'echarts'

const fetchData = async () => {
  loading.value = true
  try {
    const [statsRes, growthRes, pieRes, barRes] = await Promise.all([
      dashboardApi.stats(),
      dashboardApi.memberGrowth(),
      dashboardApi.courseTypeDistribution(),
      dashboardApi.checkinWeekly(),
    ])
    stats.value = statsRes.data
    memberGrowth.value = growthRes.data.map((d: any) => ({ name: d.month, value: d.count }))
    courseDistribution.value = pieRes.data
    weeklyCheckins.value = barRes.data.map((d: any) => ({ name: d.day, value: d.count }))
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await fetchData()
  await initGrowthChart()
  await initPieChart()
  await initBarChart()
  updateGrowthChart(buildGrowthOption(memberGrowth.value))
  updatePieChart(buildPieOption(courseDistribution.value))
  updateBarChart(buildBarOption(weeklyCheckins.value))
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: $content-padding;
  max-width: 1400px;
}

// ── Stat Grid ────────────────────────────────
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: $gap-unit;
}

.stat-card {
  position: relative;
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: $radius-lg;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow: hidden;
  animation: card-enter 0.5s ease-out both;

  // Accent left line
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 16px;
    bottom: 16px;
    width: 3px;
    border-radius: 0 3px 3px 0;
  }

  &.accent--cobalt::before { background: $color-cobalt; }
  &.accent--green::before { background: #5C9A4F; }
  &.accent--amber::before { background: #D4953B; }
  &.accent--iron::before { background: $color-iron-red; }
}

// The breathing ring
.stat-ring {
  position: absolute;
  inset: 0;
  border-radius: $radius-lg;
  border: 1.5px solid $color-cobalt;
  opacity: 0;
  pointer-events: none;
  animation: ring-expand 1.4s ease-out forwards;
}

@keyframes ring-expand {
  0% {
    inset: 4px;
    opacity: 0.5;
    border-width: 2px;
  }
  60% {
    opacity: 0.15;
  }
  100% {
    inset: -8px;
    opacity: 0;
    border-width: 0.5px;
  }
}

.stat-value {
  font-family: $font-display;
  font-size: 38px;
  font-weight: 600;
  line-height: 1;
  color: $color-carbon;
  letter-spacing: 0.02em;
  animation: value-land 0.6s ease-out both;
}

@keyframes value-land {
  0% {
    opacity: 0;
    transform: translateY(8px) scale(0.96);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.stat-label {
  font-size: 13px;
  color: $color-lead;
  font-weight: 500;
}

@keyframes card-enter {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

// ── Charts ───────────────────────────────────
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-card {
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: $radius-lg;
  overflow: hidden;

  &--full {
    grid-column: 1 / -1;
  }

  &__header {
    padding: 18px 24px 0;
  }

  &__title {
    font-size: 15px;
    font-weight: 600;
    color: $color-carbon;
    margin: 0;
  }

  &__body {
    padding: 16px 20px 20px;
  }
}

.chart-box {
  width: 100%;
  height: 300px;

  &--large {
    height: 320px;
  }
}

// ── Dark Mode ────────────────────────────────
html.dark {
  .stat-card {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .stat-value {
    color: $dark-text;
  }

  .chart-card {
    background: $dark-bg-secondary;
    border-color: $dark-border;

    &__title {
      color: $dark-text;
    }
  }
}

// ── Responsive ───────────────────────────────
@media (max-width: 1024px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .dashboard-page {
    padding: 16px;
  }

  .stat-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-value {
    font-size: 32px;
  }

  .chart-box {
    height: 240px;

    &--large {
      height: 260px;
    }
  }
}
</style>
