import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AuthResult } from '@/types'

export const useUserStore = defineStore(
  'user',
  () => {
    const token = ref('')
    const nickname = ref('')
    const avatar = ref('')
    const userId = ref('')
    const role = ref('')

    const isLoggedIn = () => !!token.value
    const isAdmin = computed(() => role.value === 'ADMIN')
    const isCoach = computed(() => role.value === 'COACH')

    const setLoginInfo = (info: AuthResult) => {
      token.value = info.token
      nickname.value = info.nickname
      avatar.value = info.avatar
      userId.value = info.userId
      role.value = info.role || 'MEMBER'
    }

    const logout = () => {
      token.value = ''
      nickname.value = ''
      avatar.value = ''
      userId.value = ''
      role.value = ''
    }

    return { token, nickname, avatar, userId, role, isLoggedIn, isAdmin, isCoach, setLoginInfo, logout }
  },
  {
    persist: true,
  },
)
