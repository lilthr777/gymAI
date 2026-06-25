import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import type { ChatMessage } from '@/types'

export function useSSE() {
  const chatStore = useChatStore()
  const abortController = ref<AbortController | null>(null)

  const sendMessage = async (message: string) => {
    chatStore.addMessage({
      role: 'user',
      content: message,
      timestamp: Date.now(),
    })

    chatStore.setStreaming(true)
    abortController.value = new AbortController()

    const assistantMsg: ChatMessage = {
      role: 'assistant',
      content: '',
      timestamp: Date.now(),
    }
    chatStore.addMessage(assistantMsg)

    try {
      const response = await fetch('/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message }),
        signal: abortController.value.signal,
      })

      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      if (!response.body) throw new Error('Response body is null')

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''

        for (const line of lines) {
          if (line.startsWith('data:')) {
            const content = line.slice(5).trim()
            if (content && content !== '[DONE]') {
              assistantMsg.content += content
              // 触发响应式更新
              chatStore.messages[chatStore.messages.length - 1] = { ...assistantMsg }
            }
          }
        }
      }
    } catch (error: any) {
      if (error.name === 'AbortError') return
      assistantMsg.content = '抱歉，AI 助手暂时无法响应，请稍后重试。'
      chatStore.messages[chatStore.messages.length - 1] = { ...assistantMsg }
    } finally {
      chatStore.setStreaming(false)
    }
  }

  const stopStreaming = () => {
    abortController.value?.abort()
    chatStore.setStreaming(false)
  }

  return { sendMessage, stopStreaming }
}
