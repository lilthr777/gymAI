import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ChatMessage } from '@/types'

export const useChatStore = defineStore(
  'chat',
  () => {
    const messages = ref<ChatMessage[]>([])
    const isStreaming = ref(false)

    const addMessage = (msg: ChatMessage) => {
      messages.value.push(msg)
    }

    const clearMessages = () => {
      messages.value = []
    }

    const setStreaming = (val: boolean) => {
      isStreaming.value = val
    }

    return { messages, isStreaming, addMessage, clearMessages, setStreaming }
  },
  {
    persist: true,
  },
)
