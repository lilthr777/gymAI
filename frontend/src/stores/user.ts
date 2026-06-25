import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginResult } from '@/types'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const nickname = ref('')
    const avatar = ref('')
    const role = ref('')

    const isLoggedIn = () => !!token.value

    const setLoginInfo = (info: LoginResult) => {
      token.value = info.token
      nickname.value = info.nickname
      avatar.value = info.avatar
      role.value = info.role
    }

    const logout = () => {
      token.value = ''
      nickname.value = ''
      avatar.value = ''
      role.value = ''
    }

    const hasRole = (requiredRole: string) => role.value === requiredRole

    return { token, nickname, avatar, role, isLoggedIn, setLoginInfo, logout, hasRole }
  },
  {
    persist: true,
  },
)
