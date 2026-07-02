<template>
  <div class="course-card" @click="$emit('click')">
    <div class="card-top">
      <div class="card-date">{{ course.courseDate?.slice(5) }}</div>
      <div class="card-status">
        <span class="status-badge" :class="statusClass">{{ tagText }}</span>
      </div>
    </div>
    <h4 class="card-name">{{ course.name }}</h4>
    <div class="card-bottom">
      <span class="card-time">{{ course.startTime?.slice(0, 5) }} - {{ course.endTime?.slice(0, 5) }}</span>
      <span class="meta-sep">&middot;</span>
      <span v-if="course.coachName">{{ course.coachName }}</span>
      <span class="meta-sep">&middot;</span>
      <span>{{ course.currentCount }}/{{ course.maxCapacity }}人</span>
    </div>
    <div class="card-extra" v-if="course.price || course.location">
      <span v-if="course.price" class="card-price">¥{{ course.price }}</span>
      <span v-if="course.location" class="card-location">📍 {{ course.location }}</span>
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
  padding: 20px;
  background: $color-bg;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-1px);
    box-shadow: $shadow-md;
  }
}

.card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.card-date {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $color-accent;
  letter-spacing: -0.01em;
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

.card-name {
  font-size: $font-size-xl;
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: -0.02em;
  line-height: 1.2;
  margin-bottom: 10px;
}

.card-bottom {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: $font-size-xs;
  color: $color-text-secondary;

  .meta-sep {
    opacity: 0.4;
    font-size: 8px;
  }
}

.card-time {
  font-weight: 500;
  color: $color-text-primary;
}

.card-extra {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  font-size: $font-size-xs;
}

.card-price {
  color: #ff9500;
  font-weight: 600;
}

.card-location {
  color: $color-text-secondary;
  font-size: 11px;
}

html.dark {
  .course-card {
    background: $dark-bg-secondary;

    &:hover {
      background: #222225;
    }
  }

  .card-name {
    color: $dark-text;
  }

  .card-time {
    color: $dark-text;
  }
}
</style>
