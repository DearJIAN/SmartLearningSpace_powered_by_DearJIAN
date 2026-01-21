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
7. **座位预约功能**：图书馆座位实时预约、签到、续约、签退管理
8. **食堂智能服务**：3D选座、智能点餐、视觉特效界面

## 🛠️ 技术栈
### 后端 (Backend)
- **核心框架**: Java 8 / Spring Boot 2.7.18
- **数据库组件**: MyBatis-Plus 3.5.3.1 + MySQL 8.0
- **连接池**: Druid 1.2.16
- **工具类**: Apache POI 5.2.3 (Excel 导出), Lombok
- **AI集成**: OkHttp 4.10.0 (豆包API)
- **接口规范**: 典型的 RESTful API，返回格式为 `Result<T>` 对象

### 前端 (Frontend)
- **核心框架**: Vue 3.5.26 (Composition API / `<script setup>`)
- **UI 组件库**: Element Plus 2.13.1
- **数据可视化**: ECharts 5
- **图标系统**: Iconify (Tabler Icons, Material Design Icons, Phosphor Icons)
- **动画库**: GSAP (GreenSock Animation Platform)
- **粒子效果**: tsParticles
- **样式方案**: 玻璃拟态(Glassmorphism) + 渐变色 + 高对比度设计
- **通信**: Axios 1.13.2

### AI 视觉识别 (AI Vision)
- **核心框架**: Python 3.8+ / Flask
- **目标检测**: YOLOv11 模型 (yolo11n.pt, yolo11m.pt, 自定义教室模型)
- **视频流处理**: OpenCV + 多线程
- **AI 对话**: 豆包 API 集成 (流式响应)
- **模型管理**: 支持多模型切换、摄像头/文件双输入源

## 📂 项目文件结构 (Project Structure)

### 核心模块说明
- **后端 (`smart-campus-backend`)**:
  - `com.smartcampus.controller`: 多模块控制器（账单、分析、预算、AI、教室管理、统计分析、认证、失物招领、座位预约等）
  - `com.smartcampus.service`: 核心业务逻辑实现
  - `com.smartcampus.entity`: 数据库映射实体（账单、分类、预算、教室、用户、统计日志、失物招领、座位等）
  - `com.smartcampus.mapper`: MyBatis-Plus Mapper 接口
  - `com.smartcampus.common`: 通用结果封装 (Result)
  - `com.smartcampus.config`: 配置类
  - 数据库初始化脚本位于 `smart-campus-backend/sql/`，项目启动时按顺序自动执行 `schema.sql`（建库/表）然后 `accounting_schema.sql`（记账预置数据）和 `lost_found_table.sql`（失物招领表结构），以便无需手动建表。
  
- **前端 (`vue-demo`)**:
  - `src/views/`: 核心视图组件
    - `Login.vue`: 企业级单点登录页面
    - `DashboardView.vue`: 教室状态实时监控看板（含YOLO子系统集成）
    - `MapView.vue`: 校园空间导航
    - `LostFound.vue`: 失物招领功能页面
    - `SeatReservation.vue`: 座位预约功能页面
    - `CanteenManagement.vue`: 食堂智能服务主页面（GSAP动画+tsParticles粒子效果）
    - `CanteenSeating.vue`: 食堂3D选座页面
    - `CanteenOrdering.vue`: 食堂智能点餐页面
    - `accounting/`: 个人记账与财务洞察模块
      - `insight/`: 洞察中心子模块
      - `BillList.vue`: 账单明细列表
      - `BillAnalysis.vue`: 财务统计报表
      - `BillCalendar.vue`: 账单日历视图
      - `AiChat.vue`: AI智能助手
  - `src/components/`: 通用组件
    - `FocusTrendChart.vue`: 专注度趋势图表组件
    - `FloatingSimulator.vue`: 数据模拟悬浮按钮组件
  - `src/api/`: API接口封装（认证、账单、统计、失物招领、座位预约等）
  - `src/utils/`: 工具类
  - `src/router/`: 路由配置（含路由守卫）
  
- **AI 视觉识别 (`my_yolo_web`)**:
  - `app.py`: Flask 主应用
  - `web_inference.py`: YOLO 检测核心逻辑（多线程+锁机制）
  - `ai_api.py`: AI 对话接口
  - `static/`: 静态资源
  - `templates/`: HTML 模板
  - `models/`: YOLO模型文件 (.pt)
  - `example/`: 示例视频和图片
  - `runs/`: 检测结果输出目录
  
- **失物招领功能**:
  - **核心逻辑**: 基于AI视觉识别的失物检测、认领管理和记录更新
  - **后端实现**: 包含LFLostItemController、LFLostItemService、LFLostItemMapper等组件
  - **前端实现**: LostFound.vue页面，包含失物招领列表、YOLO子系统集成、认领功能等
  - **核心特性**: 支持实时更新失物数量、部分认领功能、拖拽式YOLO子系统窗口等
  - **AI 集成**: 通过iframe嵌入YOLO子系统，实时接收检测结果

- **座位预约功能**:
  - **核心逻辑**: 图书馆座位实时预约、签到、续约、签退管理
  - **后端实现**: 
    - `SeatController`: 处理座位预约相关API请求
    - `SysSeatServiceImpl`: 实现座位预约的业务逻辑
    - `initSeats()`: 项目启动时初始化180个座位
    - `reserve()`: 预约座位，设置状态为已预约
    - `checkIn()`: 签到入座，设置状态为使用中
    - `renew()`: 续约座位，延长使用时间
    - `checkOut()`: 签退座位，释放座位
    - `resetAll()`: 重置所有座位状态
    - `simulate()`: 模拟预约场景（30%占用，10%预约）
    - `checkExpiration()`: 定时检查预约超时和使用结束（2秒间隔）
  - **前端实现**: 
    - `SeatReservation.vue`: 座位预约列表、预约弹窗、签到续约签退功能
    - **实时时钟**: 模拟时钟显示当前时间
    - **通知中心**: 实时通知系统（超时、未守约、自动释放）
    - **预约弹窗**: 支持自定义开始时间、使用时长选择
    - **状态管理**: 空闲、已预约、使用中三种状态
    - **智能提醒**: 重复提醒、自动释放机制
    - **数据同步**: 2秒间隔自动刷新座位状态
  - **核心特性**: 
    - 180个座位网格展示
    - 实时时钟（模拟时钟+数字时钟）
    - 三种状态可视化（空闲/已预约/使用中）
    - 预约15分钟缓冲期
    - 重复提醒机制（10秒间隔）
    - 自动释放机制（30秒后自动签退）
    - 支持续约功能（30分钟/1小时/2小时）

### 目录树 (Directory Tree)
```text
E:\LEAR-CODE
├── smart-campus-backend (Backend / Spring Boot)
│   ├── src/main/java/com/smartcampus
│   │   ├── config/          # 配置类 (MyBatisPlusConfig, WebConfig)
│   │   ├── controller/      # 控制层 (AccBillController, AccAiController, ClassroomController, StatsController, LFLostItemController, SeatController等)
│   │   ├── entity/          # 实体类 (AccBill, AccCategory, SysClassroom, VisualStatsLog, LFLostItem, SysSeat等)
│   │   ├── mapper/          # DAO 层接口 (AccBillMapper, SysClassroomMapper, VisualStatsLogMapper, LFLostItemMapper, SysSeatMapper等)
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
│   │   ├── api/             # API 接口封装 (accounting.js, seat.js)
│   │   ├── components/      # 通用组件 (FocusTrendChart, FloatingSimulator等)
│   │   ├── router/          # 路由配置（含路由守卫）
│   │   ├── utils/           # 工具类 (request.js)
│   │   └── views/           # 核心视图组件
│   │       ├── Login.vue          # 企业级单点登录页面
│   │       ├── DashboardView.vue   # 教室状态监控看板
│   │       ├── MapView.vue          # 校园空间导航
│   │       ├── LostFound.vue        # 失物招领功能页面
│   │       ├── SeatReservation.vue  # 座位预约功能页面
│   │       ├── CanteenManagement.vue # 食堂智能服务主页面（GSAP动画+tsParticles粒子效果）
│   │       ├── CanteenSeating.vue   # 食堂3D选座页面
│   │       ├── CanteenOrdering.vue  # 食堂智能点餐页面
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
- **实现方式**: 基于JWT的认证机制（架构预留）
- **API 路径**: `/api/accounting/auth/`
- **关键特性**: 
  - 支持用户名/密码登录
  - 支持新用户注册
  - 路由守卫保护敏感页面
  - 登录状态通过localStorage持久化
  - **登录后默认跳转**: 校园空间导航 (`/`)
  - **路由守卫优化**: 1小时内免重复验证，提升加载速度

### 2. 教室状态监控模块 (DashboardView.vue)
- **核心功能**: 实时监控教室人数、专注度、设备状态，集成YOLO视觉识别
- **数据生成**: 启动时自动生成符合课表规律的模拟数据（智能算法）
- **API 路径**: `/api/classroom/`、`/api/stats/`
- **关键逻辑**: 
  - 上课时间自动调整人数和专注度阈值
  - 支持按教室ID查询实时数据和历史趋势
  - 提供24小时专注度趋势图表
  - **悬浮控制面板**: AI视觉入口、数据模拟按钮、YOLO子系统最小化窗口
  - **YOLO子系统窗口**: 支持拖拽、调整大小、最小化，底部居中显示
  - **数据模拟按钮**: 紫色渐变背景，橙红色互补色呼吸灯（#ff6b35），使用Tabler图标
  - **AI视觉按钮**: 蓝色渐变，眼睛图标(i-tabler-eye)，脉冲动画

### 3. 校园空间导航
- **核心功能**: 校园地图展示和空间可视化
- **实现方式**: 基于静态地图资源的可视化展示

### 4. AI 视觉识别子系统
- **核心功能**: 实时视频流目标检测、AI 对话
- **集成方式**: 前端通过 iframe 嵌入 Flask 应用
- **API 路径**: `http://localhost:5000/api/`
- **关键特性**: 
  - 支持摄像头(0)和本地视频/图片双输入源
  - 可配置检测模型和参数（conf_thres, iou_thres, imgsz）
  - 实时返回检测结果和统计数据（JSON格式）
  - **多模型支持**: yolo11n.pt(5.3MB), yolo11m.pt(38.8MB), 自定义教室模型
  - **结果持久化**: 自动保存检测视频/图片到 `runs/web_exp/`
  - **流式AI对话**: 豆包API集成，支持实时对话
  - **线程安全**: 使用Lock机制保证多线程下的数据安全

### 5. 个人记账与财务洞察模块
- **财务健康模型**: 计算逻辑位于 `AccBillServiceImpl.getFinancialHealth`，基于消费稳定性、储蓄率、支出集中度等多维加权计算
- **AI 助手集成**: 对话时注入"实时数据库快照"，能回答预算余额、消费风险、近期结余等问题
- **数据联动**: 个人财务画像与数据库历史数据实时同步
- **UI 交互**: 优化的消费占比饼图、支持多行输入的 AI 聊天框

### 6. 失物招领功能 (LostFound.vue)
- **核心逻辑**: 基于AI视觉识别的失物检测、认领管理和记录更新
- **后端实现**: 
  - `LFLostItemController`: 处理失物招领相关API请求
  - `LFLostItemServiceImpl`: 实现失物招领的业务逻辑
  - `autoGenerateLostItem`: 处理YOLO检测结果，自动更新或创建失物记录
  - `updateLostItemQuantity`: 支持部分认领功能
  - `clearAllLostItems`: 清空所有记录（使用TRUNCATE重置自增ID）
- **前端实现**: 
  - `LostFound.vue`: 失物招领列表、YOLO子系统集成、认领功能
  - **拖拽式YOLO窗口**: 支持拖拽、调整大小、最小化/恢复/关闭
  - **认领弹窗**: 支持全部认领或指定数量认领
  - **筛选功能**: 按教室、物品类型、状态、时间范围筛选
  - **分页组件**: 支持页码、页大小切换
  - **动态物品类型**: 从数据中提取唯一物品类型作为筛选选项
  - **图标系统**: 使用Tabler图标(i-tabler-eye, i-tabler-chart-line等)
  - **AI 集成**: 通过iframe嵌入YOLO子系统，实时接收检测结果
  - **数据同步**: 1秒间隔自动刷新列表

### 7. 座位预约功能 (SeatReservation.vue)
- **核心逻辑**: 图书馆座位实时预约、签到、续约、签退管理
- **后端实现**: 
  - `SeatController`: 处理座位预约相关API请求
  - `SysSeatServiceImpl`: 实现座位预约的业务逻辑
  - `initSeats()`: 项目启动时初始化180个座位（S001-S180）
  - `reserve()`: 预约座位，设置状态为已预约，支持自定义开始时间和使用时长
  - `checkIn()`: 签到入座，设置状态为使用中，计算结束时间
  - `renew()`: 续约座位，延长使用时间
  - `checkOut()`: 签退座位，释放座位
  - `resetAll()`: 重置所有座位状态为空闲
  - `simulate()`: 模拟预约场景（30%占用，10%预约）
  - `checkExpiration()`: 定时检查预约超时（15分钟未签到）和使用结束
- **前端实现**: 
  - `SeatReservation.vue`: 座位预约列表、预约弹窗、签到续约签退功能
  - **实时时钟**: 模拟时钟显示（指针式+数字式）
  - **通知中心**: 实时通知系统（超时、未守约、自动释放）
  - **预约弹窗**: 支持自定义开始时间、使用时长选择（小时/分钟/秒）
  - **状态管理**: 空闲、已预约、使用中三种状态
  - **智能提醒**: 重复提醒机制（10秒间隔）、自动释放机制（30秒后）
  - **数据同步**: 2秒间隔自动刷新座位状态
  - **UI设计**: 玻璃拟态风格，渐变色设计，实时状态可视化

### 8. 数据模拟器 (FloatingSimulator.vue)
- **核心功能**: 为教室监控系统生成模拟数据
- **智能生成**: 根据当前时间自动判断课表规律(上课/休息/深夜)
- **实时同步**: 1秒间隔更新当前时间
- **UI设计**: 胶囊形悬浮按钮，紫色渐变，橙红色互补色呼吸灯
- **图标**: i-tabler-chart-line (Tabler图标库)

### 9. 食堂智能服务模块 (CanteenManagement.vue)
- **核心功能**: 3D选座、智能点餐、视觉特效界面
- **前端实现**: 
  - `CanteenManagement.vue`: 食堂智能服务主页面，集成GSAP动画和tsParticles粒子效果
  - `CanteenSeating.vue`: 食堂3D选座页面
  - `CanteenOrdering.vue`: 食堂智能点餐页面
- **关键特性**: 
  - **GSAP动画**: 实现元素入场动画、3D卡片倾斜效果、火花粒子系统
  - **tsParticles粒子效果**: 背景粒子效果，支持鼠标交互
  - **数据流背景**: 动态数据流线条动画
  - **全息服务指引**: 步骤指引动画效果
  - **响应式设计**: 适配不同屏幕尺寸
- **交互体验**: 
  - 鼠标移动产生火花效果
  - 点击产生炸裂火花效果
  - 卡片悬停3D倾斜效果
  - 流畅的页面转场动画

## 💡 开发指南

### API 路径前缀
- 用户认证: `/api/accounting/auth/`
- 教室管理: `/api/classroom/`
- 统计分析: `/api/stats/`
- 个人记账: `/api/accounting/`
- 失物招领: `/api/lost-found/`
- 座位预约: `/api/seat/`
- AI 视觉识别: `http://localhost:5000/api/`

### 前端路由路径
- 登录: `/login`
- 校园空间导航: `/`
- 教室状态监控: `/dashboard`
- 失物招领: `/lost-found`
- 图书馆座位预约: `/seat`
- 食堂智能服务: `/canteen`
- 食堂智能选座: `/canteen/seating`
- 食堂智能点餐: `/canteen/ordering`
- 个人记账: `/accounting/*`

### 开发注意事项
1. **用户认证模块**: 
   - 登录状态通过 localStorage 持久化
   - 路由守卫保护 `/dashboard`、`/accounting/*` 等敏感页面
   - 未登录用户自动跳转到 `/login`
   - 登录/注册成功后默认跳转到校园空间导航 (`/`)
   - 路由守卫优化：验证信息缓存1小时，避免重复API调用
   
2. **教室监控模块**: 
   - 修改监控逻辑时，请优先检查 `StatsController` 和 `VisualStatsLogService`
   - 图表调整请前往 `FocusTrendChart.vue` 和 `DashboardView.vue`
   - YOLO子系统窗口样式在 `DashboardView.vue` 和 `LostFound.vue` 中保持一致
   - 悬浮按钮布局: YOLO最小化居中(40px), AI视觉(130px), 数据模拟(40px)
   - 颜色方案: 紫色按钮(#9a65fd) + 橙红色呼吸灯(#ff6b35), 蓝色AI按钮(#00c6ff)
   
3. **AI 视觉识别模块**: 
   - 模型文件需放置在 `my_yolo_web/models/` 目录
   - 示例文件需放置在 `my_yolo_web/example/` 目录
   - 检测结果自动保存到 `runs/web_exp/`
   - 多线程环境下注意使用Lock保证线程安全
   
4. **前端开发**: 
   - 全局样式遵循简约、玻璃拟态（Glassmorphism）和渐变色风格
   - 新页面需在 `router/index.js` 中配置路由
   - 新增 API 需在对应模块的 api 文件中封装
   - 敏感页面需添加路由守卫保护
   - 图标优先使用Tabler图标库(i-tabler-xxx)
   - 动画效果注意性能，使用transform和opacity
   - **GSAP动画**: 使用Timeline管理复杂动画序列，注意内存管理
   - **tsParticles**: 合理配置粒子数量，避免性能问题
   - **响应式设计**: 针对不同屏幕尺寸调整动画效果和粒子数量
   
5. **后端开发**: 
   - 所有 API 返回格式统一使用 `Result<T>` 对象
   - 新增实体类需同步创建对应的 Mapper 和 Service
   - 数据库操作优先使用 MyBatis-Plus 内置方法
   - 失物招领模块注意状态(0未认领/1已认领)和数量的原子性操作
   - 座位预约模块注意状态(0空闲/1已预约/2使用中)和时间管理
   
6. **运行测试说明**:
   - 如果需要项目进行运行测试，不要自己运行测试，告诉我，我来运行测试。
   - 确保后端服务(8080端口)和AI服务(5000端口)同时运行
   - 前端dev服务器使用5173端口，已配置代理到后端

## 📊 系统集成关系
- 前端通过 Axios 调用后端 API 获取数据
- 前端通过 iframe 集成 AI 视觉识别系统
- 后端提供统一的数据访问接口
- AI 视觉识别系统可独立运行，也可与主系统集成
- 所有模块共享用户认证信息
- YOLO检测结果通过HTTP POST推送到后端，自动更新失物记录

## 🎨 UI/UX 设计规范

### 颜色系统
- **主色调**: 蓝色系 (#409EFF, #00c6ff, #0072ff)
- **辅助色**: 紫色 (#9a65fd, #7b4af0) - 用于数据模拟
- **强调色**: 橙红色 (#ff6b35) - 用于呼吸灯效果
- **成功色**: #67C23A
- **警告色**: #E6A23C
- **危险色**: #F56C6C

### 图标系统
- **图标库**: Iconify (已安装 Tabler, Material Design, Phosphor)
- **使用方式**: `<i-tabler-icon-name />`
- **推荐图标**:
  - 视觉识别: i-tabler-eye
  - 数据模拟: i-tabler-chart-line
  - 设置: i-tabler-settings
  - 分析: i-tabler-chart-bar

### 动画规范
- **呼吸灯**: 2-3秒周期，ease-in-out缓动
- **悬停**: 0.3秒过渡，transform scale/shadow
- **拖拽**: 实时响应，cursor: move
- **脉冲**: 2秒周期，box-shadow扩散

### 布局原则
- **悬浮元素**: 右下三角布局，YOLO最小化居中
- **间距**: 40px/80px/10px三级间距系统
- **层级**: z-index 1000-2000，YOLO窗口最高
- **响应式**: 支持窗口调整大小，最小尺寸800x600px

---
