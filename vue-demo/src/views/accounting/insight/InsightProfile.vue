<template>
  <div class="profile-container">
    <div class="profile-header">
      <el-button link @click="router.back()">
        <template #icon><i-tabler-arrow-left style="color: #6366f1; font-weight: bold" /></template>
        <span style="color: #6366f1">返回洞察总览</span>
      </el-button>
      <h2>个人财务画像</h2>
    </div>

    <el-row :gutter="24">
      <!-- Left: Identity Card -->
      <el-col :span="10">
        <div class="identity-card">
          <div class="avatar-orbit">
             <div class="avatar-inner">
               <span class="user-initial">{{ profileData.grade || 'S' }}</span>
             </div>
             <div class="orbit-dot dot-1"></div>
             <div class="orbit-dot dot-2"></div>
          </div>
          <div class="identity-info">
            <h3>{{ profileData.title || '稳健型理财家' }}</h3>
            <p class="desc">{{ profileData.description || '您在财务平衡方面表现卓越，消费习惯非常克制且具有前瞻性。' }}</p>
          </div>
          <div class="stats-grid">
            <div class="stat-box">
              <span class="val">{{ profileData.savingsRate ? (profileData.savingsRate.toString().includes('%') ? profileData.savingsRate : profileData.savingsRate + '%') : '35%' }}</span>
              <span class="lab">平均储蓄率</span>
            </div>
            <div class="stat-box">
              <span class="val">¥{{ profileData.netWorth || '12,800' }}</span>
              <span class="lab">资产积累</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- Right: Detailed Metrics -->
      <el-col :span="14">
        <div class="metrics-card">
          <div class="card-title">财务多维指标</div>
          <div class="radar-container" ref="radarRef"></div>
          
          <div class="tags-section">
            <div class="section-title">财务特征标签</div>
            <div class="tags-group">
              <el-tag v-for="tag in (profileData.tags || ['理智消费', '目标感强', '低风险偏好', '储蓄达人'])" 
                :key="tag" 
                effect="dark"
                round
                :color="getRandomColor()"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Bottom: Suggestions -->
    <div class="suggestions-section">
      <h3>AI 进阶建议</h3>
      <div class="suggestion-grid">
        <div v-for="(sug, i) in (profileData.suggestions || defaultSuggestions)" :key="i" class="sug-item">
          <el-icon class="sug-icon"><i-tabler-circle-check /></el-icon>
          <p>{{ sug }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
// import { ArrowLeft, Check } from '@element-plus/icons-vue' // Removed
import * as echarts from 'echarts'
import { getFinancialProfile } from '@/api/accounting'

const router = useRouter()
const radarRef = ref()
const profileData = ref({})
let radarChart = null

const defaultSuggestions = [
  '您的储蓄率已超过 90% 的用户，建议尝试进阶的资产配置。',
  '近期饮食支出略有抬头，建议留意周末的大额聚餐。',
  '可以将部分闲置资金转入定投计划，提升长期收益。'
]

const getRandomColor = () => {
  const colors = ['#6366f1', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6']
  return colors[Math.floor(Math.random() * colors.length)]
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', () => radarChart?.resize())
})

onUnmounted(() => {
  radarChart?.dispose()
})

const fetchData = async () => {
  try {
    const res = await getFinancialProfile()
    profileData.value = res.data
    initRadar(res.data.radarData || [80, 70, 90, 65, 85])
  } catch (err) {}
}

const initRadar = (data) => {
  if (!radarRef.value) return
  radarChart = echarts.init(radarRef.value)
  radarChart.setOption({
    radar: {
      indicator: [
        { name: '储蓄能力', max: 100 },
        { name: '支出控制', max: 100 },
        { name: '风险偏好', max: 100 },
        { name: '成长潜力', max: 100 },
        { name: '资产健康', max: 100 }
      ],
      splitArea: { show: false },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      splitLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [{
      type: 'radar',
      areaStyle: { color: 'rgba(99, 102, 241, 0.2)' },
      lineStyle: { color: '#6366f1', width: 2 },
      itemStyle: { color: '#6366f1' },
      data: [{ value: data }]
    }]
  })
}
</script>

<style scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-header h2 {
  margin: 0;
  font-size: 20px;
  color: #1e293b;
}

.identity-card {
  background: #fff;
  border-radius: 24px;
  padding: 40px 30px;
  text-align: center;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  height: 100%;
}

.avatar-orbit {
  width: 120px;
  height: 120px;
  margin: 0 auto 30px;
  border: 2px dashed #e2e8f0;
  border-radius: 50%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-inner {
  width: 90px;
  height: 90px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 16px rgba(99, 102, 241, 0.3);
}

.user-initial {
  font-size: 40px;
  font-weight: 900;
  color: #fff;
}

.orbit-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  background: #6366f1;
  border-radius: 50%;
  border: 2px solid #fff;
}

.dot-1 { top: 10%; right: 10%; animation: float 3s infinite ease-in-out; }
.dot-2 { bottom: 10%; left: 10%; animation: float 3s infinite ease-in-out 1.5s; }

@keyframes float {
  0%, 100% { transform: translate(0,0); }
  50% { transform: translate(4px, -4px); }
}

.identity-info h3 {
  font-size: 22px;
  color: #1e293b;
  margin: 0 0 12px;
}

.identity-info .desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.stat-box {
  background: #f8fafc;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
}

.stat-box .val {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.stat-box .lab {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

.metrics-card {
  background: #fff;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 20px;
}

.radar-container {
  flex: 1;
  min-height: 300px;
}

.tags-section {
  margin-top: 20px;
}

.section-title {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 12px;
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.suggestions-section {
  background: #fff;
  border-radius: 24px;
  padding: 30px;
  border: 1px solid #f1f5f9;
}

.suggestions-section h3 {
  margin: 0 0 20px;
  font-size: 16px;
  color: #1e293b;
}

.suggestion-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.sug-item {
  display: flex;
  gap: 12px;
  background: #f8fafc;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.sug-item:hover {
  background: #fff;
  border-color: #6366f1;
  transform: translateY(-2px);
}

.sug-icon {
  color: #10b981;
  font-size: 18px;
  margin-top: 2px;
}

.sug-item p {
  margin: 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

@media (max-width: 1200px) {
  .suggestion-grid { grid-template-columns: 1fr; }
}
</style>
