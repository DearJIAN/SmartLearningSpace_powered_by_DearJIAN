<template>
  <div class="treemap-page">
    <el-card class="filter-card">
      <el-row :gutter="20" align="middle">
        <el-col :span="12">
          <h3><i class="fas fa-project-diagram"></i> 资金流向树</h3>
        </el-col>
        <el-col :span="12" style="text-align: right">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchData"
          />
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover">
      <div ref="treeChartRef" class="tree-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const dateRange = ref([])
const treeChartRef = ref()
let chart = null

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) chart.dispose()
})

const handleResize = () => {
  if (chart) chart.resize()
}

const fetchData = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = formatDate(dateRange.value[0])
      params.endDate = formatDate(dateRange.value[1])
    }

    const res = await axios.get('/api/accounting/analysis/treemap', { params })
    if (res.data.code === 200) {
      initChart(res.data.data)
    }
  } catch (error) {
    ElMessage.error('获取资金流向数据失败')
  }
}

const initChart = (treeData) => {
  if (!chart) chart = echarts.init(treeChartRef.value)
  
  chart.setOption({
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [
      {
        type: 'tree',
        data: [treeData],
        top: '5%',
        left: '10%',
        bottom: '5%',
        right: '25%',
        symbolSize: 10,
        label: {
          position: 'left',
          verticalAlign: 'middle',
          align: 'right',
          fontSize: 12
        },
        leaves: {
          label: {
            position: 'right',
            verticalAlign: 'middle',
            align: 'left'
          }
        },
        emphasis: { focus: 'descendant' },
        expandAndCollapse: true,
        animationDuration: 550,
        animationDurationUpdate: 750
      }
    ]
  })
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.treemap-page {
  padding: 20px;
}
.filter-card {
  margin-bottom: 20px;
}
.tree-container {
  height: 700px;
}
h3 {
  margin: 0;
}
</style>
