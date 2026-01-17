import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
// 2. 引入它的样式文件（必须引，不然没有颜色）
import 'element-plus/dist/index.css'

const app = createApp(App)

// 3. 注册使用
app.use(ElementPlus)
app.use(router)
app.mount('#app')