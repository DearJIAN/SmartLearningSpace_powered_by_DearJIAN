<template>
  <div class="map-view-container">
    <div class="map-wrapper">
      <!-- 强化背景：使用模糊的背景填充边缘空洞 -->
      <div class="map-background-blur" :style="{ backgroundImage: `url(${mapUrl})` }"></div>
      
      <!-- 校园地图主体 -->
      <div class="map-content">
        <img :src="mapUrl" alt="Campus Map" class="campus-map-img" />

        <!-- 空间节点 -->
        <div 
          v-for="node in spatialNodes" 
          :key="node.id"
          class="spatial-node"
          :style="{ left: node.x + '%', top: node.y + '%' }"
        >
          <el-tooltip
            effect="dark"
            :content="node.description"
            placement="top"
          >
            <div 
              class="node-marker" 
              :class="[
                'node-marker-' + node.name.toLowerCase().replace(/\s+/g, '-'),
                {'lost-found': node.id === 5}
              ]"
              @click="navigateTo(node.path)"
            >
              <div class="marker-dot"></div>
              <div class="marker-label">{{ node.name }}</div>
            </div>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 顶栏叠加层 - 调整位置避免重叠 -->
    <div class="map-overlay-header">
      <div class="header-glass-card">
        <h3>🏛️ 校园空间概览</h3>
        <p>点击地图节点进入对应管理模块</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import mapUrl from '../assets/Map.jpg'

const router = useRouter()

console.log('=== MapView 组件加载 ===')
console.log('地图图片路径:', mapUrl)

onMounted(() => {
  console.log('=== MapView 组件已挂载 ===')
  console.log('空间节点数量:', spatialNodes.value.length)
})

/**
 * 空间节点坐标修正说明 (基于用户标记图):
 * 1. 1号教学楼 (红色矩形旁蓝色矩形): x: 69.5, y: 43.5
 * 2. 校图书馆 (中间红色矩形): x: 60.5, y: 43.5
 * 3. 校园食堂 (蓝色椭圆下绿色矩形): x: 34.0, y: 43.5
 * 4. 学生公寓 (蓝色椭圆): x: 34.0, y: 26.5
 * 5. 失物招领 (中心位置): x: 50.0, y: 50.0
 */
const spatialNodes = ref([
  {
    id: 1,
    name: '1号教学楼',
    description: '查看实时教室占用与学生专注度看板',
    x: 69.5,
    y: 30,
    path: '/dashboard'
  },
  {
    id: 2,
    name: '校图书馆',
    description: '座位预约与自习位监控',
    x: 60.5,
    y: 43.5,
    path: '/seat'
  },
  {
    id: 3,
    name: '校园食堂',
    description: '食堂智能服务与点餐系统',
    x: 34.0,
    y: 43.5,
    path: '/canteen'
  },
  {
    id: 4,
    name: '个人记账',
    description: '个人财务记录与账单管理',
    x: 34.0,
    y: 26.5,
    path: '/accounting/bills'
  },
  {
    id: 5,
    name: '失物招领',
    description: '失物智能辅助与查询系统',
    x: 51.0,
    y: 30.0,
    path: '/lost-found'
  }
])

const navigateTo = (path) => {
  console.log('导航到:', path)
  if (path && path !== '#') {
    router.push(path)
  }
}
</script>

<style scoped>
.map-view-container {
  width: 100%;
  height: 100%;
  position: relative;
  background-color: #1a1a1a;
  overflow: hidden;
}

.map-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 背景模糊填充，防止两侧空洞 */
.map-background-blur {
  position: absolute;
  top: -10%;
  left: -10%;
  width: 120%;
  height: 120%;
  background-size: cover;
  background-position: center;
  filter: blur(40px) brightness(0.4);
  z-index: 1;
}

.map-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.campus-map-img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  box-shadow: 0 0 50px rgba(0,0,0,0.5);
  border-radius: 4px;
}

/* 空间节点样式 */
.spatial-node {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  z-index: 10;
}

/* 功能节点分类样式 */
.node-marker-personal-accounting .marker-dot {
  background-color: #f59e0b;
}

.node-marker-seat-booking .marker-dot {
  background-color: #34d399;
}

.node-marker-canteen .marker-dot {
  background-color: #fb923c;
}

/* 完全移除失物招领按钮的特殊效果 */
.node-marker-lost-found {
  animation: none !important;
}

.node-marker-lost-found .marker-dot {
  background-color: #8b5cf6;
  width: 14px;
  height: 14px;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(139, 92, 246, 0.8);
  animation: none !important;
}

.node-marker-lost-found:hover {
  transform: translate(-50%, -50%) scale(1.15);
  box-shadow: 0 0 15px rgba(139, 92, 246, 0.8);
}

.node-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.marker-dot {
  width: 14px;
  height: 14px;
  background-color: #409EFF;
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(64, 158, 255, 0.8);
  animation: pulse 2s infinite;
}

.marker-label {
  margin-top: 6px;
  padding: 4px 10px;
  background-color: rgba(26, 26, 26, 0.8);
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
  backdrop-filter: blur(4px);
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.node-marker:hover {
  transform: scale(1.15);
}

/* 不同功能节点的颜色 */
.node-marker.personal-accounting:hover .marker-dot {
  background-color: #f59e0b;
  box-shadow: 0 0 15px rgba(245, 158, 11, 0.8);
}

.node-marker.seat-booking:hover .marker-dot {
  background-color: #34d399;
  box-shadow: 0 0 15px rgba(52, 211, 153, 0.8);
}

.node-marker.canteen:hover .marker-dot {
  background-color: #fb923c;
  box-shadow: 0 0 15px rgba(251, 146, 60, 0.8);
}

.node-marker.lost-found:hover .marker-dot {
  background-color: #8b5cf6;
  box-shadow: 0 0 20px rgba(139, 92, 246, 0.9);
}

.node-marker.dashboard:hover .marker-dot {
  background-color: #a78bfa;
  box-shadow: 0 0 15px rgba(167, 139, 250, 0.8);
}

.node-marker:hover .marker-dot {
  background-color: #67C23A;
  box-shadow: 0 0 15px rgba(103, 194, 58, 0.8);
}

/* 3D悬浮效果 */
@keyframes float3d {
  0% { 
    transform: translate(-50%, -50%) scale(1) rotateX(0deg) rotateY(0deg);
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.6);
  }
  33% { 
    transform: translate(-50%, -50%) scale(1.05) rotateX(5deg) rotateY(5deg);
    box-shadow: 0 0 30px rgba(64, 158, 255, 0.8);
  }
  66% { 
    transform: translate(-50%, -50%) scale(1.05) rotateX(-5deg) rotateY(-5deg);
    box-shadow: 0 0 30px rgba(64, 158, 255, 0.8);
  }
  100% { 
    transform: translate(-50%, -50%) scale(1) rotateX(0deg) rotateY(0deg);
    box-shadow: 0 0 20px rgba(64, 158, 255, 0.6);
  }
}

/* 呼吸动画 */
@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.7); }
  70% { box-shadow: 0 0 0 10px rgba(64, 158, 255, 0); }
  100% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0); }
}

/* 叠加页头样式 */
.map-overlay-header {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 20;
}

.header-glass-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.map-overlay-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
  font-weight: 600;
}

.map-overlay-header p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #7f8c8d;
}
</style>
