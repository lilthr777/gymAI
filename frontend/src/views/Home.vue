<template>
  <div class="home-page">
    <div v-if="userStore.isLoggedIn()" class="welcome-card">
      <p class="greeting">{{ greeting }}，{{ userStore.nickname }}</p>
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
      <!-- 会员卡条移入 Hero -->
      <div v-if="homeData.card?.cardType" class="card-strip" @click="$router.push('/card')">
        <div class="strip-left">
          <span class="strip-type">{{ cardLabel }}</span>
          <span class="strip-days" :class="{ warn: cardRemaining <= 7 }">
            {{ cardRemaining <= 0 ? '已过期' : '剩余 ' + cardRemaining + ' 天' }}
          </span>
        </div>
        <div class="strip-right">
          <div class="strip-progress">
            <div class="strip-bar" :style="{ width: cardProgress + '%' }" :class="{ warn: cardRemaining <= 7 }"></div>
          </div>
          <span class="strip-arrow">&rsaquo;</span>
        </div>
      </div>
    </div>

    <div v-else class="welcome-card">
      <h2>欢迎来到 gymAI</h2>
      <p class="welcome-desc">注册/登录后开始你的健身之旅</p>
      <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/login')">立即登录</el-button>
    </div>

    <!-- 我的课程 -->
    <section v-if="userStore.isLoggedIn() && homeData.myCourses?.length" class="section">
      <div class="section-header">
        <h3>我的课程</h3>
        <span class="more-link" @click="$router.push('/my-courses')">全部</span>
      </div>
      <CourseCard v-for="c in homeData.myCourses" :key="c.id" :course="c" @click="goDetail(c.id)" />
    </section>

    <section class="section">
      <div class="section-header">
        <h3>推荐课程</h3>
        <span class="more-link" @click="$router.push('/courses')">全部</span>
      </div>
      <CourseCard v-for="c in homeData.upcomingCourses" :key="c.id" :course="c" @click="goDetail(c.id)" />
      <div v-if="!homeData.upcomingCourses?.length" class="empty-box">
        <span class="empty-emoji">📅</span>
        <p>还没有课程</p>
        <router-link to="/courses" class="empty-link">去看看有什么好课</router-link>
      </div>
    </section>

    <section class="section">
      <div class="section-header">
        <h3>推荐教练</h3>
        <span class="more-link" @click="$router.push('/coaches')">全部</span>
      </div>
      <CoachCard v-for="c in homeData.coaches" :key="c.id" :coach="c" @click="router.push(`/coaches/${c.id}`)" />
      <div v-if="!homeData.coaches?.length" class="empty-box">
        <span class="empty-emoji">👨‍🏫</span>
        <p>暂无教练信息</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { homeApi } from '@/api'
import type { HomeData } from '@/types'
import CourseCard from '@/components/CourseCard.vue'
import CoachCard from '@/components/CoachCard.vue'

const router = useRouter()
const userStore = useUserStore()

const homeData = ref<HomeData>({
  myCourseCount: 0, monthCheckins: 0, myCourses: [], upcomingCourses: [], coaches: [], card: {},
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 8) return '早上好'
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const cardMap: Record<string, string> = { MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', LIFETIME: '终身卡' }
const cardLabel = computed(() => cardMap[homeData.value.card?.cardType || ''] || '')
const cardRemaining = computed(() => {
  const end = homeData.value.card?.cardEndDate
  if (!end) return 999
  return Math.ceil((new Date(end).getTime() - Date.now()) / 86400000)
})
const cardProgress = computed(() => {
  const card = homeData.value.card
  if (!card?.cardType || card.cardType === 'LIFETIME' || !card.cardStartDate || !card.cardEndDate) return 100
  const start = new Date(card.cardStartDate).getTime()
  const end = new Date(card.cardEndDate).getTime()
  if (end <= start) return 0
  return Math.min(100, Math.max(0, Math.round(((Date.now() - start) / (end - start)) * 100)))
})

const goDetail = (id?: number) => { if (id) router.push(`/courses/${id}`) }

onMounted(async () => {
  if (!userStore.isLoggedIn()) return
  try { const res = await homeApi.get(); homeData.value = res.data } catch { /* handled */ }
})
</script>

<style scoped lang="scss">
.home-page { padding: 16px; }

.welcome-card {
  background: linear-gradient(135deg, $color-cobalt, $color-cobalt-dark);
  color: #fff; padding: 24px; border-radius: $radius-lg; margin-bottom: 24px;
  h2 { font-size: 22px; font-weight: 600; margin-bottom: 4px; }
  .greeting { font-size: $font-size-sm; opacity: 0.8; margin-bottom: 16px; }
  .welcome-desc { font-size: 14px; opacity: 0.85; margin-bottom: 16px; }
  .cta-btn { margin-top: 12px; }
}

.stats-row { display: flex; gap: 32px; margin-bottom: 18px; }
.stat-item { display: flex; flex-direction: column; }
.stat-num { font-size: 28px; font-weight: 700; font-family: $font-display; }
.stat-label { font-size: 12px; opacity: 0.8; }

.card-strip {
  display: flex; align-items: center; justify-content: space-between;
  padding: 12px 16px; background: rgba(255,255,255,0.12);
  border-radius: $radius-md; cursor: pointer; margin-top: 4px;
  &:hover { background: rgba(255,255,255,0.18); }
}
.strip-left { display: flex; flex-direction: column; gap: 2px; }
.strip-type { font-family: $font-display; font-size: $font-size-base; font-weight: 700; letter-spacing: 0.04em; text-transform: uppercase; }
.strip-days { font-size: $font-size-sm; opacity: 0.7; &.warn { color: #ff9f43; opacity: 1; } }
.strip-right { display: flex; align-items: center; gap: 10px; }
.strip-progress { width: 60px; height: 3px; background: rgba(255,255,255,0.2); border-radius: 2px; }
.strip-bar { height: 100%; background: #fff; border-radius: 2px; transition: width 0.5s; &.warn { background: #ff9f43; } }
.strip-arrow { font-size: 22px; opacity: 0.6; }

.section { margin-bottom: 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h3 { font-size: 17px; font-weight: 600; color: $color-carbon; margin: 0; }
}
.more-link { font-size: 13px; color: $color-cobalt; cursor: pointer; &:hover { text-decoration: underline; } }

.empty-box { text-align: center; padding: 40px 0; color: $color-lead;
  .empty-emoji { font-size: 40px; display: block; margin-bottom: 12px; }
  p { font-size: $font-size-sm; margin-bottom: 8px; }
}
.empty-link { font-size: $font-size-sm; color: $color-cobalt; text-decoration: none; font-weight: 500;
  &:hover { text-decoration: underline; }
}

html.dark {
  .section-header h3 { color: $dark-text; }
}
</style>
