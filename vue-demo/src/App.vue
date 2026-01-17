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
          <el-icon><Location /></el-icon>
          <span>校园空间导航</span>
        </el-menu-item>

        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>教室状态监控</span>
        </el-menu-item>
        
        <el-menu-item index="/reservation" disabled>
          <el-icon><Calendar /></el-icon>
          <span>座位预约</span>
        </el-menu-item>
        
        <el-menu-item index="/accounting/login">
          <el-icon><Wallet /></el-icon>
          <span>个人记账</span>
        </el-menu-item>
        
        <el-menu-item index="/analysis" disabled>
          <el-icon><PieChart /></el-icon>
          <span>数据分析</span>
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
import { Monitor, Calendar, PieChart, Location, Wallet } from '@element-plus/icons-vue'
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
  flex: 1;
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
