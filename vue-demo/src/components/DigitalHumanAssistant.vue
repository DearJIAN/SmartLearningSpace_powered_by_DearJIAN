<template>
  <div class="digital-human-layer" :style="layerStyle">
    <button
      class="assistant-tab"
      type="button"
      @mousedown="tabDragStart"
      @touchstart.prevent="tabDragStart"
      @click="handleTabClick"
    >
      <span class="assistant-tab__dot" :class="{ active: isBusy || isSpeaking || isListening }"></span>
      <span>数字人助手</span>
      <small>{{ status }}</small>
    </button>

    <section
      v-show="panelOpen"
      ref="panelRef"
      class="assistant-panel"
      :class="{ dragging: isDragging, 'is-dragged': isDragged }"
      :style="isDragged ? {
        position: 'fixed',
        right: 'auto',
        bottom: 'auto',
        left: panelPosition.x + 'px',
        top: panelPosition.y + 'px'
      } : {}"
    >
      <header class="assistant-header" @mousedown="startDrag" @touchstart="startDrag">
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

      <!-- Expression Panel -->
      <div class="expression-panel">
        <div class="expression-title">
          <span>表情</span>
          <small>水印/月卡可与其他表情共存</small>
        </div>
        <div class="expression-grid">
          <button
            v-for="exp in expressions"
            :key="exp.name"
            class="expression-btn"
            :class="{ 
              'active': isExpressionActive(exp.name),
              'coexist': exp.coexist 
            }"
            :title="exp.coexist ? '可与其它表情共存' : ''"
            @click="toggleExpression(exp.name)"
          >
            {{ exp.label }}
          </button>
        </div>
      </div>

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
const panelRef = ref(null)
const isBusy = ref(false)
const isListening = ref(false)
const isSpeaking = ref(false)
const muted = ref(localStorage.getItem('digital_human_muted') === '1')
const sessionId = ref(localStorage.getItem('digital_human_session_id') || createSessionId())

// Drag state
const isDragging = ref(false)
const isDragged = ref(false)
const panelPosition = ref({ x: 0, y: 0 })
const dragOffset = ref({ x: 0, y: 0 })

// Expression state
const activeExpressions = ref(new Set(['水印'])) // Default: watermark on
const coexistExpressions = new Set(['水印', '月卡'])

const expressions = [
  { name: '01黑脸', label: '黑脸', coexist: false },
  { name: '02 脸红爱心', label: '脸红', coexist: false },
  { name: '03 生气', label: '生气', coexist: false },
  { name: '04 晕', label: '晕', coexist: false },
  { name: '05 ＞＜', label: '＞＜', coexist: false },
  { name: '06 0.0', label: '0.0', coexist: false },
  { name: '07 星星眼', label: '星星眼', coexist: false },
  { name: '08 流泪', label: '流泪', coexist: false },
  { name: '10 捧心', label: '捧心', coexist: false },
  { name: '11 要饭', label: '要饭', coexist: false },
  { name: '水印', label: '水印', coexist: true },
  { name: '月卡', label: '月卡', coexist: true },
]

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

// Expression control
function isExpressionActive(name) {
  return activeExpressions.value.has(name)
}

function toggleExpression(name) {
  console.log('[Live2D] toggleExpression called:', name)
  const isCoexist = coexistExpressions.has(name)
  const isActive = activeExpressions.value.has(name)
  
  if (isCoexist) {
    if (isActive) {
      activeExpressions.value.delete(name)
    } else {
      activeExpressions.value.add(name)
    }
  } else {
    if (isActive) {
      activeExpressions.value.delete(name)
    } else {
      for (const exp of activeExpressions.value) {
        if (!coexistExpressions.has(exp)) {
          activeExpressions.value.delete(exp)
        }
      }
      activeExpressions.value.add(name)
    }
  }
  
  console.log('[Live2D] activeExpressions after toggle:', Array.from(activeExpressions.value))
  applyExpressions()
}

// ============================================================
// Live2D 核心辅助函数（参考成功项目实现）
// ============================================================

function getCurrentCubism5Model() {
  const manager = window.__live2dWidgetModelManager
  const appDelegate = manager?.cubism5model
  const subdelegate = appDelegate?.subdelegates?.at?.(0)
  const live2dManager = subdelegate?.getLive2DManager?.()
  const model = live2dManager?._models?.at?.(0) || null
  console.log('[Live2D] getCurrentCubism5Model:', model ? 'found' : 'not found')
  return model
}

function getModelTargets(target) {
  const seen = new Set()
  const out = []
  const push = (x) => {
    if (!x || seen.has(x)) return
    seen.add(x)
    out.push(x)
  }
  push(target)
  push(target?._model)
  push(target?._model?._model)
  push(target?.model)
  push(target?.model?._model)
  push(target?.model?._model?._model)
  return out
}

function idToString(idObj) {
  try {
    if (!idObj) return ''
    if (typeof idObj === 'string') return idObj
    if (typeof idObj.getString === 'function') {
      const s = idObj.getString()
      if (typeof s === 'string') return s
      if (s && typeof s.s === 'string') return s.s
    }
    if (typeof idObj.s === 'string') return idObj.s
  } catch (_) {}
  return ''
}

function getCubismCoreModel(target) {
  const targets = getModelTargets(target)
  for (const current of targets) {
    if (current?._parameterIds?.getSize && current?._parameterIds?.at) {
      return current
    }
  }
  return null
}

function resolveParamIdObject(core, paramId) {
  try {
    const ids = core?._parameterIds
    if (!ids?.getSize || !ids?.at) return null
    for (let i = 0; i < ids.getSize(); i += 1) {
      const idObj = ids.at(i)
      if (idToString(idObj) === paramId) return idObj
    }
  } catch (_) {}
  return null
}

function resolvePartIdObject(core, partId) {
  try {
    const ids = core?._partIds
    if (!ids?.getSize || !ids?.at) return null
    for (let i = 0; i < ids.getSize(); i += 1) {
      const idObj = ids.at(i)
      if (idToString(idObj) === partId) return idObj
    }
  } catch (_) {}
  return null
}

// 兼容旧代码的 getModel 函数
function getModel() {
  return getCurrentCubism5Model()
}

// 表情 overlay 规则
const overlayRules = {
  '月卡': { parameters: ['key9'], parts: [] },
  '水印': { parameters: ['key12', 'Param45', 'Param48', 'Param49', 'Param50'], parts: [] }
}

// 应用 overlay 状态到 core model
function applyOverlayStateToCore(core) {
  if (!core) return

  const activeOv = activeExpressions.value
  const allParameterIds = new Set()
  const allPartIds = new Set()
  
  for (const rule of Object.values(overlayRules)) {
    const parameters = rule?.parameters || []
    const parts = rule?.parts || []
    for (const parameterId of parameters) allParameterIds.add(parameterId)
    for (const partId of parts) allPartIds.add(partId)
  }

  for (const parameterId of allParameterIds) {
    const shouldEnable = [...activeOv].some((overlayName) => {
      return overlayRules[overlayName]?.parameters?.includes(parameterId)
    })

    const idObj = resolveParamIdObject(core, parameterId)
    if (!idObj) continue
    if (typeof core.getParameterIndex !== 'function' || typeof core.setParameterValueByIndex !== 'function') continue

    const idx = core.getParameterIndex(idObj)
    if (idx >= 0) {
      core.setParameterValueByIndex(idx, shouldEnable ? 1 : 0, 1)
    }
  }

  for (const partId of allPartIds) {
    const shouldEnable = [...activeOv].some((overlayName) => {
      return overlayRules[overlayName]?.parts?.includes(partId)
    })

    const idObj = resolvePartIdObject(core, partId)
    if (!idObj || typeof core.getPartIndex !== 'function' || typeof core.setPartOpacityByIndex !== 'function') continue

    const idx = core.getPartIndex(idObj)
    if (idx >= 0) {
      core.setPartOpacityByIndex(idx, shouldEnable ? 1 : 0)
    }
  }
}

// 应用 overlay 状态到 model
function applyOverlayState(model) {
  if (!model) return
  const core = getCubismCoreModel(model)
  applyOverlayStateToCore(core)
}

// Monkey-patch core.update 实现 overlay 持续生效
function setupExpressionWithOverlay() {
  const model = getCurrentCubism5Model()
  if (!model || model.__expressionOverlayPatched) return

  const core = getCubismCoreModel(model)

  if (core && typeof core.update === 'function' && !core.__overlayUpdatePatched) {
    const oldCoreUpdate = core.update.bind(core)
    core.update = function(...args) {
      try {
        applyOverlayStateToCore(core)
      } catch (error) {}
      const result = oldCoreUpdate(...args)
      return result
    }
    core.__overlayUpdatePatched = true
    console.log('[Live2D] Monkey-patch core.update success')
  }

  model.__expressionOverlayPatched = true
}

let _cubismModelRef = null
let _cubismPollingActive = false

// ============================================================
// Expression control
// ============================================================
function applyExpressions() {
  const expArray = Array.from(activeExpressions.value)
  console.log('[Live2D] applyExpressions called with:', expArray)
  
  const model = getCurrentCubism5Model()

  if (!model) {
    console.warn('[Live2D] CubismModel not available yet')
    return
  }

  try {
    const mgr = window.__live2dWidgetModelManager
    const isCubism5 = mgr?.currentModelVersion === 3 || !!mgr?.cubism5model
    console.log('[Live2D] Model version:', isCubism5 ? 'Cubism5' : 'Cubism2')

    // 获取当前的基础表情（非叠加效果）
    const baseExpression = expArray.find(exp => !coexistExpressions.has(exp))
    
    if (isCubism5) {
      // 步骤1：重置表情相关参数（防止表情残留导致眼睛闪烁）
      const core = getCubismCoreModel(model)
      if (core) {
        const expressionParams = ['ParamEyeSmile', 'ParamEyeOpen', 'ParamTear']
        expressionParams.forEach(paramName => {
          const paramId = resolveParamIdObject(core, paramName)
          if (paramId) {
            const idx = core.getParameterIndex(paramId)
            if (idx >= 0) {
              core.setParameterValueByIndex(idx, 0, 1)
            }
          }
        })
      }
      
      // 步骤2：设置基础表情
      if (baseExpression && typeof model.setExpression === 'function') {
        console.log('[Live2D] Setting base expression:', baseExpression)
        model.setExpression(baseExpression)
      }
      
      // 步骤3：应用叠加效果
      applyOverlayState(model)
    } else {
      // Cubism 2: 使用 expression 方法
      if (typeof model.expression === 'function') {
        model.expression(-1)
        expArray.forEach(expName => {
          const idx = model.expressions?.findIndex(e => e.name === expName)
          if (idx >= 0) model.expression(idx)
        })
      }
    }

    console.log('[Live2D] Expressions applied successfully:', expArray)
    window.__live2dExpressionUpdateTime = Date.now()
  } catch (e) {
    console.error('[Live2D] Expression apply failed:', e)
  }

  notifyLive2d('onExpressionChange', { expressions: expArray })
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
      
      // 监听模型就绪事件
      const onModelReady = () => {
        console.log('[Live2D] Model ready event received')
        
        // 等待模型完全加载
        let attempts = 0
        const maxAttempts = 50
        const checkModel = () => {
          attempts++
          const model = getCurrentCubism5Model()
          if (model) {
            console.log('[Live2D] Model found, setting up overlay...')
            setupExpressionWithOverlay()
            applyExpressions()
          } else if (attempts < maxAttempts) {
            setTimeout(checkModel, 200)
          } else {
            console.warn('[Live2D] Model not found after', maxAttempts, 'attempts')
          }
        }
        setTimeout(checkModel, 500)
      }
      window.addEventListener('live2d:model-ready', onModelReady)

      window.__live2dCleanup = () => {
        window.removeEventListener('live2d:model-ready', onModelReady)
      }
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
  document.removeEventListener('mousemove', tabOnDrag)
  document.removeEventListener('mouseup', tabStopDrag)
  document.removeEventListener('touchmove', tabOnDrag)
  document.removeEventListener('touchend', tabStopDrag)
})

function handleExternalOpen(event) {
  panelOpen.value = true
  const prompt = String(event?.detail?.message || '').trim()
  if (prompt) {
    sendMessage(prompt)
  }
}

// Panel drag functionality
function startDrag(event) {
  if (!panelRef.value) return
  
  // Switch to fixed positioning on first drag
  if (!isDragged.value) {
    const rect = panelRef.value.getBoundingClientRect()
    panelPosition.value = { x: rect.left, y: rect.top }
    isDragged.value = true
    // Wait a tick for fixed position to apply
    requestAnimationFrame(() => {
      dragStartCore(event)
    })
    return
  }
  
  dragStartCore(event)
}

function dragStartCore(event) {
  if (!panelRef.value) return
  
  isDragging.value = true
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY
  
  dragOffset.value = {
    x: clientX - panelPosition.value.x,
    y: clientY - panelPosition.value.y
  }
  
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
  document.addEventListener('touchmove', onDrag)
  document.addEventListener('touchend', stopDrag)
}

function onDrag(event) {
  if (!isDragging.value) return
  
  event.preventDefault()
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY
  
  const newX = clientX - dragOffset.value.x
  const newY = clientY - dragOffset.value.y
  
  const panel = panelRef.value
  const panelWidth = panel?.offsetWidth || 420
  const panelHeight = panel?.offsetHeight || 600
  
  const maxX = window.innerWidth - panelWidth
  const maxY = window.innerHeight - panelHeight
  
  panelPosition.value = {
    x: Math.max(0, Math.min(newX, maxX)),
    y: Math.max(0, Math.min(newY, maxY))
  }
}

function stopDrag() {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('touchmove', onDrag)
  document.removeEventListener('touchend', stopDrag)
}

// ============================================
// Assistant tab drag
// ============================================
const tabPosition = ref({ x: 0, y: 0 })
const isTabDragged = ref(false)
const tabDragging = ref(false)
const tabDragOffset = ref({ x: 0, y: 0 })
let tabMoved = false

const layerStyle = computed(() => {
  if (!isTabDragged.value) return {}
  return {
    position: 'fixed',
    left: tabPosition.value.x + 'px',
    top: tabPosition.value.y + 'px',
    right: 'auto',
    bottom: 'auto'
  }
})

function tabDragStart(event) {
  tabMoved = false

  // Capture current position on first drag
  if (!isTabDragged.value) {
    const layer = document.querySelector('.digital-human-layer')
    if (layer) {
      const rect = layer.getBoundingClientRect()
      tabPosition.value = { x: rect.left, y: rect.top }
      isTabDragged.value = true
    }
  }

  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY

  tabDragOffset.value = {
    x: clientX - tabPosition.value.x,
    y: clientY - tabPosition.value.y
  }

  tabDragging.value = true
  document.addEventListener('mousemove', tabOnDrag)
  document.addEventListener('mouseup', tabStopDrag)
  document.addEventListener('touchmove', tabOnDrag, { passive: false })
  document.addEventListener('touchend', tabStopDrag)
}

function tabOnDrag(event) {
  if (!tabDragging.value) return
  event.preventDefault()

  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY

  const dx = clientX - tabDragOffset.value.x - tabPosition.value.x
  const dy = clientY - tabDragOffset.value.y - tabPosition.value.y
  if (Math.abs(dx) > 3 || Math.abs(dy) > 3) tabMoved = true

  tabPosition.value = {
    x: Math.max(0, Math.min(clientX - tabDragOffset.value.x, window.innerWidth - 150)),
    y: Math.max(0, Math.min(clientY - tabDragOffset.value.y, window.innerHeight - 50))
  }
}

function tabStopDrag() {
  tabDragging.value = false
  document.removeEventListener('mousemove', tabOnDrag)
  document.removeEventListener('mouseup', tabStopDrag)
  document.removeEventListener('touchmove', tabOnDrag)
  document.removeEventListener('touchend', tabStopDrag)
}

function handleTabClick() {
  if (tabMoved) return
  panelOpen.value = !panelOpen.value
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
  cursor: grab;
  font-weight: 700;
  user-select: none;
}

.assistant-tab:active {
  cursor: grabbing;
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
  max-height: min(720px, calc(100vh - 104px));
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
  cursor: move;
  user-select: none;
}

.assistant-header:active {
  cursor: grabbing;
}

.assistant-panel.dragging {
  opacity: 0.9;
  box-shadow: 0 25px 60px rgba(15, 23, 42, 0.3);
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

/* Expression Panel Styles */
.expression-panel {
  padding: 12px 14px;
  border-bottom: 1px solid #edf2f7;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.03), rgba(16, 185, 129, 0.03));
}

.expression-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
  color: #475569;
}

.expression-title small {
  font-size: 10px;
  font-weight: 400;
  color: #94a3b8;
  background: rgba(99, 102, 241, 0.08);
  padding: 2px 6px;
  border-radius: 4px;
}

.expression-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 6px;
}

.expression-btn {
  padding: 6px 4px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  color: #64748b;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.expression-btn:hover {
  border-color: #409eff;
  color: #409eff;
  transform: translateY(-1px);
}

.expression-btn.active {
  background: linear-gradient(135deg, #409eff, #60a5fa);
  border-color: #409eff;
  color: #fff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.expression-btn.coexist {
  border-style: dashed;
  border-color: #a78bfa;
  color: #7c3aed;
}

.expression-btn.coexist.active {
  background: linear-gradient(135deg, #a78bfa, #c4b5fd);
  border-color: #a78bfa;
  border-style: solid;
  color: #fff;
  box-shadow: 0 2px 8px rgba(167, 139, 250, 0.3);
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
  min-height: 140px;
  max-height: 240px;
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

  .expression-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .assistant-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
