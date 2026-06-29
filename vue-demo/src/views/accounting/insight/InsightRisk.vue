<template>
  <div class="risk-container">
    <div class="page-header">
      <el-button link icon="ArrowLeft" @click="router.back()">返回洞察总览</el-button>
      <h2>风险异常检测</h2>
    </div>

    <div class="risk-summary-bar" v-if="riskTotal > 0">
      <div class="summary-info">
        <el-icon><WarningFilled /></el-icon>
        <span>当前检测到 {{ riskTotal }} 项潜在财务风险，建议立即查看</span>
      </div>
      <el-button type="danger" plain size="small" @click="fetchData">重新扫描</el-button>
    </div>

    <div v-if="riskItems.length > 0" class="risk-grid">
      <div v-for="(risk, index) in riskItems" :key="index" class="risk-card" :class="risk.level">
         <div class="card-glow"></div>
         <div class="risk-header">
            <span class="level-tag">{{ risk.level === 'danger' ? '高危' : '注意' }}</span>
            <span class="time">{{ risk.date }}</span>
         </div>
         <div class="risk-main">
            <h3>{{ risk.title }}</h3>
            <p>{{ risk.description }}</p>
         </div>
         <div class="risk-footer">
            <div class="impact">影响程度: {{ risk.impact }}</div>
            <el-button type="primary" link @click="handleFix(risk)">查看解决建议</el-button>
         </div>
      </div>
    </div>

    <div v-else class="risk-empty-state">
      <el-empty :image-size="180">
        <template #description>
          <div class="risk-empty-copy">
            <h3>恭喜！未检测到任何财务风险异常</h3>
            <p>当前账单与预算表现稳定，暂时没有需要优先处理的风险项。</p>
          </div>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, WarningFilled } from '@element-plus/icons-vue'
import { getRiskAlerts } from '@/api/accounting'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const riskItems = ref([])
const riskTotal = ref(0)

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  try {
    const res = await getRiskAlerts()
    riskItems.value = res.data
    riskTotal.value = res.data.length
  } catch (err) {}
}

const handleFix = (risk) => {
  ElMessageBox.alert(risk.advice || '建议保持冷静，合理规划。', risk.title, {
    confirmButtonText: '我知道了',
    type: risk.level === 'danger' ? 'warning' : 'info'
  })
}
</script>

<style scoped>
.risk-container {
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

.risk-summary-bar {
  background: #fef2f2;
  border: 1px solid #fee2e2;
  padding: 16px 24px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #ef4444;
  font-weight: 600;
  font-size: 14px;
}

.risk-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.risk-empty-state {
  min-height: 58vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 0 40px;
}

.risk-empty-copy {
  text-align: center;
  max-width: 520px;
}

.risk-empty-copy h3 {
  margin: 12px 0 10px;
  font-size: 30px;
  line-height: 1.25;
  color: #1e293b;
  font-weight: 800;
}

.risk-empty-copy p {
  margin: 0;
  font-size: 15px;
  line-height: 1.8;
  color: #64748b;
}

.risk-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #f1f5f9;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.risk-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px -5px rgba(0,0,0,0.1);
}

.risk-card.danger { border-left: 4px solid #ef4444; }
.risk-card.warning { border-left: 4px solid #f59e0b; }

.level-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 700;
  text-transform: uppercase;
}

.danger .level-tag { background: #fee2e2; color: #ef4444; }
.warning .level-tag { background: #fef3c7; color: #f59e0b; }

.risk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.time { font-size: 12px; color: #94a3b8; }

.risk-main h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #1e293b;
}

.risk-main p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.risk-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.impact {
  font-size: 12px;
  color: #94a3b8;
}

.card-glow {
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(239, 68, 68, 0.05) 0%, transparent 70%);
  z-index: 0;
}
</style>
