import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
    baseURL: '', // 使用相对路径以配合 Vite 代理
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 可以在这里添加 Token 等鉴权信息
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || 'Error')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        console.error('API Error:', error)
        const msg = error.response?.data?.msg || error.message || '网络错误'
        ElMessage.error(msg)
        return Promise.reject(error)
    }
)

export default service
