import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AuthResult } from '@/types'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const nickname = ref('')
    const avatar = ref('')
    const userId = ref('')

    const isLoggedIn = () => !!token.value

    const setLoginInfo = (info: AuthResult) => {
      token.value = info.token
      nickname.value = info.nickname
      avatar.value = info.avatar
      userId.value = info.userId
    }

    const logout = () => {
      token.value = ''
      nickname.value = ''
      avatar.value = ''
      userId.value = ''
    }

    return { token, nickname, avatar, userId, isLoggedIn, setLoginInfo, logout }
  },
  {
    persist: true,
  },
)
