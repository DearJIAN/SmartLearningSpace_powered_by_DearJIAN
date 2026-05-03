# YOLOv11 智能检测与数字人 AI 系统 (Flask 子系统)

基于 **YOLOv11** 的 Web 端目标检测系统，集成**数字人 AI**（Live2D + 语音交互），
支持图片/视频/摄像头推理、AI 对话、流式聊天、语音识别（ASR/TTS）。

本子系统由前端 Vue 通过 Vite 代理调用，同时作为智学空间主项目的 AI 能力层。

---

## 项目结构

```
my_yolo_web/
├── app.py                          # Flask 主应用（YOLO + AI + 语音）
├── ai_api.py                       # [已废弃] 旧版 AI 接口，请勿使用
├── ai_api_example.py               # AI 客户端模板（无密钥）
├── web_inference.py                # YOLOv11 检测器核心
├── yolo_io_utils.py                # IO 工具类
├── requirements.txt                # Python 依赖
├── .env                            # 环境变量（密钥，不上传）
├── .env.example                    # 环境变量模板
├── .gitignore                      # Git 忽略规则
├── backend/
│   └── services/
│       ├── langchain_service.py    # 火山方舟 ARK 大模型服务
│       ├── tts_service.py          # TTS 语音合成服务
│       ├── volc_realtime_bridge.py  # 火山实时语音桥接
│       └── volc_realtime_protocol.py
├── models/                         # YOLO 模型文件（.pt，需自行下载）
├── example/                        # 示例图片/视频
├── static/
│   ├── script.js
│   └── style.css
├── templates/
│   └── index.html
└── runs/                          # 检测结果输出（不纳入版本控制）
```

---

## 环境要求

### Python 环境

```
conda 环境: newyolo
路径: D:\TOOLS\anaconda\envs\newyolo
Python: D:\TOOLS\anaconda\envs\newyolo\python.exe
```

### Python 依赖

详见 `requirements.txt`，主要依赖：

- flask / flask-cors
- opencv-python
- ultralytics（YOLOv11）
- volcenginesdkarkruntime（火山方舟 ARK SDK）
- faster-whisper（本地 ASR）
- opencc（简繁体转换）
- websockets
- langchain / langchain-community

---

## 快速开始

### 1. 配置环境变量

```bash
cd my_yolo_web
cp .env.example .env
# 编辑 .env，填入你的 ARK_API_KEY 等配置
```

主要环境变量：

```bash
ARK_API_KEY=你的火山方舟API密钥
ARK_BASE_URL=https://ark.cn-beijing.volces.com/api/v3
ARK_MODEL=doubao-seed-1-6-251015
VOICE_REALTIME_BOT_NAME=火花
```

### 2. 安装依赖

```bash
conda activate newyolo
pip install -r requirements.txt
```

### 3. 启动服务

```bash
python app.py
```

服务运行在 `http://localhost:5000`

---

## API 接口

### YOLO 检测

| 端点 | 方法 | 说明 |
|------|------|------|
| `/` | GET | 前端页面 |
| `/video_feed` | GET | 视频流 |
| `/api/models` | GET | 列出模型 |
| `/api/examples` | GET | 列出示例 |
| `/api/start` | POST | 启动检测 |
| `/api/pause` | POST | 暂停/继续 |
| `/api/stop` | POST | 停止检测 |
| `/api/results` | GET | 获取结果 |
| `/api/clear_results` | POST | 清空结果 |
| `/api/stats` | GET | 实时统计 |

### AI 数字人

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/health` | GET | AI/语音/YOLO 综合健康状态 |
| `/api/voice/config` | GET | 语音配置状态 |
| `/api/chat` | POST | 普通 AI 对话 |
| `/api/chat/stream` | POST | 流式 AI 对话（SSE）|
| `/api/voice/chat` | POST | 实时语音对话 |
| `/api/asr` | POST | 音频上传语音识别 |
| `/api/tts/synthesize` | POST | TTS 语音合成 |
| `/api/tts/audio/<filename>` | GET | TTS 音频下载 |

---

## 注意事项

1. **环境变量**：所有 API 密钥已移至 `.env`，请勿在代码中硬编码。`.env` 不会上传至 Git。
2. **模型文件**：YOLO `.pt` 模型文件需从 ultralytics 官方下载，放入 `models/` 目录。
3. **示例文件**：`example/` 目录下的图片/视频示例文件是 YOLO 功能所需，会纳入版本控制。
4. **检测结果**：`runs/` 目录下的推理输出文件不纳入版本控制。

---

## License

MIT
