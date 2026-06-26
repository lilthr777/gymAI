<template>
  <div class="coach-card" @click="$emit('click')">
    <el-avatar :size="48" icon="UserFilled" :src="coach.avatar" />
    <div class="card-info">
      <div class="coach-name">{{ coach.name }}</div>
      <div class="coach-tags">
        <el-tag v-for="tag in tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
      </div>
      <div class="coach-desc">{{ coach.description?.slice(0, 40) }}{{ coach.description?.length > 40 ? '...' : '' }}</div>
    </div>
    <el-icon :size="16" color="#ccc"><ArrowRight /></el-icon>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import type { Coach } from '@/types'

const props = defineProps<{ coach: Coach }>()

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
  padding: 14px 16px;
  background: $color-sheet;
  border-radius: $radius-md;
  margin-bottom: 8px;
  cursor: pointer;
  gap: 14px;
  transition: box-shadow $transition-fast;

  &:hover {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }
}

.card-info {
  flex: 1;
}

.coach-name {
  font-size: 15px;
  font-weight: 500;
  color: $color-carbon;
  margin-bottom: 4px;
}

.coach-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

.coach-desc {
  font-size: 12px;
  color: $color-lead;
}

html.dark {
  .coach-card {
    background: $dark-bg-card;
  }

  .coach-name {
    color: $dark-text;
  }
}
</style>
