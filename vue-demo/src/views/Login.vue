<template>
  <div class="project-login">
    <div class="left-panel">
      <div class="brand">
        <div class="logo">
          <!-- Inline SVG logo -->
          <svg width="48" height="48" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden>
            <rect width="64" height="64" rx="12" fill="#0b84ff" />
            <path d="M16 40 L32 24 L48 40" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" fill="none" />
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
          <el-button type="text" @click="isLogin = !isLogin">{{ isLogin ? '立即注册' : '去登录' }}</el-button>
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
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
  const res = await login({ username: form.value.username, password: form.value.password })
  ElMessage.success(res.msg || '登录成功')
  try { localStorage.setItem('currentUser', JSON.stringify(res.data || {})) } catch(e) {}
  router.push('/dashboard')
  } catch (err) {
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
      try { localStorage.setItem('currentUser', JSON.stringify(res.data || {})) } catch(e) {}
      ElMessage.success('已自动登录')
      router.push('/dashboard')
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
  background: linear-gradient(135deg,#071226 0%, #0b84ff 100%);
}
.left-panel { flex: 0 0 54%; color:#fff; padding:48px; display:flex; flex-direction:column; justify-content:center; }
.brand { font-size:28px; font-weight:700; }
.brand .logo { font-size:36px; margin-bottom:8px }
.brand .tag { color:rgba(255,255,255,0.85); margin-bottom:24px }
.illustration { height:320px; background: url('/images/login-illus.svg') no-repeat center/contain; opacity:0.9 }
.right-panel { flex: 0 0 46%; max-width:480px; display:flex; align-items:center; justify-content:center; padding:24px 18px; background: transparent; }
.card { width:100%; background:#ffffff; padding:36px; box-shadow:0 10px 30px rgba(2,6,23,0.08); margin-left: -40px; transform: translateY(20px); border-radius: 12px; }
.card h1 { margin:0 0 6px 0 }
.card .desc { color:#666; margin-bottom:18px }
.login-form { margin-top:12px }
.foot { margin-top:14px; text-align:right }
.pwd-toggle { background: transparent; border: none; padding: 0 6px; display:flex; align-items:center; cursor:pointer }
.pwd-toggle:focus { outline: none }

/* Remove unintended white background behind suffix/button inside Element Plus input */
/* Use deep selector so scoped styles apply to internal input suffix elements */
.login-form ::v-deep .pwd-toggle,
.login-form ::v-deep .el-input__suffix button.pwd-toggle {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 6px !important;
}

/* Normalize el-input inner background so suffix area doesn't appear as white rectangle */
/* 强制输入框宽度一致，前缀图标区绝对定位，输入区100%宽度 */
.login-form ::v-deep .el-input {
  position: relative !important;
  width: 100% !important;
}
.login-form ::v-deep .el-input__prefix {
  position: absolute !important;
  left: 0 !important;
  top: 0 !important;
  width: 44px !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  background: #fff !important;
  z-index: 2 !important;
  border-radius: 0 !important;
  padding: 0 !important;
}
.login-form ::v-deep .el-input__inner {
  width: 100% !important;
  margin-left: 44px !important;
  background: rgba(232,240,254,0.96) !important;
  border-radius: 0 !important;
  padding: 8px 10px !important;
  box-sizing: border-box !important;
}

/* Suffix (eye icon) should be transparent and not show extra box */
.login-form ::v-deep .el-input__suffix {
  position: absolute !important;
  right: 0 !important;
  top: 0 !important;
  width: 44px !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  background: transparent !important;
  z-index: 2 !important;
  padding: 0 !important;
}
/* 让输入框整体（含图标、输入、眼睛）都在同一圆角浅蓝背景内，且高度、宽度、对齐完全一致 */
.login-form ::v-deep .el-input {
  width: 100% !important;
}
.login-form ::v-deep .el-input__wrapper {
  background: rgba(232,240,254,0.96) !important;
  border-radius: 8px !important;
  box-shadow: none !important;
  padding: 0 8px !important;
  min-height: 40px !important;
  box-sizing: border-box !important;
}
.login-form ::v-deep .el-input__inner {
  background: transparent !important;
  border-radius: 0 !important;
  padding: 0 0 0 0 !important;
  height: 38px !important;
  line-height: 38px !important;
}
.login-form ::v-deep .el-input__prefix,
.login-form ::v-deep .el-input__suffix {
  background: transparent !important;
  display: flex !important;
  align-items: center !important;
  height: 38px !important;
}
.login-form ::v-deep .el-input__prefix {
  margin-right: 4px !important;
}
.login-form ::v-deep .el-input__suffix {
  margin-left: 4px !important;
}

/* Prefix area (icon) keep card background so icon sits on white */
.login-form ::v-deep .el-input__prefix {
  /* prefix styled above */
}

/* Ensure input wrapper occupies full width */
.login-form ::v-deep .el-input {
  /* width set above */
}

/* Remove inner border on input so background looks as single block */
.login-form ::v-deep .el-input__inner,
.login-form ::v-deep .el-input__prefix {
  border: none !important;
}

.login-form ::v-deep .el-input__inner::placeholder {
  color: rgba(20,28,66,0.45) !important;
}

/* removed separate right-panel background so card sits on same full-page gradient */
</style>