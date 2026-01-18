<template>
  <div class="ai-assistant-container">
    <!-- Chat Header -->
    <div class="chat-header">
      <div class="header-main">
        <div class="bot-avatar" style="background: linear-gradient(135deg, #a78bfa, #8b5cf6);">
          <el-icon style="font-size: 24px;"><i-tabler-robot /></el-icon>
          <span class="status-dot"></span>
        </div>
        <div class="header-info">
          <h3>智能记账助理</h3>
          <p>基于 AI 的财务洞察与自动化操作</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button link @click="clearHistory">
          <el-icon style="color: #ef4444"><i-tabler-trash /></el-icon> <span style="color: #64748b">清空对话</span>
        </el-button>
      </div>
    </div>

    <!-- Message Stream -->
    <div class="message-stream" ref="streamRef">
      <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
        <div class="avatar">
          <el-icon v-if="msg.role === 'user'"><i-tabler-user /></el-icon>
          <el-icon v-else><i-tabler-robot /></el-icon>
        </div>
        <div class="message-bubble-wrapper">
          <div class="bubble">
            <div v-if="msg.role === 'bot' && msg.content" v-html="renderMarkdown(msg.content)" class="markdown-body"></div>
            <div v-else>{{ msg.content }}</div>
            <div v-if="msg.streaming" class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
          <span class="timestamp">{{ msg.time }}</span>
        </div>
      </div>
      
      <!-- Empty State -->
      <div v-if="messages.length === 0" class="empty-chat">
        <div class="welcome-box">
          <span class="emoji">👋</span>
          <h2>您好！我是您的智能财务管家</h2>
          <p>您可以试着对我说：</p>
          <div class="quick-prompts">
            <button @click="autoFill('帮我生成10条本月的测试账单')">"帮我生成 10 条测试账单"</button>
            <button @click="autoFill('分析一下我近期的消费风险')">"分析我的消费风险"</button>
            <button @click="autoFill('本月还有多少预算？')">"查看本月剩余预算"</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="input-container">
      <div class="input-wrapper">
        <el-input
          v-model="input"
          placeholder="给助理发送消息(Shift+Enter换行)..."
          type="textarea"
          :rows="1"
          autosize
          @keydown.enter="handleKeyDown"
          class="chat-input"
        />
        <el-button 
          type="primary" 
          class="send-btn" 
          :disabled="!input.trim() || isStreaming"
          @click="handleSend"
        >
          <el-icon><i-tabler-send /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
// import { User, Promotion, Delete, Service } from '@element-plus/icons-vue' // Removed
import { ElMessage } from 'element-plus'
import { marked } from 'marked'
import dompurify from 'dompurify'
import dayjs from 'dayjs'
import { messages } from './chatState' // 引入全局状态实现持久化

const input = ref('')
const isStreaming = ref(false)
const streamRef = ref()

const renderMarkdown = (text) => {
  return dompurify.sanitize(marked(text))
}

const scrollToBottom = () => {
  nextTick(() => {
    if (streamRef.value) {
      streamRef.value.scrollTop = streamRef.value.scrollHeight
    }
  })
}

const autoFill = (text) => {
  input.value = text
  handleSend()
}

const handleKeyDown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault() // 阻止默认换行
    handleSend()
  }
}

const handleSend = () => {
  if (!input.value.trim() || isStreaming.value) return
  
  const content = input.value.trim()
  messages.value.push({
    role: 'user',
    content: content,
    time: dayjs().format('HH:mm')
  })
  input.value = ''
  scrollToBottom()
  
  startStreaming(content)
}

const startStreaming = (content) => {
  isStreaming.value = true
  const botMsg = reactive({
    role: 'bot',
    content: '',
    time: dayjs().format('HH:mm'),
    streaming: true
  })
  messages.value.push(botMsg)
  
  // 使用新的 API 路径
  const eventSource = new EventSource(`/api/accounting/ai/chat?message=${encodeURIComponent(content)}`)
  
  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'content') {
        botMsg.content += data.content.replace(/<br_mark>/g, '\n')
        scrollToBottom()
      } else if (data.type === 'magic') {
        window.dispatchEvent(new CustomEvent('accounting-data-refresh'))
        ElMessage.success({
          message: '✨ 智能指令已执行',
          plain: true,
          duration: 3000
        })
      } else if (data.type === 'done') {
        botMsg.streaming = false
        isStreaming.value = false
        eventSource.close()
      }
    } catch (e) {
      // 兼容非 JSON 格式的消息（直接文本）
      botMsg.content += event.data.replace(/<br_mark>/g, '\n')
      scrollToBottom()
    }
  }
  
  eventSource.onerror = () => {
    isStreaming.value = false
    botMsg.streaming = false
    eventSource.close()
  }
}

const clearHistory = () => {
  messages.value = []
}

onMounted(() => {
  scrollToBottom() // 切换回来时自动滚动到底部
  // 保持连接后的自动刷新同步
  window.addEventListener('accounting-data-refresh', () => {
    console.log('[AI] 数据已自动同步')
  })
})
</script>

<style scoped>
.ai-assistant-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 25px -5px rgba(0,0,0,0.05);
  overflow: hidden;
  border: 1px solid #f1f5f9;
}

.chat-header {
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bot-avatar {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  position: relative;
}

.status-dot {
  position: absolute;
  bottom: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #10b981;
  border: 2.5px solid #fff;
  border-radius: 50%;
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
  font-weight: 700;
}

.header-info p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #94a3b8;
}

.message-stream {
  flex: 1;
  overflow-y: auto;
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 28px;
  background: #f8fafc;
}

.message-item {
  display: flex;
  gap: 16px;
  max-width: 85%;
}

.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.user .avatar { background: #eef2ff; color: #6366f1; border: 1px solid #e0e7ff; }
.bot .avatar { background: #6366f1; color: #fff; }

.message-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.user .message-bubble-wrapper { align-items: flex-end; }

.bubble {
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.6;
  position: relative;
  box-shadow: 0 1px 2px rgba(0,0,0,0.03);
}

.user .bubble {
  background: #6366f1;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bot .bubble {
  background: #fff;
  color: #1e293b;
  border-bottom-left-radius: 4px;
}

.timestamp {
  font-size: 11px;
  color: #94a3b8;
  margin: 0 4px;
}

.input-container {
  padding: 24px;
  background: #fff;
  border-top: 1px solid #f1f5f9;
}

.input-wrapper {
  background: #f1f5f9;
  border-radius: 16px;
  padding: 6px;
  display: flex;
  align-items: flex-end;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.input-wrapper:focus-within {
  background: #fff;
  border-color: #6366f1;
  box-shadow: 0 0 0 4px #6366f111;
}

:deep(.chat-input .el-textarea__inner) {
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 12px 14px;
  color: #1e293b;
  font-size: 14px;
  max-height: 150px;
}

.send-btn {
  margin: 4px;
  border-radius: 12px;
  height: 40px;
  width: 40px;
  transition: all 0.2s;
}

.empty-chat {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.welcome-box {
  max-width: 450px;
}

.emoji { font-size: 60px; display: block; margin-bottom: 20px; }

.welcome-box h2 {
    font-size: 24px;
    color: #1e293b;
    margin-bottom: 12px;
}

.quick-prompts {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.quick-prompts button {
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.quick-prompts button:hover {
  border-color: #6366f1;
  color: #6366f1;
  background: #f8faff;
  transform: translateX(4px);
}

.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 10px 0;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  background: #6366f1;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.3); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

/* Markdown styling */
:deep(.markdown-body) {
  font-size: 14px;
}
:deep(.markdown-body p) { margin-bottom: 8px; }
:deep(.markdown-body p:last-child) { margin-bottom: 0; }

:deep(.markdown-body table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

:deep(.markdown-body th), :deep(.markdown-body td) {
  padding: 10px 14px;
  text-align: left;
  border: 1px solid #e2e8f0;
}

:deep(.markdown-body th) {
  background-color: #f8fafc;
  font-weight: 600;
  color: #475569;
}

:deep(.markdown-body tr:nth-child(even)) {
  background-color: #fcfcfc;
}

:deep(.markdown-body tr:hover) {
  background-color: #f8faff;
}
</style>
