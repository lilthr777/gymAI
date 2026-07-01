<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="64" icon="UserFilled" :src="profile.avatar" />
      <h2>{{ profile.nickname }}</h2>
    </div>

    <div class="menu-list">
      <div class="menu-item" @click="$router.push('/edit-profile')">
        <span>编辑资料</span>
        <span class="menu-extra">{{ profile.nickname }}</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/my-courses')">
        <span>我的课程</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/my-checkins')">
        <span>签到记录</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
      <div class="menu-item" @click="$router.push('/card')">
        <span>会员卡</span>
        <span class="menu-extra">{{ profile.cardType ? cardLabel : '未开通' }}</span>
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="logout-area">
      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
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
  username: '', phone: '', nickname: '', avatar: '', gender: 0, status: 1,
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
  padding: 16px;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;

  h2 {
    font-size: 20px;
    font-weight: 600;
    color: $color-carbon;
    margin: 12px 0 0;
  }
}

.menu-list {
  background: $color-sheet;
  border-radius: $radius-lg;
  margin-bottom: 24px;
  overflow: hidden;
}

.menu-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px; cursor: pointer;
  border-bottom: 1px solid $color-steel; font-size: 14px; color: $color-carbon;
  &:last-child { border-bottom: none; }
  &:hover { background: rgba(0, 0, 0, 0.02); }
}
.menu-extra { font-size: $font-size-sm; color: $color-lead; flex: 1; text-align: right; margin-right: 12px; }

.logout-area { padding: 32px 0; text-align: center; }

html.dark {
  .profile-header h2 { color: $dark-text; }
  .menu-list { background: $dark-bg-card; }
  .menu-item { color: $dark-text; border-bottom-color: $dark-border;
    &:hover { background: rgba(255, 255, 255, 0.03); }
  }
}
</style>
