<template>
  <div class="my-courses">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
      <h2>我的课程</h2>
    </div>

    <div v-if="list.length">
      <CourseCard
        v-for="course in list"
        :key="course.id"
        :course="course"
        @click="$router.push(`/courses/${course.id}`)"
      />
    </div>
    <el-empty v-else description="还没有报名任何课程" />

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { courseApi } from '@/api'
import type { Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const list = ref<Course[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await courseApi.myCourses({ pageNum: pageNum.value, pageSize: 10 })
    list.value.push(...res.data.records)
    total.value = res.data.total
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const loadMore = () => { pageNum.value++; fetchList() }

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.my-courses {
  padding: 16px;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: $color-carbon;
    margin: 0;
  }
}

.load-more {
  text-align: center;
  padding: 16px;
}

html.dark {
  .page-nav h2 {
    color: $dark-text;
  }
}
</style>
