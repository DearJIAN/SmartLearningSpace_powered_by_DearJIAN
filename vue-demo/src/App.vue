<template>
  <div class="app-wrapper">
    <el-container style="height: 100%;">
      <!-- Sidebar -->
      <el-aside width="240px" class="sidebar-container">
        <div class="logo-container">
          <span class="logo-icon">🎓</span>
          <span class="logo-text">智学空间</span>
        </div>

        <el-menu
          default-active="/"
          class="sidebar-menu"
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
            <el-menu-item index="/">
            <el-icon style="color: #22d3ee"><i-tabler-map-2 /></el-icon>
            <span>校园空间导航</span>
          </el-menu-item>

          <el-menu-item index="/dashboard">
            <el-icon style="color: #a78bfa"><i-tabler-building-broadcast-tower /></el-icon>
            <span>教室状态监控</span>
          </el-menu-item>

          <el-menu-item index="/seat">
            <el-icon style="color: #34d399"><i-tabler-calendar-event /></el-icon>
            <span>座位预约</span>
          </el-menu-item>

          <el-menu-item index="/accounting/bills">
            <el-icon style="color: #fbbf24"><i-tabler-wallet /></el-icon>
            <span>个人记账</span>
          </el-menu-item>

          <el-menu-item index="/lost-found">
            <el-icon style="color: #f59e0b"><i-tabler-search /></el-icon>
            <span>失物招领</span>
          </el-menu-item>

          <el-menu-item index="/canteen">
            <el-icon style="color: #fb923c"><i-tabler-building-warehouse /></el-icon>
            <span>食堂智能服务</span>
          </el-menu-item>

          <el-menu-item index="logout" class="menu-logout" @click="handleSidebarLogout">
            <el-icon style="color: #ef4444"><i-tabler-power /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container class="main-container">
        <!-- Header -->
        <el-header class="app-header">
          <div class="header-left">
            <span class="page-title">智慧校园治理平台</span>
          </div>
          <div class="header-right">
            <el-tag effect="dark" type="success">系统运行中</el-tag>
          </div>
        </el-header>

      <!-- Main Content -->
      <el-main class="app-main">
        <router-view />
      </el-main>
      </el-container>
    </el-container>
    <DigitalHumanAssistant />
    <GuideTour />

    <!-- ============================================ -->
    <!-- YOLO AI Vision Button (global fixed position) -->
    <!-- ============================================ -->
    <div v-if="showYoloButton" class="ai-vision-launcher" @click="openYoloSystem">
      <div class="launcher-ball">
        <i-tabler-eye class="ai-icon" />
        <span class="label">AI 视觉</span>
      </div>
      <div class="launcher-tooltip">点击进入 AI 视觉识别子系统</div>
    </div>

    <!-- YOLO Dialog -->
    <div v-if="yoloVisible" class="custom-dialog-overlay" v-show="!yoloMinimized">
      <div class="custom-dialog" ref="yoloDialogRef">
        <div class="custom-dialog-header" @mousedown="startYoloDrag">
          <span>AI 视觉识别子系统 (实时监控)</span>
          <div class="custom-dialog-actions">
            <button class="dialog-btn minimize-btn" @click="handleYoloMinimize" title="最小化">
              <el-icon><Minus /></el-icon>
            </button>
            <button class="dialog-btn close-btn" @click="handleYoloDialogClose" title="关闭">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </div>
        <div class="custom-dialog-body">
          <iframe
            src="http://localhost:5000"
            frameborder="0"
            class="custom-iframe"
            allow="autoplay; camera"
          ></iframe>
        </div>
      </div>
    </div>

    <!-- YOLO Minimized float -->
    <div v-if="yoloVisible && yoloMinimized" class="yolo-floating-window">
      <div class="yolo-floating-header">
        <span>YOLO运行中</span>
        <div class="yolo-floating-actions">
          <button class="dialog-btn restore-btn" @click="handleYoloMinimize" title="恢复">
            <el-icon><FullScreen /></el-icon>
          </button>
          <button class="dialog-btn close-btn" @click="handleYoloDialogClose" title="关闭">
            <el-icon><Close /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <BrandingFooter v-if="!isLoginPage" class="app-branding" variant="light" />
  </div>
</template>

<script setup>
// Icons are now auto-imported via unplugin-icons
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/accounting'
import DigitalHumanAssistant from '@/components/DigitalHumanAssistant.vue'
import BrandingFooter from '@/components/BrandingFooter.vue'
import GuideTour from '@/components/GuideTour.vue'
import { ref, watch, onMounted, computed } from 'vue'
import { Minus, Close, FullScreen } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const appLoadTime = performance.now()
console.log('=== App.vue 组件加载 ===', '时间戳:', appLoadTime.toFixed(2))

const isLoginPage = computed(() => route.path === '/login')

onMounted(() => {
  const mountTime = performance.now()
  console.log('=== App.vue 组件已挂载 ===', '从加载到挂载耗时:', (mountTime - appLoadTime).toFixed(2), 'ms')
})

watch(route, (newRoute) => {
  const changeTime = performance.now()
  console.log('=== 路由变化 ===', '时间戳:', changeTime.toFixed(2))
  console.log('当前路由路径:', newRoute.path)
  console.log('当前路由名称:', newRoute.name)
}, { immediate: true })

const handleSidebarLogout = async () => {
  try {
    await logout()
  } catch (e) {}
  try { localStorage.removeItem('currentUser') } catch(e) {}
  ElMessage.success('已退出登录')
  router.push('/login')
}

// ============================================
// YOLO AI Vision System (global, outside router-view)
// ============================================
const yoloVisible = ref(false)
const yoloMinimized = ref(false)

const openYoloSystem = () => {
  yoloVisible.value = true
}

const handleYoloDialogClose = () => {
  yoloVisible.value = false
  yoloMinimized.value = false
}

const handleYoloMinimize = () => {
  yoloMinimized.value = !yoloMinimized.value
  if (yoloMinimized.value) {
    ElMessage.success('YOLO子系统已最小化，将在后台继续运行')
  }
}

// YOLO dialog drag
const yoloDialogRef = ref(null)
const yoloDragging = ref(false)
const yoloDragStart = ref({ x: 0, y: 0 })
const yoloDialogStart = ref({ x: 0, y: 0 })

const startYoloDrag = (e) => {
  if (e.target.closest('.custom-dialog-actions')) return
  yoloDragging.value = true
  yoloDragStart.value = { x: e.clientX, y: e.clientY }
  const el = yoloDialogRef.value
  if (el) {
    const rect = el.getBoundingClientRect()
    yoloDialogStart.value = { x: rect.left, y: rect.top }
  }
  document.addEventListener('mousemove', onYoloDrag)
  document.addEventListener('mouseup', stopYoloDrag)
}

const onYoloDrag = (e) => {
  if (!yoloDragging.value || !yoloDialogRef.value) return
  const dx = e.clientX - yoloDragStart.value.x
  const dy = e.clientY - yoloDragStart.value.y
  const el = yoloDialogRef.value
  el.style.left = `${yoloDialogStart.value.x + dx}px`
  el.style.top = `${yoloDialogStart.value.y + dy}px`
  el.style.margin = '0'
  el.style.transform = 'none'
}

const stopYoloDrag = () => {
  yoloDragging.value = false
  document.removeEventListener('mousemove', onYoloDrag)
  document.removeEventListener('mouseup', stopYoloDrag)
}

const yoloOnUnmounted = () => {
  document.removeEventListener('mousemove', onYoloDrag)
  document.removeEventListener('mouseup', stopYoloDrag)
}

// Check route to show YOLO button
const showYoloButton = ref(false)
watch(route, (r) => {
  showYoloButton.value = r.path === '/dashboard'
}, { immediate: true })
</script>

<style>
/* Global Reset */
body {
  margin: 0;
  padding: 0;
  height: 100vh;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  height: 100%;
}

.app-wrapper {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
}

/* Sidebar Styling */
.sidebar-container {
  background-color: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 1001;
  height: 100%;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #2b3a42;
  overflow: hidden;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
  color: #fff;
}

.sidebar-menu {
  border-right: none;
  flex: none;
}

.menu-logout {
  padding: 0 20px !important;
}
.menu-logout .el-icon {
  margin-right: 8px;
}

/* Ensure logout menu item matches height and vertical alignment of other items */
.menu-logout {
  height: 56px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

/* Main Container Styling */
.main-container {
  flex-direction: column;
  background-color: #f0f2f5;
  height: 100vh;
}

.app-header {
  background-color: #fff;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 1000;
}

.header-left .page-title {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.app-main {
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 60px);
}

/* Transitions - 快速淡入淡出，不带mode="out-in" */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ============================================ */
/* AI Vision Button - Fixed Position (Global) */
/* ============================================ */
.ai-vision-launcher {
  position: fixed;
  right: 40px;
  bottom: 130px;
  z-index: 99999;
  cursor: pointer;
  pointer-events: auto;
}

.ai-vision-launcher:hover .launcher-tooltip {
  opacity: 1;
  transform: translateX(0);
}

.launcher-tooltip {
  position: absolute;
  right: 70px;
  top: 50%;
  transform: translateY(-50%) translateX(10px);
  background: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 12px;
  white-space: nowrap;
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
}

.launcher-ball {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #00c6ff 0%, #0072ff 100%);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 15px rgba(0, 114, 255, 0.3);
  border: 2px solid rgba(0, 198, 255, 0.4);
  transition: all 0.3s;
  animation: ai-pulse-blue 2s infinite;
}

.launcher-ball:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(0, 114, 255, 0.5);
  border-color: #00c6ff;
}

.launcher-ball .label {
  font-size: 10px;
  margin-top: 2px;
  font-weight: bold;
}

.launcher-ball .ai-icon {
  font-size: 24px;
  margin-bottom: 2px;
}

@keyframes ai-pulse-blue {
  0% { box-shadow: 0 0 0 0 rgba(0, 198, 255, 0.4); }
  70% { box-shadow: 0 0 0 15px rgba(0, 198, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(0, 198, 255, 0); }
}

/* ============================================ */
/* YOLO Dialog Styles (Global) */
/* ============================================ */
.custom-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 99998;
}

.custom-dialog {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  min-width: 800px;
  min-height: 600px;
  max-width: 95vw;
  max-height: 95vh;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: move;
  height: 80vh;
}

.custom-dialog-header {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
}

.custom-dialog-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.custom-dialog-actions {
  display: flex;
  gap: 10px;
}

.dialog-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  padding: 0;
  border: none;
  background-color: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.minimize-btn { color: #409eff; }
.minimize-btn:hover { background-color: rgba(64, 158, 255, 0.1); color: #66b1ff; }
.close-btn { color: #f56c6c; }
.close-btn:hover { background-color: rgba(245, 108, 108, 0.1); color: #f78989; }
.restore-btn { color: #67c23a; }
.restore-btn:hover { background-color: rgba(103, 194, 58, 0.1); color: #85ce61; }

.custom-dialog-body {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: 500px;
}

.custom-iframe {
  width: 100%;
  height: 100%;
  border: none;
  min-height: 500px;
}

.custom-dialog::after {
  content: '';
  position: absolute;
  bottom: 0;
  right: 0;
  width: 20px;
  height: 20px;
  cursor: se-resize;
  background: linear-gradient(135deg, transparent 50%, rgba(0, 0, 0, 0.5) 50%);
  pointer-events: auto;
  z-index: 10;
}

/* Minimized float */
.yolo-floating-window {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 99998;
}

.yolo-floating-header {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.yolo-floating-header span {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.yolo-floating-actions {
  display: flex;
  gap: 5px;
}

/* ============================================ */
/* Live2D Widget Drag Support */
/* ============================================ */
#waifu {
  pointer-events: none !important;
}

#waifu canvas {
  pointer-events: auto !important;
  cursor: grab;
}

#waifu canvas:active {
  cursor: grabbing;
}

#live2d {
  pointer-events: auto !important;
  cursor: grab;
}

/* ============================================ */
/* Global Branding Footer */
/* ============================================ */
.app-branding {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 10000;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.8));
  backdrop-filter: blur(4px);
  padding: 8px 0 6px;
  border-top: 1px solid rgba(99, 102, 241, 0.1);
  pointer-events: auto;
}

.app-branding a {
  pointer-events: auto;
}
</style>
