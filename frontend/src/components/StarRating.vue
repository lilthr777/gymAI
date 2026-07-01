<template>
  <span class="star-rating" :class="{ readonly: !interactive, interactive }">
    <span v-for="s in 5" :key="s" class="star" :class="{ filled: s <= modelValue }"
          @click="interactive && $emit('update:modelValue', s)">
      {{ s <= modelValue ? '★' : '☆' }}
    </span>
  </span>
</template>

<script setup lang="ts">
defineProps<{ modelValue: number; interactive?: boolean }>()
defineEmits<{ 'update:modelValue': [value: number] }>()
</script>

<style scoped lang="scss">
.star {
  font-size: 20px;
  margin-right: 2px;
  color: $color-border-light;

  &.filled {
    color: #ff9f0a;
  }
}

.interactive .star {
  cursor: pointer;

  &:hover {
    color: #ff9f0a;
  }
}

html.dark {
  .star {
    color: rgba(255, 255, 255, 0.15);

    &.filled {
      color: #ff9f0a;
    }
  }
}
</style>
