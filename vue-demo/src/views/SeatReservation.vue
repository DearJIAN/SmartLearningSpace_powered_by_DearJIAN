<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import { getSeatList, reserveSeat, checkInSeat, renewSeat, checkOutSeat, resetSeats, simulateSeats } from '@/api/seat'
import { ElMessage, ElMessageBox } from 'element-plus'
import { markRaw } from 'vue'

// Import Icons
import IconClockExclamation from '~icons/tabler/clock-exclamation'
import IconUserExclamation from '~icons/tabler/user-exclamation'
import IconLockOpen from '~icons/tabler/lock-open'
import IconInbox from '~icons/tabler/inbox'
import IconBell from '~icons/tabler/bell'
import IconTrash from '~icons/tabler/trash'
import IconX from '~icons/tabler/x'
import IconInfoCircle from '~icons/tabler/info-circle'
import IconChevronDown from '~icons/tabler/chevron-down'
import IconCalendarTime from '~icons/tabler/calendar-time'
import IconCheck from '~icons/tabler/check'
import IconLogout from '~icons/tabler/logout'
import IconPlayerPlay from '~icons/tabler/player-play'
import IconRefresh from '~icons/tabler/refresh'
import IconClock from '~icons/tabler/clock'
import IconArmchair from '~icons/tabler/armchair'
import IconClick from '~icons/tabler/click'

const seats = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const currentSeat = ref(null)

// Form data
const form = ref({
  userName: '',
  reserveTime: '',
  // 预约时长：拆分为小时、分钟、秒
  usageHours: 1,
  usageMinutes: 0,
  usageSeconds: 0,
  // 自定义预约开始时间
  customStartTime: null,
  extendTime: 3600
})

let timer = null
let usageTimer = null
const currentTime = ref(new Date())

// 通知系统
const notifications = ref([])
const showNotificationPanel = ref(true)
const processedSeats = ref(new Set()) // 记录已处理的座位，避免重复通知
const selectedNotification = ref(null) // 选中的通知
const reminderTimers = ref(new Map()) // 重复提醒定时器
const autoReleaseTimers = ref(new Map()) // 自动释放定时器

// 时钟显示
const clockHours = computed(() => currentTime.value.getHours())
const clockMinutes = computed(() => currentTime.value.getMinutes())
const clockSeconds = computed(() => currentTime.value.getSeconds())
const clockDate = computed(() => {
  const d = currentTime.value
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${days[d.getDay()]}`
})
const clockTime = computed(() => {
  return `${String(clockHours.value).padStart(2, '0')}:${String(clockMinutes.value).padStart(2, '0')}:${String(clockSeconds.value).padStart(2, '0')}`
})

// 计算预约开始时间（自定义时间或当前时间 + 15分钟）
const reserveStartTime = computed(() => {
  if (form.value.customStartTime) {
    // el-time-picker 现在绑定的是 Date 对象 (默认可能带有 2000 年或其他日期)
    // 我们需要提取时间部分，并应用到“今天”
    const pickedTime = new Date(form.value.customStartTime)
    const targetTime = new Date() // 今天
    
    targetTime.setHours(pickedTime.getHours())
    targetTime.setMinutes(pickedTime.getMinutes())
    targetTime.setSeconds(pickedTime.getSeconds())
    targetTime.setMilliseconds(0)
    
    return targetTime
  }
  // 未选择时，默认为“当前时间 + 15分钟”
  const start = new Date(currentTime.value)
  start.setMinutes(start.getMinutes() + 15)
  return start
})

// 时间选择限制逻辑
const makeRange = (start, end) => {
  const result = []
  for (let i = start; i <= end; i++) {
    result.push(i)
  }
  return result
}

const disabledHours = () => {
    const now = new Date()
    const currentHour = now.getHours()
    // 允许当前小时 和 未来2小时（最多）
    // 例如 10:00 -> 允许 10, 11, 12. 
    // 也就是 disabled < currentHour || disabled > currentHour + 2
    const maxHour = currentHour + 2
    
    // 如果跨天了（比如23点，+2=25），则maxHour逻辑要处理
    // 但用户说"只可以预约当天"，所以不能超过23点。
    const actualMax = Math.min(23, maxHour)
    
    const disabled = []
    for (let i = 0; i < 24; i++) {
        if (i < currentHour || i > actualMax) {
            disabled.push(i)
        }
    }
    return disabled
}

const disabledMinutes = (hour) => {
    const now = new Date()
    const currentHour = now.getHours()
    const currentMinute = now.getMinutes()
    const maxHour = currentHour + 2
    
    // 如果选的是当前小时，分钟必须 >= 当前分钟
    if (hour === currentHour) {
        return makeRange(0, currentMinute - 1)
    }
    
    // 如果选的是最后一个允许的小时（current + 2）
    // 比如 10:30 -> max time 12:30. 
    // hour 12: minutes > 30 allowed? No, max is 12:30.
    // user said "max 2 hours later".
    if (hour === maxHour) {
        return makeRange(currentMinute + 1, 59)
    }
    
    return []
}

const disabledSeconds = (hour, minute) => {
    const now = new Date()
    const currentHour = now.getHours()
    const currentMinute = now.getMinutes()
    const currentSecond = now.getSeconds()
    const maxHour = currentHour + 2
    
    if (hour === currentHour && minute === currentMinute) {
        return makeRange(0, currentSecond - 1)
    }
    
    if (hour === maxHour && minute === currentMinute) {
         return makeRange(currentSecond + 1, 59)
    }
    
    return []
}

// 计算总使用时长（秒）
const totalUsageSeconds = computed(() => {
  return form.value.usageHours * 3600 + form.value.usageMinutes * 60 + form.value.usageSeconds
})

// 计算预约结束时间
const reserveEndTime = computed(() => {
  const end = new Date(reserveStartTime.value)
  end.setSeconds(end.getSeconds() + totalUsageSeconds.value)
  return end
})

// 计算已使用时间（秒）
const usedTime = computed(() => {
  if (!currentSeat.value || !currentSeat.value.checkInTime) return 0
  const checkIn = new Date(currentSeat.value.checkInTime)
  const now = currentTime.value
  return Math.floor((now - checkIn) / 1000)
})

// 计算剩余时间（秒）
const remainingTime = computed(() => {
  if (!currentSeat.value || !currentSeat.value.endTime) return 0
  const end = new Date(currentSeat.value.endTime)
  const now = currentTime.value
  return Math.max(0, Math.floor((end - now) / 1000))
})

// 格式化时长为可读格式
const formatDuration = (seconds) => {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  // 始终显示 小时:分钟:秒 格式，即使小时为0
  const h = hours > 0 ? `${hours}小时` : '00小时'
  const m = minutes > 0 ? `${minutes}分` : '00分'
  
  if (hours > 0) {
      return `${hours}小时${minutes}分${secs}秒`
  } else {
      // 这里的逻辑可以统一，如下：
      return `${hours > 0 ? hours : '00'}小时${minutes > 0 ? String(minutes).padStart(2, '0') : '00'}分${String(secs).padStart(2, '0')}秒`
  }
}

// 格式化时间为字符串
const formatTime = (date) => {
  if (!date) return ''
  const d = date instanceof Date ? date : new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// Fetch data
const fetchData = async () => {
  try {
    const res = await getSeatList()
    if (res.code === 200) {
      seats.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

// 添加通知
const addNotification = (notification) => {
  const id = Date.now() + Math.random()
  notifications.value.unshift({
    id,
    ...notification,
    time: new Date(),
    read: false
  })
  // 最多保留20条通知
  if (notifications.value.length > 20) {
    notifications.value = notifications.value.slice(0, 20)
  }
}

// 状态数量统计
const freeCount = computed(() => seats.value ? seats.value.filter(s => s.status === 0).length : 0)
const reservedCount = computed(() => seats.value ? seats.value.filter(s => s.status === 1).length : 0)
const occupiedCount = computed(() => seats.value ? seats.value.filter(s => s.status === 2).length : 0)

// 清除通知
const removeNotification = (id) => {
  const index = notifications.value.findIndex(n => n.id === id)
  if (index !== -1) {
    notifications.value.splice(index, 1)
  }
}

// 标记通知为已读
const markAsRead = (id) => {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    notification.read = true
  }
}

// 清空所有通知
const clearAllNotifications = () => {
  notifications.value = []
  processedSeats.value.clear()
}

// 监测座位状态变化
watch(seats, (newSeats) => {
  if (!newSeats || newSeats.length === 0) return
  
  newSeats.forEach(seat => {
    const seatKey = `${seat.id}-${seat.status}`
    
    // 检测超时（使用中且剩余时间为0）
    if (seat.status === 2 && seat.endTime) {
      const end = new Date(seat.endTime)
      const now = currentTime.value
      const remaining = Math.floor((end - now) / 1000)
      
      if (remaining <= 0 && !processedSeats.value.has(`timeout-${seat.id}`)) {
        // 初始提醒也取整到最近的10秒，保持一致性
        const rawSeconds = Math.abs(remaining)
        const overtimeSeconds = Math.round(rawSeconds / 10) * 10
        
        addNotification({
          type: 'timeout',
          icon: markRaw(IconClockExclamation),
          title: '超时提醒',
          seatCode: seat.seatCode,
          userName: seat.userName,
          message: `座位 ${seat.seatCode} 使用时间已到${overtimeSeconds > 0 ? `，已超时 ${Math.floor(overtimeSeconds / 60)}分${overtimeSeconds % 60}秒` : ''}`,
          details: `用户：${seat.userName}\n签到时间：${formatTime(seat.checkInTime)}\n结束时间：${formatTime(seat.endTime)}\n已超时：${Math.floor(overtimeSeconds / 60)}分${overtimeSeconds % 60}秒`
        })
        processedSeats.value.add(`timeout-${seat.id}`)
        // 启动重复提醒和自动释放
        startReminder(seat.id, 'timeout')
        startAutoRelease(seat.id, 'timeout')
      }
    }
    
    // 检测未守约（已预约且超过预约开始时间未签到）
    if (seat.status === 1 && seat.startTime && seat.updateTime) {
      const start = new Date(seat.startTime)
      const now = currentTime.value
      // 只要现在时间 >= 开始时间，就是未守约（因为我们去掉了15分钟缓冲）
      const overdueSeconds = Math.floor((now - start) / 1000)
      
      if (overdueSeconds >= 0 && !processedSeats.value.has(`noshow-${seat.id}`)) {
        // 初始提醒同样取整
        const displaySeconds = Math.round(overdueSeconds / 10) * 10
        
        addNotification({
          type: 'no-show',
          icon: markRaw(IconUserExclamation),
          title: '未守约提醒',
          seatCode: seat.seatCode,
          userName: seat.userName,
          message: `座位 ${seat.seatCode} 预约时间已到，未签到`,
          details: `用户：${seat.userName}\n预约开始时间：${formatTime(seat.startTime)}\n请尽快签到或取消预约`
        })
        processedSeats.value.add(`noshow-${seat.id}`)
        // 启动重复提醒和自动释放
        startReminder(seat.id, 'noshow')
        startAutoRelease(seat.id, 'noshow')
      }
    }
    
    // 如果座位变为空闲，清除对应的处理记录和定时器
    if (seat.status === 0) {
      processedSeats.value.delete(`timeout-${seat.id}`)
      processedSeats.value.delete(`noshow-${seat.id}`)
      stopReminder(seat.id, 'timeout')
      stopReminder(seat.id, 'noshow')
    }
  })
}, { deep: true })

// 未读通知数量
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// 开始重复提醒
const startReminder = (seatId, type) => {
  const key = `${type}-${seatId}`
  // 清除已存在的定时器
  if (reminderTimers.value.has(key)) {
    clearInterval(reminderTimers.value.get(key))
  }
  // 每10秒提醒一次
  const timer = setInterval(() => {
    const seat = seats.value.find(s => s.id === seatId)
    if (!seat) {
      clearInterval(timer)
      reminderTimers.value.delete(key)
      return
    }
    
    if (type === 'timeout' && seat.status === 2) {
      const end = new Date(seat.endTime)
      const now = currentTime.value
      const remaining = Math.floor((end - now) / 1000)
      if (remaining <= 0) {
        // 取整到最近的10秒，避免因为定时器执行延迟导致出现 21秒、31秒 这种非整十数
        const rawSeconds = Math.abs(remaining)
        const overtimeSeconds = Math.round(rawSeconds / 10) * 10
        
        addNotification({
          type: 'timeout',
          icon: markRaw(IconClockExclamation),
          title: '超时提醒（重复）',
          seatCode: seat.seatCode,
          userName: seat.userName,
          message: `座位 ${seat.seatCode} 已超时 ${Math.floor(overtimeSeconds / 60)}分${overtimeSeconds % 60}秒，请尽快签退`,
          details: `用户：${seat.userName}\n签到时间：${formatTime(seat.checkInTime)}\n结束时间：${formatTime(seat.endTime)}\n已超时：${Math.floor(overtimeSeconds / 60)}分${overtimeSeconds % 60}秒`
        })
      }
    } else if (type === 'noshow' && seat.status === 1) {
      const start = new Date(seat.startTime)
      const now = currentTime.value
      const overdueSeconds = Math.floor((now - start) / 1000)
      
      if (overdueSeconds >= 0) {
        // 同样取整到最近的10秒
        const displaySeconds = Math.round(overdueSeconds / 10) * 10
        
        addNotification({
          type: 'no-show',
          icon: markRaw(IconUserExclamation),
          title: '未守约提醒（重复）',
          seatCode: seat.seatCode,
          userName: seat.userName,
          message: `座位 ${seat.seatCode} 已到预约时间，请尽快签到`,
          details: `用户：${seat.userName}\n预约开始时间：${formatTime(seat.startTime)}\n已逾期：${Math.floor(displaySeconds / 60)}分${displaySeconds % 60}秒`
        })
      }
    }
  }, 10000) // 10秒
  
  reminderTimers.value.set(key, timer)
}

// 开始自动释放倒计时
const startAutoRelease = (seatId, type) => {
  const key = `${type}-${seatId}`
  // 清除已存在的定时器
  if (autoReleaseTimers.value.has(key)) {
    clearTimeout(autoReleaseTimers.value.get(key))
  }
  // 30秒后自动释放
  const timer = setTimeout(async () => {
    const seat = seats.value.find(s => s.id === seatId)
    if (!seat) return
    
    try {
      // 调用签退接口释放座位
      await checkOutSeat({ seatId: seat.id })
      // 移除弹窗，只在通知中心显示
      // ElMessage.warning(`座位 ${seat.seatCode} 已自动释放`)
      addNotification({
        type: 'auto-release',
        icon: markRaw(IconLockOpen),
        title: '自动释放',
        seatCode: seat.seatCode,
        userName: seat.userName,
        message: `座位 ${seat.seatCode} 因${type === 'timeout' ? '超时' : '未守约'}已自动释放`,
        details: `用户：${seat.userName}\n座位：${seat.seatCode}\n原因：${type === 'timeout' ? '使用超时30秒' : '预约后30秒未签到'}\n释放时间：${formatTime(new Date())}`
      })
      fetchData()
      // 清除定时器
      stopReminder(seatId, type)
    } catch (e) {
      console.error('自动释放失败', e)
    }
    
    autoReleaseTimers.value.delete(key)
  }, 30000) // 30秒
  
  autoReleaseTimers.value.set(key, timer)
}

// 停止提醒和自动释放
const stopReminder = (seatId, type) => {
  const key = `${type}-${seatId}`
  if (reminderTimers.value.has(key)) {
    clearInterval(reminderTimers.value.get(key))
    reminderTimers.value.delete(key)
  }
  if (autoReleaseTimers.value.has(key)) {
    clearTimeout(autoReleaseTimers.value.get(key))
    autoReleaseTimers.value.delete(key)
  }
}

// Interactions
const handleSeatClick = (seat) => {
  currentSeat.value = seat
  // Reset form
  form.value = {
    userName: seat.userName || '',
    reserveTime: new Date().toLocaleString(),
    usageHours: 1,
    usageMinutes: 0,
    usageSeconds: 0,
    customStartTime: null,
    extendTime: 3600
  }
  dialogVisible.value = true
}

const handleReserve = async () => {
  if (!form.value.userName) {
    ElMessage.warning('请输入您的姓名')
    return
  }
  if (totalUsageSeconds.value <= 0) {
    ElMessage.warning('请设置有效的使用时长')
    return
  }
  try {
    const res = await reserveSeat({
      seatId: currentSeat.value.id,
      userName: form.value.userName,
      usageTimeSec: totalUsageSeconds.value,
      startTime: formatTime(reserveStartTime.value), // 传递格式化后的开始时间
      endTime: formatTime(reserveEndTime.value)
    })
    if (res.code === 200) {
      ElMessage.success('预约成功')
      
      // 立即更新本地数据，确保显示一致
      const seatIndex = seats.value.findIndex(s => s.id === currentSeat.value.id)
      if (seatIndex !== -1) {
        seats.value[seatIndex].status = 1
        seats.value[seatIndex].userName = form.value.userName
        seats.value[seatIndex].startTime = formatTime(reserveStartTime.value)
        seats.value[seatIndex].endTime = formatTime(reserveEndTime.value)
      }
      
      dialogVisible.value = false
      // fetchData() // 依然重新获取，但本地先行更新
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleCheckIn = async () => {
  try {
    const res = await checkInSeat({ seatId: currentSeat.value.id })
    if (res.code === 200) {
      ElMessage.success('签到成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleRenew = async () => {
    try {
    const res = await renewSeat({ 
        seatId: currentSeat.value.id,
        extendedTimeSec: form.value.extendTime
    })
    if (res.code === 200) {
      ElMessage.success('续约成功')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const handleCheckOut = async () => {
    try {
    await ElMessageBox.confirm('确定要结束使用并释放该座位吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const res = await checkOutSeat({ seatId: currentSeat.value.id })
    if (res.code === 200) {
      ElMessage.success('已签退')
      dialogVisible.value = false
      fetchData()
    } else {
      ElMessage.error(res.msg)
    }
  } catch (e) {
    // Cancelled
  }
}

const handleReset = async () => {
    await ElMessageBox.confirm('确定要重置所有座位状态吗?', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await resetSeats()
    ElMessage.success('已重置')
    fetchData()
}

const handleSimulate = async () => {
    await simulateSeats()
    ElMessage.success('模拟数据生成完毕')
    fetchData()
}

const getStatusClass = (status) => {
    switch(status) {
        case 0: return 'status-free'
        case 1: return 'status-reserved'
        case 2: return 'status-occupied'
        default: return ''
    }
}

onMounted(() => {
  fetchData()
  timer = setInterval(fetchData, 2000)
  // 每秒更新当前时间，用于实时计算已使用时间
  usageTimer = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (usageTimer) clearInterval(usageTimer)
  // 清理所有提醒定时器
  reminderTimers.value.forEach(timer => clearInterval(timer))
  reminderTimers.value.clear()
  // 清理所有自动释放定时器
  autoReleaseTimers.value.forEach(timer => clearTimeout(timer))
  autoReleaseTimers.value.clear()
})

</script>

<template>
  <div class="reservation-container">
    <!-- Header -->
    <div class="header-card">
        <div class="header-info">
            <h1 class="page-title">图书馆座位预约</h1>
            <p class="page-subtitle">Library Seat Reservation System · 实时状态监控</p>
        </div>
        <div class="header-actions">
            <div class="status-legend">
                <div class="legend-item">
                    <span class="dot dot-free"></span> 空闲 ({{ freeCount }})
                </div>
                <div class="legend-item">
                    <span class="dot dot-reserved"></span> 已预约 ({{ reservedCount }})
                </div>
                <div class="legend-item">
                    <span class="dot dot-occupied"></span> 使用中 ({{ occupiedCount }})
                </div>
            </div>

            <el-button type="primary" plain @click="handleSimulate">
                <i-tabler-player-play class="icon-left"/> 模拟场景
            </el-button>
            <el-button type="danger" plain @click="handleReset">
                <i-tabler-refresh class="icon-left"/> 重置系统
            </el-button>
        </div>
    </div>

    <!-- 时钟和通知中心容器 -->
    <div class="top-widgets">
        <!-- 实时时钟 -->
        <div class="clock-widget">
            <div class="widget-header">
                <i-tabler-clock class="widget-icon"/>
                <span>实时时钟</span>
            </div>
            <div class="analog-clock">
                <div class="clock-face">
                    <div class="clock-center"></div>
                    <div class="clock-hand hour-hand" :style="{ transform: `rotate(${(clockHours % 12) * 30 + clockMinutes * 0.5}deg)` }"></div>
                    <div class="clock-hand minute-hand" :style="{ transform: `rotate(${clockMinutes * 6}deg)` }"></div>
                    <div class="clock-hand second-hand" :style="{ transform: `rotate(${clockSeconds * 6}deg)` }"></div>
                    <div v-for="i in 12" :key="i" class="clock-mark" :style="{ transform: `rotate(${i * 30}deg)` }">
                        <div class="mark-line"></div>
                    </div>
                </div>
            </div>
            <div class="digital-clock">{{ clockTime }}</div>
            <div class="clock-date">{{ clockDate }}</div>
        </div>

        <!-- 通知中心 -->
        <div class="notification-widget">
            <div class="widget-header clickable" @click="showNotificationPanel = !showNotificationPanel">
                <div class="notification-title">
                    <i-tabler-bell class="widget-icon" :class="{ 'ringing': unreadCount > 0 }"/>
                    <span>通知中心</span>
                    <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
                </div>
                <i-tabler-chevron-down class="toggle-icon" :class="{ 'rotated': !showNotificationPanel }"/>
            </div>
            
            <transition name="slide-down">
                <div v-show="showNotificationPanel" class="notification-list">
                    <div v-if="notifications.length === 0" class="notification-empty">
                        <i-tabler-inbox class="empty-icon"/>
                        <p>暂无通知</p>
                    </div>
                    
                    <template v-else>

                        
                        <!-- 左侧：通知列表 -->
                        <div class="notification-list-panel">
                            <div 
                                v-for="notification in notifications" 
                                :key="notification.id"
                                class="notification-item"
                                :class="{ 
                                    'unread': !notification.read,
                                    'timeout': notification.type === 'timeout',
                                    'no-show': notification.type === 'no-show',
                                    'auto-release': notification.type === 'auto-release',
                                    'selected': selectedNotification?.id === notification.id
                                }"
                                @click="selectedNotification = notification; markAsRead(notification.id)"
                            >
                                <div class="notification-icon-wrapper">
                                    <component :is="notification.icon" class="notification-type-icon"/>
                                </div>
                                <div class="notification-content">
                                    <div class="notification-item-title">{{ notification.title }}</div>
                                    <div class="notification-message">{{ notification.message }}</div>
                                </div>
                                <el-button 
                                    size="small" 
                                    text 
                                    type="danger" 
                                    @click.stop="removeNotification(notification.id)"
                                    class="notification-delete-btn"
                                >
                                    <i-tabler-x />
                                </el-button>
                            </div>
                        </div>
                        
                        <!-- 右侧：通知详情 -->
                        <div class="notification-detail-panel">
                            <div class="notification-detail-header">
                                <h3 v-if="selectedNotification">通知详情</h3>
                                <h3 v-else>选择通知查看详情</h3>
                                <el-button size="small" text @click="clearAllNotifications">
                                    <i-tabler-trash class="icon-left"/> 清空全部
                                </el-button>
                            </div>
                            
                            <div v-if="selectedNotification" class="notification-detail">
                                <div class="detail-icon-wrapper" :class="selectedNotification.type">
                                    <component :is="selectedNotification.icon" class="detail-icon"/>
                                </div>
                                <h4>{{ selectedNotification.title }}</h4>
                                <div class="detail-content">
                                    <pre>{{ selectedNotification.details }}</pre>
                                </div>
                            </div>
                            <div v-else class="notification-detail-empty">
                                <i-tabler-click class="empty-icon"/>
                                <p>点击左侧通知查看详情</p>
                            </div>
                        </div>
                    </template>
                </div>
            </transition>
        </div>
    </div>

    <!-- Seat Grid Container -->
    <div class="grid-wrapper">
        <div class="grid-container">
            <div 
                v-for="seat in seats" 
                :key="seat.id"
                @click="handleSeatClick(seat)"
                class="seat-item"
                :class="getStatusClass(seat.status)"
            >   
                <!-- Status Dot (Pulse) -->
                 <div v-if="seat.status === 2" class="pulse-dot dot-occupied"></div>
                 <div v-if="seat.status === 1" class="pulse-dot dot-reserved"></div>

                <i-tabler-armchair class="seat-icon" />
                <span class="seat-code">{{ seat.seatCode }}</span>
                
                <!-- Tooltip info on hover -->
                <div class="seat-tooltip">
                    {{ seat.status === 0 ? '可预约' : (seat.userName || '未知用户') }}
                </div>
            </div>
        </div>
    </div>

    <!-- Interaction Modal -->
    <el-dialog
        v-model="dialogVisible"
        :title="`座位详情: ${currentSeat?.seatCode}`"
        width="400px"
        class="glass-dialog"
        destroy-on-close
    >
        <div v-if="currentSeat" class="dialog-content">
            <!-- Status Header -->
            <div class="status-banner" :class="getStatusClass(currentSeat.status)">
                <div class="status-text">
                    {{ currentSeat.status === 0 ? '空闲' : (currentSeat.status === 1 ? '已预约' : '使用中') }}
                </div>
                <div class="user-info">
                    <span v-if="currentSeat.status !== 0">用户: {{ currentSeat.userName }}</span>
                    <span v-if="currentSeat.status === 1 && (new Date(currentSeat.startTime) > currentTime)">请尽快签到</span>
                    <span v-if="currentSeat.status === 1 && (new Date(currentSeat.startTime) <= currentTime)" style="color: #ff4d4f; font-weight: bold;">
                        <i-tabler-alert-circle style="vertical-align: middle; margin-right: 4px;"/>
                        未守约 (即将释放)
                    </span>
                    <span v-if="currentSeat.status === 2">正在学习中...</span>
                </div>
            </div>

            <!-- Action Area -->
            
            <!-- 1. Reserve (If Empty) -->
            <template v-if="currentSeat.status === 0">
                <el-form label-position="top">
                    <el-form-item label="使用人姓名">
                        <el-input v-model="form.userName" placeholder="请输入姓名" />
                    </el-form-item>
                    <el-form-item label="预约开始时间">
                        <el-time-picker
                            v-model="form.customStartTime"
                            placeholder="默认为当前时间后15分钟"
                            :disabled-hours="disabledHours"
                            :disabled-minutes="disabledMinutes"
                            :disabled-seconds="disabledSeconds"
                            style="width: 100%"
                        />
                        <div class="time-hint">
                            仅限预约今日，2小时内入座
                        </div>
                    </el-form-item>
                    <el-form-item label="预计使用时长">
                        <div class="duration-input-group">
                            <div class="duration-input-item">
                                <el-input-number 
                                    v-model="form.usageHours" 
                                    :min="0" 
                                    :max="24"
                                    controls-position="right"
                                    size="default"
                                />
                                <span class="duration-unit">小时</span>
                            </div>
                            <div class="duration-input-item">
                                <el-input-number 
                                    v-model="form.usageMinutes" 
                                    :min="0" 
                                    :max="59"
                                    controls-position="right"
                                    size="default"
                                />
                                <span class="duration-unit">分钟</span>
                            </div>
                            <div class="duration-input-item">
                                <el-input-number 
                                    v-model="form.usageSeconds" 
                                    :min="0" 
                                    :max="59"
                                    controls-position="right"
                                    size="default"
                                />
                                <span class="duration-unit">秒</span>
                            </div>
                        </div>
                    </el-form-item>
                    
                    <!-- 预约时间信息 -->
                    <div class="reserve-time-info">
                        <div class="time-row">
                            <span class="time-label">预约开始时间:</span>
                            <span class="time-value">{{ formatTime(reserveStartTime) }}</span>
                        </div>
                        <div class="time-row">
                            <span class="time-label">预计结束时间:</span>
                            <span class="time-value">{{ formatTime(reserveEndTime) }}</span>
                        </div>
                        <div class="time-row">
                            <span class="time-label">使用时长:</span>
                            <span class="time-value highlight">{{ formatDuration(totalUsageSeconds) }}</span>
                        </div>
                    </div>
                    
                    <el-button type="primary" class="action-button" @click="handleReserve" size="large">
                        <i-tabler-calendar-time class="icon-left"/> 立即预约
                    </el-button>
                </el-form>
            </template>

            <!-- 2. Check In (If Reserved) -->
            <template v-if="currentSeat.status === 1">
                <div class="action-prompt">
                    <div class="reserve-info-card">
                        <div class="info-row">
                            <span class="label">预约开始时间:</span>
                            <span class="value">{{ formatTime(currentSeat.startTime) || '未记录' }}</span>
                        </div>
                        <!-- <div class="info-row">
                            <span class="label">签到截止时间:</span>
                            <span class="value">{{ currentSeat.startTime ? formatTime(new Date(new Date(currentSeat.startTime).getTime() + 15 * 60000)) : '未记录' }}</span>
                        </div> -->
                        <div class="info-row highlight-row" v-if="new Date(currentSeat.startTime) > currentTime">
                            <span class="label">剩余签到时间:</span>
                            <span class="value highlight">
                                {{ formatDuration(Math.max(0, Math.floor((new Date(currentSeat.startTime).getTime() - currentTime) / 1000))) }}
                            </span>
                        </div>
                        <div class="info-row warning-row" v-else>
                            <span class="label">状态:</span>
                            <span class="value warning">
                                <i-tabler-alert-circle style="vertical-align: middle; margin-right: 4px;"/>
                                已未守约 (即将释放)
                            </span>
                        </div>
                    </div>

                    <p style="margin-top: 16px;">请即刻签到以保留您的座位。</p>
                    <div class="button-row">
                        <el-button type="success" @click="handleCheckIn" size="large" style="flex: 1">
                            <i-tabler-check class="icon-left"/> 签到入座
                        </el-button>
                        <el-button type="danger" plain @click="handleCheckOut" size="large" style="flex: 1">
                            <i-tabler-logout class="icon-left"/> 取消预约
                        </el-button>
                    </div>
                </div>
            </template>

            <!-- 3. Renew / Leave (If Occupied) -->
            <template v-if="currentSeat.status === 2">
                <el-form label-position="top">
                    <div class="usage-stats">
                        <div class="stat-row">
                            <span>签到时间:</span>
                            <span class="stat-value">{{ formatTime(currentSeat.checkInTime) }}</span>
                        </div>
                        <div class="stat-row">
                            <span>结束时间:</span>
                            <span class="stat-value">{{ formatTime(currentSeat.endTime) }}</span>
                        </div>
                        <div class="stat-row highlight-row">
                            <span>已使用时间:</span>
                            <span class="stat-value highlight">{{ formatDuration(usedTime) }}</span>
                        </div>
                        <div class="stat-row" :class="{ 'warning-row': remainingTime <= 300 }">
                            <span>剩余时间:</span>
                            <span class="stat-value" :class="{ 'warning': remainingTime <= 300 }">
                                {{ remainingTime > 0 ? formatDuration(remainingTime) : '已超时' }}
                            </span>
                        </div>
                    </div>

                    <el-form-item label="续约时长">
                        <div class="renew-row">
                            <el-select v-model="form.extendTime" style="width: 200px">
                                <el-option label="30分钟 (1800秒)" :value="1800" />
                                <el-option label="1小时 (3600秒)" :value="3600" />
                                <el-option label="2小时 (7200秒)" :value="7200" />
                            </el-select>
                            <el-button type="primary" plain @click="handleRenew">
                                续约
                            </el-button>
                        </div>
                    </el-form-item>
                    
                    <el-divider />
                    
                    <el-button type="danger" class="action-button" @click="handleCheckOut" size="large">
                        <i-tabler-logout class="icon-left"/> 签退 / 释放座位
                    </el-button>
                </el-form>
            </template>
        </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.reservation-container {
    padding: 24px;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 24px;
    background-color: #f0f2f5;
}

.header-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    padding: 20px 24px;
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.page-title {
    margin: 0;
    font-size: 24px;
    font-weight: bold;
    background: linear-gradient(135deg, #409eff 0%, #0072ff 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
}

.page-subtitle {
    margin: 4px 0 0 0;
    color: #909399;
    font-size: 14px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.status-legend {
    display: flex;
    gap: 16px;
    margin-right: 24px;
    background: rgba(0, 0, 0, 0.03);
    padding: 8px 16px;
    border-radius: 8px;
    font-size: 14px;
    color: #606266;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.legend-item {
    display: flex;
    align-items: center;
    gap: 8px;
}

.dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
}

.dot-free { background-color: #67c23a; }
.dot-reserved { background-color: #e6a23c; }
.dot-occupied { background-color: #f56c6c; }

.icon-left {
    margin-right: 6px;
}

/* 时钟和通知中心容器 */
.top-widgets {
    display: grid;
    grid-template-columns: 300px 1fr;
    gap: 24px;
    margin-bottom: 8px;
}

/* 时钟组件样式 */
.clock-widget {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    width: 300px;
}

.notification-widget {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 16px;
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    display: flex;
    flex-direction: column;
    height: 450px;
}

.widget-header {
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
}

.widget-header.clickable {
    cursor: pointer;
    transition: background 0.2s;
}

.widget-header.clickable:hover {
    background: rgba(0, 0, 0, 0.02);
}

.widget-icon {
    font-size: 20px;
    color: #409eff;
}

/* 模拟时钟 */
.analog-clock {
    padding: 30px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.clock-face {
    width: 150px;
    height: 150px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    position: relative;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3), inset 0 2px 4px rgba(255, 255, 255, 0.2);
}

.clock-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 12px;
    height: 12px;
    background: #fff;
    border-radius: 50%;
    z-index: 10;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.clock-hand {
    position: absolute;
    bottom: 50%;
    left: 50%;
    transform-origin: bottom center;
    background: #fff;
    border-radius: 4px 4px 0 0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.hour-hand {
    width: 6px;
    height: 40px;
    margin-left: -3px;
    opacity: 0.9;
}

.minute-hand {
    width: 4px;
    height: 55px;
    margin-left: -2px;
    opacity: 0.95;
}

.second-hand {
    width: 2px;
    height: 60px;
    margin-left: -1px;
    background: #ff6b6b;
    transition: transform 0.1s cubic-bezier(0.4, 0, 0.2, 1);
}

.clock-mark {
    position: absolute;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
}

.mark-line {
    position: absolute;
    top: 5px;
    left: 50%;
    transform: translateX(-50%);
    width: 2px;
    height: 10px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 2px;
}

/* 数字时钟 */
.digital-clock {
    text-align: center;
    font-size: 32px;
    font-weight: bold;
    font-family: 'Courier New', monospace;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    background-clip: text;
    -webkit-text-fill-color: transparent;
    padding: 8px 0;
}

.clock-date {
    text-align: center;
    font-size: 14px;
    color: #606266;
    padding-bottom: 20px;
}

/* 通知中心样式 */
.notification-title {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
}

.notification-badge {
    background: linear-gradient(135deg, #f56c6c 0%, #ff4757 100%);
    color: #fff;
    font-size: 12px;
    padding: 2px 8px;
    border-radius: 10px;
    font-weight: bold;
    animation: pulse-badge 1.5s infinite;
}

@keyframes pulse-badge {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
}

.widget-icon.ringing {
    animation: ring 1s infinite;
}

@keyframes ring {
    0%, 100% { transform: rotate(0deg); }
    10%, 30% { transform: rotate(-10deg); }
    20%, 40% { transform: rotate(10deg); }
}

.toggle-icon {
    transition: transform 0.3s;
}

.toggle-icon.rotated {
    transform: rotate(-90deg);
}

.notification-list {
    flex: 1;
    display: flex;
    overflow: hidden;
}

.notification-list-panel {
    width: 350px;
    border-right: 1px solid rgba(0, 0, 0, 0.05);
    overflow-y: auto;
    flex-shrink: 0;
}

.notification-detail-panel {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: rgba(0, 0, 0, 0.01);
}

.notification-empty {
    position: absolute; /* 使用绝对定位强制居中 */
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
}

.empty-icon {
    font-size: 48px;
    opacity: 0.3;
    margin-bottom: 12px;
}

.notification-actions {
    padding: 12px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    justify-content: flex-end;
}

.notification-item {
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    display: flex;
    gap: 12px;
    cursor: pointer;
    transition: all 0.3s;
}

.notification-item:hover {
    background: rgba(0, 0, 0, 0.02);
}

.notification-item.selected {
    background: rgba(64, 158, 255, 0.1);
    border-left-width: 4px;
}

.notification-delete-btn {
    opacity: 0;
    transition: opacity 0.2s;
}

.notification-item:hover .notification-delete-btn {
    opacity: 1;
}

.notification-item.unread {
    background: rgba(64, 158, 255, 0.05);
    border-left: 3px solid #409eff;
}

.notification-item.timeout {
    border-left-color: #f56c6c;
}

.notification-item.no-show {
    border-left-color: #e6a23c;
}

.notification-icon-wrapper {
    flex-shrink: 0;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.notification-item.timeout .notification-icon-wrapper {
    background: linear-gradient(135deg, #ff6b6b 0%, #f56c6c 100%);
}

.notification-item.no-show .notification-icon-wrapper {
    background: linear-gradient(135deg, #ffa502 0%, #e6a23c 100%);
}

.notification-item.auto-release .notification-icon-wrapper {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.notification-type-icon {
    font-size: 20px;
    color: #fff;
}

.notification-content {
    flex: 1;
    min-width: 0;
}

.notification-item-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
}

.notification-item-title {
    font-weight: 600;
    color: #303133;
    font-size: 14px;
}

.notification-message {
    color: #606266;
    font-size: 13px;
    margin-bottom: 8px;
    line-height: 1.5;
}

.notification-footer {
    display: flex;
    gap: 8px;
}

/* 通知详情面板 */
.notification-detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.notification-detail-header h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.notification-detail {
    padding: 20px;
    text-align: center;
}

.detail-icon-wrapper {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
}

.detail-icon-wrapper.timeout {
    background: linear-gradient(135deg, #ff6b6b 0%, #f56c6c 100%);
}

.detail-icon-wrapper.no-show {
    background: linear-gradient(135deg, #ffa502 0%, #e6a23c 100%);
}

.detail-icon-wrapper.auto-release {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
}

.detail-icon {
    font-size: 30px;
    color: #fff;
}

.notification-detail h4 {
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #303133;
}

.detail-content pre {
    white-space: pre-wrap;
    word-wrap: break-word;
    font-family: inherit;
    line-height: 1.8;
    color: #606266;
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    text-align: left;
}

.notification-detail-empty {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #909399;
}

.time-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
}

/* 动画 */
.slide-down-enter-active, .slide-down-leave-active {
    transition: all 0.3s ease;
}

.slide-down-enter-from, .slide-down-leave-to {
    max-height: 0;
    opacity: 0;
}

.notification-item-enter-active {
    animation: slideIn 0.3s;
}

.notification-item-leave-active {
    animation: slideOut 0.3s;
}

@keyframes slideIn {
    from {
        transform: translateX(-20px);
        opacity: 0;
    }
    to {
        transform: translateX(0);
        opacity: 1;
    }
}

@keyframes slideOut {
    from {
        transform: translateX(0);
        opacity: 1;
    }
    to {
        transform: translateX(20px);
        opacity: 0;
    }
}

/* Grid Styles */
.grid-wrapper {
    /* flex: 1; */
    /* overflow: hidden; */
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(5px);
    border-radius: 20px;
    border: 1px solid rgba(255, 255, 255, 0.5);
    padding: 50px 24px 24px 24px;
    display: flex;
    flex-direction: column;
}

.grid-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
    gap: 16px;
    /* 移除内部滚动，让整体页面滚动 */
    /* overflow-y: auto; */
    /* flex: 1; */
    padding-bottom: 20px;
}

.seat-item {
    aspect-ratio: 1;
    border-radius: 12px;
    border: 1px solid #dcdfe6;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    background: #fff;
}

.seat-item:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.seat-icon {
    font-size: 32px;
    margin-bottom: 4px;
    opacity: 0.7;
}

.seat-code {
    font-size: 12px;
    font-weight: bold;
    font-family: monospace;
}

/* Status variants */
.status-free {
    background-color: #f0f9eb;
    color: #67c23a;
    border-color: #e1f3d8;
}
.status-free:hover { background-color: #e1f3d8; }

.status-reserved {
    background-color: #fdf6ec;
    color: #e6a23c;
    border-color: #faecd8;
}
.status-reserved:hover { background-color: #faecd8; }

.status-occupied {
    background-color: #fef0f0;
    color: #f56c6c;
    border-color: #fde2e2;
}
.status-occupied:hover { background-color: #fde2e2; }

/* Pulse dot for active states */
.pulse-dot {
    position: absolute;
    top: 8px;
    right: 8px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    animation: pulse 1.5s infinite;
}

@keyframes pulse {
    0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.2); }
    70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(0, 0, 0, 0); }
    100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0, 0, 0, 0); }
}

.pulse-dot.dot-reserved { box-shadow: 0 0 0 0 rgba(230, 162, 60, 0.4); }
.pulse-dot.dot-occupied { box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }

.seat-tooltip {
    position: absolute;
    top: -35px;
    left: 50%;
    transform: translateX(-50%);
    background: #303133;
    color: #fff;
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 10px;
    white-space: nowrap;
    opacity: 0;
    transition: opacity 0.2s;
    pointer-events: none;
    z-index: 100;
}

.seat-tooltip::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border: 5px solid transparent;
    border-top-color: #303133;
}

.seat-item:hover .seat-tooltip {
    opacity: 1;
}

/* Dialog Styles */
.dialog-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.status-banner {
    padding: 16px;
    border-radius: 12px;
}

.status-text {
    font-size: 24px;
    font-weight: bold;
}

.user-info {
    font-size: 14px;
    opacity: 0.8;
    display: flex;
    flex-direction: column;
    margin-top: 4px;
}

.action-button {
    width: 100%;
}

.action-prompt {
    text-align: center;
    padding: 16px 0;
}

.action-prompt p {
    margin-bottom: 20px;
    color: #606266;
}

.button-row {
    display: flex;
    gap: 12px;
}

.usage-stats {
    background: #f5f7fa;
    padding: 16px;
    border-radius: 8px;
    margin-bottom: 16px;
}

.stat-row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    color: #606266;
    margin-bottom: 8px;
}

.stat-row:last-child { margin-bottom: 0; }

.stat-value {
    color: #303133;
    font-family: monospace;
    font-weight: bold;
}

.stat-value.highlight {
    color: #409eff;
    font-size: 16px;
}

.stat-value.warning {
    color: #f56c6c;
    font-size: 16px;
    font-weight: bold;
}

.highlight-row {
    background: #ecf5ff;
    padding: 8px;
    margin: 0 -8px;
    border-radius: 4px;
}

.warning-row {
    background: #fef0f0;
    padding: 8px;
    margin: 0 -8px;
    border-radius: 4px;
}

/* 预约时间信息样式 */
.reserve-time-info {
    background: #f0f9ff;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 16px;
    border-left: 3px solid #409eff;
}

.time-row {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 6px;
}

.time-row:last-child {
    margin-bottom: 0;
}

.time-label {
    color: #606266;
}

.time-value {
    color: #303133;
    font-family: monospace;
    font-weight: 500;
}

.time-value.highlight {
    color: #409eff;
    font-weight: bold;
}

.renew-row {
    display: flex;
    gap: 12px;
}

/* 时长输入框样式 */
.duration-input-group {
    display: flex;
    gap: 12px;
    align-items: center;
}

.duration-input-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

.duration-input-item :deep(.el-input-number) {
    width: 80px;
}

.duration-unit {
    color: #606266;
    font-size: 14px;
    white-space: nowrap;
}

:deep(.glass-dialog) {
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px);
    border-radius: 20px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.5);
}

:deep(.el-dialog__header) {
    border-bottom: 1px solid #f0f0f0;
    margin-right: 0;
}
</style>
