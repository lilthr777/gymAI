<template>
  <div class="course-card" @click="$emit('click')">
    <div class="card-left">
      <div class="card-time">{{ course.courseDate }}</div>
      <div class="card-hours">{{ course.startTime?.slice(0, 5) }} - {{ course.endTime?.slice(0, 5) }}</div>
    </div>
    <div class="card-body">
      <div class="card-name">{{ course.name }}</div>
      <div class="card-meta">
        <span v-if="course.coachName">{{ course.coachName }}</span>
        <span v-if="course.coachName" class="meta-sep">|</span>
        <span>{{ course.currentCount }}/{{ course.maxCapacity }}人</span>
      </div>
    </div>
    <div class="card-right">
      <el-tag :type="tagType" size="small">{{ tagText }}</el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Course } from '@/types'

const props = defineProps<{ course: Course }>()

defineEmits<{ click: [] }>()

const tagText = computed(() => {
  if (props.course.status === 0) return '已取消'
  if (props.course.status === 2) return '已满员'
  if (props.course.currentCount >= props.course.maxCapacity) return '已满员'
  if (props.course.registered) return '已报名'
  return '可报名'
})

const tagType = computed(() => {
  if (props.course.status === 0) return 'danger'
  if (props.course.status === 2) return 'warning'
  if (props.course.currentCount >= props.course.maxCapacity) return 'warning'
  if (props.course.registered) return ''
  return 'success'
})
</script>

<style scoped lang="scss">
.course-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: $color-sheet;
  border-radius: $radius-md;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-1px);
  }
}

.card-left {
  text-align: center;
  min-width: 64px;
  margin-right: 14px;
}

.card-time {
  font-size: 13px;
  font-weight: 600;
  color: $color-carbon;
}

.card-hours {
  font-size: 11px;
  color: $color-lead;
  margin-top: 2px;
}

.card-body {
  flex: 1;
}

.card-name {
  font-size: 15px;
  font-weight: 500;
  color: $color-carbon;
  margin-bottom: 4px;
}

.card-meta {
  font-size: 12px;
  color: $color-lead;

  .meta-sep {
    margin: 0 4px;
    opacity: 0.5;
  }
}

html.dark {
  .course-card {
    background: $dark-bg-card;
  }

  .card-time,
  .card-name {
    color: $dark-text;
  }
}
</style>
