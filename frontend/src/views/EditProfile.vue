<template>
  <div class="edit-profile">
    <div class="page-nav">
      <button class="back-btn" @click="$router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h2>编辑资料</h2>
    </div>

    <div class="avatar-section">
      <div class="avatar-upload" @click="triggerUpload">
        <el-avatar :size="80" icon="UserFilled" :src="avatarPreview || profile.avatar" />
        <div class="avatar-overlay">
          <el-icon :size="20"><Camera /></el-icon>
        </div>
      </div>
      <input ref="fileInput" type="file" accept="image/*" hidden @change="handleFileChange" />
      <p class="avatar-hint" v-if="!uploading">点击更换头像</p>
      <p class="avatar-hint" v-else>上传中...</p>
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
      <p class="section-title">可修改信息</p>
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
import { ArrowLeft, Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { userApi, uploadApi } from '@/api'
import type { User } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)
const uploading = ref(false)
const avatarPreview = ref('')
const fileInput = ref<HTMLInputElement>()

const profile = reactive<User>({
  username: '', phone: '', nickname: '', avatar: '', gender: 0,
})

const form = reactive({ nickname: '', phone: '', gender: 0 })

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleFileChange = async (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const res = await uploadApi.avatar(file)
    const url = res.data
    avatarPreview.value = url
    profile.avatar = url
    ElMessage.success('头像上传成功，保存后生效')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
    if (target) target.value = ''
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await userApi.updateProfile({ nickname: form.nickname, phone: form.phone, gender: form.gender, avatar: profile.avatar })
    userStore.nickname = form.nickname
    userStore.avatar = profile.avatar
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
.edit-profile {
  padding: 24px 20px;
  max-width: 480px;
  margin: 0 auto;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;

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

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.avatar-upload {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;

  &:hover .avatar-overlay {
    opacity: 1;
  }
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease;
  color: #fff;
}

.avatar-hint {
  font-size: $font-size-xs;
  color: $color-text-secondary;
  margin-top: 10px;
}

.info-card {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.info-value {
  font-size: $font-size-sm;
  color: $color-text-primary;
}

.section-title {
  font-size: $font-size-xs;
  font-weight: 600;
  color: $color-text-secondary;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  margin-bottom: 16px;
}

.form-card {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  padding: 20px;
  margin-bottom: 28px;
}

.save-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: $radius-md;
}

.btn-area {
  padding: 0 4px;
}

html.dark {
  .page-nav h2,
  .info-value {
    color: $dark-text;
  }

  .info-label,
  .avatar-hint,
  .section-title {
    color: $dark-text-secondary;
  }

  .info-card,
  .form-card {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .info-item {
    border-bottom-color: $dark-border;
  }
}
</style>
