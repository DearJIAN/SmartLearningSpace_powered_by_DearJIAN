<template>
  <div class="budget-page">
    <el-card>
      <template #header>
        <h3><el-icon><Money /></el-icon> 月度预算管理</h3>
      </template>

      <!-- 当前月份预算 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="budget-form">
            <h4>设置预算</h4>
            <el-form :inline="true">
              <el-form-item label="月份">
                <el-date-picker
                  v-model="selectedMonth"
                  type="month"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  placeholder="选择月份"
                />
              </el-form-item>
              <el-form-item label="总预算">
                <el-input-number v-model="budgetAmount" :min="0" :precision="2" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveBudget">保存</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="budget-status">
            <h4>当前预算状态</h4>
            <div class="status-item">
              <span>总预算：</span>
              <span class="value">¥{{ budgetStatus.total || 0 }}</span>
            </div>
            <div class="status-item">
              <span>已使用：</span>
              <span class="value" :class="budgetStatus.isOver ? 'text-danger' : 'text-success'">
                ¥{{ budgetStatus.used || 0 }}
              </span>
            </div>
            <div class="status-item">
              <span>剩余：</span>
              <span class="value" :class="budgetStatus.remaining < 0 ? 'text-danger' : 'text-primary'">
                ¥{{ budgetStatus.remaining || 0 }}
              </span>
            </div>
            <el-progress
              :percentage="budgetStatus.progress || 0"
              :status="budgetStatus.isOver ? 'exception' : 'success'"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money } from '@element-plus/icons-vue'
import axios from 'axios'

const selectedMonth = ref(getCurrentMonth())
const budgetAmount = ref(0)

const budgetStatus = reactive({
  total: 0,
  used: 0,
  remaining: 0,
  progress: 0,
  isOver: false
})

onMounted(() => {
  fetchBudgetStatus()
})

function getCurrentMonth() {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const fetchBudgetStatus = async () => {
  try {
    const response = await axios.get(`/api/accounting/budget/${selectedMonth.value}`)
    if (response.data.code === 200) {
      Object.assign(budgetStatus, response.data.data)
      budgetAmount.value = budgetStatus.total
    }
  } catch (error) {
    ElMessage.error('获取预算失败')
  }
}

const saveBudget = async () => {
  try {
    const response = await axios.post('/api/accounting/budget', {
      month: selectedMonth.value,
      totalBudget: budgetAmount.value
    })
    if (response.data.code === 200) {
      ElMessage.success('保存成功')
      fetchBudgetStatus()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}
</script>

<style scoped>
.budget-page {
  padding: 20px;
}

h4 {
  margin-bottom: 20px;
  color: #333;
}

.budget-form {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.budget-status {
  padding: 20px;
  background: #ecf5ff;
  border-radius: 8px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 16px;
}

.status-item .value {
  font-weight: bold;
}

.text-danger {
  color: #f56c6c;
}

.text-success {
  color: #67c23a;
}

.text-primary {
  color: #409eff;
}
</style>
