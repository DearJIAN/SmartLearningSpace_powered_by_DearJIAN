<template>
  <div class="dashboard-view">
    <div class="page-header">
      <div class="header-left">
        <h2>🏫 教室状态实时看板 (Classroom Monitor)</h2>
        <div class="room-selector">
          <span class="label">当前监控教室:</span>
          <el-select v-model="selectedRoomId" placeholder="选择教室" style="width: 100px" @change="handleRoomChange">
            <el-option
              v-for="item in roomOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
      </div>
      <div class="header-right-actions">
        <el-button type="warning" plain size="small" @click="handleReset">重置数据</el-button>
        <el-button size="small" circle :icon="Refresh" @click="refreshData" />
      </div>
    </div>

    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="stat-title">当前教室总人数</div>
          <div class="stat-value text-blue">{{ statsData.totalPeople }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="stat-title">今日平均专注度</div>
          <div class="stat-value text-green">{{ statsData.avgFocus }}%</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="stat-title">违规占座预警</div>
          <div class="stat-value text-red">{{ statsData.warningCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card">
          <div class="stat-title">开放教室数量</div>
          <div class="stat-value text-purple">{{ statsData.activeClassrooms }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>📈 24小时专注度趋势监控</span>
          <el-button size="small" circle :icon="Refresh" @click="refreshChart" />
        </div>
      </template>
      <FocusTrendChart ref="chartRef" />
    </el-card>

    <!-- 辅助入口：YOLO AI 视觉识别子系统 -->
    <div class="ai-vision-launcher" @click="openYoloSystem">
      <el-tooltip content="点击进入 AI 视觉识别子系统" placement="left">
        <div class="launcher-ball">
          <!-- 方案1: 眼睛/视觉图标 (推荐 - 线性风格) -->
          <i-tabler-eye class="ai-icon" />
          
          <!-- 方案2: AI大脑图标 (智能感) -->
          <!-- <i-tabler-brain class="ai-icon" /> -->
          
          <!-- 方案3: 摄像头识别图标 (监控感) -->
          <!-- <i-tabler-video class="ai-icon" /> -->
          
          <!-- 方案4: 神经元/网络图标 (科技感) -->
          <!-- <i-tabler-layers-linked class="ai-icon" /> -->
          
          <!-- 方案5: 智能分析图标 (魔法感) -->
          <!-- <i-tabler-wand class="ai-icon" /> -->
          
          <span class="label">AI 视觉</span>
        </div>
      </el-tooltip>
    </div>

    <!-- 局部模拟器：仅在看板页面显示 -->
    <FloatingSimulator />

    <!-- AI 视觉识别子系统弹窗 (窗口化集成) -->
    <div v-if="yoloVisible" class="custom-dialog-overlay" v-show="!yoloMinimized">
      <div class="custom-dialog" ref="yoloDialogRef">
        <div class="custom-dialog-header" @mousedown="startDrag">
          <span>🤖 AI 视觉识别子系统 (实时监控)</span>
          <div class="custom-dialog-actions">
            <button
              class="dialog-btn minimize-btn"
              @click="handleYoloMinimize"
              title="最小化"
            >
              <Minus class="dialog-icon" />
            </button>
            <button
              class="dialog-btn close-btn"
              @click="handleYoloDialogClose"
              title="关闭"
            >
              <Close class="dialog-icon" />
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
    
    <!-- YOLO子系统最小化悬浮窗 -->
    <div v-if="yoloVisible && yoloMinimized" class="floating-window">
      <div class="floating-header">
        <span>🤖 YOLO运行中</span>
        <div class="floating-actions">
          <button
            class="dialog-btn restore-btn"
            @click="handleYoloMinimize"
            title="恢复"
          >
            <FullScreen class="dialog-icon" />
          </button>
          <button
            class="dialog-btn close-btn"
            @click="handleYoloDialogClose"
            title="关闭"
          >
            <Close class="dialog-icon" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, VideoCamera, Minus, Close, FullScreen } from '@element-plus/icons-vue'
import axios from 'axios'
import FocusTrendChart from '../components/FocusTrendChart.vue'
import FloatingSimulator from '../components/FloatingSimulator.vue'

const chartRef = ref(null)
const selectedRoomId = ref(101)
const yoloVisible = ref(false)
const yoloMinimized = ref(false)

// 拖拽相关变量
const yoloDialogRef = ref(null)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dialogStartX = ref(0)
const dialogStartY = ref(0)
const roomOptions = [
  { value: 101, label: '101' },
  { value: 102, label: '102' },
  { value: 103, label: '103' },
  { value: 104, label: '104' },
  { value: 105, label: '105' }
]

// 响应式统计数据
const statsData = reactive({
  totalPeople: 0,
  avgFocus: '--',
  warningCount: 0,
  activeClassrooms: 0
})

// 获取统计摘要
const fetchSummary = async () => {
  const roomId = selectedRoomId.value
  console.log(`[Dashboard] 开始获取教室 ${roomId} 的统计数据...`)
  try {
    const response = await axios.get('http://127.0.0.1:8080/api/stats/summary', {
      params: { roomId: roomId }
    })
    
    if (response.data.code === 200) {
      const data = response.data.data
      console.log(`[Dashboard] 收到数据:`, data)
      
      // 1. 更新总人数 (处理 0 的情况)
      statsData.totalPeople = (data.totalPeople !== null && data.totalPeople !== undefined) ? data.totalPeople : 0
      
      // 2. 更新专注度 (转换为百分比，保留两位小数)
      if (data.avgFocus !== null && data.avgFocus !== undefined) {
        // 后端返回的是 0-1 之间的小数，需要乘以 100
        statsData.avgFocus = (parseFloat(data.avgFocus) * 100).toFixed(2)
      } else {
        statsData.avgFocus = '0.00'
      }
      
      // 3. 更新其他统计项
      statsData.warningCount = data.warnings !== null ? data.warnings : 0
      statsData.activeClassrooms = data.activeClassrooms !== null ? data.activeClassrooms : 0
    } else {
      console.error('[Dashboard] 接口返回错误:', response.data.msg)
    }
  } catch (error) {
    console.error('[Dashboard] 请求失败:', error)
    ElMessage.error('无法同步最新数据，请检查网络或后端状态')
  }
}

// 刷新图表
const refreshChart = () => {
  if (chartRef.value) {
    chartRef.value.fetchData(selectedRoomId.value)
  }
  fetchSummary()
}

// 刷新所有数据 (包装函数)
const refreshData = () => {
  console.log(`🔔 [Event] 监听到数据更新事件，正在刷新教室 ${selectedRoomId.value} 的看板...`)
  fetchSummary()
  if (chartRef.value) {
    chartRef.value.fetchData(selectedRoomId.value)
  }
}


// 重置数据
const handleReset = async () => {
  const roomId = selectedRoomId.value
  console.log(`[Dashboard] 正在重置教室 ${roomId} 的数据...`)
  try {
    // 调用后端重置接口
    const response = await axios.post(`http://127.0.0.1:8080/api/stats/reset?roomId=${roomId}`)
    
    if (response.data.code === 200) {
      ElMessage.success(`教室 ${roomId} 数据已成功重置为固定基准状态`)
      // 触发全量刷新
      refreshData()
    } else {
      ElMessage.error(response.data.msg || '重置失败')
    }
  } catch (error) {
    console.error('[Dashboard] 重置请求失败:', error)
    ElMessage.error('重置失败，请检查后端服务状态')
  }
}

// 打开 YOLO 视觉识别系统
const openYoloSystem = () => {
  yoloVisible.value = true
}

// 处理YOLO对话框关闭事件
const handleYoloDialogClose = () => {
  // 关闭对话框
  yoloVisible.value = false
  yoloMinimized.value = false
}

// 处理YOLO对话框最小化事件
const handleYoloMinimize = () => {
  // 切换最小化状态
  yoloMinimized.value = !yoloMinimized.value
  // 如果是最小化，显示提示信息
  if (yoloMinimized.value) {
    ElMessage.success('YOLO子系统已最小化，将在后台继续运行')
  }
}

// 开始拖拽
const startDrag = (e) => {
  if (e.target.closest('.custom-dialog-actions')) {
    return // 如果点击的是按钮区域，不触发拖拽
  }
  isDragging.value = true
  dragStartX.value = e.clientX
  dragStartY.value = e.clientY
  
  const dialogEl = yoloDialogRef.value
  if (dialogEl) {
    const rect = dialogEl.getBoundingClientRect()
    dialogStartX.value = rect.left
    dialogStartY.value = rect.top
  }
  
  // 添加事件监听
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', stopDrag)
}

// 处理拖拽
const handleDrag = (e) => {
  if (!isDragging.value || !yoloDialogRef.value) {
    return
  }
  
  const dialogEl = yoloDialogRef.value
  const deltaX = e.clientX - dragStartX.value
  const deltaY = e.clientY - dragStartY.value
  
  // 计算新位置
  const newX = dialogStartX.value + deltaX
  const newY = dialogStartY.value + deltaY
  
  // 设置新位置
  dialogEl.style.left = `${newX}px`
  dialogEl.style.top = `${newY}px`
  dialogEl.style.margin = '0'
  dialogEl.style.transform = 'none'
}

// 停止拖拽
const stopDrag = () => {
  isDragging.value = false
  // 移除事件监听
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
}

// 切换教室
const handleRoomChange = (value) => {
  console.log(`🔄 [Action] 切换至教室: ${value}`)
  refreshData()
}

onMounted(() => {
  fetchSummary()
  // 👂 添加监听器：一旦模拟器发送了数据，这里就会自动刷新
  window.addEventListener('data-updated', refreshData)
})

onUnmounted(() => {
  // 🗑️ 组件销毁时移除监听，防止内存泄漏
  window.removeEventListener('data-updated', refreshData)
  // 移除拖拽事件监听
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped>
.dashboard-view {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #2b3a42;
  font-size: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-right-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.room-selector {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-selector .label {
  font-size: 14px;
  color: #606266;
}

/* 统计卡片样式 */
.stat-cards {
  margin-bottom: 20px;
}

.data-card {
  text-align: center;
  transition: all 0.3s;
  cursor: pointer;
}

.data-card:hover {
  transform: translateY(-5px);
}

.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.text-blue { color: #409EFF; }
.text-green { color: #67C23A; }
.text-red { color: #F56C6C; }
.text-purple { color: #9a65fd; }

/* 图表卡片 */
.chart-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* AI 视觉识别悬浮入口 - 位置微调 */
.ai-vision-launcher {
  position: fixed;
  right: 40px;
  bottom: 130px; /* 微调至130px，避开最小化窗口 */
  z-index: 2000;
  cursor: pointer;
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

/* 自定义对话框样式 */
.custom-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: block;
  z-index: 1000;
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
  position: relative;
  resize: both;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  cursor: move;
}

/* 对话框头部 */
.custom-dialog-header {
  padding: 10px 20px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
}

/* 对话框标题 */
.custom-dialog-header span {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

/* 对话框操作按钮 */
.custom-dialog-actions {
  display: flex;
  gap: 10px;
}

/* 按钮样式 */
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
  text-decoration: none;
}

/* 最小化按钮样式 */
.minimize-btn {
  color: #409eff;
}

.minimize-btn:hover {
  background-color: rgba(64, 158, 255, 0.1);
  color: #66b1ff;
}

/* 关闭按钮样式 */
.close-btn {
  color: #f56c6c;
}

.close-btn:hover {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f78989;
}

/* 恢复按钮样式 */
.restore-btn {
  color: #67c23a;
}

.restore-btn:hover {
  background-color: rgba(103, 194, 58, 0.1);
  color: #85ce61;
}

/* 对话框内容区域 */
.custom-dialog-body {
  flex: 1;
  overflow: hidden;
  position: relative;
  min-height: 500px;
}

/* 确保iframe能随容器大小变化 */
.custom-iframe {
  width: 100%;
  height: 100%;
  border: none;
  min-height: 500px;
  object-fit: cover;
}

/* 确保初始加载时iframe内容能正确显示 */
.custom-dialog-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}

/* 调整大小的手柄 */
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

/* 图标样式 */
.dialog-icon {
  font-size: 14px;
}

/* 浮动窗口样式 - 居中显示，避免遮挡右侧按钮 */
.floating-window {
  position: fixed;
  bottom: 40px;  /* 改回40px，贴近底部 */
  left: 50%;     /* 水平居中 */
  transform: translateX(-50%); /* 使用transform实现精准居中 */
  width: 200px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
  cursor: move;
}

/* 浮动窗口头部 */
.floating-header {
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 浮动窗口标题 */
.floating-header span {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

/* 浮动窗口操作按钮 */
.floating-actions {
  display: flex;
  gap: 5px;
}

/* 确保对话框初始高度正确 */
.custom-dialog {
  height: 80vh;
  min-height: 600px;
}

/* 确保iframe初始高度正确 */
.custom-dialog-body {
  min-height: 540px;
}

</style>
