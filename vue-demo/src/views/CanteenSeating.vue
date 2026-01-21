<template>
  <div class="canteen-seating-container">
    <div class="seating-header">
      <div style="display: flex; align-items: center; gap: 20px; margin-bottom: 15px;">
        <el-button type="primary" size="large" :icon="ArrowLeft" @click="goBack">返回</el-button>
        <h1 class="seating-title">食堂智能选座</h1>
        <!-- 实体闹钟 -->
        <div class="alarm-clock-main">
          <div class="clock-face">
            <div class="clock-hour">{{ formattedTime.hours }}</div>
            <div class="clock-separator">:</div>
            <div class="clock-minute">{{ formattedTime.minutes }}</div>
            <div class="clock-separator">:</div>
            <div class="clock-second">{{ formattedTime.seconds }}</div>
          </div>
          <div class="clock-date">{{ formattedDate }}</div>
        </div>
      </div>
      <div class="seating-status">
        <div class="status-item">
          <span class="status-dot available"></span>
          <span>空闲</span>
        </div>

        <div class="status-item">
          <span class="status-dot occupied"></span>
          <span>已使用</span>
        </div>
      </div>
    </div>

    <div class="seating-content">
      <!-- 左侧：座位分布图 -->
      <div class="seating-map-section">
        <div class="seating-map-header">
          <h2>实时座位分布</h2>
          <div class="map-controls">
            <el-button type="primary" size="small" @click="refreshSeats">
              <el-icon><i-tabler-refresh /></el-icon>
              刷新
            </el-button>
            <el-button 
              :type="is3DView ? 'primary' : 'default'" 
              size="small" 
              @click="toggleView"
            >
              <el-icon v-if="is3DView"><i-tabler-cube-off /></el-icon>
              <el-icon v-else><i-tabler-cube /></el-icon>
              {{ is3DView ? '2D视图' : '3D视图' }}
            </el-button>
          </div>
        </div>

        <div class="seating-map">
          <!-- 楼层选择 -->
          <div class="canteen-floor-selector">
            <el-icon size="32"><i-tabler-building /></el-icon>
            <div class="floor-buttons">
              <el-button
                v-for="floor in floors"
                :key="floor"
                :type="currentFloor === floor ? 'primary' : 'default'"
                size="large"
                @click="switchFloor(floor)"
              >
                {{ floor }}楼
              </el-button>
            </div>
          </div>

          <!-- 2D座位网格 -->
          <div v-if="!is3DView" class="seats-grid">
            <div
              v-for="seat in seats"
              :key="seat.id"
              class="seat-item"
              :class="{
                'seat-available': seat.status === 'available' || (seat.currentUsers < seat.capacity),
                'seat-occupied': seat.status === 'occupied' && seat.currentUsers === seat.capacity
              }"
              @click="handleSeatClick(seat)"
            >
              <div class="seat-info">
                <span class="seat-number">{{ seat.number }}</span>
                <div class="seat-capacity">
                  <span class="capacity-text">{{ seat.currentUsers }}/{{ seat.capacity }}</span>
                </div>
              </div>
              <div v-if="seat.status === 'occupied'" class="seat-status-indicator">
                <el-tooltip
                  :content="getSeatTooltipContent(seat)"
                  placement="top"
                  effect="light"
                >
                  <div class="status-icon-wrapper">
                    <el-icon size="14" class="status-icon-clock"><i-tabler-clock /></el-icon>
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>

          <!-- 3D座位场景 -->
          <div v-else class="seats-3d-container">
            <div ref="seats3DCanvas" class="seats-3d-canvas"></div>
          </div>

          <!-- 取餐区 -->
          <div class="canteen-counter" @click="showCounterInfo">
            <el-icon size="32"><i-tabler-clipboard-list /></el-icon>
            <span>取餐区</span>
          </div>
        </div>
      </div>

      <!-- 右侧：预订信息和导航 -->
      <div class="seating-sidebar">
        <!-- 我的座位卡片 -->
        <el-card class="booking-card">
          <template #header>
            <h3>我的座位</h3>
          </template>
          <div v-if="currentBooking" class="current-booking">
            <div class="booking-info">
              <div class="booking-item">
                <span class="label">座位号：</span>
                <span class="value">{{ currentBooking.seatNumber }}</span>
              </div>
              <div class="booking-item">
                <span class="label">楼层：</span>
                <span class="value">{{ currentBooking.floor }}楼</span>
              </div>
              <div class="booking-item">
                <span class="label">使用时间：</span>
                <span class="value">{{ currentBooking.bookingTime }}</span>
              </div>
              <div class="booking-item">
                <span class="label">有效期至：</span>
                <span class="value">{{ currentBooking.expiryTime }}</span>
              </div>
            </div>
            <div class="booking-actions">
              <el-button type="primary" @click="navigateToSeat">
                <el-icon><i-tabler-navigation /></el-icon>
                导航到座位
              </el-button>
              <el-button type="success" @click="navigateToOrdering">
                <el-icon><i-tabler-shopping-cart /></el-icon>
                前往点餐
              </el-button>
            </div>
          </div>
          <div v-else class="no-booking">
            <el-icon size="48" style="color: #909399;"><i-tabler-armchair /></el-icon>
            <p>您当前没有使用座位</p>
          </div>
        </el-card>

        <!-- 前往点餐 -->
        <el-card class="reservation-form-card" v-if="selectedSeat && (selectedSeat.status === 'available' || (selectedSeat.currentUsers < selectedSeat.capacity))">
          <template #header>
            <h3>前往点餐</h3>
          </template>
          <div class="seat-info">
            <div class="detail-item">
              <span class="label">座位号：</span>
              <span class="value">{{ selectedSeat.number }}</span>
            </div>
            <div class="detail-item">
              <span class="label">楼层：</span>
              <span class="value">{{ selectedSeat.floor }}楼</span>
            </div>
            <div class="detail-item">
              <span class="label">容量：</span>
              <span class="value">{{ selectedSeat.capacity }}人座</span>
            </div>
            <div class="detail-item">
              <span class="label">已使用：</span>
              <span class="value">{{ selectedSeat.currentUsers }}人</span>
            </div>
            <div class="detail-item">
              <span class="label">剩余容量：</span>
              <span class="value">{{ selectedSeat.capacity - selectedSeat.currentUsers }}人</span>
            </div>
          </div>
          <div class="form-actions">
            <el-button @click="selectedSeat = null">取消</el-button>
            <el-button type="primary" @click="navigateToOrdering">前往点餐</el-button>
          </div>
        </el-card>

        <!-- 座位详情 -->
        <el-card class="seat-detail-card" v-if="selectedSeat">
          <template #header>
            <h3>座位详情</h3>
          </template>
          <div class="seat-detail">
            <div class="detail-item">
              <span class="label">座位号：</span>
              <span class="value">{{ selectedSeat.number }}</span>
            </div>
            <div class="detail-item">
              <span class="label">区域：</span>
              <span class="value">{{ selectedAreaText }}</span>
            </div>
            <div class="detail-item">
              <span class="label">楼层：</span>
              <span class="value">{{ selectedSeat.floor }}楼</span>
            </div>
            <div class="detail-item">
              <span class="label">状态：</span>
              <el-tag
              :type="selectedSeat.status === 'available' ? 'success' : 'danger'"
            >
              {{ selectedSeat.status === 'available' ? '空闲' : '已使用' }}
            </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">容量：</span>
              <span class="value">{{ selectedSeat.capacity }}人座</span>
            </div>
            <div class="detail-item">
              <span class="label">当前人数：</span>
              <span class="value">{{ selectedSeat.currentUsers }}人</span>
            </div>
            <div class="detail-item">
              <span class="label">剩余容量：</span>
              <span class="value">{{ selectedSeat.capacity - selectedSeat.currentUsers }}人</span>
            </div>
            
            <!-- 时间信息 -->
              <div v-if="selectedSeat.status === 'occupied'" class="time-info-section">
                <div class="detail-item">
                  <span class="label">使用开始时间：</span>
                  <span class="value">{{ selectedSeat.startTime?.toLocaleTimeString() }}</span>
                </div>
              </div>
              
              <!-- 使用者列表 -->
            <div v-if="selectedSeat.status === 'occupied' && selectedSeat.reservedBy && selectedSeat.reservedBy.length > 0" class="users-list-section">
              <div class="detail-item users-item" v-for="(userId, index) in selectedSeat.reservedBy" :key="index">
                <span class="label">使用者{{ index + 1 }}：</span>
                <span class="value">用户{{ userId }}</span>
              </div>
            </div>
            
            <!-- 操作按钮组 -->
            <div class="detail-actions">
              <!-- 签到按钮 -->
              <el-button 
                type="primary" 
                @click="checkInSeat"
                v-if="selectedSeat.status === 'reserved' && currentBooking && currentBooking.seatNumber === selectedSeat.number"
              >
                <el-icon><i-tabler-check /></el-icon>
                签到使用
              </el-button>
              <!-- 点餐按钮 -->
              <el-button 
                type="success" 
                @click="navigateToOrdering(selectedSeat)"
                :disabled="selectedSeat.status === 'occupied'"
              >
                <el-icon><i-tabler-shopping-cart /></el-icon>
                前往点餐
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 导航模态框 -->
    <el-dialog v-model="navigationDialogVisible" title="前往您的座位" width="60%">
      <div class="navigation-content">
        <div class="navigation-map">
          <h3>导航路线</h3>
          <div class="map-placeholder">
            <el-icon size="64" style="color: #909399;"><i-tabler-map-2 /></el-icon>
            <p>导航地图（模拟）</p>
            <div class="route-info">
              <p>从入口到座位 {{ currentBooking?.seatNumber }} 的路线：</p>
              <ol>
                <li>进入食堂入口</li>
                <li>直行 10 米</li>
                <li>左转进入 {{ currentBooking?.area }}</li>
                <li>找到座位号 {{ currentBooking?.seatNumber }}</li>
              </ol>
            </div>
          </div>
        </div>
        <div class="navigation-actions">
          <el-button type="primary" size="large" @click="startNavigation">
            <el-icon><i-tabler-navigation /></el-icon>
            开始导航
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElButton } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'

// 状态管理

const seats = ref([])
const selectedSeat = ref(null)
const currentBooking = ref(null)
const navigationDialogVisible = ref(false)


// 楼层数据缓存对象，用于存储各楼层的座位数据
const floorDataCache = ref({})


// 3D渲染容器引用
const seats3DCanvas = ref(null)

// 3D视图状态
const is3DView = ref(false)
// 使用普通变量存储Three.js核心对象，避免响应式代理冲突
let scene = null
let camera = null
let renderer = null
const seatObjects = [] // 使用普通数组存储Three.js对象，避免响应式代理冲突
let controls = null
const animationId = ref(null)

// 路由
const router = useRouter()

// 楼层选择
const floors = ref([1, 2, 3])
const currentFloor = ref(1)

// 实时时钟
const currentTime = ref(new Date())
const formattedTime = computed(() => {
  const hours = currentTime.value.getHours().toString().padStart(2, '0')
  const minutes = currentTime.value.getMinutes().toString().padStart(2, '0')
  const seconds = currentTime.value.getSeconds().toString().padStart(2, '0')
  return { hours, minutes, seconds }
})

const formattedDate = computed(() => {
  const year = currentTime.value.getFullYear()
  const month = (currentTime.value.getMonth() + 1).toString().padStart(2, '0')
  const day = currentTime.value.getDate().toString().padStart(2, '0')
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const weekday = weekdays[currentTime.value.getDay()]
  return `${year}-${month}-${day} 星期${weekday}`
})

let timer = null

// 返回按钮点击事件
const goBack = () => {
  window.history.back()
}

// 楼层切换方法
const switchFloor = (floor) => {
  currentFloor.value = floor
  
  // 检查缓存中是否有当前楼层的数据
  if (floorDataCache.value[floor]) {
    // 使用缓存数据
    seats.value = floorDataCache.value[floor]
    ElMessage.success(`已切换到${floor}楼，使用缓存数据`)
  } else {
    // 缓存中没有数据，生成新数据并缓存
    const newSeats = generateSeats()
    seats.value = newSeats
    floorDataCache.value[floor] = newSeats
    ElMessage.success(`已切换到${floor}楼，生成新数据`)
  }
  
  // 如果当前是3D视图，更新3D座位
  if (is3DView.value) {
    update3DSeats()
  }
}

// 入口点击事件
const showEntranceInfo = () => {
  ElMessage({
    message: '欢迎进入食堂！请遵守食堂秩序，文明用餐。',
    type: 'info',
    duration: 3000
  })
}

// 取餐区点击事件
const showCounterInfo = () => {
  // 跳转到智能点餐页面
  router.push('/canteen/ordering')
}

// 预订表单引用
const reservationFormRef = ref(null)

// 预订表单
const reservationForm = ref({
  seatNumber: '',
  area: '',
  startTime: null,
  endTime: null
})

// 最大预订时长（分钟）
const MAX_BOOKING_DURATION = 30

// 预订表单验证规则
const reservationRules = ref({
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
})

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 只能选择今天及以后的日期
}

// 禁用过去的小时
const disabledHours = () => {
  const now = new Date()
  const currentHour = now.getHours()
  return Array.from({ length: currentHour }, (_, i) => i)
}

// 禁用过去的分钟
const disabledMinutes = (selectedHour) => {
  const now = new Date()
  const currentHour = now.getHours()
  const currentMinute = now.getMinutes()
  
  if (selectedHour === currentHour) {
    return Array.from({ length: currentMinute + 1 }, (_, i) => i)
  }
  return []
}

// 计算预订时长（分钟）
const getDurationMinutes = () => {
  if (!reservationForm.value.startTime || !reservationForm.value.endTime) {
    return 0
  }
  
  const start = new Date(reservationForm.value.startTime)
  const end = new Date(reservationForm.value.endTime)
  const diffMs = end - start
  return Math.ceil(diffMs / (1000 * 60))
}

// 检查预订是否有效
const isReservationValid = computed(() => {
  if (!reservationForm.value.startTime || !reservationForm.value.endTime) {
    return false
  }
  
  const duration = getDurationMinutes()
  return duration > 0 && duration <= MAX_BOOKING_DURATION
})



// 生成多个使用者ID数组
const generateMultipleUsers = (count) => {
  const users = []
  for (let i = 0; i < count; i++) {
    users.push(Math.floor(Math.random() * 1000).toString().padStart(4, '0'))
  }
  return users
}

// 模拟座位数据生成
const generateSeats = () => {
  const newSeats = []
  
  for (let i = 1; i <= 50; i++) {
    const statuses = ['available', 'occupied']
    const randomStatus = statuses[Math.floor(Math.random() * statuses.length)]
    // 随机生成座位容量（2-4人）
    const capacity = Math.floor(Math.random() * 3) + 2
    // 根据状态生成当前使用人数
    let currentUsers = 0
    if (randomStatus === 'occupied') {
      currentUsers = Math.floor(Math.random() * capacity) + 1
    }
    
    // 时间相关字段
    let startTime = null
    let endTime = null
    let remainingTime = 0
    
    if (randomStatus === 'occupied') {
      // 生成随机开始时间（过去30分钟内）
      const startTimeMinutes = Math.floor(Math.random() * 30)
      startTime = new Date(Date.now() - startTimeMinutes * 60 * 1000)
      
      // 生成随机结束时间（未来30-120分钟内）
      const endTimeMinutes = Math.floor(Math.random() * 90) + 30
      endTime = new Date(Date.now() + endTimeMinutes * 60 * 1000)
      
      // 计算剩余时间（秒）
      remainingTime = Math.floor((endTime - Date.now()) / 1000)
    }
    
    newSeats.push({
      id: `seat-${currentFloor.value}-${i}`,
      number: `${currentFloor.value}${String.fromCharCode(65 + Math.floor((i - 1) / 10))}${(i % 10 || 10).toString().padStart(2, '0')}`,
      floor: currentFloor.value,
      status: randomStatus,
      capacity: capacity,
      currentUsers: currentUsers,
      startTime: startTime,
      endTime: endTime,
      remainingTime: remainingTime,
      reservedBy: randomStatus === 'occupied' ? generateMultipleUsers(currentUsers) : [],
      reservedTime: null
    })
  }
  return newSeats
}

// 切换2D/3D视图
const toggleView = async () => {
  is3DView.value = !is3DView.value
  if (is3DView.value) {
    // 等待DOM更新，确保3D容器已渲染
    await nextTick()
    // 检查容器是否存在
    if (seats3DCanvas.value) {
      // 确保3D视图容器可见
      seats3DCanvas.value.style.display = 'block'
      init3DScene()
    } else {
      console.error('3D canvas element not found')
      ElMessage.error('3D视图初始化失败：画布元素未找到')
      // 切换回2D视图
      is3DView.value = false
    }
  } else {
    cleanup3DScene()
  }
}

// 初始化3D场景
const init3DScene = async () => {
  try {
    await nextTick()
    
    if (!seats3DCanvas.value) {
      console.error('3D canvas element not found')
      ElMessage.error('3D视图初始化失败：画布元素未找到')
      return
    }
    
    // 确保容器有正确的尺寸
    seats3DCanvas.value.style.width = '100%'
    seats3DCanvas.value.style.height = '500px'
    seats3DCanvas.value.style.position = 'relative'
    
    // 清除现有场景
    cleanup3DScene()
    
    // 创建场景
    scene = new THREE.Scene()
    scene.background = new THREE.Color(0xf5f7fa)
    
    // 创建相机
    camera = new THREE.PerspectiveCamera(
      75,
      seats3DCanvas.value.clientWidth / seats3DCanvas.value.clientHeight,
      0.1,
      1000
    )
    camera.position.set(20, 15, 20)
    camera.lookAt(0, 0, 0)
    
    // 创建渲染器 - 禁用阴影映射解决modelViewMatrix错误
    renderer = new THREE.WebGLRenderer({ antialias: true })
    renderer.setSize(seats3DCanvas.value.clientWidth, seats3DCanvas.value.clientHeight)
    renderer.shadowMap.enabled = false // 禁用阴影映射
    renderer.autoClear = true // 确保自动清除
    seats3DCanvas.value.innerHTML = ''
    seats3DCanvas.value.appendChild(renderer.domElement)
    
    // 添加轨道控制器
    controls = new OrbitControls(camera, renderer.domElement)
    controls.enableDamping = true
    controls.dampingFactor = 0.05
    controls.minDistance = 10
    controls.maxDistance = 50
    controls.maxPolarAngle = Math.PI / 2
    
    // 添加光源
    const ambientLight = new THREE.AmbientLight(0xffffff, 0.8) // 增强环境光
    scene.add(ambientLight)
    
    const directionalLight = new THREE.DirectionalLight(0xffffff, 1.0) // 增强方向光
    directionalLight.position.set(10, 20, 10)
    directionalLight.castShadow = false // 禁用阴影
    scene.add(directionalLight)
    
    // 添加地面
    const floorGeometry = new THREE.PlaneGeometry(50, 50)
    const floorMaterial = new THREE.MeshStandardMaterial({ color: 0xe0e0e0 })
    const floor = new THREE.Mesh(floorGeometry, floorMaterial)
    floor.rotation.x = -Math.PI / 2
    floor.receiveShadow = false // 禁用地面接收阴影
    scene.add(floor)
    
    // 添加坐标轴辅助线（用于调试）
    const axesHelper = new THREE.AxesHelper(5)
    scene.add(axesHelper)
    
    // 渲染座位
    create3DSeats()
    
    // 开始动画循环
    animate()
    
    // 处理窗口大小变化
    window.addEventListener('resize', onWindowResize)
    
    ElMessage.success('已切换到3D视图')
  } catch (error) {
    console.error('Error initializing 3D scene:', error)
    ElMessage.error('3D视图初始化失败：' + error.message)
    // 切换回2D视图
    is3DView.value = false
  }
}

// 清理3D场景
const cleanup3DScene = () => {
  try {
    if (animationId.value) {
      cancelAnimationFrame(animationId.value)
      animationId.value = null
    }
    
    if (controls) {
      controls.dispose()
      controls = null
    }
    
    if (renderer) {
      renderer.dispose()
      if (seats3DCanvas.value && renderer.domElement) {
        seats3DCanvas.value.removeChild(renderer.domElement)
      }
      renderer = null
    }
    
    // 清理资源
    seatObjects.forEach(obj => {
      try {
        if (obj.geometry) {
          obj.geometry.dispose()
        }
        if (obj.material) {
          if (Array.isArray(obj.material)) {
            obj.material.forEach(material => material.dispose())
          } else {
            obj.material.dispose()
          }
        }
      } catch (error) {
        console.error('Error disposing seat object:', error)
      }
    })
    
    if (scene) {
      // 清理场景中的所有对象
      while (scene.children.length > 0) {
        const child = scene.children[0]
        scene.remove(child)
      }
      scene = null
    }
    
    camera = null
    seatObjects.length = 0
    
    // 移除事件监听器
    window.removeEventListener('resize', onWindowResize)
    
    ElMessage.success('已切换到2D视图')
  } catch (error) {
    console.error('Error cleaning up 3D scene:', error)
  }
}

// 刷新座位数据
const refreshSeats = () => {
  // 生成新数据并更新缓存
  const newSeats = generateSeats()
  seats.value = newSeats
  floorDataCache.value[currentFloor.value] = newSeats
  
  if (is3DView.value) {
    update3DSeats()
  }
  ElMessage.success('座位数据已刷新')
}

// 动画循环
const animate = () => {
  try {
    animationId.value = requestAnimationFrame(animate)
    
    if (controls) {
      controls.update()
    }
    
    if (renderer && scene && camera) {
      // 使用try-catch包裹渲染调用，隔离错误
      try {
        renderer.render(scene, camera)
      } catch (renderError) {
        // 忽略 Matrix4 相关的代理错误
        if (!renderError.message.includes('modelViewMatrix')) {
          console.error('Render error:', renderError)
          throw renderError
        }
      }
    }
  } catch (error) {
    console.error('Animation error:', error)
    // 切换回2D视图
    ElMessage.error('3D视图动画出错：' + error.message)
    is3DView.value = false
  }
}

// 创建3D座位
const create3DSeats = () => {
  try {
    if (!scene) {
      console.error('Scene not initialized')
      return
    }
    
    // 清空现有座位
    seatObjects.forEach(obj => {
      try {
        scene.remove(obj)
      } catch (error) {
        console.error('Error removing seat object:', error)
      }
    })
    seatObjects.length = 0
    
    // 座位尺寸和间距
    const seatSize = 1
    const seatSpacing = 1.8 // 增加座位间距
    const rows = 10
    const cols = 5
    
    // 计算起始位置，使座位居中
    const startX = -(cols * seatSpacing) / 2
    const startZ = -(rows * seatSpacing) / 2
    
    seats.value.forEach((seat, index) => {
      try {
        // 计算座位位置
        const row = Math.floor(index / cols)
        const col = index % cols
        const x = startX + col * seatSpacing
        const z = startZ + row * seatSpacing
        
        // 根据座位状态和剩余容量选择颜色
        let seatColor
        // 如果还有剩余容量，显示为绿色
        if (seat.status === 'available' || (seat.currentUsers < seat.capacity)) {
          seatColor = 0x67c23a // 绿色 - 可预订
        } else if (seat.status === 'occupied') {
          seatColor = 0xf56c6c // 红色 - 已使用
        } else {
          seatColor = 0x909399 // 灰色 - 不可用
        }
        
        // 创建座位组
        const seatGroup = new THREE.Group()
        
        // 创建座位底座
        const baseGeometry = new THREE.BoxGeometry(seatSize, 0.2, seatSize)
        const baseMaterial = new THREE.MeshStandardMaterial({ color: seatColor })
        const base = new THREE.Mesh(baseGeometry, baseMaterial)
        base.position.y = 0.1
        base.castShadow = false // 禁用阴影
        seatGroup.add(base)
        
        // 创建座位靠背
        const backGeometry = new THREE.BoxGeometry(seatSize, 1, 0.2)
        const backMaterial = new THREE.MeshStandardMaterial({ color: seatColor })
        const back = new THREE.Mesh(backGeometry, backMaterial)
        back.position.y = 0.6
        back.position.z = -0.4
        back.castShadow = false // 禁用阴影
        seatGroup.add(back)
        
        // 添加座位号
        const seatNumber = createSeatNumber(seat.number, seatColor)
        seatNumber.position.y = 1.4 // 升高座位号位置
        seatGroup.add(seatNumber)
        
        // 设置座位位置
        seatGroup.position.set(x, 0, z)
        seatGroup.userData = seat
        
        // 添加到场景和座位对象数组
        scene.add(seatGroup)
        seatObjects.push(seatGroup)
      } catch (error) {
        console.error(`Error creating seat ${seat.number}:`, error)
      }
    })
    
    console.log(`Created ${seatObjects.length} 3D seats`)
    
    // 强制渲染一次
    if (renderer && scene && camera) {
      renderer.render(scene, camera)
    }
  } catch (error) {
    console.error('Error in create3DSeats:', error)
  }
}

// 创建座位号 - 简化实现，避免 proxy 错误
const createSeatNumber = (number, color) => {
  try {
    // 创建一个简单的平面作为座位号显示
    const material = new THREE.MeshBasicMaterial({ 
      color: color === 0x67c23a ? 0x000000 : 0xffffff, 
      transparent: true, 
      opacity: 0.9
    })
    const geometry = new THREE.PlaneGeometry(0.8, 0.4)
    const mesh = new THREE.Mesh(geometry, material)
    mesh.rotation.y = Math.PI
    return mesh
  } catch (error) {
    console.error('Error creating seat number:', error)
    // 创建一个简单的平面作为 fallback
    const material = new THREE.MeshBasicMaterial({ 
      color: color, 
      transparent: true, 
      opacity: 0.8 
    })
    const geometry = new THREE.PlaneGeometry(1.2, 0.6)
    const mesh = new THREE.Mesh(geometry, material)
    mesh.rotation.y = Math.PI
    return mesh
  }
}

// 窗口大小变化处理
const onWindowResize = () => {
  if (!camera || !renderer || !seats3DCanvas.value) return
  
  camera.aspect = seats3DCanvas.value.clientWidth / seats3DCanvas.value.clientHeight
  camera.updateProjectionMatrix()
  
  renderer.setSize(seats3DCanvas.value.clientWidth, seats3DCanvas.value.clientHeight)
}

// 更新3D座位
const update3DSeats = () => {
  if (!is3DView.value) return
  
  // 重新创建座位
  create3DSeats()
}

// 座位点击事件
const handleSeatClick = (seat) => {
  selectedSeat.value = seat
  if (seat.status === 'available' || (seat.currentUsers < seat.capacity)) {
    reservationForm.value.seatNumber = seat.number

    
    // 初始化开始时间为当前时间，结束时间为当前时间+30分钟
    const now = new Date()
    const defaultStartTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours(), now.getMinutes())
    const defaultEndTime = new Date(defaultStartTime.getTime() + MAX_BOOKING_DURATION * 60 * 1000)
    
    reservationForm.value.startTime = defaultStartTime.toISOString().slice(0, 16)
    reservationForm.value.endTime = defaultEndTime.toISOString().slice(0, 16)
  }
}

// 确认预订
const confirmReservation = async () => {
  if (!selectedSeat.value) return
  
  // 表单验证
  if (reservationFormRef.value) {
    await reservationFormRef.value.validate((valid) => {
      if (!valid) {
        ElMessage.error('请填写完整的预订信息')
        return false
      }
    })
  }
  
  // 验证预订时长
  const duration = getDurationMinutes()
  if (duration <= 0) {
    ElMessage.error('结束时间必须晚于开始时间')
    return
  }
  
  if (duration > MAX_BOOKING_DURATION) {
    ElMessage.error(`预订时长不能超过${MAX_BOOKING_DURATION}分钟`)
    return
  }
  
  // 模拟预订逻辑
  const bookingTime = new Date(reservationForm.value.startTime)
  const expiryTime = new Date(reservationForm.value.endTime)
  
  currentBooking.value = {
    seatNumber: selectedSeat.value.number,
    
    bookingTime: bookingTime.toLocaleString(),
    expiryTime: expiryTime.toLocaleString()
  }
  
  // 更新座位状态
  const seatIndex = seats.value.findIndex(s => s.id === selectedSeat.value.id)
  if (seatIndex !== -1) {
    // 如果座位当前是空的，设置为reserved状态
    if (seats.value[seatIndex].status === 'available') {
      seats.value[seatIndex].status = 'reserved'
    }
    // 如果座位当前是占用状态，保持不变
    
    // 更新预订时间信息
    seats.value[seatIndex].reservedTime = bookingTime.toLocaleTimeString()
    seats.value[seatIndex].startTime = bookingTime
    seats.value[seatIndex].endTime = expiryTime
    seats.value[seatIndex].remainingTime = Math.floor((expiryTime - Date.now()) / 1000)
    seats.value[seatIndex].reservedBy = Math.floor(Math.random() * 1000).toString().padStart(4, '0')
    // 增加当前使用人数
    seats.value[seatIndex].currentUsers += 1
  }
  
  selectedSeat.value = null
  ElMessage.success('座位预订成功！')
}

// 取消预订
const cancelBooking = () => {
  if (!currentBooking.value) return
  
  // 更新座位状态
  const seatNumber = currentBooking.value.seatNumber
  const seatIndex = seats.value.findIndex(s => s.number === seatNumber)
  if (seatIndex !== -1) {
    seats.value[seatIndex].status = 'available'
    seats.value[seatIndex].reservedTime = null
    seats.value[seatIndex].reservedBy = null
  }
  
  currentBooking.value = null
  ElMessage.success('预订已取消')
}

// 导航到座位
const navigateToSeat = () => {
  if (!currentBooking.value) return
  navigationDialogVisible.value = true
}

// 开始导航
const startNavigation = () => {
  navigationDialogVisible.value = false
  ElMessage.success('导航已开始')
  // 这里可以集成实际的导航API
}

// 座位签到
const checkInSeat = () => {
  if (!selectedSeat.value || !currentBooking.value) return
  
  // 更新座位状态
  const seatIndex = seats.value.findIndex(s => s.id === selectedSeat.value.id)
  if (seatIndex !== -1) {
    seats.value[seatIndex].status = 'occupied'
    seats.value[seatIndex].startTime = new Date()
    
    // 更新3D视图
    if (is3DView.value) {
      update3DSeats()
    }
    
    ElMessage.success('座位签到成功，开始使用')
  }
}

// 格式化剩余时间为 HH:MM:SS 格式
const formatRemainingTime = (seconds) => {
  if (seconds <= 0) return '00:00:00'
  const hours = Math.floor(seconds / 3600).toString().padStart(2, '0')
  const mins = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0')
  const secs = (seconds % 60).toString().padStart(2, '0')
  return `${hours}:${mins}:${secs}`
}

// 更新座位剩余时间
const updateSeatRemainingTime = () => {
  seats.value.forEach(seat => {
    if ((seat.status === 'reserved' || seat.status === 'occupied') && seat.endTime) {
      seat.remainingTime = Math.max(0, Math.floor((seat.endTime - Date.now()) / 1000))
    }
  })
}

// 生成座位tooltip内容
const getSeatTooltipContent = (seat) => {
  if (seat.status === 'occupied') {
    return `已使用
使用开始时间：${seat.startTime?.toLocaleTimeString()}`
  }
  return ''
}

// 初始化数据
onMounted(() => {
  // 初始化时检查缓存，如果没有缓存数据则刷新
  if (!floorDataCache.value[currentFloor.value]) {
    refreshSeats()
  } else {
    // 使用缓存数据
    seats.value = floorDataCache.value[currentFloor.value]
  }
  
  // 模拟用户已有座位（用于测试"我的座位"卡片显示）
  currentBooking.value = {
    seatNumber: '1A01',
    floor: 1,
    bookingTime: new Date().toLocaleString(),
    expiryTime: new Date(Date.now() + 60 * 60 * 1000).toLocaleString() // 1小时后过期
  }
  
  // 启动时钟
  timer = setInterval(() => {
    currentTime.value = new Date()
    // 更新座位剩余时间
    updateSeatRemainingTime()
  }, 1000)
})

// 导航到点餐页面
const navigateToOrdering = (seat) => {
  if (seat) {
    // 跳转到点餐页面，并携带座位信息参数
    router.push({
      path: '/canteen/ordering',
      query: {
        seatNumber: seat.number,

        floor: seat.floor
      }
    })
  } else {
    router.push('/canteen/ordering')
  }
}

// 组件卸载时停止时钟
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.canteen-seating-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.seating-header {
  margin-bottom: 30px;
}

.seating-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.seating-status {
  display: flex;
  gap: 30px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.status-dot.available {
  background-color: #67c23a;
}

.status-dot.reserved {
  background-color: #e6a23c;
}

.status-dot.occupied {
  background-color: #f56c6c;
}

.seating-content {
  display: grid;
  grid-template-columns: 1fr 350px;
  gap: 30px;
}

.seating-map-section {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.seating-map-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.seating-map-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.map-controls {
  display: flex;
  gap: 10px;
  align-items: center;
}

.seating-map {
  position: relative;
  background-color: #f5f7fa;
  border-radius: 12px;
  padding: 40px;
  min-height: 500px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.canteen-entrance,
.canteen-counter {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  margin-bottom: 30px;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 150px;
}

.canteen-counter {
  margin-top: 30px;
  margin-bottom: 0;
}

.seats-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 20px; /* 增加座位间距 */
  max-width: 900px; /* 扩大网格宽度 */
}

.seats-3d-container {
  width: 100%;
  height: 500px;
  position: relative;
  border-radius: 8px;
  overflow: hidden;
}

.seats-3d-canvas {
  width: 100%;
  height: 100%;
  cursor: grab;
}

.seats-3d-canvas:active {
  cursor: grabbing;
}

.seat-item {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.seat-item:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.seat-available {
  background-color: #67c23a;
  color: white;
}

.seat-reserved {
  background-color: #e6a23c;
  color: white;
}

.seat-occupied {
  background-color: #f56c6c;
  color: white;
  cursor: not-allowed;
}

.seat-occupied:hover {
  transform: none;
  box-shadow: none;
}

.seat-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 100%;
  height: 100%;
  justify-content: center;
}

.seat-number {
  font-weight: bold;
  font-size: 14px;
  letter-spacing: 1px;
}

.seat-status-indicator {
  position: absolute;
  top: -6px;
  right: -6px;
  z-index: 10;
}

.status-icon-wrapper {
  width: 22px;
  height: 22px;
  background-color: #ffffff;
  border: 2px solid #f56c6c;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.status-icon-wrapper:hover {
  transform: scale(1.15);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
}

.status-icon-clock {
  color: #f56c6c;
}

.seat-capacity {
  margin-top: 2px;
}

.capacity-text {
  font-size: 12px;
  background-color: rgba(255, 255, 255, 0.3);
  padding: 2px 6px;
  border-radius: 10px;
  font-weight: bold;
}

.seat-timer {
  margin-top: 2px;
}

.timer-text {
  font-size: 11px;
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
  padding: 1px 5px;
  border-radius: 8px;
  font-weight: bold;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

.seating-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.booking-card,
.reservation-form-card,
.seat-detail-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.booking-card .el-card__header,
.reservation-form-card .el-card__header,
.seat-detail-card .el-card__header {
  border-bottom: 1px solid #ebeef5;
  padding: 15px 20px;
}

.booking-card .el-card__body,
.reservation-form-card .el-card__body,
.seat-detail-card .el-card__body {
  padding: 20px;
}

.current-booking {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.booking-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.booking-item,
.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.booking-item .label,
.detail-item .label {
  color: #606266;
  font-weight: 500;
}

.booking-item .value,
.detail-item .value {
  color: #303133;
  font-weight: bold;
}

.time-info-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
}

.users-list-section {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed #ebeef5;
}

.users-item {
  margin-bottom: 8px;
}

.timer-value {
  color: #f56c6c;
  font-size: 18px;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}

.booking-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.detail-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.no-booking {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  padding: 30px 0;
  color: #909399;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.duration-hint {
  margin-top: 8px;
  font-size: 14px;
}

.duration-error {
  color: #f56c6c;
}

.duration-hint .el-icon {
  margin-left: 5px;
  cursor: help;
}

.navigation-content {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.navigation-map {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.navigation-map h3 {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.map-placeholder {
  background-color: #f5f7fa;
  border-radius: 12px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  text-align: center;
  color: #909399;
}

.route-info {
  margin-top: 20px;
  text-align: left;
  color: #333;
}

.route-info ol {
  margin-top: 10px;
  padding-left: 20px;
}

.route-info li {
  margin-bottom: 8px;
}

.navigation-actions {
  display: flex;
  justify-content: center;
}

/* 实体闹钟样式 */
.alarm-clock-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-left: auto;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.alarm-clock {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.clock-face {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 36px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.clock-hour, .clock-minute, .clock-second {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  padding: 8px 15px;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  color: white;
  font-size: 28px;
}

.clock-separator {
  animation: blink 1s infinite;
  font-size: 28px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.clock-date {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.canteen-floor-selector {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  margin-bottom: 30px;
  padding: 15px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: default;
  transition: all 0.3s ease;
}

.canteen-floor-selector:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.floor-buttons {
  display: flex;
  gap: 10px;
}

.canteen-counter {
  cursor: pointer;
  transition: all 0.3s ease;
}

.canteen-counter:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

@media (max-width: 1200px) {
  .seating-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .seats-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 10px;
  }
  
  .seat-item {
    width: 50px;
    height: 50px;
  }
  
  .seating-map {
    padding: 20px;
  }
  
  .clock-face {
    font-size: 32px;
    gap: 5px;
  }
  
  .clock-hour, .clock-minute, .clock-second {
    padding: 8px 15px;
  }
}
</style>