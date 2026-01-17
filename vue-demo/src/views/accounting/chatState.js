import { ref } from 'vue'

// 全局响应式状态，用于存储 AI 助手的消息记录
export const messages = ref([])
export const isStreamingGlobal = ref(false)
