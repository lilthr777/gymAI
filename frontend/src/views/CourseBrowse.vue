<template>
  <div class="course-browse">
    <el-input v-model="keyword" placeholder="搜索课程" clearable class="search-bar" @input="search">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>

    <div class="filter-row">
      <button v-for="f in filters" :key="f.key" class="filter-btn" :class="{ active: activeFilter === f.key }" @click="activeFilter = f.key">
        {{ f.label }}
      </button>
    </div>

    <CourseCard v-for="c in filteredList" :key="c.id" :course="c" @click="$router.push(`/courses/${c.id}`)" />

    <el-empty v-if="!loading && !filteredList.length" description="暂无课程" />

    <div v-if="total > courseList.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { courseApi } from '@/api'
import type { Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const userStore = useUserStore()
const keyword = ref('')
const activeFilter = ref('all')
const courseList = ref<Course[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const filters = [
  { key: 'all', label: '全部' },
  { key: 'available', label: '可报名' },
  { key: 'registered', label: '已报名' },
]

const filteredList = computed(() => {
  if (activeFilter.value === 'registered') return courseList.value.filter(c => c.registered)
  if (activeFilter.value === 'available') return courseList.value.filter(c => !c.registered && c.status === 1)
  return courseList.value
})

const fetchList = async (reset = false) => {
  loading.value = true
  if (reset) { pageNum.value = 1; courseList.value = [] }
  try {
    const res = await courseApi.list({ pageNum: pageNum.value, pageSize: 10, keyword: keyword.value || undefined })
    if (reset) courseList.value = res.data.records
    else courseList.value.push(...res.data.records)
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

let timer: ReturnType<typeof setTimeout>
const search = () => { clearTimeout(timer); timer = setTimeout(() => fetchList(true), 300) }

const loadMore = () => { pageNum.value++; fetchList() }

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.course-browse { padding: 16px; }

.search-bar { margin-bottom: 12px; :deep(.el-input__wrapper) { border-radius: 20px; } }

.filter-row { display: flex; gap: 8px; margin-bottom: 14px; }

.filter-btn {
  padding: 6px 16px; border: 1px solid $color-steel; background: $color-sheet; border-radius: 20px;
  font-size: $font-size-sm; color: $color-lead; cursor: pointer; transition: all $transition-fast; font-family: $font-body;
  &:hover { border-color: $color-cobalt; color: $color-cobalt; }
  &.active { background: $color-cobalt; color: #fff; border-color: $color-cobalt; }
}

.load-more { text-align: center; padding: 16px; }

html.dark {
  .filter-btn { background: $dark-bg-card; border-color: $dark-border; color: $dark-text-secondary; }
  .filter-btn.active { background: $color-cobalt; color: #fff; border-color: $color-cobalt; }
}
</style>
