<template>
  <div class="insight-dashboard">
    <!-- Section 1: Dynamic Summary -->
    <div class="summary-section">
      <div class="welcome-text">
        <h2>财务洞察报告</h2>
        <p>基于您近期的收支行为，AI 为您准备了以下分析</p>
      </div>
      <div class="ai-status">
        <el-tag type="success" effect="dark" round>
          <el-icon><MagicStick /></el-icon> 实时洞察已开启
        </el-tag>
      </div>
    </div>

    <!-- Section 2: KPIs & Alerts -->
    <el-row :gutter="20" class="kpi-row">
      <el-col :span="16">
        <div class="main-stats-card">
          <div class="card-content">
            <div class="insight-label">综合财务建议</div>
            <p class="insight-text">{{ dashboardData.summary || '您的财务状况稳健，建议继续保持。AI 正在为您挖掘更多省钱技巧...' }}</p>
            <div class="stat-highlights">
              <div class="highlight-item">
                <span class="num">{{ dashboardData.savingRate || '25' }}%</span>
                <span class="lab">月储蓄率</span>
              </div>
              <div class="highlight-divider"></div>
              <div class="highlight-item">
                <span class="num">¥{{ dashboardData.dailyAvg || '0' }}</span>
                <span class="lab">日均支出</span>
              </div>
            </div>
          </div>
          <div class="card-deco">
             <i class="fas fa-brain"></i>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="alert-card" :class="{ 'warning': dashboardData.riskCount > 0 }">
          <div class="alert-header">
            <el-icon><WarningFilled /></el-icon>
            <span>风险预警</span>
          </div>
          <div class="alert-body">
            <div class="count">{{ dashboardData.riskCount || 0 }}</div>
            <div class="label">项待关注异常</div>
            <el-button link type="primary" @click="router.push('/accounting/insight/risk')">查看详情 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Section 3: Detailed Tiles -->
    <el-row :gutter="20" class="grid-row">
      <el-col :span="8">
        <div class="tile-card">
          <div class="tile-header">
            <el-icon class="icon-orange"><PieChart /></el-icon>
            <h3>消费大户</h3>
          </div>
          <div class="tile-body">
            <div v-for="cat in (dashboardData.topCategories || [])" :key="cat.name" class="cat-item">
              <div class="cat-info">
                <span class="name">{{ cat.name }}</span>
                <span class="val">¥{{ cat.amount }}</span>
              </div>
              <el-progress :percentage="cat.percent" :color="cat.color || '#f59e0b'" stroke-width="8" :show-text="false" />
            </div>
            <el-empty v-if="!dashboardData.topCategories?.length" :image-size="40" description="暂无分类数据" />
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="tile-card">
          <div class="tile-header">
            <el-icon class="icon-indigo"><Flag /></el-icon>
            <h3>目标追踪</h3>
          </div>
          <div class="tile-body center">
            <el-progress 
              type="dashboard" 
              :percentage="dashboardData.goalProgress || 0" 
              :width="140"
              :color="[
                { color: '#f56c6c', percentage: 20 },
                { color: '#e6a23c', percentage: 40 },
                { color: '#5cb87a', percentage: 60 },
                { color: '#1989fa', percentage: 80 },
                { color: '#6366f1', percentage: 100 }
              ]"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
                <span class="percentage-label">储蓄目标</span>
              </template>
            </el-progress>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="tile-card">
          <div class="tile-header">
            <el-icon class="icon-green"><TrendCharts /></el-icon>
            <h3>财务画像</h3>
          </div>
          <div class="tile-body profile-tile">
            <div class="user-grade">
              <span class="grade-label">信用等阶</span>
              <span class="grade-value">{{ dashboardData.grade || 'A' }}</span>
            </div>
            <div class="tags-cloud">
              <el-tag v-for="tag in (dashboardData.userTags || ['理智消费', '储蓄达人'])" :key="tag" effect="plain" round size="small">
                {{ tag }}
              </el-tag>
            </div>
            <el-button type="primary" plain round class="w-full" @click="router.push('/accounting/insight/profile')">
              查看完整画像
            </el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MagicStick, WarningFilled, ArrowRight, PieChart, Flag, TrendCharts } from '@element-plus/icons-vue'
import { getInsightDashboard } from '@/api/accounting'

const router = useRouter()
const dashboardData = ref({
  summary: '',
  savingRate: 0,
  dailyAvg: 0,
  riskCount: 0,
  topCategories: [],
  goalProgress: 0,
  grade: 'A',
  userTags: []
})

onMounted(() => {
  fetchData()
})

const fetchData = async () => {
  try {
    const res = await getInsightDashboard()
    if (res.data) {
       dashboardData.value = res.data
    }
  } catch (err) {
    console.error('Fetch dashboard error:', err)
  }
}
</script>

<style scoped>
.insight-dashboard {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.summary-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0;
  font-size: 24px;
  color: #1e293b;
}

.welcome-text p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 14px;
}

.main-stats-card {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  border-radius: 24px;
  padding: 30px;
  display: flex;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  box-shadow: 0 12px 20px -5px rgba(99, 102, 241, 0.3);
}

.card-content {
  position: relative;
  z-index: 2;
  flex: 1;
}

.insight-label {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.8;
  margin-bottom: 12px;
}

.insight-text {
  font-size: 18px;
  line-height: 1.6;
  font-weight: 500;
  margin-bottom: 24px;
  max-width: 80%;
}

.stat-highlights {
  display: flex;
  align-items: center;
  gap: 40px;
}

.highlight-item {
  display: flex;
  flex-direction: column;
}

.highlight-item .num {
  font-size: 26px;
  font-weight: 800;
}

.highlight-item .lab {
  font-size: 12px;
  opacity: 0.7;
}

.highlight-divider {
  width: 1px;
  height: 34px;
  background: rgba(255,255,255,0.2);
}

.card-deco {
  position: absolute;
  right: -20px;
  bottom: -20px;
  font-size: 140px;
  opacity: 0.08;
  transform: rotate(-15deg);
}

.alert-card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
}

.alert-card.warning {
  border-color: #fee2e2;
  background: #fef2f2;
}

.alert-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ef4444;
  font-weight: 600;
  margin-bottom: 20px;
}

.alert-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.alert-body .count {
  font-size: 48px;
  font-weight: 800;
  color: #ef4444;
  line-height: 1;
}

.alert-body .label {
  color: #64748b;
  font-size: 14px;
  margin: 8px 0 16px;
}

.tile-card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  height: 340px;
  display: flex;
  flex-direction: column;
  border: 1px solid #f1f5f9;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.tile-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.tile-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
  font-weight: 600;
}

.tile-header .el-icon {
  font-size: 22px;
}

.icon-orange { color: #f59e0b; }
.icon-indigo { color: #6366f1; }
.icon-green { color: #10b981; }

.tile-body {
  flex: 1;
  overflow-y: auto;
}

.tile-body.center {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cat-item {
  margin-bottom: 18px;
}

.cat-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.cat-info .name {
  font-size: 13px;
  color: #475569;
}

.cat-info .val {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.percentage-value {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
}

.percentage-label {
    display: block;
    font-size: 12px;
    color: #94a3b8;
}

.profile-tile {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.user-grade {
  text-align: center;
  margin-bottom: 24px;
}

.grade-label {
  display: block;
  font-size: 12px;
  color: #94a3b8;
}

.grade-value {
  font-size: 48px;
  font-weight: 900;
  background: linear-gradient(135deg, #10b981, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 24px;
}

.w-full { width: 100%; }
</style>
