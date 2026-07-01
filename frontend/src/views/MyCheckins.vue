<template>
  <div class="my-checkins">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon>返回</el-button>
      <h2>签到记录</h2>
    </div>

    <div v-if="list.length">
      <div v-for="item in list" :key="item.id" class="checkin-item">
        <div class="checkin-left">
          <div class="checkin-time">{{ item.checkinTime?.slice(0, 16)?.replace('T', ' ') }}</div>
          <div class="checkin-course">{{ item.courseName || '课程 #' + item.courseId }}</div>
        </div>
        <el-tag :type="item.status === 1 ? 'success' : item.status === 2 ? 'warning' : 'info'" size="small">
          {{ item.status === 1 ? '已签到' : item.status === 2 ? '迟到' : '缺席' }}
        </el-tag>
      </div>
    </div>
    <div v-else class="empty-box">
      <span class="empty-emoji">📊</span>
      <p>还没有签到记录</p>
      <router-link to="/courses" class="empty-link">去约一节课吧</router-link>
    </div>

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
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

.checkin-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: $color-sheet;
  border-radius: $radius-md;
  margin-bottom: 8px;
}

.checkin-time {
  font-size: 14px;
  color: $color-carbon;
  font-weight: 500;
}

.checkin-course {
  font-size: 12px;
  color: $color-lead;
  margin-top: 4px;
}

.load-more {
  text-align: center;
  padding: 16px;
}

.empty-box { text-align: center; padding: 40px 0; color: $color-lead;
  .empty-emoji { font-size: 40px; display: block; margin-bottom: 12px; }
  p { font-size: $font-size-sm; margin-bottom: 8px; }
}
.empty-link { font-size: $font-size-sm; color: $color-cobalt; text-decoration: none; font-weight: 500;
  &:hover { text-decoration: underline; }
}

html.dark {
  .page-nav h2,
  .checkin-time {
    color: $dark-text;
  }

  .checkin-item {
    background: $dark-bg-card;
  }
}
</style>
