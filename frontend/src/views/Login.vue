<template>
  <div class="login-page">
    <!-- Brand Header -->
    <div class="login-brand">
      <h1 class="brand-logo">gymAI</h1>
      <p class="brand-tagline">你的专属健身伙伴</p>
    </div>

    <!-- Form -->
    <div class="login-form-side">
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
          <!-- 记住密码 & 忘记密码 -->
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

        <!-- 第三方登录占位 -->
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

// 记住密码：从 localStorage 恢复
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
      // 记住密码
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
  flex-direction: column;
  align-items: center;
  background: $color-sheet;
}

// ── Brand Header ──────────────────────────────
.login-brand {
  width: 100%;
  text-align: center;
  padding: 64px 24px 48px;
  background: linear-gradient(180deg, $color-carbon 0%, #343840 100%);
}

.brand-logo {
  font-family: $font-display;
  font-size: 42px;
  font-weight: 600;
  letter-spacing: 0.06em;
  color: $color-sheet;
  margin-bottom: 6px;
}

.brand-tagline {
  font-size: $font-size-base;
  color: #9B9EA3;
}

// ── Form Section ──────────────────────────────
.login-form-side {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 32px 24px 48px;
  flex: 1;
}

.form-card {
  width: 100%;
  max-width: 380px;
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

.form-extra { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }
.forgot-link { font-size: $font-size-sm; color: $color-lead; cursor: pointer; &:hover { color: $color-cobalt; } }

// ── Social Login ─────────────────────────────
.social-login { text-align: center; margin: 24px 0 16px; }
.social-divider { display: flex; align-items: center; gap: 12px; margin-bottom: 14px;
  &::before, &::after { content: ''; flex: 1; height: 1px; background: $color-steel; }
  span { font-size: $font-size-sm; color: $color-lead; white-space: nowrap; }
}
.social-icons { display: flex; justify-content: center; gap: 20px; }
.social-btn { width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border: 1px solid $color-steel; border-radius: 50%; cursor: pointer; color: $color-lead; transition: all $transition-fast;
  &:hover { border-color: $color-cobalt; color: $color-cobalt; }
}

.form-hint {
  text-align: center;
  color: #B0B4BA;
  font-size: 12px;

  .register-link {
    color: $color-cobalt;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 480px) {
  .login-brand { padding: 36px 16px 28px; }
  .brand-logo { font-size: 34px; }
  .login-form-side { padding: 24px 16px 40px; }
}
</style>
