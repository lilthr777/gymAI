<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="form-title">注册</h2>
      <p class="form-desc">创建账号开始健身之旅</p>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleRegister">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" :prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <p class="form-hint">
        已有账号？<router-link to="/login" class="login-link">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Phone, Lock } from '@element-plus/icons-vue'
import { authApi } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.register({
        username: form.username,
        phone: form.phone,
        password: form.password,
      })
      userStore.setLoginInfo(res.data)
      ElMessage.success('注册成功')
      router.push('/home')
    } catch {
      // error handled by interceptor
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-magnesium;
  padding: 24px;
}

.register-card {
  width: 100%;
  max-width: 380px;
  background: $color-sheet;
  padding: 40px 36px;
  border-radius: $radius-lg;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
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
  margin-bottom: 32px;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
}

.form-hint {
  text-align: center;
  color: #B0B4BA;
  font-size: 12px;

  .login-link {
    color: $color-cobalt;
    text-decoration: none;
    font-weight: 500;

    &:hover {
      text-decoration: underline;
    }
  }
}

html.dark {
  .register-page {
    background: $dark-bg;
  }

  .register-card {
    background: $dark-bg-secondary;
  }

  .form-title {
    color: $dark-text;
  }
}
</style>
