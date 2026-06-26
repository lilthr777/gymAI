<template>
  <div class="course-browse">
    <el-input v-model="keyword" placeholder="搜索课程" clearable class="search-bar" @input="search">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>

    <CourseCard
      v-for="course in courseList"
      :key="course.id"
      :course="course"
      @click="$router.push(`/courses/${course.id}`)"
    />

    <el-empty v-if="!loading && !courseList.length" description="暂无课程" />

    <div v-if="total > courseList.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { courseApi } from '@/api'
import type { Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const keyword = ref('')
const courseList = ref<Course[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const fetchList = async (reset = false) => {
  loading.value = true
  if (reset) { pageNum.value = 1; courseList.value = [] }
  try {
    const res = await courseApi.list({ pageNum: pageNum.value, pageSize: 10, keyword: keyword.value || undefined })
    if (reset) {
      courseList.value = res.data.records
    } else {
      courseList.value.push(...res.data.records)
    }
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

const loadMore = () => {
  pageNum.value++
  fetchList()
}

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.course-browse {
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
