<template>
  <div class="home-page">
    <div v-if="userStore.isLoggedIn()" class="welcome-card">
      <h2>Hi, {{ userStore.nickname }}</h2>
      <div class="stats-row">
        <div class="stat-item">
          <span class="stat-num">{{ homeData.myCourseCount }}</span>
          <span class="stat-label">我的课程</span>
        </div>
        <div class="stat-item">
          <span class="stat-num">{{ homeData.monthCheckins }}</span>
          <span class="stat-label">本月签到</span>
        </div>
      </div>
    </div>

    <div v-else class="welcome-card">
      <h2>欢迎来到 gymAI</h2>
      <p class="welcome-desc">注册/登录后开始你的健身之旅</p>
      <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/login')">立即登录</el-button>
    </div>

    <section class="section">
      <div class="section-header">
        <h3>推荐课程</h3>
        <span class="more-link" @click="$router.push('/courses')">全部</span>
      </div>
      <CourseCard v-for="course in homeData.upcomingCourses" :key="course.id" :course="course" @click="goDetail(course.id)" />
      <el-empty v-if="!homeData.upcomingCourses?.length" description="暂无课程" />
    </section>

    <section class="section">
      <div class="section-header">
        <h3>推荐教练</h3>
        <span class="more-link" @click="$router.push('/coaches')">全部</span>
      </div>
      <CoachCard v-for="coach in homeData.coaches" :key="coach.id" :coach="coach" @click="$router.push(`/coaches/${coach.id}`)" />
      <el-empty v-if="!homeData.coaches?.length" description="暂无教练" />
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { homeApi } from '@/api'
import type { HomeData, Course, Coach } from '@/types'
import CourseCard from '@/components/CourseCard.vue'
import CoachCard from '@/components/CoachCard.vue'

const router = useRouter()
const userStore = useUserStore()

const homeData = ref<HomeData>({
  myCourseCount: 0,
  monthCheckins: 0,
  upcomingCourses: [],
  coaches: [],
})

const goDetail = (id?: number) => {
  if (id) router.push(`/courses/${id}`)
}

const fetchHome = async () => {
  if (!userStore.isLoggedIn()) return
  try {
    const res = await homeApi.get()
    homeData.value = res.data
  } catch {
    // handled by interceptor
  }
}

onMounted(() => {
  // 从首页接口加载数据，未登录时只展示课程和教练
  fetchHome()
})
</script>

<style scoped lang="scss">
.home-page {
  padding: 16px;
}

.welcome-card {
  background: linear-gradient(135deg, $color-cobalt, $color-cobalt-dark);
  color: #fff;
  padding: 24px;
  border-radius: $radius-lg;
  margin-bottom: 24px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    margin-bottom: 4px;
  }

  .welcome-desc {
    font-size: 14px;
    opacity: 0.85;
    margin-bottom: 16px;
  }

  .cta-btn {
    margin-top: 12px;
  }
}

.stats-row {
  display: flex;
  gap: 32px;
  margin-top: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
}

.stat-num {
  font-size: 28px;
  font-weight: 700;
  font-family: $font-display;
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
}

.section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  h3 {
    font-size: 17px;
    font-weight: 600;
    color: $color-carbon;
    margin: 0;
  }
}

.more-link {
  font-size: 13px;
  color: $color-cobalt;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

html.dark {
  .section-header h3 {
    color: $dark-text;
  }
}
</style>
