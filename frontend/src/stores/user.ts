import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { LoginResult } from '@/types'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const nickname = ref('')
    const avatar = ref('')

    const isLoggedIn = () => !!token.value

    const setLoginInfo = (info: LoginResult) => {
      token.value = info.token
      nickname.value = info.nickname
      avatar.value = info.avatar
    }

    const logout = () => {
      token.value = ''
      nickname.value = ''
      avatar.value = ''
    }

    return { token, nickname, avatar, isLoggedIn, setLoginInfo, logout }
  },
  {
    persist: true,
  },
)
