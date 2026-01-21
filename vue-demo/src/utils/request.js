import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
    baseURL: '', // 使用相对路径以配合 Vite 代理
    timeout: 10000,
    withCredentials: true // 允许发送跨域凭证（session cookie）
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        console.log('=== API请求 ===')
        console.log('请求URL:', config.baseURL + config.url)
        console.log('请求方法:', config.method?.toUpperCase())
        console.log('请求数据:', config.data)
        console.log('请求头:', config.headers)
        // 可以在这里添加 Token 等鉴权信息
        return config
    },
    error => {
        console.error('请求拦截器错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        console.log('=== API响应成功 ===')
        console.log('请求URL:', response.config.url)
        console.log('响应状态:', response.status)
        console.log('响应数据:', response.data)

        const res = response.data
        if (res.code !== 200) {
            console.error('业务逻辑错误:', res.msg)
            ElMessage.error(res.msg || 'Error')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        console.log('业务逻辑成功')
        return res
    },
    error => {
        console.error('=== API响应错误 ===')
        console.error('请求URL:', error.config?.url)
        console.error('错误信息:', error.message)
        console.error('错误详情:', error)

        const msg = error.response?.data?.msg || error.message || '网络错误'
        console.error('显示错误消息:', msg)
        ElMessage.error(msg)
        return Promise.reject(error)
    }
)

export default service
