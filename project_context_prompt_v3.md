# 项目上下文引导 Prompt (Project Context Prompt)

请将以下内容作为开启新对话时的首条指令发送给 AI 助手，以使其快速进入开发状态。

---

## 🚀 项目概览
本项目是一个完整的**智学空间·校园智慧空间治理系统**，包含多个核心模块：
1. **教室状态实时监控**：基于AI视觉识别的教室人数统计、专注度分析、设备管理
2. **校园空间导航**：校园地图和空间可视化
3. **用户认证系统**：企业级单点登录和注册功能
4. **个人记账与财务洞察**：账单管理、多维统计分析、AI财务助手
5. **数据分析中心**：校园运营数据综合分析（待开发）
6. **失物招领功能**：基于AI视觉识别的失物检测、认领管理和记录更新

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
  - `com.smartcampus.controller`: 多模块控制器（账单、分析、预算、AI、教室管理、统计分析、认证、失物招领等）
  - `com.smartcampus.service`: 核心业务逻辑实现
  - `com.smartcampus.entity`: 数据库映射实体（账单、分类、预算、教室、用户、统计日志、失物招领等）
  - `com.smartcampus.mapper`: MyBatis-Plus Mapper 接口
  - `com.smartcampus.common`: 通用结果封装 (Result)
  - `com.smartcampus.config`: 配置类
  - 数据库初始化脚本位于 `smart-campus-backend/sql/`，项目启动时按顺序自动执行 `schema.sql`（建库/表）然后 `accounting_schema.sql`（记账预置数据）和 `lost_found_table.sql`（失物招领表结构），以便无需手动建表。
  
- **前端 (`vue-demo`)**:
  - `src/views/`: 核心视图组件
    - `Login.vue`: 企业级单点登录页面
    - `DashboardView.vue`: 教室状态实时监控看板
    - `MapView.vue`: 校园空间导航
    - `LostFound.vue`: 失物招领功能页面
    - `accounting/`: 个人记账与财务洞察模块
      - `insight/`: 洞察中心子模块
      - `BillList.vue`: 账单明细列表
      - `BillAnalysis.vue`: 财务统计报表
      - `BillCalendar.vue`: 账单日历视图
      - `AiChat.vue`: AI智能助手
  - `src/components/`: 通用组件（FocusTrendChart、FloatingSimulator等）
  - `src/api/`: API接口封装（认证、账单、统计、失物招领等）
  - `src/utils/`: 工具类
  - `src/router/`: 路由配置（含路由守卫）
  
- **AI 视觉识别 (`my_yolo_web`)**:
  - `app.py`: Flask 主应用
  - `web_inference.py`: YOLO 检测核心逻辑
  - `ai_api.py`: AI 对话接口
  - `static/`: 静态资源
  - `templates/`: HTML 模板
  
- **失物招领功能**:
  - **核心逻辑**: 基于AI视觉识别的失物检测、认领管理和记录更新
  - **后端实现**: 包含LFLostItemController、LFLostItemService、LFLostItemMapper等组件
  - **前端实现**: LostFound.vue页面，包含失物招领列表、YOLO子系统集成、认领功能等
  - **核心特性**: 支持实时更新失物数量、部分认领功能、拖拽式YOLO子系统窗口等

### 目录树 (Directory Tree)
```text
E:\LEAR-CODE
├── smart-campus-backend (Backend / Spring Boot)
│   ├── src/main/java/com/smartcampus
│   │   ├── config/          # 配置类 (MyBatisPlusConfig, WebConfig)
│   │   ├── controller/      # 控制层 (AccBillController, AccAiController, ClassroomController, StatsController, LFLostItemController等)
│   │   ├── entity/          # 实体类 (AccBill, AccCategory, SysClassroom, VisualStatsLog, LFLostItem等)
│   │   ├── mapper/          # DAO 层接口 (AccBillMapper, SysClassroomMapper, VisualStatsLogMapper, LFLostItemMapper等)
│   │   ├── service/         # Service 接口及实现
│   │   │   └── impl/        # Service 实现类
│   │   └── common/          # 通用结果封装 (Result)
│   ├── sql/                 # 数据库初始化脚本
│   │   ├── accounting_schema.sql # 记账模块预置数据
│   │   ├── lost_found_table.sql # 失物招领表结构
│   │   └── schema.sql       # 数据库表结构
│   └── src/main/resources
│       └── mapper/          # MyBatis XML Mapper
│           └── LFLostItemMapper.xml # 失物招领Mapper配置
│
├── vue-demo (Frontend / Vue 3)
│   ├── src
│   │   ├── api/             # API 接口封装 (accounting.js)
│   │   ├── components/      # 通用组件 (FocusTrendChart, FloatingSimulator等)
│   │   ├── router/          # 路由配置（含路由守卫）
│   │   ├── utils/           # 工具类 (request.js)
│   │   └── views/           # 核心视图组件
│   │       ├── Login.vue          # 企业级单点登录页面
│   │       ├── DashboardView.vue   # 教室状态监控看板
│   │       ├── MapView.vue          # 校园空间导航
│   │       ├── LostFound.vue        # 失物招领功能页面
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

### 1. 用户认证系统
- **核心功能**: 企业级单点登录、注册、登出和当前用户信息获取
- **实现方式**: 基于JWT的认证机制
- **API 路径**: `/api/accounting/auth/`
- **关键特性**: 
  - 支持用户名/密码登录
  - 支持新用户注册
  - 路由守卫保护敏感页面
  - 登录状态持久化

### 2. 教室状态监控模块
- **核心功能**: 实时监控教室人数、专注度、设备状态
- **数据生成**: 启动时自动生成符合课表规律的模拟数据
- **API 路径**: `/api/classroom/`、`/api/stats/`
- **关键逻辑**: 
  - 上课时间自动调整人数和专注度阈值
  - 支持按教室ID查询实时数据和历史趋势
  - 提供24小时专注度趋势图表

### 3. 校园空间导航
- **核心功能**: 校园地图展示和空间导航
- **实现方式**: 基于静态地图资源的可视化展示

### 4. AI 视觉识别子系统
- **核心功能**: 实时视频流目标检测、AI 对话
- **集成方式**: 前端通过 iframe 嵌入 Flask 应用
- **API 路径**: `http://localhost:5000/`
- **关键特性**: 
  - 支持摄像头和本地视频源
  - 可配置检测模型和参数
  - 实时返回检测结果和统计数据

### 5. 个人记账与财务洞察模块
- **财务健康模型**: 计算逻辑位于 `AccBillServiceImpl.getFinancialHealth`，基于消费稳定性、储蓄率、支出集中度等多维加权计算
- **AI 助手集成**: 对话时注入"实时数据库快照"，能回答预算余额、消费风险、近期结余等问题
- **数据联动**: 个人财务画像与数据库历史数据实时同步
- **UI 交互**: 优化的消费占比饼图、支持多行输入的 AI 聊天框

### 6. 失物招领功能
- **核心逻辑**: 基于AI视觉识别的失物检测和认领管理，支持实时更新失物数量和发现时间
- **后端实现**: 
  - `LFLostItemController`: 处理失物招领相关API请求
  - `LFLostItemServiceImpl`: 实现失物招领的业务逻辑，包括记录更新、认领管理等
  - `autoGenerateLostItem`: 处理YOLO检测结果，自动更新或创建失物记录
  - `updateLostItemQuantity`: 支持部分认领功能
- **前端实现**: 
  - `LostFound.vue`: 失物招领列表、YOLO子系统集成、认领功能
  - 支持拖拽的YOLO子系统窗口
  - 优化的认领弹窗，支持全部或指定数量认领
- **AI 集成**: 通过iframe嵌入YOLO子系统，实时接收检测结果

## 💡 开发指南

### API 路径前缀
- 用户认证: `/api/accounting/auth/`
- 教室管理: `/api/classroom/`
- 统计分析: `/api/stats/`
- 个人记账: `/api/accounting/`
- 失物招领: `/api/lost-found/`
- AI 视觉识别: `http://localhost:5000/api/`

### 开发注意事项
1. **用户认证模块**: 
   - 登录状态通过 localStorage 持久化
   - 路由守卫保护 `/dashboard`、`/accounting/*` 等敏感页面
   - 未登录用户自动跳转到 `/login`
   
2. **教室监控模块**: 
   - 修改监控逻辑时，请优先检查 `StatsController` 和 `VisualStatsLogService`
   - 图表调整请前往 `FocusTrendChart.vue` 和 `DashboardView.vue`
   
3. **AI 视觉识别模块**: 
   - 模型文件需放置在 `my_yolo_web/models/` 目录
   - 示例文件需放置在 `my_yolo_web/example/` 目录
   
4. **前端开发**: 
   - 全局样式遵循简约、玻璃拟态（Glassmorphism）和渐变色风格
   - 新页面需在 `router/index.js` 中配置路由
   - 新增 API 需在对应模块的 api 文件中封装
   - 敏感页面需添加路由守卫保护
   
5. **后端开发**: 
   - 所有 API 返回格式统一使用 `Result<T>` 对象
   - 新增实体类需同步创建对应的 Mapper 和 Service
   - 数据库操作优先使用 MyBatis-Plus 内置方法
   
6. **运行测试说明**:
   - 如果需要项目进行运行测试，不要自己运行测试，告诉我，我来运行测试。

## 📊 系统集成关系
- 前端通过 Axios 调用后端 API 获取数据
- 前端通过 iframe 集成 AI 视觉识别系统
- 后端提供统一的数据访问接口
- AI 视觉识别系统可独立运行，也可与主系统集成
- 所有模块共享用户认证信息

---