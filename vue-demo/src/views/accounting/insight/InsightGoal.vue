<template>
  <div class="goal-container">
    <div class="page-header">
      <el-button link icon="ArrowLeft" @click="router.back()">返回洞察总览</el-button>
      <h2>财务目标管理</h2>
    </div>

    <!-- Active Goal Card -->
    <div class="active-goal-card" v-if="goalData">
      <div class="card-bg-glow"></div>
      <div class="main-content">
        <div class="goal-header">
           <div class="goal-title-box">
             <span class="badge">当前目标</span>
             <h3>{{ goalData.goalName || '年度储蓄计划' }}</h3>
           </div>
           <el-button type="primary" round @click="showEdit = true">修改目标</el-button>
        </div>

        <div class="progress-section">
           <div class="progress-text">
              <div class="current">
                <span class="label">已达成</span>
                <span class="val">¥{{ (goalData.currentSaved || 0).toLocaleString() }}</span>
              </div>
              <div class="target">
                <span class="label">总目标</span>
                <span class="val">¥{{ (goalData.targetAmount || 0).toLocaleString() }}</span>
              </div>
           </div>
           <el-progress 
              :percentage="calculatePercent(goalData.currentSaved, goalData.targetAmount)" 
              :stroke-width="24"
              striped
              striped-flow
              :color="customColors"
           />
        </div>

        <div class="prediction-bar">
          <div class="predict-item">
            <el-icon><Calendar /></el-icon>
            <span>预计达成时间: {{ goalData.estimatedDays || '45' }} 天后</span>
          </div>
          <div class="predict-item">
            <el-icon><TrendCharts /></el-icon>
            <span>每日需存: ¥{{ goalData.dailyNeeds || '50' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Encouragement Box -->
    <div class="encouragement-box" v-if="goalData">
       <div class="box-icon"><el-icon><Trophy /></el-icon></div>
       <div class="box-text">
          <h4>坚持就是胜利！</h4>
          <p>{{ goalData.encouragement || '按照目前的进度，您将在两个月内达成目标。继续保持这份克制与专注！' }}</p>
       </div>
    </div>

    <!-- Edit Goal Dialog -->
    <el-dialog v-model="showEdit" title="设定财务目标" width="400px" align-center class="premium-dialog">
      <el-form :model="form" label-position="top">
        <el-form-item label="目标名称">
          <el-input v-model="form.goalName" placeholder="例如：买台新电脑" />
        </el-form-item>
        <el-form-item label="目标金额">
          <el-input-number v-model="form.targetAmount" :min="1" class="w-full" :controls="false">
             <template #prefix>¥</template>
          </el-input-number>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.deadline" type="date" placeholder="选择日期" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showEdit = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存设置</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Calendar, TrendCharts, Trophy } from '@element-plus/icons-vue'
import { getGoalTracking, updateGoal } from '@/api/accounting'
import { ElMessage } from 'element-plus'

const router = useRouter()
const goalData = ref(null)
const showEdit = ref(false)
const submitting = ref(false)
const form = ref({
  goalName: '',
  targetAmount: 0,
  deadline: ''
})

const customColors = [
  { color: '#f59e0b', percentage: 30 },
  { color: '#6366f1', percentage: 70 },
  { color: '#10b981', percentage: 100 }
]

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  try {
    const res = await getGoalTracking()
    goalData.value = res.data
    form.value = { ...res.data }
  } catch (err) {}
}

const calculatePercent = (cur, target) => {
  if (!target) return 0
  return Math.min(100, Math.floor((cur / target) * 100))
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    await updateGoal(form.value)
    ElMessage.success('目标更新成功！')
    showEdit.value = false
    fetchData()
  } catch (err) {
    ElMessage.error('更新失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.goal-container {
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

.active-goal-card {
  background: #fff;
  border-radius: 24px;
  padding: 32px;
  border: 1px solid #f1f5f9;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.card-bg-glow {
  position: absolute;
  top: -50%;
  right: -50%;
  width: 100%;
  height: 200%;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.05) 0%, transparent 70%);
  pointer-events: none;
}

.main-content { position: relative; z-index: 1; }

.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
}

.goal-title-box .badge {
  font-size: 11px;
  background: #eef2ff;
  color: #6366f1;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 600;
  margin-bottom: 8px;
  display: inline-block;
}

.goal-title-box h3 {
  margin: 0;
  font-size: 24px;
  color: #1e293b;
}

.progress-section {
  margin-bottom: 32px;
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.progress-text .label {
  display: block;
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 4px;
}

.progress-text .val {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
}

.progress-text .target { text-align: right; }

:deep(.el-progress-bar__outer) {
  border-radius: 12px;
  background-color: #f1f5f9;
}

.prediction-bar {
  display: flex;
  gap: 40px;
  padding-top: 24px;
  border-top: 1px solid #f1f5f9;
}

.predict-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.predict-item .el-icon { color: #6366f1; }

.encouragement-box {
  background: linear-gradient(135deg, #10b98111, #3b82f611);
  border: 1px solid #10b98122;
  border-radius: 20px;
  padding: 24px;
  display: flex;
  gap: 20px;
  align-items: center;
}

.box-icon {
  width: 48px;
  height: 48px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #f59e0b;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.box-text h4 { margin: 0 0 4px; color: #1e293b; }
.box-text p { margin: 0; font-size: 14px; color: #64748b; line-height: 1.5; }

.w-full { width: 100%; }

.premium-dialog :deep(.el-dialog) {
  border-radius: 20px;
}
</style>
