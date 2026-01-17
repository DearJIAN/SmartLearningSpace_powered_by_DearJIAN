<template>
  <div class="calendar-page">
    <el-card>
      <template #header>
        <el-row :gutter="20" align="middle">
          <el-col :span="14" class="header-left">
            <h3><i class="fas fa-calendar-alt"></i> 账单日历</h3>
            <el-date-picker
              v-model="currentDate"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              :clearable="false"
              style="width: 140px; margin-left: 20px;"
            />
          </el-col>
          <el-col :span="10" style="text-align: right">
            <el-radio-group v-model="filterType" size="small" @change="fetchEvents">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="1">收入</el-radio-button>
              <el-radio-button :label="2">支出</el-radio-button>
            </el-radio-group>
          </el-col>
        </el-row>
      </template>

      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-day-content" @click="handleDayClick(data.day)">
            <div class="day-number">{{ data.day.split('-').slice(-1)[0] }}</div>
            <div class="events-container">
              <div
                v-for="(event, index) in getEventsForDay(data.day)"
                :key="index"
                class="event-tag"
                :style="{ backgroundColor: event.color }"
                :title="event.title"
              >
                {{ event.title }}
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="dialogVisible" :title="selectedDay + ' 账单列表'" width="400px">
      <el-table :data="selectedDayEvents" size="small">
        <el-table-column prop="extendedProps.category" label="分类" width="100" />
        <el-table-column prop="extendedProps.amount" label="金额" width="100">
           <template #default="{row}">
             <span :style="{color: row.color}">{{ row.extendedProps.amount }}</span>
           </template>
        </el-table-column>
        <el-table-column prop="extendedProps.remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getCalendarData } from '@/api/accounting'

const currentDate = ref(new Date())
const events = ref([])
const filterType = ref(null)

const dialogVisible = ref(false)
const selectedDay = ref('')
const selectedDayEvents = ref([])

onMounted(() => {
  fetchEvents()
})

const fetchEvents = async () => {
  try {
    // 获取当前月份范围
    const year = currentDate.value.getFullYear()
    const month = currentDate.value.getMonth()
    const start = new Date(year, month - 1, 1).toISOString().split('T')[0]
    const end = new Date(year, month + 2, 0).toISOString().split('T')[0]

    const params = { start, end }
    if (filterType.value) params.type = filterType.value

    const res = await getCalendarData(params)
    if (res.data) {
      events.value = res.data
    }
  } catch (error) {
    // Error handled by request.js
  }
}

// 监听日期变化（切换月份）
watch(currentDate, () => {
  fetchEvents()
})

const getEventsForDay = (day) => {
  return events.value.filter(e => e.start === day).slice(0, 3) // 最多显示 3 条
}

const handleDayClick = (day) => {
  selectedDay.value = day
  selectedDayEvents.value = events.value.filter(e => e.start === day)
  if (selectedDayEvents.value.length > 0) {
    dialogVisible.value = true
  }
}
</script>

<style scoped>
.calendar-page {
  padding: 20px;
}
.header-left {
  display: flex;
  align-items: center;
}
.calendar-day-content {
  height: 100%;
  padding: 4px;
}
.day-number {
  font-size: 14px;
  font-weight: bold;
}
.events-container {
  margin-top: 4px;
}
.event-tag {
  font-size: 10px;
  color: white;
  padding: 1px 4px;
  border-radius: 2px;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
}
:deep(.el-calendar-table .el-calendar-day) {
  height: 100px;
  padding: 0;
}
h3 {
  margin: 0;
}
</style>
