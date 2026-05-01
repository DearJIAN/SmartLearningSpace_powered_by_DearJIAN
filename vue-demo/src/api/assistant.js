export const FLASK_API_PREFIX = import.meta.env.VITE_FLASK_API_PREFIX || '/flask-api'

export const getAssistantHealth = async () => {
  const response = await fetch(`${FLASK_API_PREFIX}/health`)
  if (!response.ok) {
    throw new Error(`Flask health check failed: ${response.status}`)
  }
  return response.json()
}

export const getVoiceConfig = async () => {
  const response = await fetch(`${FLASK_API_PREFIX}/voice/config`)
  if (!response.ok) {
    throw new Error(`Voice config check failed: ${response.status}`)
  }
  return response.json()
}

export const postAsrAudio = async (blob) => {
  const formData = new FormData()
  formData.append('audio', blob, 'speech.webm')
  const response = await fetch(`${FLASK_API_PREFIX}/asr`, {
    method: 'POST',
    body: formData
  })
  const data = await response.json().catch(() => ({}))
  if (!response.ok) {
    throw new Error(data.error || `ASR failed: ${response.status}`)
  }
  return data
}

export const streamFlaskChat = async ({ message, sessionId, scene, onSession, onDelta }) => {
  const response = await fetch(`${FLASK_API_PREFIX}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message, sessionId, scene })
  })

  if (!response.ok) {
    const detail = await response.text().catch(() => '')
    throw new Error(detail || `AI request failed: ${response.status}`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('当前浏览器不支持流式响应')
  }

  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split(/\r?\n/)
    buffer = lines.pop() || ''
    for (const raw of lines) {
      const line = raw.trim()
      if (!line) continue
      if (line.startsWith('sessionId:')) {
        onSession?.(line.slice('sessionId:'.length).trim())
      } else if (line.startsWith('delta:')) {
        onDelta?.(line.slice('delta:'.length))
      } else if (line.startsWith('error:')) {
        throw new Error(line.slice('error:'.length).trim() || 'AI 流式输出失败')
      }
    }
  }
}

