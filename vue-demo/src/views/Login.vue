<template>
  <div class="project-login">
    <div class="left-panel">
      <div class="brand">
        <div class="logo">
          <!-- Inline SVG logo -->
          <svg width="48" height="48" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
            <defs>
              <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#667eea" />
                <stop offset="100%" stop-color="#764ba2" />
              </linearGradient>
              <filter id="logoShadow" x="-50%" y="-50%" width="200%" height="200%">
                <feDropShadow dx="0" dy="4" stdDeviation="6" flood-color="rgba(102, 126, 234, 0.3)" />
              </filter>
            </defs>
            <rect width="64" height="64" rx="14" fill="url(#logoGradient)" filter="url(#logoShadow)" />
            <path d="M16 40 L32 24 L48 40" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none" stroke-dasharray="16 8" stroke-dashoffset="0" />
            <circle cx="32" cy="46" r="4" fill="#fff" />
          </svg>
        </div>
        <div class="tag">校园智慧空间治理系统</div>
      </div>
      <div class="illustration"></div>
    </div>
    <div class="right-panel">
      <div class="card">
        <h1>欢迎登陆</h1>
        <p class="desc">使用企业级单点登录进入系统</p>

        <el-form ref="formRef" :model="form" label-width="0" class="login-form">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名 / 邮箱" size="large" :clearable="false">
              <template #prefix>
                <i-tabler-user style="color:#4f46e5; font-size:18px" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="密码" size="large" :clearable="false">
              <template #prefix>
                <i-tabler-lock style="color:#ef4444; font-size:18px" />
              </template>
              <template #suffix>
                <button type="button" class="pwd-toggle" @click="showPassword = !showPassword" aria-label="切换显示密码">
                  <i-tabler-eye v-if="!showPassword" style="font-size:18px;color:#64748b" />
                  <i-tabler-eye-off v-else style="font-size:18px;color:#64748b" />
                </button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item v-if="!isLogin">
            <el-input v-model="form.realName" placeholder="真实姓名（可选）" size="large">
              <template #prefix>
                <i-mdi-account-circle style="color:#06b6d4; font-size:18px" />
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" style="width:100%" @click="isLogin ? handleLogin() : handleRegister()">{{ isLogin ? '登 录' : '注 册' }}</el-button>
          </el-form-item>
        </el-form>

        <div class="foot">
          <span v-if="isLogin">还没有账号？</span>
          <span v-else>已有账户？</span>
          <el-button link @click="isLogin = !isLogin">{{ isLogin ? '立即注册' : '去登录' }}</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/accounting'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const isLogin = ref(true)
const form = ref({ username: '', password: '', realName: '' })
const showPassword = ref(false)

const handleLogin = async () => {
  console.log('=== 开始登录流程 ===')
  console.log('用户名:', form.value.username)
  console.log('密码:', form.value.password ? '***' : '')

  if (!form.value.username || !form.value.password) {
    console.warn('登录验证失败：用户名或密码为空')
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  console.log('开始调用登录API...')

  try {
    const res = await login({ username: form.value.username, password: form.value.password })
    console.log('登录API响应:', res)
    ElMessage.success(res.msg || '登录成功')

    // 缓存用户信息并标记为已验证，避免路由守卫重复验证
    try {
      const userData = res.data || {}
      console.log('准备存储的用户数据:', userData)
      // 添加时间戳和验证标记，路由守卫可以直接信任
      userData._verified = true
      userData._timestamp = Date.now()
      localStorage.setItem('currentUser', JSON.stringify(userData))
      console.log('用户数据已存储到localStorage')

      // 验证存储是否成功
      const stored = localStorage.getItem('currentUser')
      console.log('从localStorage读取的数据:', stored ? JSON.parse(stored) : null)
    } catch(e) {
      console.error('存储用户数据时出错:', e)
    }

    // 登录后跳转到校园空间导航（默认首页）
    console.log('准备跳转到首页...')
    await router.push('/')
    console.log('跳转成功')
  } catch (err) {
    console.error('登录失败:', err)
    console.error('错误详情:', {
      message: err.message,
      response: err.response?.data,
      status: err.response?.status,
      stack: err.stack
    })
    ElMessage.error(err.response?.data?.msg || err.message || '登录失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const reg = await register({ username: form.value.username, password: form.value.password, realName: form.value.realName })
    ElMessage.success(reg.msg || '注册成功')
    // 自动登录
    try {
      const res = await login({ username: form.value.username, password: form.value.password })
      try { 
        const userData = res.data || {}
        userData._verified = true
        userData._timestamp = Date.now()
        localStorage.setItem('currentUser', JSON.stringify(userData)) 
      } catch(e) {}
      ElMessage.success('已自动登录')
      // 注册后跳转到校园空间导航
      router.push('/')
      return
    } catch (e) {
      // 登录失败，回到登录模式
      isLogin.value = true
      form.value.password = ''
    }
  } catch (err) {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.project-login { display:flex; min-height:100vh; /* Make full-page gradient background */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  background-size: 400% 400%;
  animation: gradientShift 8s ease infinite;
  overflow: hidden;
  position: relative;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.project-login::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: floating 15s ease infinite;
}

@keyframes floating {
  0% { transform: rotate(0deg) translate(0, 0); }
  25% { transform: rotate(90deg) translate(20px, -20px); }
  50% { transform: rotate(180deg) translate(0, 0); }
  75% { transform: rotate(270deg) translate(-20px, 20px); }
  100% { transform: rotate(360deg) translate(0, 0); }
}
.left-panel { flex: 0 0 50%; color:#fff; padding:60px 48px; display:flex; flex-direction:column; justify-content:center; position: relative; z-index: 1; }
.brand { font-size:32px; font-weight:700; margin-bottom:16px; }
.brand .logo { font-size:48px; margin-bottom:12px; transition: transform 0.3s ease; }
.brand .logo:hover { transform: scale(1.05); }
.brand .tag { color:rgba(255,255,255,0.9); margin-bottom:32px; font-size:18px; }
.illustration { height:360px; background: url('/images/login-illus.svg') no-repeat center/contain; opacity:0.95; animation: fadeInUp 1s ease-out; }
.right-panel { flex: 0 0 50%; max-width:500px; display:flex; align-items:center; justify-content:center; padding:32px 24px; background: transparent; position: relative; z-index: 1; }
.card { width:100%; background: rgba(255, 255, 255, 0.95); padding:48px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 8px 24px rgba(0, 0, 0, 0.1); margin-left: 0; transform: translateY(0); border-radius: 20px; backdrop-filter: blur(10px); border: 1px solid rgba(255, 255, 255, 0.2); transition: all 0.3s ease; animation: slideInRight 0.8s ease-out; }

.card:hover {
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.2), 0 12px 32px rgba(0, 0, 0, 0.15);
  transform: translateY(-5px);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 0.95; transform: translateY(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(50px) translateY(20px); }
  to { opacity: 1; transform: translateX(0) translateY(0); }
}
.card h1 { margin:0 0 12px 0; font-size:28px; font-weight:700; color:#1a1a1a }
.card .desc { color:#666; margin-bottom:32px; font-size:16px; line-height:1.5 }
.login-form { margin-top:24px }
.foot { margin-top:24px; text-align:right; font-size:14px }

/* 添加卡片内部元素的间距优化 */
.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

/* 优化按钮样式 */
.login-form :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.login-form :deep(.el-button--primary):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.5);
}

.login-form :deep(.el-button--primary):active {
  transform: translateY(0);
}

/* 优化链接按钮样式 */
.login-form :deep(.el-button--link) {
  color: #667eea;
  transition: all 0.3s ease;
}

.login-form :deep(.el-button--link):hover {
  color: #764ba2;
  text-decoration: underline;
}
.pwd-toggle { background: transparent; border: none; padding: 0 6px; display:flex; align-items:center; cursor:pointer }
.pwd-toggle:focus { outline: none }

/* 优化密码切换按钮 */
.pwd-toggle {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 6px !important;
  display:flex !important;
  align-items:center !important;
  cursor:pointer !important;
  transition: all 0.3s ease !important;
}

.pwd-toggle:hover {
  transform: scale(1.1) !important;
}

.pwd-toggle:focus {
  outline: none !important;
}

/* 优化输入框样式和交互效果 */
.login-form :deep(.el-input__wrapper) {
  background: rgba(232, 240, 254, 0.96) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08) !important;
  padding: 0 12px !important;
  min-height: 50px !important;
  box-sizing: border-box !important;
  transition: all 0.3s ease !important;
  border: 2px solid transparent !important;
}

/* 输入框聚焦效果 */
.login-form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.98) !important;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3) !important;
  border-color: rgba(102, 126, 234, 0.5) !important;
  transform: translateY(-1px) !important;
}

/* 输入框悬停效果 */
.login-form :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.95) !important;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.12) !important;
}

/* 输入框内部样式 */
.login-form :deep(.el-input__inner) {
  background: transparent !important;
  border-radius: 0 !important;
  padding: 0 0 0 0 !important;
  height: 46px !important;
  line-height: 46px !important;
  font-size: 16px !important;
  transition: all 0.3s ease !important;
}

/* 输入框前缀和后缀样式 */
.login-form :deep(.el-input__prefix),
.login-form :deep(.el-input__suffix) {
  background: transparent !important;
  display: flex !important;
  align-items: center !important;
  height: 46px !important;
  transition: all 0.3s ease !important;
}

.login-form :deep(.el-input__prefix) {
  margin-right: 8px !important;
  font-size: 20px !important;
}

.login-form :deep(.el-input__suffix) {
  margin-left: 8px !important;
}

/* 输入框聚焦时图标颜色变化 */
.login-form :deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: #667eea !important;
  transform: scale(1.1) !important;
}

/* 优化占位符样式 */
.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(20, 28, 66, 0.45) !important;
  transition: all 0.3s ease !important;
}

.login-form :deep(.el-input__wrapper.is-focus .el-input__inner::placeholder) {
  color: rgba(20, 28, 66, 0.3) !important;
  transform: translateX(4px) !important;
}

/* 添加输入框内容变化时的反馈动画 */
@keyframes inputPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.01); }
  100% { transform: scale(1); }
}

.login-form :deep(.el-input__wrapper) {
  animation: inputPulse 0.3s ease-out;
}

/* 优化密码切换图标动画 */
.login-form :deep(.el-input__suffix) {
  transition: all 0.3s ease;
}

/* 移除不必要的样式重置 */
.login-form :deep(.el-input__inner),
.login-form :deep(.el-input__prefix) {
  border: none !important;
}
</style>