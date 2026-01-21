<template>
  <div class="canteen-management-container">
    <div class="canteen-header">
      <h1 class="canteen-title">食堂智能服务</h1>
      <p class="canteen-subtitle">智能选座 · 智能点餐</p>
    </div>

    <div class="canteen-features">
      <!-- 智能选座卡片 -->
      <el-card
        class="feature-card seating-card"
        hoverable
        @click="navigateToSeating"
      >
        <div class="feature-icon seating-icon">
          <el-icon size="64"><i-tabler-armchair /></el-icon>
        </div>
        <h2 class="feature-title">智能选座</h2>
        <p class="feature-description">实时查看食堂座位分布，轻松预订理想座位</p>
        <div class="feature-action">
          <el-button type="primary" size="large" :icon="ArrowRight" circle />
        </div>
      </el-card>

      <!-- 智能点餐卡片 -->
      <el-card
        class="feature-card ordering-card"
        hoverable
        @click="navigateToOrdering"
      >
        <div class="feature-icon ordering-icon">
          <el-icon size="64"><i-tabler-clipboard-list /></el-icon>
        </div>
        <h2 class="feature-title">智能点餐</h2>
        <p class="feature-description">浏览菜品、下单支付，享受便捷用餐体验</p>
        <div class="feature-action">
          <el-button type="primary" size="large" :icon="ArrowRight" circle />
        </div>
      </el-card>
    </div>

    <!-- 系统信息卡片 -->
    <el-card class="info-card">
      <h3 class="info-title">使用指南</h3>
      <div class="info-content">
        <div class="info-item">
          <el-icon class="info-item-icon"><i-tabler-info-circle /></el-icon>
          <span>选择智能选座，查看实时座位分布并预订</span>
        </div>
        <div class="info-item">
          <el-icon class="info-item-icon"><i-tabler-info-circle /></el-icon>
          <span>选择智能点餐，浏览菜品并完成下单支付</span>
        </div>
        <div class="info-item">
          <el-icon class="info-item-icon"><i-tabler-info-circle /></el-icon>
          <span>预订成功后，系统将为您保留座位15分钟</span>
        </div>
        <div class="info-item">
          <el-icon class="info-item-icon"><i-tabler-info-circle /></el-icon>
          <span>点餐完成后，请前往取餐口凭取餐码取餐</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const navigateToSeating = () => {
  router.push('/canteen/seating')
}

const navigateToOrdering = () => {
  router.push('/canteen/ordering')
}
</script>

<style scoped>
.canteen-management-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.canteen-header {
  text-align: center;
  margin-bottom: 40px;
}

.canteen-title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.canteen-subtitle {
  font-size: 18px;
  color: #666;
}

.canteen-features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
}

.feature-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #fb923c 0%, #f97316 100%);
  transition: all 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
}

.feature-card:hover::before {
  height: 100%;
  opacity: 0.1;
}

.seating-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
}

.ordering-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
}

.feature-icon {
  text-align: center;
  margin-bottom: 20px;
}

.seating-icon {
  color: #fb923c;
}

.ordering-icon {
  color: #10b981;
}

.feature-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  text-align: center;
}

.feature-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
  text-align: center;
  line-height: 1.5;
}

.feature-action {
  text-align: center;
  margin-top: 20px;
}

.feature-action .el-button {
  background: linear-gradient(135deg, #fb923c 0%, #f97316 100%);
  border: none;
  transition: all 0.3s ease;
}

.feature-action .el-button:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 16px rgba(251, 146, 60, 0.4);
}

.info-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.info-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.info-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  color: #555;
}

.info-item-icon {
  color: #fb923c;
  font-size: 18px;
}

@media (max-width: 768px) {
  .canteen-features {
    grid-template-columns: 1fr;
  }

  .info-content {
    grid-template-columns: 1fr;
  }
}
</style>