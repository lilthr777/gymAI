<template>
  <div class="course-browse">
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
    </div>

    <div class="search-wrap">
      <el-input v-model="keyword" placeholder="搜索课程" clearable class="search-bar" @input="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="filter-row">
      <button v-for="f in filters" :key="f.key" class="filter-btn" :class="{ active: activeFilter === f.key }" @click="activeFilter = f.key">
        {{ f.label }}
      </button>
    </div>

    <div class="list-area">
      <CourseCard v-for="c in filteredList" :key="c.id" :course="c" @click="$router.push(`/courses/${c.id}`)" />
    </div>

    <el-empty v-if="!loading && !filteredList.length" description="暂无课程" />

    <div v-if="total > courseList.length" class="load-more">
      <el-button :loading="loading" size="large" class="more-btn" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Search, ArrowLeft } from '@element-plus/icons-vue'
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
.course-browse {
  padding: 24px 20px;
  max-width: 680px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 12px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  background: none;
  color: $color-accent;
  cursor: pointer;
  border-radius: 50%;
  transition: background $transition-fast;

  &:hover {
    background: rgba($color-accent, 0.08);
  }
}

.search-wrap {
  margin-bottom: 16px;

  :deep(.el-input__wrapper) {
    border-radius: $radius-md;
    background: $color-bg-secondary;
    border: none;
    box-shadow: none;
    padding: 4px 16px;
    height: 44px;

    &:hover {
      background: $color-border-light;
      box-shadow: none;
    }

    &.is-focus {
      background: $color-bg;
      box-shadow: 0 0 0 2px $color-accent inset;
    }
  }
}

.filter-row {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 4px;
}

.filter-btn {
  padding: 6px 18px;
  border: none;
  background: $color-bg-secondary;
  border-radius: $radius-pill;
  font-size: $font-size-sm;
  font-family: $font-family;
  color: $color-text-primary;
  cursor: pointer;
  white-space: nowrap;
  transition: all $transition-fast;
  font-weight: 500;

  &:hover {
    background: $color-border-light;
  }

  &.active {
    background: $color-text-primary;
    color: $color-bg;
  }
}

.list-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.load-more {
  text-align: center;
  padding: 24px 0;
}

.more-btn {
  border-radius: $radius-pill;
  font-weight: 500;
}

html.dark {
  .search-wrap :deep(.el-input__wrapper) {
    background: $dark-bg-secondary;
    color: $dark-text;

    &.is-focus {
      background: $dark-bg-secondary;
    }
  }

  .filter-btn {
    background: $dark-bg-secondary;
    color: $dark-text;

    &.active {
      background: $dark-text;
      color: $dark-bg;
    }
  }

  .filter-btn {
    background: $dark-bg-secondary;
    color: $dark-text;

    &.active {
      background: $dark-text;
      color: $dark-bg;
    }
  }
}
</style>
