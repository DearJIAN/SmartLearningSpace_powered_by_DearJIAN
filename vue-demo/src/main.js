import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// 1. 引入 Element Plus
import ElementPlus from 'element-plus'
// 2. 引入它的样式文件
import 'element-plus/dist/index.css'

// 3. 引入 Particles
import Particles from '@tsparticles/vue3'
import { loadFull } from 'tsparticles'

const app = createApp(App)

// 4. 注册使用
app.use(ElementPlus)
app.use(router)

// 修改这里的注册方式：需要一个 init 函数来加载粒子引擎
app.use(Particles, {
    init: async engine => {
        await loadFull(engine); // 这会加载所有插件
    }
})

app.mount('#app')