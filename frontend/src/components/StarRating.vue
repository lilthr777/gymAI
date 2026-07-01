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
.star { font-size: 18px; margin-right: 2px; color: #ddd;
  &.filled { color: #f5a623; }
}
.interactive .star { cursor: pointer;
  &:hover { color: #f5a623; }
}
</style>
