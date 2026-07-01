<template>
  <div class="home-page">
    <!-- ======== Logged-in Hero ======== -->
    <div v-if="userStore.isLoggedIn()" class="hero">
      <p class="hero-greet">{{ greeting }}，{{ userStore.nickname }}</p>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="hero-num">{{ homeData.myCourseCount }}</span>
          <span class="hero-label">我的课程</span>
        </div>
        <div class="hero-stat">
          <span class="hero-num">{{ homeData.monthCheckins }}</span>
          <span class="hero-label">本月签到</span>
        </div>
      </div>
      <!-- Membership strip -->
      <div v-if="homeData.card?.cardType" class="hero-card-strip" @click="$router.push('/card')">
        <div class="strip-info">
          <span class="strip-type">{{ cardLabel }}</span>
          <span class="strip-days" :class="{ warn: cardRemaining <= 7 }">
            {{ cardRemaining <= 0 ? '已过期' : '剩余 ' + cardRemaining + ' 天' }}
          </span>
        </div>
        <div class="strip-bar">
          <div class="strip-fill" :style="{ width: cardProgress + '%' }" :class="{ warn: cardRemaining <= 7 }"></div>
        </div>
      </div>
      <!-- Upcoming courses -->
      <div v-if="upcomingMyCourses.length" class="hero-next">
        <p class="next-title">即将开始</p>
        <div v-for="c in upcomingMyCourses" :key="c.id" class="hero-next-item" @click="$router.push(`/courses/${c.id}`)">
          <span class="dot"></span>
          <span class="next-name">{{ c.name }}</span>
          <span class="next-time">{{ c.courseDate?.slice(5) }} {{ c.startTime?.slice(0, 5) }}</span>
        </div>
      </div>
    </div>

    <!-- ======== Guest Hero ======== -->
    <div v-else class="hero hero-guest">
      <h2 class="guest-title">gymAI</h2>
      <p class="guest-sub">你的专属健身伙伴</p>
      <div class="guest-actions">
        <span class="guest-link" @click="$router.push('/login')">登录</span>
        <span class="guest-sep">&nbsp;&middot;&nbsp;</span>
        <span class="guest-link" @click="$router.push('/register')">注册</span>
      </div>
    </div>

    <!-- ======== Course Module ======== -->
    <section class="module">
      <div class="module-inner">
        <div class="module-head">
          <h3>课程</h3>
          <span class="module-more" @click="$router.push('/courses')">查看全部 &rarr;</span>
        </div>
        <div class="module-body">
          <div class="module-tabs">
            <button :class="{ active: courseTab === 'my' }" @click="courseTab = 'my'">我的</button>
            <button :class="{ active: courseTab === 'all' }" @click="courseTab = 'all'">全部课程</button>
          </div>
          <template v-if="courseTab === 'my'">
            <template v-if="homeData.myCourses?.length">
              <CourseCard v-for="c in homeData.myCourses.slice(0, 4)" :key="c.id" :course="c" @click="goDetail(c.id)" />
            </template>
            <div v-else class="module-empty">
              <p>还没有报名的课程</p>
              <span class="module-empty-link" @click="courseTab = 'all'">去看看全部课程 &rarr;</span>
            </div>
          </template>
          <template v-else>
            <template v-if="homeData.upcomingCourses?.length">
              <CourseCard v-for="c in homeData.upcomingCourses.slice(0, 6)" :key="c.id" :course="c" @click="goDetail(c.id)" />
            </template>
            <div v-else class="module-empty">
              <p>暂无课程安排</p>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- ======== Coach Module ======== -->
    <section class="module">
      <div class="module-inner">
        <div class="module-head">
          <h3>教练团队</h3>
          <span class="module-more" @click="$router.push('/coaches')">查看全部 &rarr;</span>
        </div>
        <div class="module-body">
          <CoachCard v-for="c in homeData.coaches" :key="c.id" :coach="c" @click="router.push(`/coaches/${c.id}`)" />
        </div>
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
  myCourseCount: 0, monthCheckins: 0, checkinDates: [], myCourses: [], upcomingCourses: [], coaches: [], card: {},
})

const courseTab = ref('my')

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

const upcomingMyCourses = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  const end = new Date(); end.setDate(end.getDate() + 3)
  const endStr = end.toISOString().slice(0, 10)
  return (homeData.value.myCourses || [])
    .filter(c => c.courseDate && c.courseDate >= today && c.courseDate <= endStr)
    .slice(0, 5)
})

const goDetail = (id?: number) => { if (id) router.push(`/courses/${id}`) }

onMounted(async () => {
  if (!userStore.isLoggedIn()) return
  try { const res = await homeApi.get(); homeData.value = res.data } catch { /* handled */ }
})
</script>

<style scoped lang="scss">
.home-page {
  padding-bottom: 40px;
}

// ======== Hero — Apple-style ========
.hero {
  padding: 48px 24px 36px;
  text-align: center;
  background: $color-bg;
}

.hero-greet {
  font-size: $font-size-4xl;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: $color-text-primary;
  margin-bottom: 32px;
  line-height: 1.1;
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin-bottom: 32px;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-num {
  font-size: $font-size-5xl;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: $color-text-primary;
  line-height: 1;
}

.hero-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-top: 4px;
}

// Membership strip
.hero-card-strip {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 28px;
  background: $color-bg-secondary;
  border-radius: $radius-lg;
  cursor: pointer;
  margin-bottom: 28px;
  transition: background $transition-fast;

  &:hover {
    background: $color-border-light;
  }
}

.strip-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.strip-type {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
}

.strip-days {
  font-size: $font-size-sm;
  color: $color-text-secondary;

  &.warn {
    color: $color-danger;
  }
}

.strip-bar {
  width: 120px;
  height: 4px;
  background: $color-border-light;
  border-radius: 2px;
  overflow: hidden;
}

.strip-fill {
  height: 100%;
  background: $color-accent;
  border-radius: 2px;
  transition: width 0.5s;

  &.warn {
    background: $color-danger;
  }
}

// Upcoming
.hero-next {
  text-align: left;
  max-width: 360px;
  margin: 0 auto;
}

.next-title {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $color-text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 10px;
}

.hero-next-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid $color-border-light;
  cursor: pointer;
  transition: opacity $transition-fast;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    opacity: 0.7;
  }
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: $color-success;
  flex-shrink: 0;
}

.next-name {
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.next-time {
  margin-left: auto;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

// ======== Guest Hero ========
.hero-guest {
  min-height: 70vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
}

.guest-title {
  font-size: 56px;
  font-weight: 700;
  letter-spacing: -0.04em;
  color: $color-text-primary;
  margin-bottom: 8px;
}

.guest-sub {
  font-size: $font-size-xl;
  color: $color-text-secondary;
  font-weight: 400;
  margin-bottom: 24px;
}

.guest-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.guest-link {
  font-size: $font-size-lg;
  color: $color-accent;
  cursor: pointer;
  font-weight: 500;

  &:hover {
    text-decoration: underline;
  }
}

.guest-sep {
  color: $color-text-tertiary;
  font-size: $font-size-lg;
}

// ======== Module ========
.module {
  padding: 48px 24px 52px;

  &:nth-child(even) {
    background: $color-bg-secondary;
  }
}

.module-inner {
  max-width: 680px;
  margin: 0 auto;
}

.module-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 20px;

  h3 {
    font-size: $font-size-2xl;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: $color-text-primary;
  }
}

.module-more {
  font-size: $font-size-sm;
  color: $color-accent;
  cursor: pointer;
  white-space: nowrap;

  &:hover {
    text-decoration: underline;
  }
}

.module-tabs {
  display: flex;
  gap: 2px;
  background: $color-bg-secondary;
  border-radius: $radius-pill;
  padding: 2px;
  width: fit-content;
  margin-bottom: 20px;

  button {
    padding: 7px 18px;
    border: none;
    background: transparent;
    font-size: $font-size-sm;
    font-weight: 500;
    font-family: $font-family;
    color: $color-text-secondary;
    cursor: pointer;
    border-radius: $radius-pill;
    transition: all $transition-fast;

    &.active {
      background: $color-bg;
      color: $color-text-primary;
      box-shadow: $shadow-sm;
    }
  }
}

.module-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.module-empty {
  text-align: center;
  padding: 48px 0;
  color: $color-text-secondary;

  p {
    font-size: $font-size-sm;
    margin-bottom: 8px;
  }
}

.module-empty-link {
  font-size: $font-size-sm;
  color: $color-accent;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

// ── Dark Mode ───────────────────────────────
html.dark {
  .hero {
    background: $dark-bg;
  }

  .hero-greet,
  .guest-title,
  .hero-num,
  .next-name {
    color: $dark-text;
  }

  .module:nth-child(even) {
    background: $dark-bg-secondary;
  }

  .module-head h3 {
    color: $dark-text;
  }

  .module-tabs {
    background: rgba(255, 255, 255, 0.08);

    button.active {
      background: $dark-border-secondary;
      color: $dark-text;
    }
  }

  .hero-card-strip {
    background: $dark-bg-secondary;

    &:hover {
      background: $dark-border-secondary;
    }
  }

  .strip-type {
    color: $dark-text;
  }

  .hero-next-item {
    border-bottom-color: $dark-border;
  }
}
</style>
