<template>
  <div class="lost-found-view">
    <div class="page-header">
      <h2>🔍 失物招领智能辅助</h2>
      <div class="header-actions">
        <el-button type="primary" size="small" @click="handleRefresh" :icon="Refresh">刷新数据</el-button>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card shadow="hover" class="filter-card">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="教室">
          <el-select v-model="filterForm.roomId" placeholder="选择教室" clearable style="width: 120px;">
            <el-option
              v-for="room in classroomOptions"
              :key="room.value"
              :label="room.label"
              :value="room.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="物品类型">
          <el-select v-model="filterForm.itemType" placeholder="选择物品类型" clearable style="width: 120px;">
            <el-option
              v-for="type in itemTypeOptions"
              :key="type"
              :label="type"
              :value="type"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="选择状态" clearable style="width: 120px;">
            <el-option label="未认领" value="0" />
            <el-option label="已认领" value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button-group>
            <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
            <el-button type="danger" @click="handleClearAll" :icon="Delete">清空记录</el-button>
          </el-button-group>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 失物招领列表 -->
    <el-card shadow="hover" class="list-card">
      <template #header>
        <div class="card-header">
          <span>📋 失物招领记录</span>
          <span class="total-count">共 {{ lostItemList.length }} 条记录</span>
        </div>
      </template>
      
      <el-table :data="lostItemList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roomName" label="教室位置" width="120">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.roomName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemType" label="物品类型" width="120">
          <template #default="scope">
            <el-tag :type="getItemTypeTagType(scope.row.itemType)">{{ scope.row.itemType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemCount" label="物品数量" width="100" align="center">
          <template #default="scope">
            <el-tag type="primary">{{ scope.row.itemCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="foundTime" label="发现时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.foundTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" width="140">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'warning' : 'success'" class="status-tag">
              <el-icon class="status-icon" v-if="scope.row.status === 0"><Warning /></el-icon>
              <el-icon class="status-icon" v-else><CircleCheck /></el-icon>
              <span class="status-text">{{ scope.row.status === 0 ? '未认领' : '已认领' }}</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="记录时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0" 
              type="success" 
              size="small" 
              @click="handleClaim(scope.row)"
              :icon="Check"
            >
              标记认领
            </el-button>
            <el-button 
              v-else 
              type="info" 
              size="small" 
              disabled
              :icon="CircleCheck"
            >
              已认领
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[15, 30, 45, 60]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- YOLO相关功能 -->
    <div class="simulate-section">
      <el-card shadow="hover" class="simulate-card">
        <template #header>
          <div class="card-header">
            <span>🤖 YOLO检测功能</span>
          </div>
        </template>
        <div class="simulate-content">
          <div class="yolo-buttons">
            <div class="button-group">
              <p>选择监控教室：</p>
              <el-select v-model="selectedRoomId" placeholder="选择教室" style="width: 150px;">
                <el-option
                  v-for="room in classroomOptions"
                  :key="room.value"
                  :label="room.label"
                  :value="room.value"
                />
              </el-select>
            </div>
            <div class="button-group">
              <p>模拟YOLO检测结果：</p>
              <el-button type="warning" size="large" @click="simulateYoloDetection" :icon="VideoCamera">
                模拟YOLO物品检测
              </el-button>
            </div>
            <div class="button-group">
              <p>打开真实YOLO子系统：</p>
              <el-button type="primary" size="large" @click="openYoloSystem" :icon="VideoCamera">
                打开YOLO视觉识别子系统
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- YOLO子系统弹窗 -->
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
            src="http://127.0.0.1:5000" 
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
    
    <!-- 认领弹窗 -->
    <el-dialog
      v-model="claimDialogVisible"
      title="确认认领"
      width="450px"
      center
      class="claim-dialog"
    >
      <div class="claim-dialog-content">
        <!-- 物品信息卡片 -->
        <el-card class="item-info-card" shadow="hover">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">物品名称：</span>
                <el-tag :type="getItemTypeTagType(currentClaimRow.itemType)" size="large">{{ currentClaimRow.itemType }}</el-tag>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <span class="info-label">当前数量：</span>
                <el-tag type="primary" size="large">{{ currentClaimRow.itemCount }}</el-tag>
              </div>
            </el-col>
          </el-row>
        </el-card>
        
        <!-- 认领方式选择 -->
        <el-card class="claim-mode-card" shadow="hover" v-if="currentClaimRow.itemCount > 1">
          <template #header>
            <div class="card-header">
              <span class="section-title">选择认领方式</span>
            </div>
          </template>
          <div class="custom-radio-group">
            <div class="custom-radio-item" :class="{ 'is-checked': claimMode === 'full' }" @click="claimMode = 'full'">
              <div class="radio-circle">
                <div class="radio-dot" v-if="claimMode === 'full'"></div>
              </div>
              <div class="radio-content">
                <span class="radio-text">认领全部数量</span>
                <el-tag type="success" size="small" class="mode-tag">{{ currentClaimRow.itemCount }} 件</el-tag>
              </div>
            </div>
            <div class="custom-radio-item" :class="{ 'is-checked': claimMode === 'custom' }" @click="claimMode = 'custom'">
              <div class="radio-circle">
                <div class="radio-dot" v-if="claimMode === 'custom'"></div>
              </div>
              <div class="radio-content">
                <span class="radio-text">认领指定数量</span>
              </div>
            </div>
          </div>
          
          <!-- 数量输入 -->
          <el-collapse-transition>
            <div v-if="claimMode === 'custom'" class="custom-quantity-section">
              <div class="quantity-input-wrapper">
                <el-input-number
                  v-model="customClaimQuantity"
                  :min="1"
                  :max="currentClaimRow.itemCount"
                  :step="1"
                  class="quantity-input"
                  size="large"
                  placeholder="请输入认领数量"
                />
                <div class="quantity-hint">
                  可认领数量范围：1 - {{ currentClaimRow.itemCount }} 件
                </div>
              </div>
            </div>
          </el-collapse-transition>
        </el-card>
      </div>
      
      <!-- 弹窗底部按钮 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="claimDialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" @click="handleConfirmClaim" :icon="Check">
            确认认领
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, 
  Search, 
  RefreshRight, 
  Warning, 
  CircleCheck, 
  Check, 
  VideoCamera,
  Minus,
  Close,
  FullScreen,
  Expand
} from '@element-plus/icons-vue'
// 使用JavaScript内置Date对象代替dayjs
import axios from 'axios'

// 状态定义
const loading = ref(false)
const lostItemList = ref([])
const yoloVisible = ref(false)
const yoloMinimized = ref(false) // YOLO子系统最小化状态
const selectedRoomId = ref(101)

// 认领弹窗相关
const claimDialogVisible = ref(false)
const currentClaimRow = ref({}) // 当前认领的记录
const customClaimQuantity = ref(1) // 自定义认领数量
const claimMode = ref('full') // 认领方式：full-全部，custom-指定数量

// 拖拽相关变量
const yoloDialogRef = ref(null)
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartY = ref(0)
const dialogStartX = ref(0)
const dialogStartY = ref(0)

// 分页相关
const pagination = reactive({
  currentPage: 1,
  pageSize: 15,
  total: 0
})

// 教室选项
const classroomOptions = ref([
  { value: 101, label: '101教室' },
  { value: 102, label: '102教室' },
  { value: 103, label: '103教室' },
  { value: 104, label: '104教室' },
  { value: 105, label: '105教室' }
])

// 动态物品类型选项（从数据中提取）
const itemTypeOptions = ref([])

// 实时更新定时器
let updateTimer = null

// 筛选表单
const filterForm = reactive({
  roomId: '',
  itemType: '',
  status: ''
})

const dateRange = ref([])

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 获取物品类型标签颜色
const getItemTypeTagType = (itemType) => {
  const typeMap = {
    '书包': 'primary',
    '水杯': 'success',
    '雨伞': 'warning',
    '电子设备': 'danger',
    '其他': 'info'
  }
  return typeMap[itemType] || 'info'
}

// 获取失物招领列表
const fetchLostItemList = async () => {
  loading.value = true
  try {
    const params = {
      roomId: filterForm.roomId || undefined,
      itemType: filterForm.itemType || undefined,
      status: filterForm.status || undefined,
      startTime: dateRange.value[0] ? `${dateRange.value[0]} 00:00:00` : undefined,
      endTime: dateRange.value[1] ? `${dateRange.value[1]} 23:59:59` : undefined,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    
    const response = await axios.get('http://127.0.0.1:8080/api/lost-found/list', { params })
    if (response.data.code === 200) {
      // 假设后端返回的数据结构包含records和total
      lostItemList.value = response.data.data.records || response.data.data
      pagination.total = response.data.data.total || response.data.total || 0
      
      // 提取物品类型，生成动态选项
      const types = [...new Set(lostItemList.value.map(item => item.itemType).filter(Boolean))]
      itemTypeOptions.value = types
    } else {
      ElMessage.error(response.data.msg || '获取失物招领列表失败')
    }
  } catch (error) {
    console.error('获取失物招领列表失败:', error)
    ElMessage.error('获取失物招领列表失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 启动实时更新
const startRealTimeUpdate = () => {
  // 清除现有的定时器
  if (updateTimer) {
    clearInterval(updateTimer)
  }
  
  // 每1秒更新一次数据
  updateTimer = setInterval(() => {
    fetchLostItemList()
  }, 1000)
}

// 停止实时更新
const stopRealTimeUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
    updateTimer = null
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchLostItemList()
}

// 搜索
const handleSearch = () => {
  fetchLostItemList()
}

// 分页大小变化事件
const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  fetchLostItemList()
}

// 当前页码变化事件
const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage
  fetchLostItemList()
}

// 清空所有记录
const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确认要清空所有失物招领记录吗？此操作不可恢复。', '确认操作', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete('http://127.0.0.1:8080/api/lost-found/clear')
    if (response.data.code === 200) {
      ElMessage.success('清空记录成功')
      fetchLostItemList() // 刷新列表
    } else {
      ElMessage.error(response.data.msg || '清空记录失败')
    }
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('清空记录失败:', error)
    ElMessage.error('清空记录失败，请稍后重试')
  }
}

// 标记认领 - 打开弹窗
const handleClaim = (row) => {
  currentClaimRow.value = row
  customClaimQuantity.value = 1 // 重置为默认值
  claimMode.value = 'full' // 重置为默认选择全部
  claimDialogVisible.value = true // 打开弹窗
}

// 确认认领 - 根据选择的方式处理
const handleConfirmClaim = async () => {
  let quantity = currentClaimRow.value.itemCount // 默认认领全部
  
  if (claimMode.value === 'custom') {
    // 验证数量
    if (customClaimQuantity.value < 1 || customClaimQuantity.value > currentClaimRow.value.itemCount) {
      ElMessage.error(`认领数量必须在1到${currentClaimRow.value.itemCount}之间`)
      return
    }
    quantity = customClaimQuantity.value
  }
  
  try {
    // 发送认领请求
    const response = await axios.put(`http://127.0.0.1:8080/api/lost-found/${currentClaimRow.value.id}/status?status=1&quantity=${quantity}`)
    if (response.data.code === 200) {
      const message = quantity === currentClaimRow.value.itemCount 
        ? '标记认领全部数量成功' 
        : `标记认领${quantity}件成功`
      ElMessage.success(message)
      
      if (quantity === currentClaimRow.value.itemCount) {
        currentClaimRow.value.status = 1
        currentClaimRow.value.itemCount = 0
      } else {
        // 如果只认领了部分数量，更新物品数量
        currentClaimRow.value.itemCount -= quantity
      }
      
      claimDialogVisible.value = false // 关闭弹窗
    } else {
      ElMessage.error(response.data.msg || '标记认领失败')
    }
  } catch (error) {
    console.error('标记认领失败:', error)
    ElMessage.error('标记认领失败，请稍后重试')
  }
}

// 模拟YOLO检测结果
const simulateYoloDetection = async () => {
  try {
    const response = await axios.post('http://127.0.0.1:8080/api/lost-found/generate-test')
    if (response.data.code === 200) {
      ElMessage.success('模拟YOLO检测成功，已生成测试失物招领记录')
      fetchLostItemList() // 刷新列表
    } else {
      ElMessage.error(response.data.msg || '模拟YOLO检测失败')
    }
  } catch (error) {
    console.error('模拟YOLO检测失败:', error)
    ElMessage.error('模拟YOLO检测失败，请检查后端服务')
  }
}

// 打开YOLO子系统
const openYoloSystem = () => {
  yoloVisible.value = true
  // 启动YOLO检测结果发送定时器
  startYoloDetectionTimer()
  // 启动实时更新
  startRealTimeUpdate()
}

// 处理YOLO对话框关闭事件
const handleYoloDialogClose = () => {
  // 停止YOLO检测结果发送定时器
  stopYoloDetectionTimer()
  // 停止实时更新，避免表格闪烁
  stopRealTimeUpdate()
  // 关闭对话框
  yoloVisible.value = false
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

// 停止YOLO检测结果发送定时器
const stopYoloDetectionTimer = () => {
  if (yoloDetectionTimer) {
    clearInterval(yoloDetectionTimer)
    yoloDetectionTimer = null
  }
}

// 启动YOLO检测结果发送定时器
let yoloDetectionTimer = null
const startYoloDetectionTimer = () => {
  stopYoloDetectionTimer() // 先停止之前的定时器
  
  // 每1秒发送一次YOLO检测结果
  yoloDetectionTimer = setInterval(async () => {
    // 这里应该是从YOLO子系统获取真实的检测结果
    // 由于YOLO子系统是通过iframe嵌入的，我们需要实现iframe与父页面的通信
    // 这里使用模拟数据来演示
    
    // 模拟YOLO检测结果
    const mockDetectionResults = [
      { roomId: selectedRoomId.value, itemType: 'book', itemCount: Math.floor(Math.random() * 10) + 1 },
      { roomId: selectedRoomId.value, itemType: 'chair', itemCount: Math.floor(Math.random() * 5) + 1 },
      { roomId: selectedRoomId.value, itemType: 'person', itemCount: Math.floor(Math.random() * 20) + 1 }
    ]
    
    // 发送检测结果到后端
    for (const result of mockDetectionResults) {
      try {
        await axios.post('http://127.0.0.1:8080/api/lost-found/yolo-detection', result)
      } catch (error) {
        console.error('发送YOLO检测结果失败:', error)
      }
    }
  }, 1000)
}

// 页面加载时获取数据
onMounted(() => {
  fetchLostItemList()
  // 不自动启动实时更新，只有在打开YOLO子系统时才启动
})

// 组件卸载时停止实时更新和YOLO检测定时器
onUnmounted(() => {
  stopRealTimeUpdate()
  stopYoloDetectionTimer()
})
</script>

<style scoped>
.lost-found-view {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
}

.list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-count {
  font-size: 14px;
  color: #606266;
}

.help-text {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.simulate-section {
  margin-top: 30px;
}

.simulate-card {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
}

.simulate-content {
  text-align: center;
  padding: 20px 0;
}

.simulate-content p {
  margin-bottom: 20px;
  color: #606266;
}

/* 当前状态标签样式 */
/* 分页容器样式 */
.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 10px 0;
}

/* 确保状态标签内的元素在同一行显示 */
.el-tag.status-tag {
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  line-height: 1.2 !important;
  padding: 4px 8px !important;
}

.status-icon {
  font-size: 14px;
  margin-right: 4px;
  display: inline-block;
  vertical-align: middle;
}

.status-text {
  font-size: 14px;
  display: inline-block;
  vertical-align: middle;
}

/* YOLO功能区域样式 */
.yolo-buttons {
  display: flex;
  gap: 40px;
  justify-content: center;
  flex-wrap: wrap;
}

.button-group {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 300px;
}

.button-group p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

/* YOLO弹窗样式 */
.yolo-dialog .el-dialog__body {
  padding: 0 !important;
  height: 85vh !important;
}

.yolo-dialog .iframe-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.yolo-dialog .yolo-iframe {
  width: 100%;
  height: 100%;
  border: none;
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
  min-height: 500px;
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

/* 浮动窗口样式 */
.floating-window {
  position: fixed;
  bottom: 20px;
  right: 20px;
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

/* 恢复按钮样式 */
.restore-btn:hover {
  background-color: rgba(0, 0, 0, 0.1);
  color: #409eff;
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

/* 认领弹窗样式 */
.claim-dialog-content {
  padding: 0;
}

/* 物品信息卡片 */
.item-info-card {
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  padding: 10px 0;
}

.info-label {
  font-weight: bold;
  color: #606266;
  min-width: 100px;
}

/* 认领方式卡片 */
.claim-mode-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-weight: bold;
  color: #303133;
  font-size: 16px;
}

/* 自定义单选按钮组 */
.custom-radio-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
  padding: 0;
}

/* 自定义单选按钮项 */
.custom-radio-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100%;
  height: 44px;
  gap: 12px;
  padding: 0 16px;
  margin: 0;
  background: transparent;
  border-radius: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
}

/* 悬停效果 */
.custom-radio-item:hover {
  background-color: rgba(64, 158, 255, 0.1);
}

/* 选中效果 */
.custom-radio-item.is-checked {
  background-color: rgba(64, 158, 255, 0.08);
}

/* 自定义单选圆圈 */
.radio-circle {
  width: 18px;
  height: 18px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

/* 选中状态的圆圈 */
.custom-radio-item.is-checked .radio-circle {
  border-color: #409eff;
  background-color: #409eff;
}

/* 选中的圆点 */
.radio-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: white;
  transition: all 0.3s ease;
  transform: scale(0);
}

/* 选中状态的圆点 */
.custom-radio-item.is-checked .radio-dot {
  transform: scale(1);
}

/* 单选内容 */
.radio-content {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  height: 100%;
  margin-left: 0;
}

/* 单选文字 */
.radio-text {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
  flex: 1;
  line-height: 1;
  transition: all 0.3s ease;
}

/* 选中状态的文字 */
.custom-radio-item.is-checked .radio-text {
  color: #409eff;
}

/* 数量标签 */
.mode-tag {
  margin-left: auto;
  font-weight: bold;
  flex-shrink: 0;
  line-height: 1;
}

/* 自定义数量输入区域 */
.custom-quantity-section {
  margin-top: 20px;
  padding: 20px;
  background-color: #f9fafc;
  border-radius: 6px;
  border: 1px dashed #dcdfe6;
}

.quantity-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.quantity-input {
  width: 100%;
  max-width: 300px;
}

.quantity-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 弹窗底部按钮 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 15px 20px 20px;
  background-color: #fafafa;
  border-top: 1px solid #ebeef5;
}

.dialog-footer .el-button {
  padding: 12px 24px;
  font-size: 15px;
  font-weight: 500;
}
</style>
