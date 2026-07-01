<template>
  <div class="course-detail" v-if="course">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
    </div>
    <div class="detail-header">
      <h2>{{ course.name }}</h2>
      <el-tag :type="statusTagType">{{ statusText }}</el-tag>
    </div>

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
      <!-- 签到反馈 -->
      <div v-if="checkinTime" class="info-row checkin-row">
        <span class="info-label">签到</span>
        <span class="info-value checkin-ok">已签到 {{ checkinTime }}</span>
      </div>
    </div>

    <div v-if="userStore.isLoggedIn()" class="detail-actions">
      <template v-if="registered">
        <template v-if="!checkedIn">
          <el-button type="success" size="large" :loading="checkingIn" class="action-btn" @click="handleCheckin">
            签到
          </el-button>
          <el-button type="danger" plain size="large" :loading="canceling" class="action-btn ghost-btn" @click="handleCancel">
            取消报名
          </el-button>
        </template>
        <div v-else class="done-box">
          <el-icon :size="18"><CircleCheckFilled /></el-icon>
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

    <!-- 评价 -->
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

// 评价
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
const statusTagType = computed(() => {
  if (course.value?.status === 0) return 'danger'
  if (course.value?.status === 2) return 'warning'
  if (registered.value) return ''
  return 'success'
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
.course-detail { padding: 16px; }
.page-nav { margin-bottom: 12px; }

.detail-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 24px;
  h2 { font-size: 22px; font-weight: 600; color: $color-carbon; margin: 0; }
}

.detail-info { background: $color-sheet; border-radius: $radius-lg; padding: 16px; margin-bottom: 24px; }

.info-row { display: flex; padding: 10px 0; border-bottom: 1px solid $color-steel;
  &:last-child { border-bottom: none; }
}
.info-label { width: 56px; font-size: 14px; color: $color-lead; flex-shrink: 0; }
.info-value { font-size: 14px; color: $color-carbon;
  &.link { color: $color-cobalt; cursor: pointer; &:hover { text-decoration: underline; } }
}

.checkin-row { border-bottom: none; }
.checkin-ok { color: $color-cobalt; font-weight: 500; }

.detail-actions { padding: 0 8px; display: flex; flex-direction: column; gap: 10px; }

.action-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 500; }
.ghost-btn { margin-left: 0 !important; }

.done-box { display: flex; align-items: center; gap: 8px; padding: 12px 0; color: $color-cobalt; font-size: $font-size-base; font-weight: 500;
  .done-time { color: $color-lead; font-weight: 400; font-size: $font-size-sm; }
}

.login-hint { text-align: center; padding: 24px; font-size: 14px; color: $color-lead;
  a { color: $color-cobalt; font-weight: 500; }
}

// 评价
.review-section { margin-top: 28px; padding: 0 4px; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h3 { font-size: 16px; font-weight: 600; color: $color-carbon; }
}
.avg-rating { font-size: 14px; color: #f5a623; font-weight: 600; }
.review-list { margin-bottom: 16px; }
.review-item { padding: 12px 0; border-bottom: 1px solid $color-steel;
  &:last-child { border-bottom: none; }
}
.review-top { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.review-author { font-size: 13px; font-weight: 500; color: $color-carbon; }
.review-time { font-size: 11px; color: $color-lead; margin-left: auto; }
.review-text { font-size: 13px; color: $color-ash; line-height: 1.5; }

.review-form { margin-top: 16px; display: flex; flex-direction: column; gap: 10px; }
.review-rate { display: flex; align-items: center; gap: 10px; }
.rate-label { font-size: 13px; color: $color-lead; }
.review-done { font-size: 13px; color: $color-lead; text-align: center; padding: 12px 0; }

html.dark {
  .section-head h3, .review-author { color: $dark-text; }
  .review-item { border-bottom-color: $dark-border; }
}

html.dark {
  .detail-header h2, .info-value { color: $dark-text; }
  .detail-info { background: $dark-bg-card; border-color: $dark-border; }
  .info-row { border-bottom-color: $dark-border; }
}
</style>
