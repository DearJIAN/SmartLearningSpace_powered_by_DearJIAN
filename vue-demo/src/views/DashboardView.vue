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


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'
import FocusTrendChart from '../components/FocusTrendChart.vue'
import FloatingSimulator from '../components/FloatingSimulator.vue'

const chartRef = ref(null)
const selectedRoomId = ref(101)
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
  window.removeEventListener('data-updated', refreshData)
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

</style>
