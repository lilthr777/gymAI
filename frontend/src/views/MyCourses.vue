<template>
  <div class="my-courses">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
      <h2>我的课程</h2>
    </div>

    <div class="tab-row">
      <button class="tab-btn" :class="{ active: tab === 'upcoming' }" @click="tab = 'upcoming'">待上课</button>
      <button class="tab-btn" :class="{ active: tab === 'past' }" @click="tab = 'past'">已结束</button>
    </div>

    <div v-if="filteredList.length">
      <CourseCard v-for="c in filteredList" :key="c.id" :course="c" @click="$router.push(`/courses/${c.id}`)" />
    </div>
    <div v-else class="empty-box">
      <span class="empty-emoji">📋</span>
      <p>还没有报名任何课程</p>
      <router-link to="/courses" class="empty-link">去看看有什么好课</router-link>
    </div>

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
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
.my-courses { padding: 16px; }

.page-nav { display: flex; align-items: center; gap: 12px; margin-bottom: 16px;
  h2 { font-size: 20px; font-weight: 600; color: $color-carbon; margin: 0; }
}

.tab-row { display: flex; gap: 8px; margin-bottom: 14px; }

.tab-btn {
  padding: 6px 20px; border: 1px solid $color-steel; background: $color-sheet; border-radius: 20px;
  font-size: $font-size-sm; color: $color-lead; cursor: pointer; transition: all $transition-fast; font-family: $font-body;
  &:hover { border-color: $color-cobalt; color: $color-cobalt; }
  &.active { background: $color-cobalt; color: #fff; border-color: $color-cobalt; }
}

.load-more { text-align: center; padding: 16px; }

.empty-box { text-align: center; padding: 40px 0; color: $color-lead;
  .empty-emoji { font-size: 40px; display: block; margin-bottom: 12px; }
  p { font-size: $font-size-sm; margin-bottom: 8px; }
}
.empty-link { font-size: $font-size-sm; color: $color-cobalt; text-decoration: none; font-weight: 500;
  &:hover { text-decoration: underline; }
}

html.dark {
  .page-nav h2 { color: $dark-text; }
  .tab-btn { background: $dark-bg-card; border-color: $dark-border; color: $dark-text-secondary; }
  .tab-btn.active { background: $color-cobalt; color: #fff; border-color: $color-cobalt; }
}
</style>
