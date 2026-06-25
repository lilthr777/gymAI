<template>
  <div class="login-page">
    <!-- Brand Side -->
    <div class="login-brand">
      <div class="brand-inner">
        <h1 class="brand-logo">gymAI</h1>
        <p class="brand-tagline">智能健身房管理系统</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>会员全生命周期管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>智能排课与签到</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>数据可视化分析</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot"></span>
            <span>AI 智能助手</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Form Side -->
    <div class="login-form-side">
      <div class="form-card">
        <h2 class="form-title">登录</h2>
        <p class="form-desc">输入账号密码进入管理系统</p>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              :loading="loading"
              class="login-btn"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="form-hint">测试账号：admin / admin123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { LoginForm } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginForm>({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login(form)
      userStore.setLoginInfo(res.data)
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/dashboard'
      router.push(redirect)
    } catch {
      // error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

// ── Brand Side ───────────────────────────────
.login-brand {
  flex: 1;
  background: $color-carbon;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  // Subtle radial glow in top-right
  &::after {
    content: '';
    position: absolute;
    top: -30%;
    right: -20%;
    width: 500px;
    height: 500px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(59, 110, 176, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }
}

.brand-inner {
  position: relative;
  z-index: 1;
  padding: 48px;
  max-width: 420px;
}

.brand-logo {
  font-family: $font-display;
  font-size: 52px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: $color-sheet;
  margin-bottom: 8px;
}

.brand-tagline {
  font-size: 17px;
  color: #8B8F94;
  margin-bottom: 48px;
  font-weight: 400;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  color: #A0A4A9;
  font-size: 15px;
}

.feature-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: $color-cobalt;
  flex-shrink: 0;
}

// ── Form Side ────────────────────────────────
.login-form-side {
  width: 460px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: $color-sheet;
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: $color-carbon;
  margin-bottom: 6px;
}

.form-desc {
  color: $color-lead;
  font-size: 14px;
  margin-bottom: 36px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
}

.form-hint {
  text-align: center;
  color: #B0B4BA;
  font-size: 12px;
}

// ── Responsive ───────────────────────────────
@media (max-width: 768px) {
  .login-brand {
    display: none;
  }

  .login-form-side {
    width: 100%;
    padding: 32px;
  }
}
</style>
