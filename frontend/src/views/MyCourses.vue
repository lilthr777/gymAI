<template>
  <div class="my-courses">
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h2>我的课程</h2>
    </div>

    <div class="tab-row">
      <button class="tab-btn" :class="{ active: tab === 'upcoming' }" @click="tab = 'upcoming'">待上课</button>
      <button class="tab-btn" :class="{ active: tab === 'past' }" @click="tab = 'past'">已结束</button>
    </div>

    <div v-if="filteredList.length" class="course-list">
      <CourseCard v-for="c in filteredList" :key="c.id" :course="c" @click="$router.push(`/courses/${c.id}`)" />
    </div>
    <div v-else class="empty-box">
      <p>还没有报名任何课程</p>
      <router-link to="/courses" class="empty-link">去看看有什么好课 &rarr;</router-link>
    </div>

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" size="large" class="more-btn" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { courseApi } from '@/api'
import type { Course } from '@/types'
import CourseCard from '@/components/CourseCard.vue'

const tab = ref('upcoming')
const list = ref<Course[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const filteredList = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  if (tab.value === 'past') return list.value.filter(c => c.courseDate < today)
  return list.value.filter(c => c.courseDate >= today)
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await courseApi.myCourses({ pageNum: pageNum.value, pageSize: 20 })
    list.value.push(...res.data.records)
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

const loadMore = () => { pageNum.value++; fetchList() }

onMounted(() => fetchList())
</script>

<style scoped lang="scss">
.my-courses {
  padding: 24px 20px;
  max-width: 680px;
  margin: 0 auto;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  h2 {
    font-size: $font-size-xl;
    font-weight: 600;
    color: $color-text-primary;
    letter-spacing: -0.01em;
  }
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: none;
  background: none;
  color: $color-accent;
  font-size: $font-size-base;
  font-family: $font-family;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

.tab-row {
  display: flex;
  gap: 4px;
  background: $color-bg-secondary;
  border-radius: $radius-pill;
  padding: 2px;
  width: fit-content;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 6px 20px;
  border: none;
  background: transparent;
  border-radius: $radius-pill;
  font-size: $font-size-sm;
  font-family: $font-family;
  color: $color-text-secondary;
  cursor: pointer;
  transition: all $transition-fast;
  font-weight: 500;

  &.active {
    background: $color-bg;
    color: $color-text-primary;
    box-shadow: $shadow-sm;
  }
}

.course-list {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
}

.load-more {
  text-align: center;
  padding: 24px 0;
}

.more-btn {
  border-radius: $radius-pill;
  font-weight: 500;
}

.empty-box {
  text-align: center;
  padding: 48px 0;
  color: $color-text-secondary;

  p {
    font-size: $font-size-sm;
    margin-bottom: 8px;
  }
}

.empty-link {
  font-size: $font-size-sm;
  color: $color-accent;
  text-decoration: none;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

html.dark {
  .page-nav h2 {
    color: $dark-text;
  }

  .tab-row {
    background: $dark-bg-secondary;

    .tab-btn.active {
      background: $dark-border-secondary;
      color: $dark-text;
    }
  }

  .course-list {
    background: $dark-bg;
    border-color: $dark-border;
  }
}
</style>
