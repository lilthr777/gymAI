<template>
  <div class="course-card" @click="$emit('click')">
    <div class="card-left">
      <div class="card-date">{{ course.courseDate }}</div>
      <div class="card-hours">{{ course.startTime?.slice(0, 5) }} - {{ course.endTime?.slice(0, 5) }}</div>
    </div>
    <div class="card-body">
      <div class="card-name">{{ course.name }}</div>
      <div class="card-meta">
        <span v-if="course.coachName">{{ course.coachName }}</span>
        <span v-if="course.coachName" class="meta-sep">&middot;</span>
        <span>{{ course.currentCount }}/{{ course.maxCapacity }}人</span>
      </div>
    </div>
    <div class="card-right">
      <span class="status-badge" :class="statusClass">{{ tagText }}</span>
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

const statusClass = computed(() => {
  if (props.course.status === 0) return 'status--danger'
  if (props.course.status === 2) return 'status--warning'
  if (props.course.currentCount >= props.course.maxCapacity) return 'status--warning'
  if (props.course.registered) return 'status--default'
  return 'status--success'
})
</script>

<style scoped lang="scss">
.course-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: $color-bg;
  border-bottom: 1px solid $color-border-light;
  cursor: pointer;
  transition: background $transition-fast;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.card-left {
  text-align: center;
  min-width: 72px;
  margin-right: 16px;
}

.card-date {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: -0.01em;
}

.card-hours {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: 2px;
}

.card-body {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-size: $font-size-base;
  font-weight: 500;
  color: $color-text-primary;
  margin-bottom: 3px;
  letter-spacing: -0.01em;
}

.card-meta {
  font-size: $font-size-xs;
  color: $color-text-secondary;

  .meta-sep {
    margin: 0 6px;
  }
}

.card-right {
  margin-left: 12px;
  flex-shrink: 0;
}

.status-badge {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: $radius-pill;

  &.status--success {
    color: $color-success;
    background: rgba($color-success, 0.1);
  }

  &.status--warning {
    color: $color-warning;
    background: rgba($color-warning, 0.1);
  }

  &.status--danger {
    color: $color-danger;
    background: rgba($color-danger, 0.1);
  }

  &.status--default {
    color: $color-accent;
    background: rgba($color-accent, 0.1);
  }
}

html.dark {
  .course-card {
    background: $dark-bg;
    border-bottom-color: $dark-border;

    &:hover {
      background: rgba(255, 255, 255, 0.03);
    }
  }

  .card-date,
  .card-name {
    color: $dark-text;
  }
}
</style>
