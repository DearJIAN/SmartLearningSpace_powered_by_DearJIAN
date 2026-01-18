<template>
  <div class="bill-list-container">
    <!-- Header Summary Stats -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="glass-card income">
          <div class="card-icon"><el-icon><i-ph-trend-down-duotone /></el-icon></div>
          <div class="card-info">
            <span class="label">本月总收入</span>
            <span class="value">¥{{ stats.income || '0.00' }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card expense">
          <div class="card-icon"><el-icon><i-ph-trend-up-duotone /></el-icon></div>
          <div class="card-info">
            <span class="label">本月总支出</span>
            <span class="value">¥{{ stats.expense || '0.00' }}</span>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="glass-card balance">
          <div class="card-icon"><el-icon><i-ph-wallet-duotone /></el-icon></div>
          <div class="card-info">
            <span class="label">当月结余</span>
            <span class="value">¥{{ stats.balance || '0.00' }}</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Toolbar & Filters -->
    <div class="action-bar-card">
      <div class="filter-group">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          size="default"
          class="custom-picker"
          style="width: 380px; flex-shrink: 0"
          @change="handleFilter"
        />
        <el-select v-model="filterParams.categoryId" placeholder="全部分类" clearable @change="handleFilter">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id">
            <span style="float: left">{{ cat.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 13px">{{ cat.type === 1 ? '入' : '出' }}</span>
          </el-option>
        </el-select>
        <el-select v-model="filterParams.type" placeholder="全部类型" clearable @change="handleFilter">
          <el-option label="收入" :value="1" />
          <el-option label="支出" :value="2" />
        </el-select>
      </div>
      <div class="btn-group">
        <el-button type="primary" round @click="handleAdd">
          <template #icon><i-tabler-plus /></template>记一笔
        </el-button>
        <el-tooltip content="生成随机账单数据" placement="top">
          <el-button circle @click="handleGenerate">
            <template #icon><i-tabler-wand style="color: #8b5cf6" /></template>
          </el-button>
        </el-tooltip>
        <el-tooltip content="导出账单Excel" placement="top">
          <el-button circle @click="handleExport">
            <template #icon><i-tabler-download style="color: #10b981" /></template>
          </el-button>
        </el-tooltip>
        <el-tooltip content="刷新列表" placement="top">
          <el-button circle @click="refreshData">
            <template #icon><i-tabler-refresh style="color: #3b82f6" /></template>
          </el-button>
        </el-tooltip>
        <el-button type="danger" link @click="handleClear">清空</el-button>
      </div>
    </div>

    <!-- Bill Table -->
    <div class="table-container">
      <el-table 
        :data="billList" 
        v-loading="loading"
        style="width: 100%" 
        class="elegant-table"
        :header-cell-style="{ background: '#f8fafc', color: '#64748b' }"
      >
        <el-table-column prop="billDate" label="交易日期" width="160">
          <template #default="{ row }">
            <div class="date-cell">
              <el-icon><i-tabler-calendar-event /></el-icon>
              <span>{{ row.billDate }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="分类" width="140">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" effect="light" round>
              {{ row.categoryName || '未分类' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="180">
          <template #default="{ row }">
            <span :class="['amount-text', row.type === 1 ? 'text-income' : 'text-expense']">
              {{ row.type === 1 ? '+' : '-' }} ¥{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="remark" label="备注说明" show-overflow-tooltip />

        <el-table-column label="操作" width="120" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">
              <template #icon><i-tabler-edit /></template>
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">
              <template #icon><i-tabler-trash /></template>
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无账单数据" :image-size="120">
            <el-button type="primary" plain round @click="handleGenerate">生成一些示例数据</el-button>
          </el-empty>
        </template>
      </el-table>

      <div class="pagination-box">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          background
          @current-change="fetchData"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="460px"
      destroy-on-close
      class="premium-dialog"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="交易类型" prop="type">
          <el-radio-group v-model="form.type" class="type-radio-group">
            <el-radio-button :label="2">支出</el-radio-button>
            <el-radio-button :label="1">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="金额" prop="amount">
              <el-input-number 
                v-model="form.amount" 
                :min="0" 
                :precision="2" 
                style="width: 100%" 
                controls-position="right"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日期" prop="billDate">
              <el-date-picker v-model="form.billDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories.filter(c => c.type === form.type)"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="添加备注..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false" round>取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit" round>确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// import { Plus, MagicStick, Download, Refresh, Calendar, Wallet, Top, Bottom, EditPen, Delete } from '@element-plus/icons-vue' // Removed
import { 
  getBillPage, getBillStats, getCategories, 
  saveBill, deleteBill, clearBills, generateBills,
  exportBillsUrl
} from '@/api/accounting'
import dayjs from 'dayjs'

const loading = ref(false)
const submitting = ref(false)
const billList = ref([])
const categories = ref([])
const dateRange = ref([])
const stats = reactive({ income: 0, expense: 0, balance: 0 })

const filterParams = reactive({
  categoryId: null,
  type: null,
  startDate: '',
  endDate: ''
})

const pagination = reactive({
  current: 1,
  size: 15,
  total: 0
})

const dialog = reactive({
  visible: false,
  title: '记一笔',
  isEdit: false
})

const formRef = ref()
const form = reactive({
  id: null,
  type: 2,
  amount: 0,
  categoryId: null,
  billDate: new Date(),
  remark: ''
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  billDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

onMounted(() => {
  fetchCategories()
  refreshData()
})

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (err) {
    console.error('Fetch categories error:', err)
    if (err.message && err.message.includes('404')) {
      ElMessage.error('分类获取失败：404 (请确认后端已重启)')
    } else {
      ElMessage.error('分类获取失败: ' + (err.response?.data?.msg || err.message))
    }
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.size,
      ...filterParams
    }
    const res = await getBillPage(params)
    billList.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchStatsData = async () => {
  try {
    const res = await getBillStats(filterParams)
    Object.assign(stats, res.data)
  } catch (err) {}
}

const refreshData = () => {
  fetchData()
  fetchStatsData()
}

const handleFilter = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    filterParams.startDate = dayjs(dateRange.value[0]).format('YYYY-MM-DD')
    filterParams.endDate = dayjs(dateRange.value[1]).format('YYYY-MM-DD')
  } else {
    filterParams.startDate = ''
    filterParams.endDate = ''
  }
  pagination.current = 1
  refreshData()
}

const handleAdd = () => {
  dialog.title = '新增记账'
  dialog.isEdit = false
  Object.assign(form, {
    id: null,
    type: 2,
    amount: 0,
    categoryId: null,
    billDate: new Date(),
    remark: ''
  })
  dialog.visible = true
}

const handleEdit = (row) => {
  dialog.title = '编辑记账'
  dialog.isEdit = true
  Object.assign(form, {
    ...row,
    billDate: new Date(row.billDate)
  })
  dialog.visible = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = {
      ...form,
      billDate: dayjs(form.billDate).format('YYYY-MM-DD')
    }
    await saveBill(data)
    ElMessage.success('保存成功')
    dialog.visible = false
    refreshData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条账单记录吗？', '确认删除', {
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteBill(id)
    ElMessage.success('已删除')
    refreshData()
  } catch (err) {}
}

const handleClear = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有账单吗？此操作不可撤销！', '警告', {
      type: 'error',
      confirmButtonText: '确定清空',
      confirmButtonClass: 'el-button--danger'
    })
    await clearBills()
    ElMessage.success('已清空')
    refreshData()
  } catch (err) {}
}

const handleGenerate = async () => {
  try {
    await generateBills(20)
    ElMessage.success('成功生成 20 条示例数据')
    refreshData()
  } catch (err) {}
}

const handleExport = () => {
  const url = exportBillsUrl(filterParams)
  window.open(url, '_blank')
}
</script>

<style scoped>
.bill-list-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stat-row {
  margin-bottom: 8px;
}

.glass-card {
  background: #fff;
  padding: 24px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s;
  border: 1px solid #f1f5f9;
}

.glass-card:hover {
  transform: translateY(-2px);
}

.card-icon {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.income .card-icon { background: #ecfdf5; color: #10b981; }
.expense .card-icon { background: #fef2f2; color: #ef4444; }
.balance .card-icon { background: #eff6ff; color: #3b82f6; }

.card-info {
  display: flex;
  flex-direction: column;
}

.card-info .label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 4px;
}

.card-info .value {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
}

.action-bar-card {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.filter-group {
  display: flex;
  gap: 12px;
}

.custom-picker {
  width: 440px !important;
}

.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.elegant-table {
  border-radius: 8px;
  overflow: hidden;
}

.date-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #64748b;
}

.amount-text {
  font-family: 'JetBrains Mono', monospace;
  font-weight: 600;
  font-size: 16px;
}

.text-income { color: #10b981; }
.text-expense { color: #ef4444; }

.pagination-box {
  padding: 20px;
  display: flex;
  justify-content: center;
}

/* Dialog Styling */
.type-radio-group {
  width: 100%;
  display: flex;
}
:deep(.el-radio-button) {
  flex: 1;
}
:deep(.el-radio-button__inner) {
  width: 100%;
}
</style>

