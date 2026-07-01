<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="64" icon="UserFilled" :src="profile.avatar" />
      <h2>{{ profile.nickname }}</h2>
    </div>

    <div class="menu-list">
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
        <el-icon><ArrowRight /></el-icon>
      </div>
    </div>

    <div class="section-title">基本信息</div>
    <el-form ref="formRef" :model="profile" label-width="64px" class="profile-form">
      <el-form-item label="用户名">
        <el-input :model-value="profile.username" disabled />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="profile.nickname" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="profile.phone" disabled />
      </el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="profile.gender">
          <el-radio :value="1">男</el-radio>
          <el-radio :value="2">女</el-radio>
          <el-radio :value="0">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" class="save-btn" @click="handleSave">保存</el-button>
      </el-form-item>
    </el-form>

    <div class="section-title">会员卡</div>
    <div class="card-info">
      <el-tag :type="cardTypeTag">{{ profile.cardType || '暂无' }}</el-tag>
      <span v-if="profile.cardEndDate" class="card-date">有效期至 {{ profile.cardEndDate }}</span>
    </div>

    <div class="logout-area">
      <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import type { User } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const profile = reactive<User>({
  username: '',
  phone: '',
  nickname: '',
  avatar: '',
  gender: 0,
  status: 1,
})

const cardTypeTag = computed(() => {
  const map: Record<string, string> = {
    MONTH: 'success', QUARTER: 'primary', YEAR: 'warning', LIFETIME: 'danger',
  }
  return map[profile.cardType || ''] || 'info'
})

const handleSave = async () => {
  saving.value = true
  try {
    await userApi.updateProfile({
      nickname: profile.nickname,
      phone: profile.phone,
      gender: profile.gender,
      avatar: profile.avatar,
    })
    userStore.nickname = profile.nickname
    ElMessage.success('保存成功')
  } catch {
    // handled
  } finally {
    saving.value = false
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

onMounted(async () => {
  try {
    const res = await userApi.getProfile()
    Object.assign(profile, res.data)
  } catch {
    // handled
  }
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
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  cursor: pointer;
  border-bottom: 1px solid $color-steel;
  font-size: 14px;
  color: $color-carbon;

  &:last-child { border-bottom: none; }

  &:hover {
    background: rgba(0, 0, 0, 0.02);
  }
}

.section-title {
  font-size: 13px;
  font-weight: 600;
  color: $color-lead;
  margin-bottom: 12px;
  text-transform: uppercase;
}

.profile-form {
  background: $color-sheet;
  border-radius: $radius-lg;
  padding: 16px;
  margin-bottom: 24px;
}

.save-btn {
  width: 100%;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  margin-bottom: 24px;
}

.card-date {
  font-size: 13px;
  color: $color-lead;
}

.logout-area {
  padding: 24px 0;
  text-align: center;
}

html.dark {
  .profile-header h2 {
    color: $dark-text;
  }

  .menu-list,
  .profile-form {
    background: $dark-bg-card;
  }

  .menu-item {
    color: $dark-text;
    border-bottom-color: $dark-border;

    &:hover {
      background: rgba(255, 255, 255, 0.03);
    }
  }
}
</style>
