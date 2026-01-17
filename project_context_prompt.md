# 项目上下文引导 Prompt (Project Context Prompt)

请将以下内容作为开启新对话时的首条指令发送给 AI 助手，以使其快速进入开发状态。

---

## 🚀 项目概览
本项目是一个**智能校园治理平台**下的**个人记账与财务洞察模块**。目前已完成核心的账单管理、多维统计分析、AI 财务助手以及基于真实数据联动的财务画像功能。

## 🛠️ 技术栈
### 后端 (Backend)
- **核心框架**: Java 8 / Spring Boot
- **数据库组件**: MyBatis-Plus
- **工具类**: Apache POI (Excel 导出), DayJS 风格的日期处理
- **接口规范**: 典型的 RESTful API，返回格式为 `Result<T>` 对象

### 前端 (Frontend)
- **核心框架**: Vue 3 (Composition API / <script setup>)
- **UI 组件库**: Element Plus
- **数据可视化**: ECharts 5
- **日期处理**: Day.js
- **通信**: Axios

## 📂 项目文件结构 (Project Structure)

### 核心模块说明
- **后端 (`smart-campus-backend`)**:
  - `com.smartcampus.controller`: 账单、分析、预算、AI 等控制器。
  - `com.smartcampus.service`: 核心业务逻辑实现（如 `AccBillServiceImpl` 处理复杂的财务模型计算）。
  - `com.smartcampus.entity`: 数据库映射实体（`AccBill`, `AccCategory`, `AccBudget` 等）。
  - `com.smartcampus.mapper`: MyBatis-Plus Mapper 接口。
- **前端 (`vue-demo`)**:
  - `src/views/accounting/`: 核心视图组件。
    - `BillList.vue`: 账单明细列表（带筛选、Excel 导出）。
    - `BillAnalysis.vue`: 财务看板（包含雷达图健康分析、收支趋势、占比饼图）。
    - `BillCalendar.vue`: 账单日历视图。
    - `AiChat.vue`: AI 智能助手（支持多行输入、实时数据快照）。
    - `insight/`: 洞察中心（`InsightProfile.vue` 用户画像）。

### 目录树 (Directory Tree)
```text
E:\LEAR-CODE
├── smart-campus-backend (Backend / Spring Boot)
│   ├── src/main/java/com/smartcampus
│   │   ├── config/          # 配置类 (MyBatisPlusConfig, CorsConfig)
│   │   ├── controller/      # 控制层 (AccBillController, AccAiController, AccAnalysisController)
│   │   ├── entity/          # 实体类 (AccBill, AccCategory, SysUser)
│   │   ├── mapper/          # DAO 层接口 (AccBillMapper)
│   │   ├── service/         # Service 接口
│   │   │   └── impl/        # Service 实现 (AccBillServiceImpl 核心算法)
│   │   └── common/          # 通用结果封装 (Result)
│   └── src/main/resources
│       └── mapper/          # MyBatis XML Mapper
│
└── vue-demo (Frontend / Vue 3)
    ├── src
    │   ├── api/             # API 接口封装 (accounting.js)
    │   ├── views/accounting # 记账模块核心视图
    │   │   ├── insight/     # 洞察中心子模块
    │   │   │   └── InsightProfile.vue # 个人财务画像 (S/A/B级评分、雷达图)
    │   │   ├── BillList.vue       # 账单明细 (CRUD、Excel导出)
    │   │   ├── BillAnalysis.vue   # 统计报表 (ECharts 可视化、消费占比、健康评分)
    │   │   ├── BillCalendar.vue   # 账单日历 (Calendar 组件)
    │   │   └── AiChat.vue         # AI 智能助手 (实时数据注入、Shift+Enter)
    │   ├── utils/           # 工具类 (request.js)
    │   └── router/          # 路由配置
    └── package.json
```

## 📈 当前进度与核心逻辑
1. **财务健康模型**: 计算逻辑位于 `AccBillServiceImpl.getFinancialHealth`。基于消费稳定性（CV 变异系数）、储蓄率、支出集中度等多维加权计算，满分 100。
2. **AI 助手集成**: AI 助手在对话时会由后端注入“实时数据库快照”，使其能回答关于预算余额、消费风险、近期结余等真实数据问题。
3. **数据联动**: “个人财务画像”已完成全面重构，资产积累、雷达图维、称号和建议均与数据库历史数据实时同步。
4. **UI 交互**: 消费占比饼图已优化，支持侧边 Legend 和 Tooltip 百分比显示；AI 聊天框已支持 `Shift+Enter` 换行逻辑。

## 💡 开发指南
- 所有的后端 API 请求路径前缀通常为 `/api/accounting`。
- 修改财务计算逻辑时，请优先检查 `AccBillServiceImpl`。
- 图表调整请前往 `BillAnalysis.vue` 或 `InsightProfile.vue`，使用 ECharts 配置项进行修改。
- 前端全局样式遵循简约、玻璃拟态（Glassmorphism）和渐变色风格。
---
