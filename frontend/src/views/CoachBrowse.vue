<template>
  <div class="coach-browse">
    <el-input v-model="keyword" placeholder="搜索教练" clearable class="search-bar" @input="search">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>

    <CoachCard
      v-for="coach in list"
      :key="coach.id"
      :coach="coach"
      @click="$router.push(`/coaches/${coach.id}`)"
    />

    <el-empty v-if="!loading && !list.length" description="暂无教练" />

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { coachApi } from '@/api'
import type { Coach } from '@/types'
import CoachCard from '@/components/CoachCard.vue'

const keyword = ref('')
const list = ref<Coach[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const fetchList = async (reset = false) => {
  loading.value = true
  if (reset) { pageNum.value = 1; list.value = [] }
  try {
    const res = await coachApi.list({ pageNum: pageNum.value, pageSize: 10, keyword: keyword.value || undefined })
    if (reset) list.value = res.data.records
    else list.value.push(...res.data.records)
    total.value = res.data.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

let timer: ReturnType<typeof setTimeout>
const search = () => {
  clearTimeout(timer)
  timer = setTimeout(() => fetchList(true), 300)
}

const loadMore = () => { pageNum.value++; fetchList() }

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.coach-browse {
  padding: 16px;
}

.search-bar {
  margin-bottom: 16px;
  :deep(.el-input__wrapper) {
    border-radius: 20px;
  }
}

.load-more {
  text-align: center;
  padding: 16px;
}
</style>
