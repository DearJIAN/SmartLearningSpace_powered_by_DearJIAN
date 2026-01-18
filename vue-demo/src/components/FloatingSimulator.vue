<template>
  <!-- 悬浮按钮 - 重新设计为胶囊形 -->
  <div 
    class="floating-btn-wrapper"
    @click="dialogVisible = true"
  >
    <div class="floating-btn-content">
      <!-- 方案1: 分析图表图标 (推荐 - 线性风格) -->
      <i-tabler-chart-line class="btn-icon" />
      
      <!-- 方案2: 数据可视化图标 (面性风格) -->
      <!-- <i-tabler-chart-area class="btn-icon" /> -->
      
      <!-- 方案3: 实验室烧杯图标 (实验感) -->
      <!-- <i-tabler-beaker class="btn-icon" /> -->
      
      <!-- 方案4: 仪表盘图标 (监控感) -->
      <!-- <i-tabler-gauge class="btn-icon" /> -->
      
      <!-- 方案5: 数据库图标 (数据感) -->
      <!-- <i-tabler-database class="btn-icon" /> -->
      
      <span class="btn-text">数据模拟</span>
    </div>
  </div>

  <!-- 模拟器弹窗 -->
  <el-dialog 
    v-model="dialogVisible" 
    title="🛠️ 设备数据模拟器" 
    width="600px"
    :close-on-click-modal="false"
  >
    <el-alert 
      title="操作说明: 支持手动输入或自动生成数据，开启实时同步可随现实时间自动更新。" 
      type="info" 
      show-icon 
      :closable="false" 
      style="margin-bottom: 20px;" 
    />

    <el-form label-width="120px">
      <el-form-item label="时间同步">
        <el-switch
          v-model="isLiveMode"
          active-text="实时同步"
          inactive-text="手动选择"
          @change="handleSyncChange"
        />
      </el-form-item>

      <el-form-item label="模拟时间">
        <el-date-picker 
          v-model="simulateTime" 
          type="datetime" 
          placeholder="选择日期时间"
          style="width: 100%;"
          format="YYYY-MM-DD HH:mm:ss"
          @change="handleTimeManualChange"
        />
      </el-form-item>

      <el-form-item label="教室 ID">
        <el-input v-model="formData.roomId" placeholder="请输入教室ID" />
      </el-form-item>
      
      <el-form-item label="识别总人数">
        <el-input-number v-model="formData.personCount" :min="0" :max="180" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="玩手机人数">
        <el-input-number v-model="formData.phoneCount" :min="0" :max="formData.personCount" style="width: 100%;" />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="resetForm">重置</el-button>
        <el-button type="success" plain @click="generateRealData">
          🎲 智能生成
        </el-button>
        <el-button type="primary" @click="sendData" :loading="loading">
          🚀 发送数据
        </el-button>
      </div>
    </template>

    <div v-if="responseText" class="log-box" :class="{ 'error-log': responseText.includes('❌') }">
      <p>📋 系统反馈：</p>
      <div class="log-content">{{ responseText }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const dialogVisible = ref(false)
const loading = ref(false)
const responseText = ref('')
const isLiveMode = ref(true) // 默认开启实时同步
let timeUpdateInterval = null

// 表单数据
const simulateTime = ref(new Date())
const formData = reactive({
  roomId: 101,
  personCount: 0,
  phoneCount: 0
})

// 时间同步逻辑
const handleSyncChange = (val) => {
  if (val) {
    simulateTime.value = new Date()
  }
}

const handleTimeManualChange = () => {
  isLiveMode.value = false // 手动选择时间时关闭实时同步
}

// 启动定时器实时更新
onMounted(() => {
  timeUpdateInterval = setInterval(() => {
    // 只有在开启实时模式时才更新时间
    if (isLiveMode.value) {
      simulateTime.value = new Date()
    }
  }, 1000)
})

onUnmounted(() => {
  if (timeUpdateInterval) {
    clearInterval(timeUpdateInterval)
  }
})

// 🎲 智能生成数据的逻辑 (根据模拟时间)
const generateRealData = () => {
  const hour = simulateTime.value.getHours()
  let pCount = 0
  let phCount = 0

  // 判定是否为上课/自习高峰 (08:00-11:50, 14:30-18:00, 18:40-22:00)
  const minute = simulateTime.value.getMinutes();
  const timeInMinutes = hour * 60 + minute;
  
  const isPeak = (timeInMinutes >= 8 * 60 && timeInMinutes < 11 * 60 + 50) || 
                 (timeInMinutes >= 14 * 60 + 30 && timeInMinutes < 18 * 60) || 
                 (timeInMinutes >= 18 * 60 + 40 && timeInMinutes < 22 * 60);
  
  if (isPeak) {
    // 峰值：80-165人 (对应 180 人容量)
    pCount = Math.floor(Math.random() * (165 - 80 + 1)) + 80
    phCount = Math.floor(Math.random() * 12) + 2
  } else if (hour >= 23 || hour < 6) {
    // 深夜：0-5人
    pCount = Math.floor(Math.random() * 6)
    phCount = 0
  } else {
    // 普通时段：10-40人
    pCount = Math.floor(Math.random() * 31) + 10
    phCount = Math.floor(Math.random() * (pCount * 0.2))
  }

  formData.personCount = pCount
  formData.phoneCount = phCount
  ElMessage.info(`已生成 ${hour}点 的模拟数据，可手动微调`)
}

// 🚀 发送数据的逻辑
const sendData = async () => {
  if (!formData.roomId) {
    ElMessage.warning('请输入教室 ID')
    return
  }
  
  loading.value = true
  try {
    const res = await axios.post('http://127.0.0.1:8080/api/device/receive', null, {
      params: {
        roomId: formData.roomId,
        personCount: formData.personCount,
        phoneCount: formData.phoneCount
      }
    })
    
    if (res.data.code === 200) {
      const timeStr = simulateTime.value.toLocaleString()
      responseText.value = `[${timeStr}] ✅ 发送成功! 教室: ${formData.roomId} | 总人数: ${formData.personCount} | 玩手机: ${formData.phoneCount}`
      ElMessage.success('数据已上传')
      window.dispatchEvent(new CustomEvent('data-updated'))
    } else {
      responseText.value = "❌ 报错：" + res.data.msg
    }
  } catch (error) {
    console.error(error)
    responseText.value = "❌ 连接失败：请检查 Java 后端是否启动！"
    ElMessage.error('连接失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  isLiveMode.value = true
  simulateTime.value = new Date()
  formData.roomId = 101
  formData.personCount = 0
  formData.phoneCount = 0
  responseText.value = ''
}
</script>

<style scoped>
/* 悬浮按钮包裹容器 - 胶囊设计 - 按钮背景色互补色呼吸灯 */
.floating-btn-wrapper {
  position: fixed;
  bottom: 40px;
  right: 40px;
  height: 48px;
  background: linear-gradient(135deg, #9a65fd 0%, #7b4af0 50%, #6a3de5 100%);
  border-radius: 24px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(154, 101, 253, 0.4);
  z-index: 2000;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  animation: breathing-complementary 2.5s infinite ease-in-out;
  user-select: none;
}

.floating-btn-wrapper:hover {
  transform: scale(1.05) translateY(-2px);
  box-shadow: 0 8px 25px rgba(154, 101, 253, 0.6);
  animation-play-state: paused;
}

.floating-btn-wrapper:active {
  transform: scale(0.95);
}

.floating-btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-icon {
  font-size: 20px;
  color: white;
}

.btn-text {
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 互补色呼吸动画 - 紫色背景的互补色是黄绿色/橙色 */
@keyframes breathing-complementary {
  0% { 
    box-shadow: 0 4px 15px rgba(255, 107, 53, 0.5); /* 橙红色 - 互补色 */
    transform: scale(1);
  }
  50% { 
    box-shadow: 0 4px 30px rgba(255, 107, 53, 0.9), 0 0 0 4px rgba(255, 107, 53, 0.2); /* 更亮的互补色 */
    transform: scale(1.02);
  }
  100% { 
    box-shadow: 0 4px 15px rgba(255, 107, 53, 0.5); 
    transform: scale(1);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.log-box {
  margin-top: 20px;
  padding: 15px;
  background: #f0f9eb;
  border-radius: 8px;
  border-left: 4px solid #67C23A;
  color: #67C23A;
  font-family: 'Consolas', monospace;
  transition: all 0.3s;
}

.error-log {
  background: #fef0f0;
  border-left-color: #f56c6c;
  color: #f56c6c;
}

.log-content {
  font-size: 13px;
  line-height: 1.6;
  font-weight: 500;
}
</style>
