# 项目上下文引导 Prompt (Project Context Prompt)

请将以下内容作为开启新对话时的首条指令发送给 AI 助手，以使其快速进入开发状态。

---

## 🚀 项目概览
本项目是一个完整的**智学空间·校园智慧空间治理系统**，包含多个核心模块：
1. **教室状态实时监控**：基于AI视觉识别的教室人数统计、专注度分析、设备管理
2. **校园空间导航**：校园地图和空间可视化
3. **座位预约**：教室座位在线预约功能（待开发）
4. **个人记账与财务洞察**：账单管理、多维统计分析、AI财务助手
5. **数据分析中心**：校园运营数据综合分析（待开发）

## 🛠️ 技术栈
### 后端 (Backend)
- **核心框架**: Java 8 / Spring Boot
- **数据库组件**: MyBatis-Plus + MySQL
- **工具类**: Apache POI (Excel 导出), 日期处理
- **接口规范**: 典型的 RESTful API，返回格式为 `Result<T>` 对象

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API / <script setup>)
- **UI 组件库**: Element Plus
- **数据可视化**: ECharts 5
- **日期处理**: Day.js
- **通信**: Axios

### AI 视觉识别 (AI Vision)
- **核心框架**: Python 3.8+ / Flask
- **目标检测**: YOLO 模型
- **视频流处理**: OpenCV
- **AI 对话**: 豆包 API 集成

## 📂 项目文件结构 (Project Structure)

### 核心模块说明
- **后端 (`smart-campus-backend`)**:
  - `com.smartcampus.controller`: 多模块控制器（账单、分析、预算、AI、教室管理、统计分析等）
  - `com.smartcampus.service`: 核心业务逻辑实现
  - `com.smartcampus.entity`: 数据库映射实体（账单、分类、预算、教室、用户、统计日志等）
  - `com.smartcampus.mapper`: MyBatis-Plus Mapper 接口
  - `com.smartcampus.common`: 通用结果封装 (Result)
  - `com.smartcampus.config`: 配置类
  
- **前端 (`vue-demo`)**:
  - `src/views/`: 核心视图组件
    - `DashboardView.vue`: 教室状态实时监控看板
    - `MapView.vue`: 校园空间导航
    - `accounting/`: 个人记账与财务洞察模块
      - `insight/`: 洞察中心子模块
      - `BillList.vue`: 账单明细列表
      - `BillAnalysis.vue`: 财务统计报表
      - `BillCalendar.vue`: 账单日历视图
      - `AiChat.vue`: AI智能助手
  - `src/components/`: 通用组件（FocusTrendChart、FloatingSimulator等）
  - `src/api/`: API接口封装
  - `src/utils/`: 工具类
  - `src/router/`: 路由配置
  
- **AI 视觉识别 (`my_yolo_web`)**:
  - `app.py`: Flask 主应用
  - `web_inference.py`: YOLO 检测核心逻辑
  - `ai_api.py`: AI 对话接口
  - `static/`: 静态资源
  - `templates/`: HTML 模板

### 目录树 (Directory Tree)
```text
E:\LEAR-CODE
├── smart-campus-backend (Backend / Spring Boot)
│   ├── src/main/java/com/smartcampus
│   │   ├── config/          # 配置类 (MyBatisPlusConfig, WebConfig)
│   │   ├── controller/      # 控制层 (AccBillController, AccAiController, ClassroomController, StatsController等)
│   │   ├── entity/          # 实体类 (AccBill, AccCategory, SysClassroom, VisualStatsLog等)
│   │   ├── mapper/          # DAO 层接口 (AccBillMapper, SysClassroomMapper, VisualStatsLogMapper等)
│   │   ├── service/         # Service 接口及实现
│   │   └── common/          # 通用结果封装 (Result)
│   └── src/main/resources
│       └── mapper/          # MyBatis XML Mapper
│
├── vue-demo (Frontend / Vue 3)
│   ├── src
│   │   ├── api/             # API 接口封装 (accounting.js)
│   │   ├── components/      # 通用组件 (FocusTrendChart, FloatingSimulator等)
│   │   ├── router/          # 路由配置
│   │   ├── utils/           # 工具类 (request.js)
│   │   └── views/           # 核心视图组件
│   │       ├── DashboardView.vue   # 教室状态监控看板
│   │       ├── MapView.vue          # 校园空间导航
│   │       └── accounting/          # 个人记账模块
│   │           ├── insight/         # 洞察中心子模块
│   │           ├── BillList.vue     # 账单明细
│   │           ├── BillAnalysis.vue # 财务统计报表
│   │           ├── BillCalendar.vue # 账单日历
│   │           └── AiChat.vue       # AI智能助手
│   └── package.json
│
└── my_yolo_web (AI Vision / Python Flask)
    ├── static/         # 静态资源
    ├── templates/      # HTML 模板
    ├── app.py          # Flask 主应用
    ├── web_inference.py # YOLO 检测核心逻辑
    └── ai_api.py       # AI 对话接口
```

## 📈 当前进度与核心逻辑

### 1. 教室状态监控模块
- **核心功能**: 实时监控教室人数、专注度、设备状态
- **数据生成**: 启动时自动生成符合课表规律的模拟数据
- **API 路径**: `/api/classroom/`、`/api/stats/`
- **关键逻辑**: 
  - 上课时间自动调整人数和专注度阈值
  - 支持按教室ID查询实时数据和历史趋势
  - 提供24小时专注度趋势图表

### 2. 校园空间导航
- **核心功能**: 校园地图展示和空间导航
- **实现方式**: 基于静态地图资源的可视化展示

### 3. AI 视觉识别子系统
- **核心功能**: 实时视频流目标检测、AI 对话
- **集成方式**: 前端通过 iframe 嵌入 Flask 应用
- **API 路径**: `http://localhost:5000/`
- **关键特性**: 
  - 支持摄像头和本地视频源
  - 可配置检测模型和参数
  - 实时返回检测结果和统计数据

### 4. 个人记账与财务洞察模块
- **财务健康模型**: 计算逻辑位于 `AccBillServiceImpl.getFinancialHealth`，基于消费稳定性、储蓄率、支出集中度等多维加权计算
- **AI 助手集成**: 对话时注入"实时数据库快照"，能回答预算余额、消费风险、近期结余等问题
- **数据联动**: 个人财务画像与数据库历史数据实时同步
- **UI 交互**: 优化的消费占比饼图、支持多行输入的 AI 聊天框

## 💡 开发指南

### API 路径前缀
- 教室管理: `/api/classroom/`
- 统计分析: `/api/stats/`
- 个人记账: `/api/accounting/`
- AI 视觉识别: `http://localhost:5000/api/`

### 开发注意事项
1. **教室监控模块**: 
   - 修改监控逻辑时，请优先检查 `StatsController` 和 `VisualStatsLogService`
   - 图表调整请前往 `FocusTrendChart.vue` 和 `DashboardView.vue`
   
2. **AI 视觉识别模块**: 
   - 模型文件需放置在 `my_yolo_web/models/` 目录
   - 示例文件需放置在 `my_yolo_web/example/` 目录
   
3. **前端开发**: 
   - 全局样式遵循简约、玻璃拟态（Glassmorphism）和渐变色风格
   - 新页面需在 `router/index.js` 中配置路由
   - 新增 API 需在对应模块的 api 文件中封装
   
4. **后端开发**: 
   - 所有 API 返回格式统一使用 `Result<T>` 对象
   - 新增实体类需同步创建对应的 Mapper 和 Service
   - 数据库操作优先使用 MyBatis-Plus 内置方法

## 📊 系统集成关系
- 前端通过 Axios 调用后端 API 获取数据
- 前端通过 iframe 集成 AI 视觉识别系统
- 后端提供统一的数据访问接口
- AI 视觉识别系统可独立运行，也可与主系统集成

---