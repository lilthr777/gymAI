<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { coachBackendApi } from '@/api'
import type { CoachScheduleItem } from '@/types'

const schedule = ref<CoachScheduleItem[]>([])
const loading = ref(true)
const expandedCourses = ref<number[]>([])

// 按课程分组（同一课程多个会员）
const groupedSchedule = computed(() => {
  const map = new Map<number, CoachScheduleItem[]>()
  for (const item of schedule.value) {
    if (!map.has(item.courseId)) map.set(item.courseId, [])
    map.get(item.courseId)!.push(item)
  }
  return Array.from(map.entries()).map(([courseId, items]) => ({
    courseId,
    courseName: items[0].courseName,
    courseDate: items[0].courseDate,
    startTime: items[0].startTime,
    endTime: items[0].endTime,
    location: items[0].location,
    maxCapacity: items[0].maxCapacity,
    currentCount: items[0].currentCount,
    price: items[0].price,
    members: items.filter(i => i.memberName),
  }))
})

function toggleExpand(courseId: number) {
  const idx = expandedCourses.value.indexOf(courseId)
  if (idx > -1) expandedCourses.value.splice(idx, 1)
  else expandedCourses.value.push(courseId)
}

function isExpanded(courseId: number) {
  return expandedCourses.value.includes(courseId)
}

onMounted(async () => {
  try {
    const res = await coachBackendApi.schedule()
    if (res.data) schedule.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="coach-schedule" v-loading="loading">
    <h2 style="font-size: 22px; font-weight: 700; margin: 0 0 20px 0">我的日程</h2>

    <div v-if="groupedSchedule.length === 0 && !loading" class="empty-state">
      暂无排课日程
    </div>

    <div v-for="group in groupedSchedule" :key="group.courseId" class="schedule-card">
      <el-card shadow="never" @click="toggleExpand(group.courseId)">
        <div class="course-header">
          <div class="course-info">
            <h3 class="course-name">{{ group.courseName }}</h3>
            <div class="course-meta">
              <span>{{ group.courseDate }}</span>
              <span>{{ group.startTime }} - {{ group.endTime }}</span>
              <span v-if="group.location">{{ group.location }}</span>
            </div>
          </div>
          <div class="course-stats">
            <span class="capacity-badge">
              {{ group.members.length }}/{{ group.maxCapacity }} 人
            </span>
            <span v-if="group.price" style="color: #ff9500; font-weight: 600">¥{{ group.price }}</span>
          </div>
        </div>
      </el-card>

      <!-- 展开显示报名会员 -->
      <transition name="expand">
        <div v-if="isExpanded(group.courseId)" class="members-panel">
          <div v-if="group.members.length === 0" class="no-members">暂无报名</div>
          <el-table v-else :data="group.members" size="small" stripe>
            <el-table-column prop="memberName" label="会员姓名" min-width="120" />
            <el-table-column prop="memberPhone" label="联系电话" min-width="140" />
          </el-table>
        </div>
      </transition>
    </div>
  </div>
</template>

<style scoped lang="scss">
.coach-schedule {
  .schedule-card {
    margin-bottom: 12px;
    cursor: pointer;

    .course-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .course-name {
        font-size: 16px;
        font-weight: 600;
        margin: 0 0 6px 0;
      }
      .course-meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: var(--text-secondary, #86868b);
      }
      .course-stats {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 4px;
      }
      .capacity-badge {
        background: rgba(0,113,227,0.1);
        color: #0071e3;
        padding: 2px 10px;
        border-radius: 10px;
        font-size: 13px;
        font-weight: 500;
      }
    }
    .members-panel {
      background: var(--bg-card, #ffffff);
      border: 1px solid var(--border-color, #e5e5ea);
      border-top: none;
      border-radius: 0 0 8px 8px;
      padding: 12px;
    }
    .no-members {
      text-align: center;
      color: var(--text-secondary, #86868b);
      padding: 16px;
      font-size: 14px;
    }
  }
  .empty-state {
    text-align: center;
    padding: 80px 0;
    font-size: 16px;
    color: var(--text-secondary, #86868b);
  }
}

.expand-enter-active, .expand-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.expand-enter-from, .expand-leave-to {
  opacity: 0;
  max-height: 0;
}
.expand-enter-to, .expand-leave-from {
  opacity: 1;
  max-height: 300px;
}
</style>
