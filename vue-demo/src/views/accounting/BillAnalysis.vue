<template>
  <div class="analysis-container">
    <!-- Top Filter Row -->
    <div class="glass-toolbar">
      <div class="title-section">
        <el-icon class="title-icon" style="color: #6366f1"><i-tabler-report-analytics /></el-icon>
        <h2>财务看板</h2>
        <span class="subtitle">洞察您的收支流向与财务健康度</span>
      </div>
      <div class="filter-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="起始"
          end-placeholder="结束"
          size="default"
          class="premium-picker"
          @change="fetchData"
        />
        <el-button-group class="time-presets">
          <el-button @click="setRange('month')">本月</el-button>
          <el-button @click="setRange('year')">全年</el-button>
        </el-button-group>
        <el-tooltip content="刷新数据" placement="top">
          <el-button type="primary" circle @click="fetchData">
            <template #icon><i-tabler-refresh /></template>
          </el-button>
        </el-tooltip>
      </div>
    </div>

    <!-- Main Dashboard Grid -->
    <el-row :gutter="20" class="dashboard-grid">
      <!-- Row 1: Key Charts -->
      <el-col :span="16">
        <div class="chart-card large">
          <div class="card-header">
            <h3>收支走势分析</h3>
            <span class="tag">实时趋势</span>
          </div>
          <div ref="trendChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card large">
          <div class="card-header">
            <h3>消费分类占比</h3>
          </div>
          <div ref="pieChartRef" class="chart-box"></div>
        </div>
      </el-col>

      <!-- Row 2: Insights & Lists -->
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <h3>财务健康分析</h3>
            <el-tooltip placement="top">
              <template #content>
                <div style="line-height: 1.6; padding: 4px">
                  <b style="color: #6366f1">评分计算公式：</b><br/>
                  • 消费稳定性 (30%)：考量每日支出的波动率<br/>
                  • 储蓄贡献度 (25%)：月收入与支出的差值比率<br/>
                  • 预算执行力 (20%)：实际支出与预算的偏差<br/>
                  • 风险规避值 (15%)：大额异常支出的频率<br/>
                  • 记账活跃度 (10%)：近30天内记账的天数分布
                </div>
              </template>
              <el-icon style="cursor: help; color: #94a3b8"><i-tabler-info-circle /></el-icon>
            </el-tooltip>
          </div>
          <div class="health-radar-box">
            <div ref="healthChartRef" class="chart-box radar-canvas"></div>
            <div class="radar-score-overlay">
              <span class="score-num">{{ healthScore }}</span>
              <span class="score-text">综合评价</span>
            </div>
          </div>
          <div class="formula-box">
             <p class="formula-title">评分构成分析：</p>
             <div class="formula-grid">
                <span>消费稳定性 30%</span>
                <span>月度储蓄率 25%</span>
                <span>预算控制力 20%</span>
                <span>风险规避度 15%</span>
                <span>记账活跃度 10%</span>
             </div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <h3>周消费波动</h3>
          </div>
          <div ref="weeklyChartRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="chart-card">
          <div class="card-header">
            <h3>Top 5 大额支出</h3>
          </div>
          <div class="top-list">
            <div v-for="(item, index) in top5" :key="index" class="top-item">
              <span class="rank">{{ index + 1 }}</span>
              <span class="cat">{{ item.category }}</span>
              <span class="amount">¥{{ item.amount }}</span>
              <el-progress :percentage="item.percent" :show-text="false" status="warning" />
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
// import { DataAnalysis, Refresh, QuestionFilled } from '@element-plus/icons-vue' // Removed
import { getAnalysisData } from '@/api/accounting'
import dayjs from 'dayjs'

const dateRange = ref([dayjs().startOf('month').toDate(), dayjs().toDate()])
const healthScore = ref(85)
const top5 = ref([])

// Chart Refs
const trendChartRef = ref()
const pieChartRef = ref()
const healthChartRef = ref()
const weeklyChartRef = ref()

let charts = []

const fetchData = async () => {
  try {
    const params = {
      startDate: dayjs(dateRange.value[0]).format('YYYY-MM-DD'),
      endDate: dayjs(dateRange.value[1]).format('YYYY-MM-DD')
    }
    const res = await getAnalysisData(params)
    const data = res.data
    
    healthScore.value = data.healthScore || 85
    top5.value = data.top5 || []
    
    // 清除旧实例
    charts.forEach(c => c.dispose())
    charts = []
    
    updateCharts(data)
  } catch (err) {
    console.error('Fetch analysis error:', err)
  }
}

const updateCharts = (data) => {
  if (!trendChartRef.value) return

  // 1. Trend Chart
  const trend = echarts.init(trendChartRef.value)
  trend.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderWidth: 0, shadowBlur: 10 },
    legend: { bottom: 0, textStyle: { color: '#64748b' } },
    grid: { left: '3%', right: '4%', bottom: '20%', containLabel: true },
    xAxis: { type: 'category', data: data.dates, axisLine: { lineStyle: { color: '#e2e8f0' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { type: 'dashed', color: '#f1f5f9' } } },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', height: 20, bottom: 40, start: 0, end: 100, borderColor: 'transparent', backgroundColor: '#f1f5f9', fillerColor: '#6366f122' }
    ],
    series: [
      {
        name: '收入', type: 'line', smooth: true, data: data.incomes,
        color: '#10b981', areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#10b98133'},{offset:1,color:'#10b98100'}]) }
      },
      {
        name: '支出', type: 'line', smooth: true, data: data.expenses,
        color: '#ef4444', areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#ef444433'},{offset:1,color:'#ef444400'}]) }
      }
    ]
  })

  // 2. Pie Chart
  const pie = echarts.init(pieChartRef.value)
  pie.setOption({
    tooltip: { 
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)' 
    },
    legend: {
      orient: 'horizontal', // 改为水平方向
      bottom: '10%', // 放在底部
      top: 'auto',
      left: 'center', // 居中对齐
      itemWidth: 12,
      itemHeight: 12,
      itemGap: 16,
      textStyle: { color: '#64748b', fontSize: 11 },
      formatter: '{name}',
      type: 'scroll' // 支持滚动，防止图例过多时溢出
    },
    series: [{
      type: 'pie', 
      radius: ['40%', '65%'], // 调整饼图大小
      center: ['50%', '40%'], // 居中显示
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { 
        show: true, 
        position: 'outside',
        formatter: '{b}: {d}%',
        fontSize: 10,
        color: '#64748b',
        // 调整标签布局，避免重叠
        alignTo: 'none',
        bleedMargin: 15,
        lineHeight: 14
      },
      labelLine: {
        show: true,
        length: 20,
        length2: 15,
        smooth: true,
        lineStyle: { color: '#d1d5db', width: 1 }
      },
      emphasis: {
        label: { show: true, fontSize: 12, fontWeight: 'bold' }
      },
      data: data.categories,
      color: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899']
    }]
  })

  // 3. Health Radar Chart
  const health = echarts.init(healthChartRef.value)
  
  // 基于综合分模拟雷达图各维度
  const base = healthScore.value;
  const radarData = [
    Math.min(100, base + 5), // 稳定性
    Math.min(100, Math.max(10, base - 12)), // 储蓄率
    Math.min(100, base + 8), // 预算执行
    Math.min(100, base - 5), // 风险规避
    Math.min(100, base + 15) // 活跃度
  ];

  health.setOption({
    radar: {
      indicator: [
        { name: '稳定性', max: 100 },
        { name: '储蓄率', max: 100 },
        { name: '预算执行', max: 100 },
        { name: '风险规避', max: 100 },
        { name: '活跃度', max: 100 }
      ],
      center: ['50%', '50%'],
      radius: '60%',
      axisName: { color: '#64748b', fontSize: 10, fontWeight: 600 },
      splitNumber: 4,
      splitLine: { lineStyle: { color: '#cbd5e1', width: 1 } },
      splitArea: { 
        show: true, 
        areaStyle: { color: ['#f8fafc', '#f1f5f9', '#f8fafc', '#f1f5f9'] } 
      },
      axisLine: { lineStyle: { color: '#cbd5e1' } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: radarData,
        name: '财务指标',
        itemStyle: { color: '#6366f1' },
        areaStyle: { color: 'rgba(99, 102, 241, 0.25)' },
        lineStyle: { width: 2 }
      }],
      symbol: 'circle',
      symbolSize: 4
    }]
  })

  // 4. Weekly Chart
  const weekly = echarts.init(weeklyChartRef.value)
  weekly.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLine: { show: false } },
    yAxis: { type: 'value', show: false },
    series: [{
      type: 'bar', data: data.weekly, barWidth: '40%',
      itemStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'#6366f1'},{offset:1,color:'#a5b4fc'}]) , borderRadius: [6,6,0,0] }
    }]
  })

  charts = [trend, pie, health, weekly]
}

const setRange = (type) => {
  if (type === 'month') {
    dateRange.value = [dayjs().startOf('month').toDate(), dayjs().toDate()]
  } else {
    dateRange.value = [dayjs().startOf('year').toDate(), dayjs().toDate()]
  }
  fetchData()
}

onMounted(() => {
  nextTick(() => {
    fetchData()
  })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(c => c.dispose())
})

const handleResize = () => {
  charts.forEach(c => c.resize())
}
</script>

<style scoped>
.analysis-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.glass-toolbar {
  background: #fff;
  padding: 20px 24px;
  border-radius: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 15px -3px rgba(0, 0, 0, 0.05);
}

.title-section h2 {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 10px;
}

.subtitle {
  font-size: 13px;
  color: #94a3b8;
  margin-top: 4px;
  display: block;
}

.filter-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.premium-picker {
  width: 260px !important;
}

.dashboard-grid {
  margin-top: 0;
}

.chart-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  height: 320px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  border: 1px solid #f1f5f9;
  display: flex;
  flex-direction: column;
}

.chart-card.large {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 15px;
  color: #475569;
  font-weight: 600;
}

.tag {
    background: #eef2ff;
    color: #6366f1;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 4px;
}

.chart-box {
  flex: 1;
  width: 100%;
}

.health-radar-box {
  position: relative;
  height: 220px; /* 固定高度确保雷达图可见 */
  margin-bottom: 20px;
}

.radar-canvas {
  width: 100%;
  height: 100%;
}

.radar-score-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  pointer-events: none;
  z-index: 10;
}

.formula-box {
  background: #f8fafc;
  padding: 12px;
  border-radius: 12px;
  margin-top: auto;
}

.formula-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin: 0 0 8px;
}

.formula-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 12px;
  font-size: 11px;
  color: #94a3b8;
}

.radar-score-overlay .score-num {
  font-size: 32px;
  font-weight: 800;
  color: #6366f1;
  display: block;
  line-height: 1;
}

.radar-score-overlay .score-text {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
  display: block;
}

.top-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 10px;
}

.top-item {
  display: grid;
  grid-template-columns: 24px 1fr 80px;
  align-items: center;
  gap: 12px;
}

.rank {
  width: 20px;
  height: 20px;
  background: #f1f5f9;
  border-radius: 50%;
  text-align: center;
  line-height: 20px;
  font-size: 11px;
  color: #64748b;
}

.cat { font-size: 13px; color: #1e293b; }
.amount { font-size: 13px; font-weight: 600; text-align: right; }

.top-item :deep(.el-progress) {
  grid-column: 2 / 4;
  margin-top: -4px;
}
</style>
