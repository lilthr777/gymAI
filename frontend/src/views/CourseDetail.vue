<template>
  <div class="course-detail" v-if="course">
    <!-- Page nav -->
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
    </div>

    <!-- Course header -->
    <div class="detail-header">
      <h2>{{ course.name }}</h2>
      <span class="status-badge" :class="statusBadgeClass">{{ statusText }}</span>
    </div>

    <!-- Info -->
    <div class="detail-info">
      <div class="info-row">
        <span class="info-label">日期</span>
        <span class="info-value">{{ course.courseDate }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">时间</span>
        <span class="info-value">{{ course.startTime?.slice(0, 5) }} - {{ course.endTime?.slice(0, 5) }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">人数</span>
        <span class="info-value">{{ course.currentCount }} / {{ course.maxCapacity }} 人</span>
      </div>
      <div class="info-row" v-if="course.coachName">
        <span class="info-label">教练</span>
        <span class="info-value link" @click="$router.push(`/coaches/${course.coachId}`)">{{ course.coachName }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">描述</span>
        <span class="info-value">{{ course.description || '暂无描述' }}</span>
      </div>
      <div v-if="checkinTime" class="info-row checkin-row">
        <span class="info-label">签到</span>
        <span class="info-value checkin-ok">已签到 {{ checkinTime }}</span>
      </div>
    </div>

    <!-- Actions -->
    <div v-if="userStore.isLoggedIn()" class="detail-actions">
      <template v-if="registered">
        <template v-if="!checkedIn">
          <el-button type="success" size="large" :loading="checkingIn" class="action-btn" @click="handleCheckin">
            签到
          </el-button>
          <button class="text-btn danger" :disabled="canceling" @click="handleCancel">
            {{ canceling ? '取消中...' : '取消报名' }}
          </button>
        </template>
        <div v-else class="done-box">
          <el-icon :size="20"><CircleCheckFilled /></el-icon>
          <span>已签到</span>
          <span v-if="checkinTime" class="done-time">{{ checkinTime }}</span>
        </div>
      </template>
      <el-button v-else-if="canRegister" type="primary" size="large" :loading="registering" class="action-btn" @click="handleRegister">
        立即报名
      </el-button>
    </div>

    <div v-else class="login-hint">
      <router-link to="/login">登录</router-link> 后即可报名和签到
    </div>

    <!-- Reviews -->
    <div class="review-section">
      <div class="section-head">
        <h3>学员评价</h3>
        <span v-if="reviews.length" class="avg-rating">★ {{ avgRating.toFixed(1) }}</span>
      </div>

      <div v-if="reviews.length" class="review-list">
        <div v-for="r in reviews" :key="r.id" class="review-item">
          <div class="review-top">
            <span class="review-author">{{ r.nickname }}</span>
            <StarRating :modelValue="r.rating" :interactive="false" />
            <span class="review-time">{{ r.createdAt?.slice(0, 10) }}</span>
          </div>
          <p v-if="r.comment" class="review-text">{{ r.comment }}</p>
        </div>
      </div>

      <div v-if="userStore.isLoggedIn() && !myReviewSubmitted" class="review-form">
        <div class="review-rate">
          <span class="rate-label">评分</span>
          <StarRating v-model="myRating" :interactive="true" />
        </div>
        <el-input v-model="myComment" type="textarea" :rows="2" placeholder="写下你的感受（可选）" class="review-input" />
        <el-button type="primary" size="small" :loading="submittingReview" @click="submitReview">提交评价</el-button>
      </div>
      <div v-else-if="myReviewSubmitted" class="review-done">已评价</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, CircleCheckFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { courseApi, checkinApi, reviewApi } from '@/api'
import type { Course } from '@/types'
import StarRating from '@/components/StarRating.vue'

const route = useRoute()
const userStore = useUserStore()
const course = ref<Course | null>(null)

const reviews = ref<any[]>([])
const myRating = ref(5)
const myComment = ref('')
const myReviewSubmitted = ref(false)
const submittingReview = ref(false)

const avgRating = computed(() => {
  if (!reviews.value.length) return 0
  return reviews.value.reduce((s, r) => s + r.rating, 0) / reviews.value.length
})

const fetchReviews = async () => {
  try {
    const res = await reviewApi.list(Number(route.params.id))
    reviews.value = res.data.records
  } catch { /* handled */ }
}

const submitReview = async () => {
  if (!course.value?.id) return
  submittingReview.value = true
  try {
    await reviewApi.submit({ courseId: course.value.id, rating: myRating.value, comment: myComment.value })
    myReviewSubmitted.value = true
    ElMessage.success('评价成功')
    fetchReviews()
  } catch { /* handled */ }
  finally { submittingReview.value = false }
}

const registered = ref(false)
const checkedIn = ref(false)
const checkinTime = ref('')
const registering = ref(false)
const checkingIn = ref(false)
const canceling = ref(false)

const canRegister = computed(() => course.value?.status === 1 && !registered.value)

const statusText = computed(() => {
  if (course.value?.status === 0) return '已取消'
  if (course.value?.status === 2 || (course.value && course.value.currentCount >= course.value.maxCapacity)) return '已满员'
  if (registered.value) return '已报名'
  return '可报名'
})

const statusBadgeClass = computed(() => {
  if (course.value?.status === 0) return 'status--danger'
  if (course.value?.status === 2) return 'status--warning'
  if (registered.value) return 'status--default'
  return 'status--success'
})

const handleRegister = async () => {
  if (!course.value?.id || registering.value) return
  registering.value = true
  try {
    await courseApi.register(course.value.id)
    registered.value = true
    if (course.value) course.value.currentCount++
    ElMessage.success('报名成功')
  } catch { /* handled */ }
  finally { registering.value = false }
}

const handleCheckin = async () => {
  if (!course.value?.id || checkingIn.value) return
  checkingIn.value = true
  try {
    await checkinApi.checkin(course.value.id)
    checkedIn.value = true
    const now = new Date()
    checkinTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    ElMessage.success('签到成功')
  } catch { /* handled */ }
  finally { checkingIn.value = false }
}

const handleCancel = async () => {
  if (!course.value?.id || canceling.value) return
  canceling.value = true
  try {
    await courseApi.cancel(course.value.id)
    registered.value = false
    checkedIn.value = false
    checkinTime.value = ''
    if (course.value) {
      course.value.currentCount--
      course.value.registered = false
    }
    ElMessage.success('已取消报名')
  } catch { /* handled */ }
  finally { canceling.value = false }
}

onMounted(async () => {
  try {
    const res = await courseApi.getById(Number(route.params.id))
    course.value = res.data
    registered.value = !!res.data.registered
  } catch { /* handled */ }
  fetchReviews()
})
</script>

<style scoped lang="scss">
.course-detail {
  padding: 24px 20px;
  max-width: 680px;
  margin: 0 auto;
}

.page-nav {
  margin-bottom: 24px;
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

.detail-header {
  margin-bottom: 36px;

  h2 {
    font-size: 36px;
    font-weight: 700;
    letter-spacing: -0.04em;
    color: $color-text-primary;
    line-height: 1.1;
    margin-bottom: 12px;
  }
}

.status-badge {
  display: inline-block;
  font-size: $font-size-xs;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: $radius-pill;

  &.status--success {
    color: $color-success;
    background: rgba($color-success, 0.1);
  }
  &.status--warning {
    color: $color-warning;
    background: rgba($color-warning, 0.1);
  }
  &.status--danger {
    color: $color-danger;
    background: rgba($color-danger, 0.1);
  }
  &.status--default {
    color: $color-accent;
    background: rgba($color-accent, 0.1);
  }
}

// Info
.detail-info {
  margin-bottom: 36px;
}

.info-row {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 48px;
  font-size: 15px;
  font-weight: 400;
  color: $color-text-secondary;
  flex-shrink: 0;
  padding-top: 2px;
}

.info-value {
  font-size: 15px;
  font-weight: 400;
  color: $color-text-primary;
  line-height: 1.45;

  &.link {
    color: $color-accent;
    cursor: pointer;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

.checkin-ok {
  color: $color-success;
  font-weight: 500;
}

// Actions
.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 40px 0 48px;
}

.action-btn {
  width: 100%;
  height: 52px;
  font-size: $font-size-base;
  font-weight: 500;
  border-radius: $radius-md;
}

.text-btn {
  display: block;
  width: 100%;
  padding: 14px 0;
  border: none;
  background: none;
  font-size: $font-size-sm;
  font-family: $font-family;
  color: $color-danger;
  cursor: pointer;
  text-align: center;

  &:hover {
    text-decoration: underline;
  }

  &:disabled {
    opacity: 0.5;
  }
}

.done-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 0;
  color: $color-success;
  font-size: $font-size-base;
  font-weight: 500;
}

.done-time {
  color: $color-text-secondary;
  font-weight: 400;
  font-size: $font-size-sm;
}

.login-hint {
  text-align: center;
  padding: 40px 0;
  font-size: $font-size-sm;
  color: $color-text-secondary;

  a {
    color: $color-accent;
    font-weight: 500;
  }
}

// Reviews
.review-section {
  border-top: 1px solid $color-border-light;
  padding-top: 36px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 24px;

  h3 {
    font-size: 24px;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: $color-text-primary;
  }
}

.avg-rating {
  font-size: 15px;
  color: #ff9f0a;
  font-weight: 600;
}

.review-item {
  padding: 18px 0;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.review-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.review-author {
  font-size: $font-size-sm;
  font-weight: 600;
  color: $color-text-primary;
}

.review-time {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-left: auto;
}

.review-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  line-height: 1.5;
}

.review-form {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-rate {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.review-done {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  text-align: center;
  padding: 20px 0;
}

html.dark {
  .detail-header h2,
  .info-value {
    color: $dark-text;
  }

  .info-row {
    border-bottom-color: $dark-border;
  }

  .review-item {
    border-bottom-color: $dark-border;
  }

  .section-head h3,
  .review-author {
    color: $dark-text;
  }

  .review-section {
    border-top-color: $dark-border;
  }
}
</style>
