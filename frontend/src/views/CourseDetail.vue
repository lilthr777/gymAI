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
        <span class="info-value">{{ course.coachName }}</span>
      </div>
      <div class="info-row">
        <span class="info-label">描述</span>
        <span class="info-value">{{ course.description || '暂无描述' }}</span>
      </div>
    </div>

    <div v-if="userStore.isLoggedIn()" class="detail-actions">
      <template v-if="registered">
        <div class="action-row">
          <el-button
            v-if="!checkedIn"
            type="success"
            size="large"
            :loading="checkingIn"
            class="action-btn"
            @click="handleCheckin"
          >
            签到
          </el-button>
          <el-tag v-if="checkedIn" type="success" size="large" class="checked-in-badge">已签到</el-tag>
          <el-button
            type="danger"
            size="large"
            :loading="canceling"
            class="action-btn cancel-btn"
            @click="handleCancel"
          >
            取消报名
          </el-button>
        </div>
      </template>
      <el-button
        v-else-if="canRegister"
        type="primary"
        size="large"
        :loading="registering"
        class="action-btn"
        @click="handleRegister"
      >
        立即报名
      </el-button>
    </div>

    <div v-else class="login-hint">
      <router-link to="/login">登录</router-link> 后即可报名和签到
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { courseApi, checkinApi } from '@/api'
import type { Course } from '@/types'

const route = useRoute()
const userStore = useUserStore()
const course = ref<Course | null>(null)
const registered = ref(false)
const checkedIn = ref(false)
const registering = ref(false)
const checkingIn = ref(false)
const canceling = ref(false)

const canRegister = computed(() => {
  if (!course.value) return false
  return course.value.status === 1 && !registered.value
})

const statusText = computed(() => {
  if (course.value?.status === 0) return '已取消'
  if (course.value?.status === 2 || (course.value && course.value.currentCount >= course.value.maxCapacity)) return '已满员'
  return '可报名'
})

const statusTagType = computed(() => {
  if (course.value?.status === 0) return 'danger'
  if (course.value?.status === 2) return 'warning'
  return 'success'
})

const handleRegister = async () => {
  if (!course.value?.id) return
  registering.value = true
  try {
    await courseApi.register(course.value.id)
    registered.value = true
    if (course.value) course.value.currentCount++
    ElMessage.success('报名成功')
  } catch {
    // handled
  } finally {
    registering.value = false
  }
}

const handleCheckin = async () => {
  if (!course.value?.id) return
  checkingIn.value = true
  try {
    await checkinApi.checkin(course.value.id)
    checkedIn.value = true
    ElMessage.success('签到成功')
  } catch {
    // handled
  } finally {
    checkingIn.value = false
  }
}

const handleCancel = async () => {
  if (!course.value?.id) return
  canceling.value = true
  try {
    await courseApi.cancel(course.value.id)
    registered.value = false
    checkedIn.value = false
    if (course.value) course.value.currentCount--
    course.value.registered = false
    ElMessage.success('已取消报名')
  } catch {
    // handled
  } finally {
    canceling.value = false
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const res = await courseApi.getById(id)
    course.value = res.data
    registered.value = !!res.data.registered
  } catch {
    // handled
  }
})
</script>

<style scoped lang="scss">
.course-detail {
  padding: 16px;
}

.page-nav {
  margin-bottom: 12px;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;

  h2 {
    font-size: 22px;
    font-weight: 600;
    color: $color-carbon;
    margin: 0;
  }
}

.detail-info {
  background: $color-sheet;
  border-radius: $radius-lg;
  padding: 16px;
  margin-bottom: 24px;
}

.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid $color-steel;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  width: 56px;
  font-size: 14px;
  color: $color-lead;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: $color-carbon;
}

.detail-actions {
  padding: 0 8px;
}

.action-row {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
}

.cancel-btn {
  margin-left: 0 !important;
}

.checked-in-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  font-size: 15px;
  width: 100%;
}

.login-hint {
  text-align: center;
  padding: 24px;
  font-size: 14px;
  color: $color-lead;

  a {
    color: $color-cobalt;
    font-weight: 500;
  }
}

html.dark {
  .detail-header h2,
  .info-value {
    color: $dark-text;
  }

  .detail-info {
    background: $dark-bg-card;
    border-color: $dark-border;
  }

  .info-row {
    border-bottom-color: $dark-border;
  }
}
</style>
