<template>
  <div class="digital-human-layer">
    <button class="assistant-tab" type="button" @click="panelOpen = !panelOpen">
      <span class="assistant-tab__dot" :class="{ active: isBusy || isSpeaking || isListening }"></span>
      <span>数字人助手</span>
      <small>{{ status }}</small>
    </button>

    <section v-show="panelOpen" class="assistant-panel">
      <header class="assistant-header">
        <div>
          <span class="assistant-kicker">Live2D + Voice</span>
          <h2>火花</h2>
          <p>{{ modelHint }}</p>
        </div>
        <div class="assistant-header__actions">
          <el-tooltip content="收起面板" placement="bottom">
            <button class="icon-button" type="button" @click="panelOpen = false">
              <i-tabler-chevron-down />
            </button>
          </el-tooltip>
        </div>
      </header>

      <div class="assistant-toolbar">
        <el-tooltip :content="isListening ? '停止语音输入' : '开始语音输入'" placement="top">
          <button class="tool-button primary" type="button" :disabled="!speechSupported || isBusy" @click="toggleListening">
            <i-tabler-microphone v-if="!isListening" />
            <i-tabler-player-stop v-else />
            <span>{{ isListening ? '停止' : '语音' }}</span>
          </button>
        </el-tooltip>

        <el-tooltip :content="muted ? '取消静音' : '静音播报'" placement="top">
          <button class="tool-button" type="button" @click="toggleMute">
            <i-tabler-volume-off v-if="muted" />
            <i-tabler-volume v-else />
            <span>{{ muted ? '静音' : '播报' }}</span>
          </button>
        </el-tooltip>

        <el-tooltip content="停止朗读" placement="top">
          <button class="tool-button" type="button" @click="stopSpeech">
            <i-tabler-player-stop />
            <span>停止</span>
          </button>
        </el-tooltip>

        <el-tooltip content="新会话" placement="top">
          <button class="tool-button" type="button" @click="resetSession">
            <i-tabler-refresh />
            <span>新会话</span>
          </button>
        </el-tooltip>
      </div>

      <div class="quick-row">
        <button v-for="item in quickPrompts" :key="item" type="button" @click="sendMessage(item)">
          {{ item }}
        </button>
      </div>

      <div ref="historyRef" class="assistant-history">
        <article v-for="(item, index) in messages" :key="index" class="assistant-message" :class="item.role">
          <strong>{{ item.role === 'user' ? '我' : item.role === 'assistant' ? '火花' : '系统' }}</strong>
          <p>{{ item.text }}</p>
        </article>
      </div>

      <div v-if="transcript" class="transcript">识别结果：{{ transcript }}</div>

      <footer class="assistant-input-row">
        <textarea
          v-model="draft"
          :disabled="isBusy"
          rows="2"
          placeholder="输入问题，或点击麦克风说话"
          @keydown.enter.exact.prevent="sendMessage()"
        ></textarea>
        <button class="send-button" type="button" :disabled="!draft.trim() || isBusy" @click="sendMessage()">
          <i-tabler-send />
        </button>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAssistantHealth, getVoiceConfig, postAsrAudio, streamFlaskChat } from '@/api/assistant'

const route = useRoute()
const panelOpen = ref(false)
const draft = ref('')
const transcript = ref('')
const status = ref('待命')
const modelHint = ref('正在连接数字人服务')
const messages = ref([])
const historyRef = ref(null)
const isBusy = ref(false)
const isListening = ref(false)
const isSpeaking = ref(false)
const muted = ref(localStorage.getItem('digital_human_muted') === '1')
const sessionId = ref(localStorage.getItem('digital_human_session_id') || createSessionId())

let recognition = null
let mediaRecorder = null
let mediaStream = null
let audioChunks = []

const speechSupported = computed(() => {
  const secure = window.isSecureContext || location.protocol === 'https:' || ['localhost', '127.0.0.1'].includes(location.hostname)
  return secure && Boolean(window.SpeechRecognition || window.webkitSpeechRecognition || navigator.mediaDevices?.getUserMedia)
})

const quickPrompts = computed(() => {
  if (route.path.startsWith('/accounting')) {
    return ['本月还有多少预算？', '分析近期消费风险', '帮我生成10条测试账单']
  }
  if (route.path.includes('lost-found')) {
    return ['失物招领怎么用？', 'AI视觉能检测什么？', '如何认领物品？']
  }
  if (route.path.includes('dashboard')) {
    return ['解读教室监控数据', '如何打开AI视觉？', '专注度如何计算？']
  }
  return ['你可以做什么？', '介绍智学空间', '如何使用语音助手？']
})

function createSessionId() {
  return `smart-campus-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 8)}`
}

function appendMessage(role, text) {
  messages.value.push({ role, text: String(text || '').trim() })
  nextTick(() => {
    if (historyRef.value) {
      historyRef.value.scrollTop = historyRef.value.scrollHeight
    }
  })
}

function getSceneName() {
  if (route.path.startsWith('/accounting')) return '智学空间-个人记账'
  if (route.path.includes('lost-found')) return '智学空间-失物招领'
  if (route.path.includes('dashboard')) return '智学空间-教室监控'
  if (route.path.includes('seat')) return '智学空间-座位预约'
  if (route.path.includes('canteen')) return '智学空间-食堂服务'
  return '智学空间'
}

function shouldUseSpringAccounting() {
  return route.path.startsWith('/accounting')
}

async function sendMessage(raw = draft.value) {
  const text = String(raw || '').trim()
  if (!text || isBusy.value) return

  stopSpeech()
  panelOpen.value = true
  draft.value = ''
  transcript.value = ''
  isBusy.value = true
  status.value = '生成中'
  appendMessage('user', text)
  appendMessage('assistant', '')
  const assistantIndex = messages.value.length - 1

  try {
    if (shouldUseSpringAccounting()) {
      await streamSpringAccounting(text, assistantIndex)
    } else {
      await streamFlaskChat({
        message: text,
        sessionId: sessionId.value,
        scene: getSceneName(),
        onSession: (nextSession) => {
          if (nextSession) {
            sessionId.value = nextSession
            localStorage.setItem('digital_human_session_id', nextSession)
          }
        },
        onDelta: (delta) => {
          messages.value[assistantIndex].text += delta
          status.value = '回复中'
          notifyLive2d('onDelta', delta)
        }
      })
    }
    const reply = messages.value[assistantIndex].text || '我刚刚没有组织出合适的回答，你可以换个方式再问一次。'
    messages.value[assistantIndex].text = reply
    status.value = '已回复'
    speak(reply)
  } catch (error) {
    messages.value.splice(assistantIndex, 1)
    appendMessage('system', error.message || '请求失败')
    status.value = '请求失败'
  } finally {
    notifyLive2d('onStreamEnd')
    isBusy.value = false
  }
}

function streamSpringAccounting(text, assistantIndex) {
  return new Promise((resolve, reject) => {
    notifyLive2d('onStreamStart', { text })
    const source = new EventSource(`/api/accounting/ai/chat?message=${encodeURIComponent(text)}`)
    source.onmessage = (event) => {
      messages.value[assistantIndex].text += String(event.data || '').replace(/<br_mark>/g, '\n')
      status.value = '回复中'
      notifyLive2d('onDelta', event.data)
    }
    source.addEventListener('magic', () => {
      window.dispatchEvent(new CustomEvent('accounting-data-refresh'))
      ElMessage.success('智能指令已执行')
    })
    source.onerror = () => {
      source.close()
      if (messages.value[assistantIndex].text) {
        resolve()
      } else {
        reject(new Error('财务 AI 服务连接失败'))
      }
    }
  })
}

function speak(text) {
  if (muted.value || !window.speechSynthesis || !text) return
  const utterance = new SpeechSynthesisUtterance(text)
  const voices = window.speechSynthesis.getVoices()
  utterance.voice = voices.find((voice) => String(voice.lang || '').includes('zh')) || null
  utterance.lang = 'zh-CN'
  utterance.rate = 0.98
  utterance.pitch = 1.05
  utterance.onstart = () => {
    isSpeaking.value = true
    status.value = '朗读中'
    notifyLive2d('onSpeechStart', { text })
  }
  utterance.onboundary = () => notifyLive2d('onSpeechPulse', { intensity: 0.55, text })
  utterance.onend = () => {
    isSpeaking.value = false
    status.value = '待命'
    notifyLive2d('onSpeechEnd')
  }
  utterance.onerror = () => {
    isSpeaking.value = false
    notifyLive2d('onSpeechEnd')
  }
  window.speechSynthesis.cancel()
  window.speechSynthesis.speak(utterance)
}

function stopSpeech() {
  if (window.speechSynthesis) {
    window.speechSynthesis.cancel()
  }
  isSpeaking.value = false
  notifyLive2d('onSpeechEnd')
}

function toggleMute() {
  muted.value = !muted.value
  localStorage.setItem('digital_human_muted', muted.value ? '1' : '0')
  if (muted.value) stopSpeech()
}

function resetSession() {
  sessionId.value = createSessionId()
  localStorage.setItem('digital_human_session_id', sessionId.value)
  messages.value = []
  appendMessage('system', '已创建新的数字人会话')
}

function toggleListening() {
  if (isListening.value) {
    stopListening(true)
    return
  }
  startListening()
}

function startListening() {
  stopSpeech()
  transcript.value = ''
  const RecognitionCtor = window.SpeechRecognition || window.webkitSpeechRecognition
  if (RecognitionCtor) {
    recognition = new RecognitionCtor()
    recognition.lang = 'zh-CN'
    recognition.interimResults = true
    recognition.continuous = true
    let finalText = ''
    recognition.onstart = () => {
      isListening.value = true
      status.value = '聆听中'
    }
    recognition.onresult = (event) => {
      let interim = ''
      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const value = event.results[index][0].transcript
        if (event.results[index].isFinal) finalText += value
        else interim += value
      }
      transcript.value = (finalText || interim).trim()
    }
    recognition.onend = () => {
      isListening.value = false
      const text = transcript.value.trim()
      if (text) sendMessage(text)
    }
    recognition.onerror = () => {
      isListening.value = false
      status.value = '识别失败'
    }
    recognition.start()
    return
  }
  startRecorderAsr()
}

async function startRecorderAsr() {
  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
    audioChunks = []
    mediaRecorder = new MediaRecorder(mediaStream, { mimeType: 'audio/webm' })
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) audioChunks.push(event.data)
    }
    mediaRecorder.onstop = async () => {
      isListening.value = false
      status.value = '识别中'
      try {
        const data = await postAsrAudio(new Blob(audioChunks, { type: 'audio/webm' }))
        transcript.value = data.text || ''
        if (transcript.value) await sendMessage(transcript.value)
      } catch (error) {
        appendMessage('system', error.message || '语音识别失败')
        status.value = '识别失败'
      } finally {
        cleanupMedia()
      }
    }
    mediaRecorder.start()
    isListening.value = true
    status.value = '录音中'
  } catch (error) {
    appendMessage('system', error.message || '无法获取麦克风权限')
    status.value = '麦克风不可用'
    cleanupMedia()
  }
}

function stopListening(manual = false) {
  if (recognition) {
    try { recognition.stop() } catch (e) {}
    recognition = null
  }
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop()
  } else if (manual && transcript.value.trim()) {
    sendMessage(transcript.value)
  }
  isListening.value = false
}

function cleanupMedia() {
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
  }
  mediaStream = null
  mediaRecorder = null
}

function notifyLive2d(name, payload) {
  const hook = window.__voiceLive2dHooks?.[name]
  if (typeof hook === 'function') {
    try { hook(payload) } catch (e) {}
  }
}

function loadScriptOnce(id, src, attrs = {}) {
  return new Promise((resolve, reject) => {
    const old = document.getElementById(id)
    if (old) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.id = id
    script.src = src
    Object.entries(attrs).forEach(([key, value]) => script.setAttribute(key, value))
    script.onload = () => resolve()
    script.onerror = () => reject(new Error(`加载脚本失败：${src}`))
    document.head.appendChild(script)
  })
}

async function initLive2d() {
  if (window.__smartCampusLive2dStarted) return
  window.__smartCampusLive2dStarted = true
  try {
    await loadScriptOnce('smart-campus-live2d-autoload', '/live2d-widget-dist/autoload.js')
    const startAt = Date.now()
    const wait = window.setInterval(() => {
      if (typeof window.initWidget !== 'function') {
        if (Date.now() - startAt > 10000) window.clearInterval(wait)
        return
      }
      window.clearInterval(wait)
      window.initWidget({
        waifuPath: '/live2d-widget-dist/waifu-huahuo.json?v=20260420d',
        cubism2Path: '/live2d-widget-dist/live2d.min.js?v=20260420c',
        cubism5Path: '/live2d-widget-dist/live2dcubismcore.min.js?v=20260420c',
        modelId: 0,
        tools: [],
        drag: true,
        logLevel: 'warn'
      })
      installLive2dGuards()
    }, 300)
  } catch (error) {
    console.warn(error)
  }
}

function installLive2dGuards() {
  const style = document.createElement('style')
  style.textContent = `
    #waifu-tool,#waifu-toggle,#waifu-tips{display:none!important}
    #waifu{z-index:10020!important;right:22px!important;bottom:92px!important;transform:translateY(20px);pointer-events:auto!important}
    #waifu.waifu-hidden{display:block!important;opacity:1!important;visibility:visible!important}
    @media (max-width: 720px){#waifu{right:-16px!important;bottom:72px!important;transform:scale(.72) translateY(30px);transform-origin:right bottom}}
  `
  document.head.appendChild(style)
  window.setInterval(() => {
    const waifu = document.getElementById('waifu')
    if (!waifu) return
    waifu.classList.remove('waifu-hidden')
    waifu.classList.add('waifu-active')
    if (waifu.style.display === 'none') waifu.style.display = ''
  }, 1500)
}

onMounted(async () => {
  localStorage.setItem('digital_human_session_id', sessionId.value)
  initLive2d()
  window.addEventListener('digital-human-open', handleExternalOpen)
  try {
    const [health, voice] = await Promise.all([getAssistantHealth(), getVoiceConfig()])
    modelHint.value = health.chat_configured
      ? `模型：${health.model || '已配置'}；语音：${voice.realtime_configured ? '实时服务已配置' : '浏览器播报'}`
      : 'Flask AI 未配置，请检查 ARK_API_KEY'
  } catch (error) {
    modelHint.value = '未连接 Flask 数字人服务'
  }
  appendMessage('assistant', '你好，我是智学空间数字人助手。你可以打字，也可以用麦克风和我交流。')
})

onBeforeUnmount(() => {
  window.removeEventListener('digital-human-open', handleExternalOpen)
  stopListening(false)
  stopSpeech()
  cleanupMedia()
})

function handleExternalOpen(event) {
  panelOpen.value = true
  const prompt = String(event?.detail?.message || '').trim()
  if (prompt) {
    sendMessage(prompt)
  }
}
</script>

<style scoped>
.digital-human-layer {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 10040;
  pointer-events: none;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

.assistant-tab,
.assistant-panel {
  pointer-events: auto;
}

.assistant-tab {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  border: 1px solid rgba(64, 158, 255, 0.22);
  border-radius: 12px;
  padding: 0 14px;
  background: #ffffff;
  color: #1f2d3d;
  box-shadow: 0 10px 26px rgba(15, 23, 42, 0.16);
  cursor: pointer;
  font-weight: 700;
}

.assistant-tab small {
  color: #64748b;
  font-weight: 500;
}

.assistant-tab__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #10b981;
}

.assistant-tab__dot.active {
  background: #409eff;
  box-shadow: 0 0 0 5px rgba(64, 158, 255, 0.16);
}

.assistant-panel {
  position: absolute;
  right: 0;
  bottom: 54px;
  width: min(420px, calc(100vw - 32px));
  max-height: min(680px, calc(100vh - 104px));
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.97);
  box-shadow: 0 20px 55px rgba(15, 23, 42, 0.24);
  backdrop-filter: blur(14px);
}

.assistant-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 14px;
  border-bottom: 1px solid #edf2f7;
}

.assistant-kicker {
  display: inline-flex;
  margin-bottom: 6px;
  color: #409eff;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.assistant-header h2 {
  margin: 0;
  color: #1f2d3d;
  font-size: 18px;
}

.assistant-header p {
  margin: 5px 0 0;
  color: #64748b;
  font-size: 12px;
  line-height: 1.45;
}

.icon-button,
.tool-button,
.quick-row button,
.send-button {
  border: 0;
  cursor: pointer;
  transition: background-color 0.18s ease, border-color 0.18s ease, color 0.18s ease, opacity 0.18s ease;
}

.icon-button {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f1f5f9;
  color: #475569;
}

.assistant-toolbar {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 12px 14px 0;
}

.tool-button {
  min-width: 0;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #f8fafc;
  color: #334155;
  font-size: 12px;
}

.tool-button.primary {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.tool-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.quick-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 12px 14px;
}

.quick-row button {
  flex: 0 0 auto;
  border: 1px solid #e2e8f0;
  border-radius: 999px;
  padding: 7px 10px;
  background: #fff;
  color: #475569;
  font-size: 12px;
}

.quick-row button:hover {
  border-color: #409eff;
  color: #1677d2;
}

.assistant-history {
  min-height: 160px;
  max-height: 290px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 14px 12px;
}

.assistant-message {
  max-width: 88%;
  border-radius: 12px;
  padding: 10px 12px;
  background: #f8fafc;
  color: #1e293b;
  line-height: 1.55;
  border: 1px solid #eef2f7;
}

.assistant-message.user {
  align-self: flex-end;
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}

.assistant-message.system {
  align-self: center;
  max-width: 100%;
  background: #fff7ed;
  border-color: #fed7aa;
  color: #9a3412;
}

.assistant-message strong {
  display: block;
  margin-bottom: 3px;
  font-size: 11px;
  opacity: 0.72;
}

.assistant-message p {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
}

.transcript {
  margin: 0 14px 10px;
  border-radius: 10px;
  padding: 8px 10px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
}

.assistant-input-row {
  display: grid;
  grid-template-columns: 1fr 44px;
  gap: 8px;
  padding: 12px 14px 14px;
  border-top: 1px solid #edf2f7;
}

.assistant-input-row textarea {
  resize: none;
  min-height: 44px;
  max-height: 110px;
  border: 1px solid #dbe3ef;
  border-radius: 12px;
  padding: 10px 12px;
  outline: none;
  color: #1e293b;
  font: inherit;
  font-size: 13px;
}

.assistant-input-row textarea:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.12);
}

.send-button {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #409eff;
  color: #fff;
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .digital-human-layer {
    right: 12px;
    bottom: 12px;
  }

  .assistant-panel {
    right: -2px;
    bottom: 50px;
    width: calc(100vw - 24px);
    max-height: calc(100vh - 86px);
  }

  .assistant-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
