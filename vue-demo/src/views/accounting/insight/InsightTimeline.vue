<template>
  <div class="timeline-container">
    <div class="page-header">
      <el-button link icon="ArrowLeft" @click="router.back()">返回洞察总览</el-button>
      <h2>财务轨迹</h2>
    </div>

    <div class="glass-timeline">
      <el-timeline v-if="timelineData.length > 0">
        <el-timeline-item
          v-for="(item, index) in timelineData"
          :key="index"
          :timestamp="item.date"
          placement="top"
          :type="item.type === 'income' ? 'success' : 'primary'"
          :hollow="true"
        >
          <div class="timeline-card" :class="item.type">
            <div class="card-left">
               <div class="icon-circle">
                 <el-icon v-if="item.type === 'income'"><PriceTag /></el-icon>
                 <el-icon v-else><ShoppingCart /></el-icon>
               </div>
               <div class="info">
                 <div class="title">{{ item.category }}</div>
                 <div class="remark">{{ item.remark || '无备注' }}</div>
               </div>
            </div>
            <div class="card-right">
              <span class="amount" :class="item.type">
                {{ item.type === 'income' ? '+' : '-' }}¥{{ item.amount }}
              </span>
              <span class="time">{{ item.time }}</span>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
      
      <el-empty v-else description="暂无财务记录轨迹" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ShoppingCart, PriceTag } from '@element-plus/icons-vue'
import { getFinancialTimeline } from '@/api/accounting'

const router = useRouter()
const timelineData = ref([])

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  try {
    const res = await getFinancialTimeline()
    timelineData.value = res.data
  } catch (err) {}
}
</script>

<style scoped>
.timeline-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
}

.glass-timeline {
  background: #fff;
  border-radius: 24px;
  padding: 40px;
  border: 1px solid #f1f5f9;
  min-height: 500px;
}

:deep(.el-timeline-item__node) {
  background-color: #6366f1;
}

.timeline-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid transparent;
  transition: all 0.2s;
  margin-top: 8px;
}

.timeline-card:hover {
  background: #fff;
  border-color: #e2e8f0;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  transform: translateX(4px);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.income .icon-circle { color: #10b981; }
.expense .icon-circle { color: #6366f1; }

.info .title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.info .remark {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

.card-right {
  text-align: right;
}

.amount {
  display: block;
  font-size: 16px;
  font-weight: 700;
}

.amount.income { color: #10b981; }
.amount.expense { color: #1e293b; }

.time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}
</style>
