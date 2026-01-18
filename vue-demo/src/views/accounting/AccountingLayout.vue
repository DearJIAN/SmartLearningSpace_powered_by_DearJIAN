<template>
  <div class="accounting-container">
    <el-container class="internal-container">
      <!-- 侧边导航 (垂直风格) -->
      <el-aside width="220px" class="side-nav">
        <div class="side-nav-header">
          <div class="icon-box">
            <el-icon style="color: #fbbf24"><i-ph-wallet-duotone /></el-icon>
          </div>
          <div class="header-text">
            <h3>记账助手</h3>
            <p>{{ userInfo.username }} ({{ userInfo.realName || '记账达人' }})</p>
          </div>
        </div>

        <el-menu
          :default-active="activeMenu"
          router
          class="elegant-menu"
        >
          <el-menu-item index="/accounting/bills">
            <el-icon style="color: #3b82f6"><i-ph-receipt-duotone /></el-icon>
            <span>账单明细</span>
          </el-menu-item>
          
          <el-menu-item index="/accounting/analysis">
            <el-icon style="color: #f43f5e"><i-ph-chart-pie-slice-duotone /></el-icon>
            <span>统计报表</span>
          </el-menu-item>
          
          <el-menu-item index="/accounting/calendar">
            <el-icon style="color: #10b981"><i-ph-calendar-blank-duotone /></el-icon>
            <span>账单日历</span>
          </el-menu-item>
          
          <el-menu-item index="/accounting/budget">
            <el-icon style="color: #6366f1"><i-ph-coin-vertical-duotone /></el-icon>
            <span>预算管理</span>
          </el-menu-item>

          <el-menu-item index="/accounting/treemap">
            <el-icon style="color: #8b5cf6"><i-ph-arrows-left-right-duotone /></el-icon>
            <span>资金流向</span>
          </el-menu-item>

          <el-menu-item index="/accounting/chat">
            <el-icon style="color: #a78bfa"><i-ph-robot-duotone /></el-icon>
            <span>AI 智能助手</span>
          </el-menu-item>

          <el-sub-menu index="insight">
            <template #title>
              <el-icon style="color: #f59e0b"><i-ph-lightbulb-duotone /></el-icon>
              <span>洞察中心</span>
            </template>
            <el-menu-item index="/accounting/insight/dashboard">
              <el-icon style="color: #0ea5e9"><i-ph-eye-duotone /></el-icon>数据总览
            </el-menu-item>
            <el-menu-item index="/accounting/insight/goal">
              <el-icon style="color: #ef4444"><i-ph-flag-duotone /></el-icon>财务目标
            </el-menu-item>
            <el-menu-item index="/accounting/insight/profile">
              <el-icon style="color: #d946ef"><i-ph-user-focus-duotone /></el-icon>用户画像
            </el-menu-item>
            <el-menu-item index="/accounting/insight/risk">
              <el-icon style="color: #f97316"><i-ph-warning-duotone /></el-icon>风险预警
            </el-menu-item>
          </el-sub-menu>

          <div class="menu-footer">
            <el-button link class="logout-btn" @click="handleLogout">
              <el-icon style="color: #ef4444"><i-ph-power-duotone /></el-icon> 退出系统
            </el-button>
          </div>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main class="content-view">
        <router-view v-slot="{ Component }">
          <transition name="slide-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

import { getCurrentUser, logout } from '@/api/accounting'

const router = useRouter()
const route = useRoute()

const userInfo = ref({
  username: '加载中...',
  realName: ''
})

const activeMenu = computed(() => route.path)

onMounted(async () => {
  try {
    const res = await getCurrentUser()
    userInfo.value = res.data
  } catch (error) {
    router.push('/accounting/login')
  }
})

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出个人记账系统吗？', '确认退出', {
      type: 'warning',
      confirmButtonText: '确定退出',
      cancelButtonText: '点错了',
      roundButton: true
    })
    
    await logout()
    router.push('/accounting/login')
  } catch (error) {}
}
</script>

<style scoped>
.accounting-container {
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: #f8fafc;
  display: flex;
}

.internal-container {
  height: 100%;
}

.side-nav {
  background: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
}

.side-nav-header {
  padding: 24px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(to right, #6366f111, transparent);
}

.icon-box {
  width: 40px;
  height: 40px;
  background: #6366f1;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.4);
}

.header-text h3 {
  margin: 0;
  font-size: 16px;
  color: #1e293b;
}

.header-text p {
  margin: 2px 0 0;
  font-size: 12px;
  color: #64748b;
}

.elegant-menu {
  border-right: none;
  flex: 1;
}

:deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
  color: #64748b;
}

:deep(.el-menu-item.is-active) {
  background: #6366f1 !important;
  color: #fff !important;
}

:deep(.el-menu-item:hover) {
  background: #f1f5f9;
}

.menu-footer {
  padding: 20px;
  border-top: 1px solid #f1f5f9;
}

.logout-btn {
  width: 100%;
  justify-content: flex-start;
  color: #ef4444;
}

.content-view {
  background: #f8fafc;
  padding: 24px;
  scroll-behavior: smooth;
}

/* Transitions */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(10px);
  opacity: 0;
}
</style>

