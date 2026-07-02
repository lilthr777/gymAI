<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="72" icon="UserFilled" :src="profile.avatar" />
      <h2>{{ profile.nickname }}</h2>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="$router.push('/edit-profile')">
        <span>编辑资料</span>
        <span class="menu-extra">{{ profile.nickname }}</span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/my-courses')">
        <span>我的课程</span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/my-checkins')">
        <span>签到记录</span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/card')">
        <span>会员卡</span>
        <span class="menu-extra">{{ profile.cardType ? cardLabel : '未开通' }}</span>
        <el-icon :size="16" class="menu-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="logout-area">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import type { User } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const profile = reactive<User>({
  username: '', phone: '', nickname: '', avatar: '', gender: 0,
})

const cardLabel = computed(() => {
  const map: Record<string, string> = { MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', LIFETIME: '终身卡' }
  return map[profile.cardType || ''] || ''
})

const handleLogout = () => { userStore.logout(); router.push('/login') }

onMounted(async () => {
  try { const res = await userApi.getProfile(); Object.assign(profile, res.data) } catch { /* handled */ }
})
</script>

<style scoped lang="scss">
.profile-page {
  padding: 32px 24px;
  max-width: 480px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0 36px;

  h2 {
    font-size: $font-size-2xl;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: $color-text-primary;
    margin-top: 16px;
  }
}

.menu-list {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: 32px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  font-size: $font-size-base;
  color: $color-text-primary;
  border-bottom: 1px solid $color-border-light;
  transition: background $transition-fast;
  letter-spacing: -0.01em;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.menu-extra {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  flex: 1;
  text-align: right;
  margin-right: 8px;
}

.menu-arrow {
  color: $color-text-tertiary;
  flex-shrink: 0;
}

.logout-area {
  text-align: center;
}

.logout-btn {
  padding: 12px 32px;
  border: none;
  background: none;
  font-size: $font-size-sm;
  font-family: $font-family;
  color: $color-danger;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

html.dark {
  .profile-header h2 {
    color: $dark-text;
  }

  .menu-list {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .menu-item {
    color: $dark-text;
    border-bottom-color: $dark-border;

    &:hover {
      background: rgba(255, 255, 255, 0.03);
    }
  }

  .menu-extra {
    color: $dark-text-secondary;
  }

  .menu-arrow {
    color: $dark-text-secondary;
  }
}
</style>
