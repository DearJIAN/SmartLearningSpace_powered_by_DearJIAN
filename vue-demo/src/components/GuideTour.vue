<template>
  <div class="guide-tour-layer">
    <!-- Guide Panel -->
    <transition name="guide-fade">
      <div
        v-if="isOpen"
        ref="panelRef"
        class="guide-panel"
        :class="{ dragging: isDragging }"
        :style="{
          left: position.x + 'px',
          top: position.y + 'px',
          opacity: opacity
        }"
      >
        <!-- Header - Draggable -->
        <header class="guide-header" @mousedown="startDrag" @touchstart="startDrag">
          <div class="guide-title">
            <i-tabler-compass class="guide-icon" />
            <span>功能引导</span>
          </div>
          <div class="guide-actions">
            <el-tooltip content="透明度" placement="bottom">
              <button class="guide-btn" @click.stop="showOpacitySlider = !showOpacitySlider">
                <i-tabler-adjustments />
              </button>
            </el-tooltip>
            <el-tooltip content="关闭" placement="bottom">
              <button class="guide-btn close" @click="closeGuide">
                <i-tabler-x />
              </button>
            </el-tooltip>
          </div>
        </header>

        <!-- Opacity Slider -->
        <div v-if="showOpacitySlider" class="opacity-control" @click.stop>
          <div class="opacity-label">
            <span>透明度</span>
            <span>{{ Math.round(opacity * 100) }}%</span>
          </div>
          <input
            v-model.number="opacity"
            type="range"
            min="0.3"
            max="1"
            step="0.05"
            class="opacity-slider"
          />
        </div>

        <!-- Content -->
        <div class="guide-content">
          <!-- Progress -->
          <div class="guide-progress">
            <div
              v-for="(step, index) in steps"
              :key="index"
              class="progress-dot"
              :class="{ active: currentStep === index, completed: currentStep > index }"
              @click="goToStep(index)"
            />
          </div>

          <!-- Step Content -->
          <div class="step-content">
            <div class="step-number">步骤 {{ currentStep + 1 }} / {{ steps.length }}</div>
            <h3 class="step-title">{{ steps[currentStep].title }}</h3>
            <p class="step-desc">{{ steps[currentStep].description }}</p>
            
            <!-- Feature Tags -->
            <div v-if="steps[currentStep].features" class="feature-tags">
              <span
                v-for="feature in steps[currentStep].features"
                :key="feature"
                class="feature-tag"
              >
                {{ feature }}
              </span>
            </div>
          </div>

          <!-- Navigation -->
          <div class="guide-nav">
            <button
              class="nav-btn"
              :disabled="currentStep === 0"
              @click="prevStep"
            >
              <i-tabler-chevron-left />
              上一步
            </button>
            <button
              class="nav-btn primary"
              @click="nextStep"
            >
              {{ currentStep === steps.length - 1 ? '完成' : '下一步' }}
              <i-tabler-chevron-right v-if="currentStep < steps.length - 1" />
            </button>
          </div>
        </div>
      </div>
    </transition>

    <!-- Floating Toggle Button -->
    <div
      v-if="!isOpen"
      ref="toggleRef"
      class="guide-toggle-wrapper"
      :class="{ dragging: isToggleDragging }"
      :style="{
        left: togglePosition.x + 'px',
        top: togglePosition.y + 'px'
      }"
      @mouseenter="isToggleHovered = true"
      @mouseleave="isToggleHovered = false"
    >
      <div
        class="guide-toggle"
        :class="{ expanded: isToggleHovered }"
        @mousedown="startToggleDrag"
        @touchstart="startToggleDrag"
        @click="openGuide"
      >
        <i-tabler-compass class="toggle-icon" />
        <span v-show="isToggleHovered" class="toggle-text">功能引导</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

// Panel state
const isOpen = ref(true)
const isDragging = ref(false)
const position = ref({ x: 100, y: 100 })
const dragOffset = ref({ x: 0, y: 0 })
const panelRef = ref(null)

// Toggle button state
const isToggleHovered = ref(false)
const isToggleDragging = ref(false)
const togglePosition = ref({ x: 20, y: 200 })
const toggleDragOffset = ref({ x: 0, y: 0 })
const toggleRef = ref(null)

// Opacity control
const opacity = ref(1)
const showOpacitySlider = ref(false)

// Guide steps
const currentStep = ref(0)
const steps = [
  {
    title: '欢迎使用智学空间',
    description: '智学空间是一个校园智慧空间治理系统，集成了AI助手、记账管理、失物招领、座位预约等多种功能。',
    features: ['AI数字人助手', '智能语音交互', 'Live2D形象']
  },
  {
    title: '数字人助手 - 火花',
    description: '点击右下角的"数字人助手"按钮，可以打开火花助手面板。支持语音输入、文字聊天，还有丰富的表情可以切换。',
    features: ['语音识别', '文字聊天', '表情切换', '语音播报']
  },
  {
    title: '个人记账',
    description: '在记账页面可以记录日常消费，AI会自动分析消费趋势，提供预算建议和财务健康报告。',
    features: ['智能记账', '消费分析', '预算管理', 'AI财务建议']
  },
  {
    title: '失物招领',
    description: '发布或查找失物信息，支持图片上传和关键词搜索，帮助校园内物品快速找回。',
    features: ['图片上传', '关键词搜索', '认领管理', '通知提醒']
  },
  {
    title: '座位预约',
    description: '查看教室座位实时状态，预约自习座位，提高学习效率。',
    features: ['实时座位', '在线预约', '签到签退', '使用记录']
  },
  {
    title: '教室监控',
    description: '通过AI视觉技术分析教室专注度，帮助了解学习环境。',
    features: ['AI视觉', '专注度分析', '数据统计', '实时监控']
  }
]

// Panel drag functionality
function startDrag(event) {
  if (!panelRef.value) return
  
  isDragging.value = true
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY
  
  dragOffset.value = {
    x: clientX - position.value.x,
    y: clientY - position.value.y
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
  
  // Get panel dimensions
  const panel = panelRef.value
  const panelWidth = panel?.offsetWidth || 380
  const panelHeight = panel?.offsetHeight || 500
  
  // Constrain to viewport
  const maxX = window.innerWidth - panelWidth
  const maxY = window.innerHeight - panelHeight
  
  position.value = {
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

// Toggle button drag functionality
function startToggleDrag(event) {
  if (!toggleRef.value) return
  
  isToggleDragging.value = true
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY
  
  toggleDragOffset.value = {
    x: clientX - togglePosition.value.x,
    y: clientY - togglePosition.value.y
  }
  
  document.addEventListener('mousemove', onToggleDrag)
  document.addEventListener('mouseup', stopToggleDrag)
  document.addEventListener('touchmove', onToggleDrag)
  document.addEventListener('touchend', stopToggleDrag)
}

function onToggleDrag(event) {
  if (!isToggleDragging.value) return
  
  event.preventDefault()
  const clientX = event.type.includes('touch') ? event.touches[0].clientX : event.clientX
  const clientY = event.type.includes('touch') ? event.touches[0].clientY : event.clientY
  
  const newX = clientX - toggleDragOffset.value.x
  const newY = clientY - toggleDragOffset.value.y
  
  // Get toggle dimensions
  const toggle = toggleRef.value
  const toggleWidth = toggle?.offsetWidth || 50
  const toggleHeight = toggle?.offsetHeight || 50
  
  // Constrain to viewport
  const maxX = window.innerWidth - toggleWidth
  const maxY = window.innerHeight - toggleHeight
  
  togglePosition.value = {
    x: Math.max(0, Math.min(newX, maxX)),
    y: Math.max(0, Math.min(newY, maxY))
  }
}

function stopToggleDrag() {
  isToggleDragging.value = false
  document.removeEventListener('mousemove', onToggleDrag)
  document.removeEventListener('mouseup', stopToggleDrag)
  document.removeEventListener('touchmove', onToggleDrag)
  document.removeEventListener('touchend', stopToggleDrag)
}

// Guide navigation
function nextStep() {
  if (currentStep.value < steps.length - 1) {
    currentStep.value++
  } else {
    closeGuide()
  }
}

function prevStep() {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

function goToStep(index) {
  currentStep.value = index
}

function closeGuide() {
  isOpen.value = false
  showOpacitySlider.value = false
  localStorage.setItem('guide_closed', 'true')
}

function openGuide() {
  if (isToggleDragging.value) return
  isOpen.value = true
  currentStep.value = 0
  localStorage.removeItem('guide_closed')
}

// Close opacity slider when clicking outside
function handleClickOutside(event) {
  if (showOpacitySlider.value && !event.target.closest('.opacity-control')) {
    showOpacitySlider.value = false
  }
}

onMounted(() => {
  // Check if guide was previously closed
  const wasClosed = localStorage.getItem('guide_closed')
  if (wasClosed) {
    isOpen.value = false
  }
  
  // Center the guide panel initially
  const panelWidth = 380
  const panelHeight = 450
  position.value = {
    x: Math.max(20, (window.innerWidth - panelWidth) / 2),
    y: Math.max(20, (window.innerHeight - panelHeight) / 2)
  }
  
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.guide-tour-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 9999;
}

/* Guide Panel */
.guide-panel {
  position: absolute;
  width: 380px;
  max-width: calc(100vw - 40px);
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 8px 24px rgba(0, 0, 0, 0.1);
  pointer-events: auto;
  overflow: hidden;
  transition: opacity 0.2s ease, box-shadow 0.2s ease;
}

.guide-panel.dragging {
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2), 0 12px 32px rgba(0, 0, 0, 0.15);
}

/* Header */
.guide-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: move;
  user-select: none;
}

.guide-header:active {
  cursor: grabbing;
}

.guide-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
}

.guide-icon {
  font-size: 22px;
}

.guide-actions {
  display: flex;
  gap: 8px;
}

.guide-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s ease;
}

.guide-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.guide-btn.close:hover {
  background: rgba(239, 68, 68, 0.8);
}

/* Opacity Control */
.opacity-control {
  padding: 12px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.opacity-label {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.opacity-slider {
  width: 100%;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #e2e8f0;
  border-radius: 3px;
  outline: none;
  cursor: pointer;
}

.opacity-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 18px;
  height: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
  transition: transform 0.2s ease;
}

.opacity-slider::-webkit-slider-thumb:hover {
  transform: scale(1.1);
}

.opacity-slider::-moz-range-thumb {
  width: 18px;
  height: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  cursor: pointer;
  border: none;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
}

/* Content */
.guide-content {
  padding: 20px;
}

/* Progress */
.guide-progress {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 20px;
}

.progress-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #e2e8f0;
  cursor: pointer;
  transition: all 0.3s ease;
}

.progress-dot:hover {
  background: #cbd5e1;
}

.progress-dot.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scale(1.2);
}

.progress-dot.completed {
  background: #10b981;
}

/* Step Content */
.step-content {
  text-align: center;
  margin-bottom: 24px;
  min-height: 180px;
}

.step-number {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
  font-weight: 500;
}

.step-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.step-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.7;
  margin: 0 0 20px 0;
}

/* Feature Tags */
.feature-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.feature-tag {
  padding: 6px 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  font-size: 12px;
  font-weight: 500;
  border-radius: 20px;
  border: 1px solid rgba(102, 126, 234, 0.2);
}

/* Navigation */
.guide-nav {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.nav-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: white;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.nav-btn:hover:not(:disabled) {
  border-color: #667eea;
  color: #667eea;
  background: rgba(102, 126, 234, 0.05);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.nav-btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* Floating Toggle Button */
.guide-toggle-wrapper {
  position: absolute;
  pointer-events: auto;
}

.guide-toggle-wrapper.dragging {
  cursor: grabbing;
}

.guide-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  overflow: hidden;
  width: 44px;
  height: 44px;
}

.guide-toggle:hover {
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.guide-toggle.expanded {
  width: auto;
  padding: 12px 16px;
  border-radius: 24px;
  gap: 8px;
}

.toggle-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.toggle-text {
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

/* Transitions */
.guide-fade-enter-active,
.guide-fade-leave-active {
  transition: all 0.3s ease;
}

.guide-fade-enter-from,
.guide-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* Responsive */
@media (max-width: 480px) {
  .guide-panel {
    width: calc(100vw - 32px);
    left: 16px !important;
    right: 16px;
  }
  
  .step-content {
    min-height: 160px;
  }
  
  .step-title {
    font-size: 18px;
  }
  
  .step-desc {
    font-size: 13px;
  }
}
</style>
