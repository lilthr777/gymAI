<template>
  <div class="ai-chat-page">
    <div class="chat-panel">
      <!-- Header -->
      <div class="chat-top">
        <div class="chat-top__title">
          <span class="chat-dot"></span>
          AI 助手
        </div>
        <button class="chat-top__clear" @click="handleClear" :disabled="messages.length === 0">
          <el-icon size="16"><Delete /></el-icon>
          <span>清空</span>
        </button>
      </div>

      <!-- Messages -->
      <div ref="messageListRef" class="chat-body">
        <!-- Empty state -->
        <div v-if="messages.length === 0 && !isStreamingLocal" class="chat-empty">
          <p class="empty-heading">Hi，我是你的健身助手</p>
          <p class="empty-hint">试试下面的问题，或者直接问我～</p>
          <div class="prompt-chips">
            <button
              v-for="prompt in quickPrompts"
              :key="prompt"
              class="prompt-chip"
              @click="handleQuickPrompt(prompt)"
            >
              {{ prompt }}
            </button>
          </div>
        </div>

        <!-- Message items -->
        <div
          v-for="(msg, idx) in messages"
          :key="msg.id ?? idx"
          class="msg-row"
          :class="msg.role === 'user' ? 'msg--user' : 'msg--bot'"
        >
          <div class="msg-bubble" :class="msg.role === 'user' ? 'bubble--user' : 'bubble--bot'">
            <template v-if="msg.role === 'assistant' && idx === messages.length - 1 && isStreamingLocal">
              <span>{{ displayedContent }}</span>
              <span class="cursor-blink">|</span>
            </template>
            <template v-else>
              <span v-if="msg.role === 'user'">{{ msg.content }}</span>
              <div v-else class="md-content" v-html="renderMarkdown(msg.content)"></div>
            </template>
          </div>
        </div>

        <!-- Streaming: first message -->
        <div v-if="isStreamingLocal && messages.length === 0" class="msg-row msg--bot">
          <div class="msg-bubble bubble--bot">
            <span>{{ displayedContent }}</span>
            <span class="cursor-blink">|</span>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="chat-foot">
        <div class="input-row">
          <input
            ref="inputRef"
            v-model="inputText"
            class="chat-input"
            placeholder="输入问题，Enter 发送"
            :disabled="isStreamingLocal"
            @keydown.enter="handleSend"
          />
          <button
            v-if="!isStreamingLocal"
            class="send-btn"
            :disabled="!inputText.trim()"
            @click="handleSend"
          >
            <el-icon size="18"><Promotion /></el-icon>
          </button>
          <button
            v-else
            class="stop-btn"
            @click="handleStop"
          >
            停止
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Promotion } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useUserStore } from '@/stores/user'
import { marked } from 'marked'

// 配置 marked 不转义 HTML（内部数据可信）
marked.setOptions({ breaks: true, gfm: true })

const renderMarkdown = (text: string) => {
  if (!text) return ''
  if (!text) return ''
  // SSE 流式拼接会吞掉 data 事件之间的 \n，导致 Markdown 表格挤成一行
  let repaired = text
    // 1. 表格行间双管道：|表头||分隔行| → |表头|\n|分隔行|
    .replace(/\|\|(?=\S)/g, '|\n|')
    // 2. 正文贴表格头：文字|表头| → 文字\n\n|表头|
    .replace(/([^\n|])\|(?=[^|\n]+\|[^|\n]+\|)/g, '$1\n\n|')
  return marked.parse(repaired) as string
}

const chatStore = useChatStore()
const userStore = useUserStore()
const messages = computed(() => chatStore.messages)

const inputRef = ref<HTMLInputElement>()
const inputText = ref('')
const isStreamingLocal = ref(false)
const displayedContent = ref('')
const fullResponseBuffer = ref('')
const messageListRef = ref<HTMLDivElement>()

let typewriterTimer: ReturnType<typeof setInterval> | null = null
let abortController: AbortController | null = null

const quickPrompts = [
  '帮我推荐适合减脂的课程',
  '这周有什么瑜伽课？',
  '我报了哪些课程？',
  '本月签到几次了？',
]

const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTo({
      top: messageListRef.value.scrollHeight,
      behavior: 'smooth',
    })
  }
}

const startTypewriter = () => {
  stopTypewriter()
  typewriterTimer = setInterval(() => {
    if (fullResponseBuffer.value.length > displayedContent.value.length) {
      const charsToAdd = Math.min(3, fullResponseBuffer.value.length - displayedContent.value.length)
      displayedContent.value += fullResponseBuffer.value.slice(
        displayedContent.value.length,
        displayedContent.value.length + charsToAdd
      )
      scrollToBottom()
    }
  }, 30)
}

const stopTypewriter = () => {
  if (typewriterTimer) {
    clearInterval(typewriterTimer)
    typewriterTimer = null
  }
}

const finishTypewriter = () => {
  stopTypewriter()
  displayedContent.value = fullResponseBuffer.value
}

const handleQuickPrompt = (prompt: string) => {
  inputText.value = prompt
  handleSend()
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || isStreamingLocal.value) return

  inputText.value = ''
  chatStore.addMessage({ role: 'user', content: text, timestamp: Date.now() })
  // 先插入空 assistant 消息，后续原地填充内容
  chatStore.addMessage({ role: 'assistant', content: '', timestamp: Date.now() })

  fullResponseBuffer.value = ''
  displayedContent.value = ''
  isStreamingLocal.value = true
  startTypewriter()

  await scrollToBottom()

  abortController = new AbortController()

  try {
    const response = await fetch('/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: text, userId: userStore.userId }),
      signal: abortController.signal,
    })

    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    if (!response.body) throw new Error('No response body')

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
          const content = line.slice(5).replace(/^ /, '')
          if (content === '[DONE]') continue
          if (content) {
            fullResponseBuffer.value += content
          }
        }
      }
    }
  } catch (err: any) {
    if (err.name !== 'AbortError') {
      fullResponseBuffer.value += '\n\n[请求失败，请稍后重试]'
    }
  } finally {
    abortController = null
    finishTypewriter()
    // 更新最后一条 assistant 消息的内容
    const msgs = chatStore.messages
    const lastMsg = msgs[msgs.length - 1]
    if (lastMsg && lastMsg.role === 'assistant') {
      lastMsg.content = fullResponseBuffer.value || displayedContent.value
    }
    isStreamingLocal.value = false
    fullResponseBuffer.value = ''
    displayedContent.value = ''
    scrollToBottom()
  }
}

const handleStop = () => {
  abortController?.abort()
  fullResponseBuffer.value += '\n\n[已停止]'
}

const handleClear = () => {
  ElMessageBox.confirm('确定要清空所有对话记录吗？', '清空确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    chatStore.clearMessages()
    ElMessage.success('对话已清空')
  })
}

onUnmounted(() => {
  stopTypewriter()
  abortController?.abort()
})
</script>

<style scoped lang="scss">
.ai-chat-page {
  height: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.chat-panel {
  width: 100%;
  max-width: 780px;
  display: flex;
  flex-direction: column;
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: $radius-lg;
  overflow: hidden;
}

// ── Top ──────────────────────────────────────
.chat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid $color-steel;

  &__title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 15px;
    font-weight: 600;
    color: $color-carbon;
  }

  &__clear {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 6px 12px;
    border: 1px solid $color-steel;
    background: transparent;
    border-radius: $radius-sm;
    color: $color-lead;
    font-size: 13px;
    cursor: pointer;
    font-family: $font-body;
    transition: all $transition-fast;

    &:hover:not(:disabled) {
      border-color: $color-iron-red;
      color: $color-iron-red;
    }

    &:disabled {
      opacity: 0.35;
      cursor: default;
    }
  }
}

.chat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #5C9A4F;
}

// ── Body ─────────────────────────────────────
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 24px 20px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  background: #FAF9F7;
}

.chat-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  text-align: center;
  padding: 40px 20px;
}

.empty-heading {
  font-size: 17px;
  font-weight: 600;
  color: $color-carbon;
  margin: 0;
}

.empty-hint {
  font-size: 14px;
  color: $color-lead;
  margin: 0 0 16px;
}

.prompt-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.prompt-chip {
  padding: 8px 16px;
  font-size: 13px;
  font-family: $font-body;
  color: $color-ash;
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: 20px;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $color-cobalt;
    color: $color-cobalt;
  }
}

// ── Messages ─────────────────────────────────
.msg-row {
  display: flex;
  max-width: 82%;

  &.msg--user {
    align-self: flex-end;
  }

  &.msg--bot {
    align-self: flex-start;
  }
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;

  &.bubble--user {
    background: $color-cobalt;
    color: $color-sheet;
    border-bottom-right-radius: 4px;
    white-space: pre-wrap;
  }

  &.bubble--bot {
    background: $color-sheet;
    color: $color-carbon;
    border: 1px solid $color-steel;
    border-bottom-left-radius: 4px;
    overflow-x: auto;
  }
}

.cursor-blink {
  animation: blink 1s step-end infinite;
  color: $color-cobalt;
}

// ── Markdown Content ─────────────────────────
.md-content {
  :deep(p) { margin: 0 0 8px; &:last-child { margin-bottom: 0; } }
  :deep(strong) { font-weight: 600; }
  :deep(em) { font-style: italic; }
  :deep(ul), :deep(ol) { margin: 4px 0 8px; padding-left: 20px; }
  :deep(li) { margin-bottom: 2px; }
  :deep(code) { background: rgba(0,0,0,0.06); padding: 1px 5px; border-radius: 3px; font-size: 13px; }
  :deep(pre) { background: rgba(0,0,0,0.04); padding: 10px 14px; border-radius: 6px; overflow-x: auto; margin: 8px 0; }
  :deep(table) { width: 100%; border-collapse: collapse; margin: 8px 0; font-size: 13px; }
  :deep(th) { background: rgba(0,0,0,0.04); font-weight: 600; text-align: left; padding: 8px 10px; border-bottom: 2px solid rgba(0,0,0,0.1); }
  :deep(td) { padding: 6px 10px; border-bottom: 1px solid rgba(0,0,0,0.06); }
  :deep(hr) { border: none; border-top: 1px solid rgba(0,0,0,0.1); margin: 12px 0; }
  :deep(blockquote) { border-left: 3px solid $color-cobalt; padding-left: 12px; margin: 8px 0; color: $color-lead; }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

// ── Foot ─────────────────────────────────────
.chat-foot {
  padding: 16px 20px;
  border-top: 1px solid $color-steel;
  background: $color-sheet;
}

.input-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-input {
  flex: 1;
  height: 42px;
  padding: 0 16px;
  font-size: 14px;
  font-family: $font-body;
  color: $color-carbon;
  background: #F5F3F0;
  border: 1px solid transparent;
  border-radius: 8px;
  outline: none;
  transition: border-color $transition-fast, background $transition-fast;

  &:focus {
    background: $color-sheet;
    border-color: $color-cobalt;
  }

  &::placeholder {
    color: #B0B4BA;
  }

  &:disabled {
    opacity: 0.5;
  }
}

.send-btn {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: $color-cobalt;
  color: $color-sheet;
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity $transition-fast;

  &:hover:not(:disabled) {
    opacity: 0.88;
  }

  &:disabled {
    opacity: 0.3;
    cursor: default;
  }
}

.stop-btn {
  height: 42px;
  padding: 0 18px;
  border: 1px solid $color-iron-red;
  background: transparent;
  color: $color-iron-red;
  border-radius: 8px;
  font-size: 14px;
  font-family: $font-body;
  cursor: pointer;
  flex-shrink: 0;
  transition: all $transition-fast;

  &:hover {
    background: $color-iron-red;
    color: $color-sheet;
  }
}

// ── Dark Mode ────────────────────────────────
html.dark {
  .chat-panel {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .chat-top {
    border-bottom-color: $dark-border;

    &__title {
      color: $dark-text;
    }

    &__clear {
      border-color: $dark-border;
      color: $dark-text-secondary;
    }
  }

  .chat-body {
    background: $dark-bg;
  }

  .empty-heading {
    color: $dark-text;
  }

  .prompt-chip {
    color: $dark-text-secondary;
    background: $dark-bg-secondary;
    border-color: $dark-border;

    &:hover {
      border-color: $color-cobalt;
      color: $color-cobalt;
    }
  }

  .msg-bubble.bubble--bot {
    background: $dark-bg-secondary;
    border-color: $dark-border;
    color: $dark-text;
  }

  .md-content {
    :deep(th) { background: rgba(255,255,255,0.06); border-bottom-color: rgba(255,255,255,0.1); }
    :deep(td) { border-bottom-color: rgba(255,255,255,0.06); }
    :deep(code) { background: rgba(255,255,255,0.08); }
    :deep(pre) { background: rgba(255,255,255,0.04); }
    :deep(hr) { border-top-color: rgba(255,255,255,0.1); }
    :deep(blockquote) { color: $dark-text-secondary; }
  }

  .chat-foot {
    border-top-color: $dark-border;
    background: $dark-bg-secondary;
  }

  .chat-input {
    background: $dark-bg;
    color: $dark-text;

    &:focus {
      background: $dark-bg-secondary;
      border-color: $color-cobalt;
    }
  }

  .stop-btn {
    border-color: $color-iron-red;

    &:hover {
      background: $color-iron-red;
    }
  }
}

// ── Responsive ───────────────────────────────
@media (max-width: 640px) {
  .ai-chat-page {
    padding: 0;
  }

  .chat-panel {
    border-radius: 0;
    border: none;
  }

  .msg-row {
    max-width: 90%;
  }
}
</style>
