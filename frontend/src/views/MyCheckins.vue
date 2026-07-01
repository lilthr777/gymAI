<template>
  <div class="my-checkins">
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h2>签到记录</h2>
    </div>

    <div v-if="list.length" class="checkin-list">
      <div v-for="item in list" :key="item.id" class="checkin-item">
        <div class="checkin-left">
          <div class="checkin-time">{{ item.checkinTime?.slice(0, 16)?.replace('T', ' ') }}</div>
          <div class="checkin-course">{{ item.courseName || '课程 #' + item.courseId }}</div>
        </div>
        <span class="checkin-status" :class="statusClass(item.status)">{{ statusText(item.status) }}</span>
      </div>
    </div>
    <div v-else class="empty-box">
      <p>还没有签到记录</p>
      <router-link to="/courses" class="empty-link">去约一节课吧 &rarr;</router-link>
    </div>

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" size="large" class="more-btn" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue'
import { checkinApi } from '@/api'
import type { Checkin } from '@/types'

const list = ref<Checkin[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)

const statusText = (status?: number) => {
  if (status === 1) return '已签到'
  if (status === 2) return '迟到'
  return '缺席'
}

const statusClass = (status?: number) => {
  if (status === 1) return 'status--ok'
  if (status === 2) return 'status--warn'
  return 'status--miss'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await checkinApi.myCheckins({ pageNum: pageNum.value, pageSize: 10 })
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
.my-checkins {
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

.checkin-list {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
}

.checkin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.checkin-time {
  font-size: $font-size-base;
  color: $color-text-primary;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.checkin-course {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: 3px;
}

.checkin-status {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: $radius-pill;
  flex-shrink: 0;

  &.status--ok {
    color: $color-success;
    background: rgba($color-success, 0.1);
  }

  &.status--warn {
    color: $color-warning;
    background: rgba($color-warning, 0.1);
  }

  &.status--miss {
    color: $color-danger;
    background: rgba($color-danger, 0.1);
  }
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
  .page-nav h2,
  .checkin-time {
    color: $dark-text;
  }

  .checkin-course {
    color: $dark-text-secondary;
  }

  .checkin-list {
    background: $dark-bg;
    border-color: $dark-border;
  }

  .checkin-item {
    border-bottom-color: $dark-border;
  }
}
</style>
