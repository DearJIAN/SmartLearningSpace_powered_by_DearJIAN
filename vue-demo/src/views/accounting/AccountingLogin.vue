<template>
  <div class="accounting-login-container">
    <div class="login-card">
      <div class="card-header">
        <h2><i class="fas fa-wallet"></i> 个人记账系统</h2>
        <p class="subtitle">{{ isLogin ? '登录您的账户' : '创建新账户' }}</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item v-if="!isLogin" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="真实姓名（可选）"
            prefix-icon="UserFilled"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isLogin ? '登 录' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="toggle-mode">
        <span>{{ isLogin ? '还没有账户？' : '已有账户？' }}</span>
        <el-button type="text" @click="isLogin = !isLogin">
          {{ isLogin ? '立即注册' : '去登录' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const formRef = ref()
const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码至少3个字符', trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  loading.value = true
  try {
    const url = isLogin.value 
      ? '/api/accounting/auth/login' 
      : '/api/accounting/auth/register'
    
    const response = await axios.post(url, form)
    
    if (response.data.code === 200) {
      ElMessage.success(response.data.msg)
      
      if (isLogin.value) {
        // 登录成功，跳转到账单列表
        router.push('/accounting/bills')
      } else {
        // 注册成功，切换到登录模式
        isLogin.value = true
        form.password = ''
      }
    } else {
      ElMessage.error(response.data.msg)
    }
  } catch (error) {
    ElMessage.error('请求失败：' + (error.response?.data?.msg || error.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.accounting-login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  color: #333;
  font-size: 28px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.card-header h2 i {
  color: #667eea;
}

.subtitle {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.toggle-mode {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.toggle-mode .el-button {
  font-size: 14px;
}
</style>
