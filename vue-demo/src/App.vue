<template>
  <el-container class="app-wrapper">
    <!-- Sidebar -->
    <el-aside width="240px" class="sidebar-container">
      <div class="logo-container">
        <span class="logo-icon">🎓</span>
        <span class="logo-text">智学空间</span>
      </div>
      
      <el-menu
        default-active="/"
        class="sidebar-menu"
        background-color="#1f2d3d"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
          <el-menu-item index="/">
          <el-icon style="color: #22d3ee"><i-tabler-map-2 /></el-icon>
          <span>校园空间导航</span>
        </el-menu-item>

        <el-menu-item index="/dashboard">
          <el-icon style="color: #a78bfa"><i-tabler-building-broadcast-tower /></el-icon>
          <span>教室状态监控</span>
        </el-menu-item>
        
        <el-menu-item index="/reservation" disabled>
          <el-icon style="color: #34d399"><i-tabler-calendar-event /></el-icon>
          <span>座位预约</span>
        </el-menu-item>
        
        <el-menu-item index="/accounting/bills">
          <el-icon style="color: #fbbf24"><i-tabler-wallet /></el-icon>
          <span>个人记账</span>
        </el-menu-item>
        
        <el-menu-item index="/lost-found">
          <el-icon style="color: #f59e0b"><i-tabler-search /></el-icon>
          <span>失物招领</span>
        </el-menu-item>
        
        <el-menu-item index="/analysis" disabled>
          <el-icon style="color: #f87171"><i-tabler-chart-pie /></el-icon>
          <span>数据分析</span>
        </el-menu-item>

        <el-menu-item index="logout" class="menu-logout" @click="handleSidebarLogout">
          <el-icon style="color: #ef4444"><i-tabler-power /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <!-- Header -->
      <el-header class="app-header">
        <div class="header-left">
          <span class="page-title">智慧校园治理平台</span>
        </div>
        <div class="header-right">
          <el-tag effect="dark" type="success">系统运行中</el-tag>
        </div>
      </el-header>

      <!-- Main Content -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

  </el-container>
</template>

<script setup>
// Icons are now auto-imported via unplugin-icons
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/accounting'
const router = useRouter()

const handleSidebarLogout = async () => {
  try {
    await logout()
  } catch (e) {}
  try { localStorage.removeItem('currentUser') } catch(e) {}
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style>
/* Global Reset */
body {
  margin: 0;
  padding: 0;
  height: 100vh;
  overflow: hidden;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

#app {
  height: 100%;
}

.app-wrapper {
  height: 100vh;
  width: 100vw;
}

/* Sidebar Styling */
.sidebar-container {
  background-color: #1f2d3d;
  color: #fff;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
  z-index: 1001;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #2b3a42;
  overflow: hidden;
}

.logo-icon {
  font-size: 24px;
  margin-right: 12px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
  color: #fff;
}

.sidebar-menu {
  border-right: none;
  flex: none;
}

.menu-logout {
  padding: 0 20px !important;
}
.menu-logout .el-icon {
  margin-right: 8px;
}

/* Ensure logout menu item matches height and vertical alignment of other items */
.menu-logout {
  height: 56px;
  display: flex;
  align-items: center;
  font-size: 14px;
}

/* Main Container Styling */
.main-container {
  flex-direction: column;
  background-color: #f0f2f5;
  height: 100vh;
}

.app-header {
  background-color: #fff;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 1000;
}

.header-left .page-title {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.app-main {
  padding: 24px;
  overflow-y: auto;
  overflow-x: hidden;
  height: calc(100vh - 60px);
}

/* Transitions */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all .3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
