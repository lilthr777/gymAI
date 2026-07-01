<template>
  <div class="coach-card" @click="$emit('click')">
    <el-avatar :size="52" icon="UserFilled" :src="coach.avatar" />
    <div class="card-info">
      <div class="coach-name">{{ coach.name }}</div>
      <div class="coach-sub">
        <span v-if="courseCount != null" class="coach-courses">{{ courseCount }} 门课</span>
        <span v-if="tags.length" class="coach-tags">
          <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
        </span>
      </div>
    </div>
    <el-icon :size="16" class="arrow-icon"><ArrowRight /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import type { Coach } from '@/types'

const props = defineProps<{ coach: Coach; courseCount?: number }>()

defineEmits<{ click: [] }>()

const tags = computed(() => {
  if (!props.coach.specialty) return []
  return props.coach.specialty.split(',').filter(Boolean).slice(0, 3)
})
</script>

<style scoped lang="scss">
.coach-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: $color-bg;
  border-bottom: 1px solid $color-border-light;
  cursor: pointer;
  gap: 16px;
  transition: background $transition-fast;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.card-info {
  flex: 1;
  min-width: 0;
}

.coach-name {
  font-size: $font-size-base;
  font-weight: 500;
  color: $color-text-primary;
  margin-bottom: 4px;
  letter-spacing: -0.01em;
}

.coach-sub {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.coach-courses {
  font-size: $font-size-xs;
  color: $color-accent;
  font-weight: 500;
}

.coach-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.tag {
  font-size: 11px;
  color: $color-text-secondary;
  background: $color-bg-secondary;
  padding: 2px 8px;
  border-radius: $radius-pill;
}

.arrow-icon {
  color: $color-text-tertiary;
  flex-shrink: 0;
}

html.dark {
  .coach-card {
    background: $dark-bg;
    border-bottom-color: $dark-border;

    &:hover {
      background: rgba(255, 255, 255, 0.03);
    }
  }

  .coach-name {
    color: $dark-text;
  }

  .tag {
    background: $dark-bg-secondary;
  }
}
</style>
