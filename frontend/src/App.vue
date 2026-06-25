<template>
  <router-view />
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { watchEffect } from 'vue'

const appStore = useAppStore()

watchEffect(() => {
  document.documentElement.classList.toggle('dark', appStore.isDark)
})
</script>
