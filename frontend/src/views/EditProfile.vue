<template>
  <div class="edit-profile">
    <div class="page-nav">
      <el-button text @click="$router.back()"><el-icon :size="18"><ArrowLeft /></el-icon></el-button>
      <h2>编辑资料</h2>
    </div>

    <div class="avatar-section">
      <el-avatar :size="72" icon="UserFilled" :src="profile.avatar" />
      <p class="avatar-hint">头像</p>
    </div>

    <div class="info-card">
      <div class="info-item">
        <span class="info-label">用户名</span>
        <span class="info-value">{{ profile.username }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">手机号</span>
        <span class="info-value">{{ profile.phone }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">注册时间</span>
        <span class="info-value">{{ profile.createdAt?.slice(0, 10) || '--' }}</span>
      </div>
    </div>

    <div class="form-card">
      <div class="section-title">可修改信息</div>
      <el-form :model="form" label-width="56px" size="large">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
            <el-radio :value="0">未设置</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </div>

    <div class="btn-area">
      <el-button type="primary" size="large" :loading="saving" class="save-btn" @click="handleSave">
        保存修改
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api'
import type { User } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const profile = reactive<User>({
  username: '', phone: '', nickname: '', avatar: '', gender: 0, status: 1,
})

const form = reactive({ nickname: '', phone: '', gender: 0 })

const handleSave = async () => {
  saving.value = true
  try {
    await userApi.updateProfile({ nickname: form.nickname, phone: form.phone, gender: form.gender, avatar: profile.avatar })
    userStore.nickname = form.nickname
    profile.nickname = form.nickname
    profile.phone = form.phone
    profile.gender = form.gender
    ElMessage.success('保存成功')
  } catch { /* handled */ }
  finally { saving.value = false }
}

onMounted(async () => {
  try {
    const res = await userApi.getProfile()
    Object.assign(profile, res.data)
    form.nickname = res.data.nickname
    form.phone = res.data.phone
    form.gender = res.data.gender
  } catch { /* handled */ }
})
</script>

<style scoped lang="scss">
.edit-profile { padding: 16px; }

.page-nav { display: flex; align-items: center; gap: 8px; margin-bottom: 24px;
  h2 { font-family: $font-display; font-size: $font-size-lg; font-weight: 600; color: $color-carbon; text-transform: uppercase; letter-spacing: 0.03em; }
}

.avatar-section { text-align: center; margin-bottom: 24px; }
.avatar-hint { font-size: $font-size-sm; color: $color-lead; margin-top: 10px; }

.info-card { background: $color-sheet; border-radius: $radius-lg; padding: 4px 16px; margin-bottom: 20px; }

.info-item { display: flex; justify-content: space-between; align-items: center; padding: 14px 0; border-bottom: 1px solid $color-steel;
  &:last-child { border-bottom: none; }
}
.info-label { font-size: $font-size-sm; color: $color-lead; }
.info-value { font-size: $font-size-sm; color: $color-carbon; }

.section-title { font-size: $font-size-sm; font-weight: 600; color: $color-lead; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.04em; }

.form-card { background: $color-sheet; border-radius: $radius-lg; padding: 16px; margin-bottom: 24px; }

.save-btn { width: 100%; height: 48px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; }

.btn-area { padding: 0 4px; }

html.dark {
  .page-nav h2, .info-value { color: $dark-text; }
  .info-card, .form-card { background: $dark-bg-card; }
  .info-item { border-bottom-color: $dark-border; }
}
</style>
