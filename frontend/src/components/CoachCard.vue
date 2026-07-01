<template>
  <div class="coach-card" @click="$emit('click')">
    <el-avatar :size="48" icon="UserFilled" :src="coach.avatar" />
    <div class="card-info">
      <div class="coach-name">{{ coach.name }}</div>
      <div class="coach-sub">
        <span v-if="courseCount != null" class="coach-courses">{{ courseCount }} 门课</span>
        <span v-if="tags.length" class="coach-tags">
          <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
        </span>
      </div>
    </div>
    <el-icon :size="16" color="#ccc"><ArrowRight /></el-icon>
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
  display: flex; align-items: center; padding: 14px 16px;
  background: $color-sheet; border-radius: $radius-md; margin-bottom: 8px;
  cursor: pointer; gap: 14px; transition: all 0.2s ease;
  &:hover { box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); transform: translateY(-1px); }
}

.card-info { flex: 1; min-width: 0; }

.coach-name { font-size: 15px; font-weight: 500; color: $color-carbon; margin-bottom: 4px; }

.coach-sub { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }

.coach-courses { font-size: 12px; color: $color-cobalt; font-weight: 500; }

.coach-tags { display: flex; gap: 4px; flex-wrap: wrap; }

.tag { font-size: 11px; color: $color-lead; background: $color-magnesium; padding: 1px 8px; border-radius: 10px; }

html.dark {
  .coach-card { background: $dark-bg-card; }
  .coach-name { color: $dark-text; }
  .tag { background: rgba(255,255,255,0.06); }
}
</style>
