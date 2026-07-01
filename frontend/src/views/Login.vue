<template>
  <div class="login-page">
    <div class="login-container">
      <!-- Brand -->
      <div class="login-brand">
        <h1 class="brand-logo">gymAI</h1>
        <p class="brand-tagline">你的专属健身伙伴</p>
      </div>

      <!-- Form -->
      <div class="form-card">
        <h2 class="form-title">登录</h2>
        <p class="form-desc">登录你的健身账户</p>

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
          <div class="form-extra">
            <el-checkbox v-model="rememberMe" size="small">记住密码</el-checkbox>
            <span class="forgot-link" @click="handleForgot">忘记密码？</span>
          </div>
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

        <!-- Social login -->
        <div class="social-login">
          <div class="social-divider"><span>其他方式登录</span></div>
          <div class="social-icons">
            <span class="social-btn" title="微信登录"><el-icon :size="20"><ChatDotSquare /></el-icon></span>
            <span class="social-btn" title="手机验证码登录"><el-icon :size="20"><Iphone /></el-icon></span>
          </div>
        </div>

        <p class="form-hint">
          还没有账号？<router-link to="/register" class="register-link">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, ChatDotSquare, Iphone } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { LoginForm } from '@/types'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive<LoginForm>({
  username: '',
  password: '',
})

const saved = localStorage.getItem('gymai_remember')
if (saved) {
  try {
    const u = JSON.parse(saved)
    form.username = u.username || ''
    form.password = u.password || ''
    rememberMe.value = true
  } catch { /* ignore */ }
}

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

const handleForgot = () => {
  ElMessage.info('请联系管理员重置密码，或重新注册账号')
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login(form)
      userStore.setLoginInfo(res.data)
      if (rememberMe.value) {
        localStorage.setItem('gymai_remember', JSON.stringify({ username: form.username, password: form.password }))
      } else {
        localStorage.removeItem('gymai_remember')
      }
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/home'
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
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg;
  padding: 24px;
}

.login-container {
  width: 100%;
  max-width: 400px;
}

// ── Brand ───────────────────────────────────
.login-brand {
  text-align: center;
  margin-bottom: 48px;
}

.brand-logo {
  font-size: 40px;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: $color-text-primary;
  margin-bottom: 4px;
}

.brand-tagline {
  font-size: $font-size-lg;
  color: $color-text-secondary;
  font-weight: 400;
}

// ── Form ────────────────────────────────────
.form-card {
  background: $color-bg;
  border-radius: $radius-lg;
  padding: 40px;
  border: 1px solid $color-border-light;
}

.form-title {
  font-size: $font-size-2xl;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: $color-text-primary;
  margin-bottom: 4px;
}

.form-desc {
  color: $color-text-secondary;
  font-size: $font-size-sm;
  margin-bottom: 32px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: $radius-md;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.forgot-link {
  font-size: $font-size-sm;
  color: $color-accent;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

// ── Social Login ────────────────────────────
.social-login {
  text-align: center;
  margin: 28px 0 20px;
}

.social-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: $color-border-light;
  }

  span {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    white-space: nowrap;
  }
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 24px;
}

.social-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid $color-border;
  border-radius: 50%;
  cursor: pointer;
  color: $color-text-secondary;
  transition: all $transition-fast;

  &:hover {
    border-color: $color-accent;
    color: $color-accent;
  }
}

.form-hint {
  text-align: center;
  color: $color-text-secondary;
  font-size: $font-size-sm;

  .register-link {
    color: $color-accent;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

// ── Dark Mode ───────────────────────────────
html.dark {
  .login-page {
    background: $dark-bg;
  }

  .brand-logo {
    color: $dark-text;
  }

  .form-card {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .form-title {
    color: $dark-text;
  }
}

@media (max-width: 480px) {
  .form-card {
    padding: 28px 24px;
  }
}
</style>
