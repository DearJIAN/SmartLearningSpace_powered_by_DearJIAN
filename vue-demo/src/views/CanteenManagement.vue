<template>
  <div class="canteen-management-container" @mousemove="spawnSparks" @click="burstSparks">
    <!-- 火花粒子层 -->
    <canvas ref="sparkCanvas" class="spark-canvas"></canvas>

    <!-- 数据流背景层 -->
    <div class="data-streams">
      <div v-for="i in 20" :key="i" class="data-stream-line"></div>
    </div>

    <!-- 粒子背景层 -->
    <div class="particles-background">
      <Particles
        id="tsparticles"
        :options="particlesOptions"
        class="tsparticles"
        @load="particlesLoaded"
      />
    </div>

    <!-- 主内容 -->
    <div class="canteen-content-box">
      <div class="canteen-3d-container">
        <!-- 头部标题 -->
        <div class="canteen-header">
          <h1 class="canteen-title">
            <span class="title-text">食堂智能服务系统</span>
            <div class="title-glow"></div>
          </h1>
          <p class="canteen-subtitle">SMART CANTEEN · INTELLIGENT FUTURE</p>
        </div>

        <!-- 功能卡片区域 -->
        <div class="canteen-features">
          <div
            class="feature-card-wrapper seating-card-wrapper"
            @click="navigateToSeating"
            @mousemove="handleTilt"
            @mouseleave="resetTilt"
          >
            <div class="feature-card-3d seating-card-3d">
              <div class="card-inner">
                <div class="card-content">
                  <div class="feature-icon seating-icon">
                    <el-icon size="80"><i-tabler-armchair /></el-icon>
                  </div>
                  <h2 class="feature-title">室内 3D 选座</h2>
                  <p class="feature-description">实时查看 2D/3D 楼层座位分布，支持扫码签到、精准室内导航，锁定专属用餐空间。</p>
                  <div class="feature-action">
                    <el-button type="primary" size="large" :icon="ArrowRight" circle class="action-btn" />
                  </div>
                </div>
                <div class="card-shine"></div>
              </div>
            </div>
          </div>

          <div
            class="feature-card-wrapper ordering-card-wrapper"
            @click="navigateToOrdering"
            @mousemove="handleTilt"
            @mouseleave="resetTilt"
          >
            <div class="feature-card-3d ordering-card-3d">
              <div class="card-inner">
                <div class="card-content">
                  <div class="feature-icon ordering-icon">
                    <el-icon size="80"><i-tabler-clipboard-list /></el-icon>
                  </div>
                  <h2 class="feature-title">极速智能点餐</h2>
                  <p class="feature-description">浏览丰富菜品，支持多渠道支付，尊享“送餐到座”服务，消费自动同步记账系统。</p>
                  <div class="feature-action">
                    <el-button type="primary" size="large" :icon="ArrowRight" circle class="action-btn" />
                  </div>
                </div>
                <div class="card-shine"></div>
              </div>
            </div>
          </div>
        </div>

        <!-- 全息操作指引 -->
        <div class="holographic-guide-container">
          <div class="guide-header">
            <div class="guide-line-left"></div>
            <h3 class="guide-title">全息服务指引流</h3>
            <div class="guide-line-right"></div>
          </div>
          
          <div class="guide-flow">
            <svg class="guide-connections" viewBox="0 0 1000 200">
              <path class="connection-path p1" d="M150 100 L350 100" />
              <path class="connection-path p2" d="M400 100 L600 100" />
              <path class="connection-path p3" d="M650 100 L850 100" />
              <circle r="4" fill="#00d2ff" class="pulse-dot">
                <animateMotion dur="3s" repeatCount="indefinite" path="M150 100 L350 100" />
              </circle>
              <circle r="4" fill="#a29bfe" class="pulse-dot">
                <animateMotion dur="3s" repeatCount="indefinite" path="M400 100 L600 100" />
              </circle>
              <circle r="4" fill="#ff7675" class="pulse-dot">
                <animateMotion dur="3s" repeatCount="indefinite" path="M650 100 L850 100" />
              </circle>
            </svg>

            <div class="step-nodes">
              <div v-for="(step, index) in guideSteps" :key="index" class="holographic-step-node">
                <div class="step-neon-box">
                  <div class="neon-ring">
                    <span class="step-index">{{ index + 1 }}</span>
                    <div class="ring-track"></div>
                    <div class="ring-active"></div>
                  </div>
                  <div class="step-card">
                    <div class="step-scan-line"></div>
                    <p class="step-text">{{ step }}</p>
                    <div class="step-corners">
                      <span></span><span></span><span></span><span></span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import gsap from 'gsap'

const router = useRouter()
const sparkCanvas = ref(null)

const guideSteps = ref([
  '开启 2D/3D 智能选座，快速锁定理想楼层与座位',
  '一键预约后开启导航，抵达座位后扫码签到激活',
  '进入智能点餐界面，挑选美食并填写配送座位号',
  '支持多渠道快捷支付，餐品精准配送至指定座位'
])

const navigateToSeating = () => {
  router.push('/canteen/seating')
}

const navigateToOrdering = () => {
  router.push('/canteen/ordering')
}

/**
 * 3D 倾斜逻辑
 */
const handleTilt = (e) => {
  const card = e.currentTarget
  const inner = card.querySelector('.feature-card-3d')
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  const rotateX = (centerY - y) / (rect.height / 15)
  const rotateY = (x - centerX) / (rect.width / 15)
  
  gsap.to(inner, {
    rotateX, rotateY, scale: 1.08,
    duration: 0.4, ease: 'power2.out',
    transformPerspective: 1000
  })

  const shine = inner.querySelector('.card-shine')
  const px = (x / rect.width) * 100
  const py = (y / rect.height) * 100
  gsap.to(shine, {
    background: `radial-gradient(circle at ${px}% ${py}%, rgba(255,255,255,0.3), transparent 80%)`,
    duration: 0.2
  })
}

const resetTilt = (e) => {
  const inner = e.currentTarget.querySelector('.feature-card-3d')
  const shine = inner.querySelector('.card-shine')
  gsap.to(inner, {
    rotateX: 0, rotateY: 0, scale: 1,
    duration: 1.5, ease: 'elastic.out(1, 0.3)'
  })
  gsap.to(shine, {
    background: `radial-gradient(circle at 50% 50%, rgba(255,255,255,0.1), transparent 80%)`,
    duration: 0.5
  })
}

/**
 * 火花粒子系统 - 使用 GSAP
 */
let ctx, width, height
const particles = []
const colors = ['#00d2ff', '#a29bfe', '#fab1a0', '#ff7675']
let autoSparkTimer = null

const spawnSparks = (e) => {
  if (!ctx) return
  for (let i = 0; i < 2; i++) {
    createParticle(e.clientX, e.clientY)
  }
}

const burstSparks = (e) => {
  if (!ctx) return
  for (let i = 0; i < 40; i++) { // 数量从 15 增加到 40
    createParticle(e.clientX, e.clientY, true)
  }
}

// 随机位置自主炸裂逻辑 - 爆发频率大幅提升
const randomBurst = () => {
  if (!ctx) return
  const rx = Math.random() * width
  const ry = Math.random() * height
  for (let i = 0; i < 30; i++) { // 数量从 12 增加到 30
    createParticle(rx, ry, true)
  }
  
  // 随机下一次炸裂的时间 (0.5-2秒) - 频率从 2-5s 缩短
  autoSparkTimer = setTimeout(randomBurst, Math.random() * 1500 + 500)
}

const createParticle = (x, y, burst = false) => {
  const p = {
    x, y,
    size: Math.random() * 5 + 3, // 尺寸从 1-4 增加到 3-8
    color: colors[Math.floor(Math.random() * colors.length)],
    alpha: 1,
    vx: (Math.random() - 0.5) * (burst ? 18 : 4), // 初速度翻倍，炸裂感更强
    vy: (Math.random() - 0.5) * (burst ? 18 : 4),
    life: burst ? 1.8 : 1.0
  }
  
  particles.push(p)
  
  gsap.to(p, {
    x: p.x + p.vx * 60, // 扩散距离更远
    y: p.y + p.vy * 60,
    alpha: 0,
    size: 0,
    duration: p.life,
    ease: 'power3.out', // 缓动更硬朗
    onComplete: () => {
      const index = particles.indexOf(p)
      if (index > -1) particles.splice(index, 1)
    }
  })
}

const renderSparks = () => {
  if (!ctx) return
  ctx.clearRect(0, 0, width, height)
  particles.forEach(p => {
    ctx.globalAlpha = p.alpha
    ctx.fillStyle = p.color
    ctx.beginPath()
    ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    ctx.fill()
    ctx.shadowBlur = 20 // 阴影发光更强
    ctx.shadowColor = p.color
  })
  requestAnimationFrame(renderSparks)
}

const handleResize = () => {
  width = sparkCanvas.value.width = window.innerWidth
  height = sparkCanvas.value.height = window.innerHeight
}

/**
 * 粒子配置
 */
const particlesOptions = {
  fullScreen: { enable: false },
  background: { color: 'transparent' },
  particles: {
    number: { value: 60, density: { enable: true, area: 800 } },
    color: { value: colors },
    shape: { type: 'circle' },
    opacity: { value: 0.5, random: true },
    size: { value: { min: 1, max: 4 } },
    links: { enable: true, distance: 150, color: '#ffffff', opacity: 0.2, width: 1 },
    move: { enable: true, speed: 1, direction: 'none', random: true, outMode: 'out' }
  }
}

onMounted(() => {
  // 初始化画布
  ctx = sparkCanvas.value.getContext('2d')
  handleResize()
  window.addEventListener('resize', handleResize)
  renderSparks()

  // 数据流背景
  gsap.utils.toArray('.data-stream-line').forEach((line) => {
    const duration = gsap.utils.random(3, 7)
    const delay = gsap.utils.random(0, 4)
    gsap.set(line, { left: `${gsap.utils.random(0, 100)}%`, top: '-30%', opacity: 0 })
    gsap.to(line, {
      top: '130%', opacity: 0.6, duration, delay, repeat: -1, ease: 'none',
      onRepeat: () => { gsap.set(line, { left: `${gsap.utils.random(0, 100)}%` }) }
    })
  })

  // 入场动画
  const tl = gsap.timeline({ defaults: { ease: 'power4.out' } })
  gsap.set(['.title-text', '.canteen-subtitle', '.feature-card-wrapper', '.holographic-guide-container', '.holographic-step-node'], { 
    opacity: 0, y: 80 
  })

  tl.to('.title-text', { opacity: 1, y: 0, duration: 1.5, clearProps: 'transform' })
    .to('.canteen-subtitle', { opacity: 1, y: 0, duration: 1.2 }, '-=1')
    .to('.feature-card-wrapper', { 
      opacity: 1, y: 0, stagger: 0.3, duration: 1.8, ease: 'back.out(2)', clearProps: 'transform'
    }, '-=0.8')
    .to('.holographic-guide-container', { opacity: 1, y: 0, duration: 1.2 }, '-=1.2')
    .to('.holographic-step-node', { 
      opacity: 1, y: 0, stagger: 0.2, duration: 1, ease: 'power2.out'
    }, '-=0.6')

  // 启动自主随机炸裂
  randomBurst()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (autoSparkTimer) clearTimeout(autoSparkTimer)
})
</script>

<style scoped>
.canteen-management-container {
  width: 100%; min-height: 100vh; position: relative; overflow-x: hidden;
  background: radial-gradient(circle at 50% 50%, #161b22 0%, #0b0e14 100%);
  display: flex; justify-content: center;
}

.spark-canvas {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none; z-index: 50;
}

.canteen-content-box { width: 100%; max-width: 1600px; padding: 100px 40px; position: relative; z-index: 10; }

.data-streams { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 1; overflow: hidden; }
.data-stream-line { 
  position: absolute; width: 4px; height: 300px;
  background: linear-gradient(to bottom, transparent, rgba(0, 210, 255, 0.4), #00d2ff, white);
  box-shadow: 0 0 25px rgba(0, 210, 255, 0.8); border-radius: 10px;
}

.particles-background { position: absolute; top: 0; left: 0; width: 100%; height: 100%; pointer-events: none; z-index: 2; }
.tsparticles { width: 100%; height: 100%; }

.canteen-header { text-align: center; margin-bottom: 80px; }
.canteen-title { font-size: 72px; font-weight: 900; margin-bottom: 30px; }
.title-text { 
  background: linear-gradient(to right, #6c5ce7, #00d2ff, #6c5ce7); background-size: 200% auto;
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  animation: shine-text 5s linear infinite; filter: drop-shadow(0 0 15px rgba(108, 92, 231, 0.5));
}
@keyframes shine-text { to { background-position: 200% center; } }
.canteen-subtitle { font-size: 24px; color: rgba(255, 255, 255, 0.4); letter-spacing: 0.8em; font-weight: 200; }

.canteen-features { display: flex; justify-content: center; gap: 100px; flex-wrap: wrap; margin-bottom: 120px; perspective: 2500px; }
.feature-card-wrapper { width: 500px; height: 650px; cursor: pointer; }
.feature-card-3d { 
  width: 100%; height: 100%; position: relative; transform-style: preserve-3d; border-radius: 45px;
  padding: 60px; transition: transform 0.1s ease; overflow: hidden;
}
.seating-card-3d { 
  background: linear-gradient(135deg, rgba(108, 92, 231, 0.25) 0%, rgba(0, 210, 255, 0.1) 100%);
  border: 2px solid rgba(162, 155, 254, 0.4); box-shadow: 0 40px 80px -15px rgba(0, 0, 0, 0.8);
}
.ordering-card-3d {
  background: linear-gradient(135deg, rgba(234, 128, 40, 0.25) 0%, rgba(255, 71, 87, 0.1) 100%);
  border: 2px solid rgba(234, 128, 40, 0.4); box-shadow: 0 40px 80px -15px rgba(0, 0, 0, 0.8);
}

.card-inner { transform: translateZ(120px); text-align: center; width: 100%; pointer-events: none; }
.feature-icon { margin-bottom: 50px; transform: translateZ(50px); filter: drop-shadow(0 0 40px currentColor); }
.seating-card-3d .feature-icon { color: #a29bfe; }
.ordering-card-3d .feature-icon { color: #fab1a0; }
.feature-title { font-size: 48px; font-weight: 800; color: #fff; margin-bottom: 30px; letter-spacing: 2px; }
.feature-description { font-size: 20px; color: rgba(255, 255, 255, 0.6); line-height: 1.8; margin-bottom: 60px; }
.action-btn { transform: translateZ(100px); padding: 25px !important; font-size: 28px !important; }
.card-shine { position: absolute; top: 0; left: 0; right: 0; bottom: 0; border-radius: 45px; pointer-events: none; z-index: 10; }

.holographic-guide-container { margin-top: 100px; width: 100%; position: relative; }
.guide-header { display: flex; align-items: center; justify-content: center; gap: 30px; margin-bottom: 80px; }
.guide-line-left, .guide-line-right { height: 2px; flex: 1; max-width: 200px; background: linear-gradient(to right, transparent, #00d2ff); }
.guide-line-right { background: linear-gradient(to left, transparent, #00d2ff); }
.guide-title { font-size: 32px; color: #fff; font-weight: 800; letter-spacing: 5px; text-shadow: 0 0 20px rgba(0, 210, 255, 0.6); }

.guide-flow { position: relative; width: 100%; min-height: 350px; }
.guide-connections { position: absolute; top: 40px; left: 0; width: 100%; height: 100px; pointer-events: none; }
.connection-path { fill: none; stroke: rgba(0, 210, 255, 0.2); stroke-width: 2; stroke-dasharray: 6, 6; }

.step-nodes { display: flex; justify-content: space-between; position: relative; z-index: 5; }
.holographic-step-node { flex: 1; display: flex; flex-direction: column; align-items: center; }

.step-neon-box { position: relative; display: flex; flex-direction: column; align-items: center; gap: 40px; }

.neon-ring {
  width: 90px; height: 90px; position: relative; display: flex; align-items: center; justify-content: center;
  background: rgba(0, 210, 255, 0.08); border-radius: 50%; border: 1px solid rgba(0, 210, 255, 0.3);
  box-shadow: 0 0 30px rgba(0, 210, 255, 0.2);
}
.step-index { font-size: 36px; font-weight: 900; color: #fff; text-shadow: 0 0 15px #00d2ff; }
.ring-track { position: absolute; inset: -8px; border: 2px dashed rgba(0, 210, 255, 0.15); border-radius: 50%; animation: spin 25s linear infinite; }
.ring-active { position: absolute; inset: -12px; border: 3px solid #00d2ff; border-radius: 50%; clip-path: polygon(0 0, 50% 0, 50% 50%, 0 50%); animation: spin 4s linear infinite; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.step-card {
  width: 280px; padding: 40px; background: rgba(255, 255, 255, 0.04); backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12); border-radius: 20px; position: relative; overflow: hidden;
  transition: all 0.5s cubic-bezier(0.23, 1, 0.32, 1);
}
.step-card:hover { 
  transform: translateY(-15px) scale(1.05); background: rgba(255, 255, 255, 0.08);
  border-color: rgba(0, 210, 255, 0.6); box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6), 0 0 30px rgba(0, 210, 255, 0.3);
}

.step-scan-line {
  position: absolute; top: 0; left: 0; width: 100%; height: 3px;
  background: linear-gradient(to right, transparent, rgba(0, 210, 255, 0.8), transparent);
  animation: scan 4s linear infinite;
}
@keyframes scan { from { top: -10%; } to { top: 110%; } }

.step-text { font-size: 18px; color: rgba(255, 255, 255, 0.85); line-height: 1.8; text-align: center; }

.step-corners span { position: absolute; width: 12px; height: 12px; border: 2px solid #00d2ff; opacity: 0.6; }
.step-corners span:nth-child(1) { top: 0; left: 0; border-right: none; border-bottom: none; }
.step-corners span:nth-child(2) { top: 0; right: 0; border-left: none; border-bottom: none; }
.step-corners span:nth-child(3) { bottom: 0; left: 0; border-right: none; border-top: none; }
.step-corners span:nth-child(4) { bottom: 0; right: 0; border-left: none; border-top: none; }

.pulse-dot { filter: drop-shadow(0 0 10px currentColor); }
</style>