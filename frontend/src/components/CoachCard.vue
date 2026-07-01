<template>
  <div class="coach-card" @click="$emit('click')">
    <el-avatar :size="60" icon="UserFilled" :src="coach.avatar" />
    <div class="card-body">
      <h4 class="coach-name">{{ coach.name }}</h4>
      <div class="coach-sub">
        <span v-if="courseCount != null" class="coach-courses">{{ courseCount }} 门课</span>
        <span v-if="tags.length" class="coach-tags">
          <span v-for="tag in tags" :key="tag" class="tag">{{ tag }}</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
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
  gap: 16px;
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

.card-body {
  flex: 1;
  min-width: 0;
}

.coach-name {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
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

html.dark {
  .coach-card {
    background: $dark-bg-secondary;

    &:hover {
      background: #222225;
    }
  }

  .coach-name {
    color: $dark-text;
  }

  .tag {
    background: rgba(255, 255, 255, 0.06);
  }
}
</style>
