# 智学空间·校园智慧空间治理系统 - 开发说明文档

## 1. 项目概述

### 1.1 项目背景
随着智慧校园建设的不断深入，传统的校园管理方式已经无法满足现代校园的需求。智学空间·校园智慧空间治理系统旨在通过AI视觉识别、物联网技术、大数据分析等先进技术，实现校园空间的智能化管理，提升校园管理效率和服务质量，为师生提供更加便捷、舒适的校园环境。

### 1.2 项目目标
- 实现教室状态的实时监控和管理
- 提供校园空间的可视化导航服务
- 构建完善的用户认证和权限管理系统
- 实现个人记账与财务洞察功能
- 开发基于AI视觉识别的失物招领系统
- 实现图书馆座位的智能预约和管理
- 提供食堂智能服务，包括3D选座和智能点餐

### 1.3 项目定位
本项目定位为一个完整的校园智慧空间治理系统，涵盖了校园管理的多个方面，包括教室管理、空间导航、用户认证、财务管理、失物招领、座位预约和食堂服务等。系统采用前后端分离架构，支持多端访问，具有良好的可扩展性和可维护性。

## 2. 技术栈详细说明

### 2.1 后端技术栈
| 技术 | 版本 | 用途 | 说明 |
| --- | --- | --- | --- |
| Java | 8 | 开发语言 | 稳定可靠，广泛应用于企业级开发 |
| Spring Boot | 2.7.18 | 核心框架 | 简化Spring应用开发，提供自动配置 |
| MyBatis-Plus | 3.5.3.1 | ORM框架 | 简化数据库操作，提供代码生成功能 |
| MySQL | 8.0 | 数据库 | 关系型数据库，支持事务和复杂查询 |
| Druid | 1.2.16 | 连接池 | 高性能的数据库连接池，支持监控和统计 |
| Apache POI | 5.2.3 | Excel处理 | 用于生成和解析Excel文件 |
| Lombok | 1.18.24 | 代码简化 | 自动生成Getter、Setter、构造函数等 |
| OkHttp | 4.10.0 | HTTP客户端 | 用于调用外部API，如豆包AI API |

### 2.2 前端技术栈
| 技术 | 版本 | 用途 | 说明 |
| --- | --- | --- | --- |
| Vue | 3.5.26 | 核心框架 | 渐进式JavaScript框架，采用Composition API |
| Element Plus | 2.13.1 | UI组件库 | 基于Vue 3的企业级UI组件库 |
| ECharts | 5 | 数据可视化 | 强大的图表库，支持多种图表类型 |
| Iconify | 3.1.0 | 图标系统 | 支持多种图标库，包括Tabler、Material Design等 |
| GSAP | 3.12.5 | 动画库 | 高性能的JavaScript动画库，支持复杂动画效果 |
| tsParticles | 3.4.0 | 粒子效果 | 用于创建各种粒子效果，提升UI视觉体验 |
| Axios | 1.13.2 | HTTP客户端 | 用于与后端API进行通信 |
| Vite | 5.0.0 | 构建工具 | 新一代前端构建工具，提供快速的开发体验 |

### 2.3 AI视觉识别技术栈
| 技术 | 版本 | 用途 | 说明 |
| --- | --- | --- | --- |
| Python | 3.8+ | 开发语言 | 广泛应用于AI和数据科学领域 |
| Flask | 2.2.3 | Web框架 | 轻量级Python Web框架，用于构建API服务 |
| YOLOv11 | 最新版 | 目标检测 | 高效的实时目标检测算法，支持多种模型 |
| OpenCV | 4.8.0 | 计算机视觉 | 用于处理图像和视频流 |
| 豆包API | 最新版 | AI对话 | 提供强大的自然语言处理能力 |

## 3. 项目结构详细介绍

### 3.1 整体项目结构
```text
E:\LEAR-CODE
├── smart-campus-backend (后端服务)
├── vue-demo (前端应用)
├── my_yolo_web (AI视觉识别服务)
├── project_context_prompt_v4.md (项目上下文文档)
└── 开发说明文档_TRAECN.md (本开发说明文档)
```

### 3.2 后端项目结构 (smart-campus-backend)
```text
smart-campus-backend
├── src/main/java/com/smartcampus
│   ├── config/          # 配置类
│   │   ├── MybatisPlusConfig.java  # MyBatis-Plus配置
│   │   └── WebConfig.java           # Web配置，包含跨域配置
│   ├── controller/      # 控制层，处理HTTP请求
│   │   ├── AccAiController.java       # 财务AI控制器
│   │   ├── AccAnalysisController.java # 财务分析控制器
│   │   ├── AccBillController.java     # 账单控制器
│   │   ├── AccBudgetController.java   # 预算控制器
│   │   ├── AccCalendarController.java # 财务日历控制器
│   │   ├── AccInsightController.java  # 财务洞察控制器
│   │   ├── AccSetupController.java    # 财务设置控制器
│   │   ├── AccountingAuthController.java # 记账模块认证控制器
│   │   ├── ClassroomController.java   # 教室控制器
│   │   ├── DeviceController.java      # 设备控制器
│   │   ├── LFLostItemController.java  # 失物招领控制器
│   │   ├── SeatController.java        # 座位预约控制器
│   │   └── StatsController.java       # 统计分析控制器
│   ├── entity/          # 实体类，对应数据库表
│   │   ├── AccBill.java             # 账单实体
│   │   ├── AccBudget.java           # 预算实体
│   │   ├── AccCategory.java         # 分类实体
│   │   ├── AccFinancialGoal.java    # 财务目标实体
│   │   ├── LFLostItem.java          # 失物招领实体
│   │   ├── SeatBooking.java         # 座位预约记录实体
│   │   ├── SysClassroom.java        # 教室实体
│   │   ├── SysSeat.java             # 座位实体
│   │   ├── SysUser.java             # 用户实体
│   │   └── VisualStatsLog.java      # 视觉统计日志实体
│   ├── mapper/          # Mapper接口，定义数据库操作
│   │   ├── AccBillMapper.java             # 账单Mapper
│   │   ├── AccBudgetMapper.java           # 预算Mapper
│   │   ├── AccCategoryMapper.java         # 分类Mapper
│   │   ├── AccFinancialGoalMapper.java    # 财务目标Mapper
│   │   ├── LFLostItemMapper.java          # 失物招领Mapper
│   │   ├── SeatBookingMapper.java         # 座位预约记录Mapper
│   │   ├── SysClassroomMapper.java        # 教室Mapper
│   │   ├── SysSeatMapper.java             # 座位Mapper
│   │   ├── SysUserMapper.java             # 用户Mapper
│   │   └── VisualStatsLogMapper.java      # 视觉统计日志Mapper
│   ├── service/         # Service层，处理业务逻辑
│   │   ├── impl/        # Service实现类
│   │   │   ├── AccAiServiceImpl.java       # 财务AI服务实现
│   │   │   ├── AccBillServiceImpl.java     # 账单服务实现
│   │   │   ├── AccBudgetServiceImpl.java   # 预算服务实现
│   │   │   ├── AccCategoryServiceImpl.java # 分类服务实现
│   │   │   ├── AccFinancialGoalServiceImpl.java # 财务目标服务实现
│   │   │   ├── LFLostItemServiceImpl.java  # 失物招领服务实现
│   │   │   ├── SeatBookingServiceImpl.java # 座位预约记录服务实现
│   │   │   ├── SysClassroomServiceImpl.java # 教室服务实现
│   │   │   ├── SysSeatServiceImpl.java     # 座位服务实现
│   │   │   ├── SysUserServiceImpl.java     # 用户服务实现
│   │   │   └── VisualStatsLogServiceImpl.java # 视觉统计日志服务实现
│   │   ├── AccAiService.java               # 财务AI服务接口
│   │   ├── AccBillService.java             # 账单服务接口
│   │   ├── AccBudgetService.java           # 预算服务接口
│   │   ├── AccCategoryService.java         # 分类服务接口
│   │   ├── AccFinancialGoalService.java    # 财务目标服务接口
│   │   ├── LFLostItemService.java          # 失物招领服务接口
│   │   ├── SeatBookingService.java         # 座位预约记录服务接口
│   │   ├── SysClassroomService.java        # 教室服务接口
│   │   ├── SysSeatService.java             # 座位服务接口
│   │   ├── SysUserService.java             # 用户服务接口
│   │   └── VisualStatsLogService.java      # 视觉统计日志服务接口
│   ├── common/          # 通用模块
│   │   └── Result.java  # 统一返回结果封装
│   └── SmartCampusApplication.java # 应用入口类
├── src/main/resources
│   ├── mapper/          # MyBatis XML Mapper文件
│   │   └── LFLostItemMapper.xml # 失物招领Mapper XML配置
│   └── application.yml  # 应用配置文件
├── sql/                 # 数据库初始化脚本
│   ├── accounting_schema.sql        # 记账模块预置数据
│   ├── backup_before_accounting.sql # 记账模块备份数据
│   ├── lost_found_table.sql         # 失物招领表结构
│   └── schema.sql                   # 数据库表结构
└── pom.xml              # Maven依赖配置
```

### 3.3 前端项目结构 (vue-demo)
```text
vue-demo
├── public/             # 公共资源
│   ├── images/         # 图片资源
│   │   └── login-illus.svg  # 登录页面插图
│   └── favicon.ico     # 网站图标
├── src
│   ├── api/             # API接口封装
│   │   ├── accounting.js # 记账模块API
│   │   └── seat.js       # 座位预约API
│   ├── assets/          # 静态资源
│   │   ├── Map.jpg              # 校园地图
│   │   ├── base.css             # 基础样式
│   │   ├── logo.svg             # 项目logo
│   │   ├── main.css             # 主样式
│   │   ├── 可乐.jpg             # 菜品图片
│   │   ├── 宫保鸡丁盖饭.jpg       # 菜品图片
│   │   ├── 橙汁.jpg             # 菜品图片
│   │   ├── 炸薯条.jpg            # 菜品图片
│   │   ├── 牛肉面.jpg            # 菜品图片
│   │   ├── 西红柿鸡蛋面.jpg       # 菜品图片
│   │   ├── 香辣鸡腿堡.jpg         # 菜品图片
│   │   └── 鱼香肉丝盖饭.jpg       # 菜品图片
│   ├── components/      # 通用组件
│   │   ├── icons/       # 图标组件
│   │   │   ├── IconCommunity.vue     # 社区图标
│   │   │   ├── IconDocumentation.vue # 文档图标
│   │   │   ├── IconEcosystem.vue     # 生态图标
│   │   │   ├── IconSupport.vue        # 支持图标
│   │   │   └── IconTooling.vue        # 工具图标
│   │   ├── FloatingSimulator.vue     # 数据模拟悬浮按钮
│   │   ├── FocusTrendChart.vue       # 专注度趋势图表
│   │   ├── HelloWorld.vue            # 示例组件
│   │   ├── TheWelcome.vue            # 欢迎组件
│   │   └── WelcomeItem.vue           # 欢迎项组件
│   ├── router/          # 路由配置
│   │   └── index.js     # 路由定义和路由守卫
│   ├── utils/           # 工具类
│   │   └── request.js   # Axios封装
│   ├── views/           # 视图组件
│   │   ├── Login.vue                 # 登录页面
│   │   ├── DashboardView.vue         # 教室状态监控看板
│   │   ├── MapView.vue               # 校园空间导航
│   │   ├── LostFound.vue             # 失物招领页面
│   │   ├── SeatReservation.vue       # 座位预约页面
│   │   ├── CanteenManagement.vue     # 食堂智能服务主页面
│   │   ├── CanteenSeating.vue        # 食堂3D选座页面
│   │   ├── CanteenOrdering.vue       # 食堂智能点餐页面
│   │   └── accounting/               # 个人记账模块
│   │       ├── AccountingLayout.vue  # 记账模块布局
│   │       ├── insight/              # 洞察中心子模块
│   │       │   ├── InsightDashboard.vue  # 洞察中心仪表盘
│   │       │   ├── InsightGoal.vue      # 财务目标洞察
│   │       │   ├── InsightProfile.vue   # 财务画像
│   │       │   ├── InsightRisk.vue      # 风险分析
│   │       │   └── InsightTimeline.vue  # 财务时间线
│   │       ├── BillList.vue          # 账单明细
│   │       ├── BillAnalysis.vue      # 财务统计报表
│   │       ├── BillCalendar.vue      # 账单日历
│   │       ├── BillTreemap.vue       # 账单树状图分析
│   │       ├── BudgetManagement.vue  # 预算管理
│   │       ├── AiChat.vue            # AI智能助手
│   │       └── chatState.js          # 聊天状态管理
│   ├── App.vue          # 根组件
│   └── main.js          # 应用入口
├── index.html           # HTML模板
├── jsconfig.json        # JavaScript配置
├── package-lock.json    # npm依赖锁定文件
├── package.json         # npm依赖配置
└── vite.config.js       # Vite配置
```

### 3.4 AI视觉识别项目结构 (my_yolo_web)
```text
my_yolo_web
├── static/         # 静态资源
│   ├── script.js   # JavaScript脚本
│   └── style.css   # CSS样式
├── templates/      # HTML模板
│   └── index.html  # 主页面模板
├── ai_api.py       # AI对话接口
├── app.py          # Flask主应用
├── web_inference.py # YOLO检测核心逻辑
├── yolo_io_utils.py # YOLO IO工具类
└── models/         # YOLO模型文件目录
```

## 4. 核心模块详细设计

### 4.1 教室状态实时监控模块

#### 4.1.1 功能概述
该模块通过AI视觉识别技术，实时监控教室的人数、学生专注度和设备状态，为学校管理提供数据支持。

#### 4.1.2 核心流程
1. YOLO视觉识别系统实时采集教室视频流
2. 检测并统计教室内人数
3. 分析学生的专注度
4. 将数据通过API推送至后端
5. 后端存储数据并提供查询接口
6. 前端实时展示监控数据和趋势图表

#### 4.1.3 关键类和方法
- `StatsController`: 处理统计数据的查询请求
- `VisualStatsLogService`: 处理视觉统计日志的业务逻辑
- `FocusTrendChart.vue`: 展示专注度趋势图表
- `FloatingSimulator.vue`: 模拟数据生成工具

#### 4.1.4 数据结构
- `VisualStatsLog`: 存储视觉统计日志，包含教室ID、人数、专注度、设备状态、时间等字段

### 4.2 校园空间导航模块

#### 4.2.1 功能概述
该模块提供校园地图的可视化展示和空间导航功能，帮助师生快速找到目标地点。

#### 4.2.2 核心流程
1. 加载校园地图资源
2. 展示校园建筑和设施
3. 提供搜索和导航功能
4. 显示实时空间状态

#### 4.2.3 关键组件
- `MapView.vue`: 校园地图展示组件

### 4.3 用户认证系统

#### 4.3.1 功能概述
该系统提供企业级单点登录、注册、登出和用户信息管理功能，确保系统的安全性。

#### 4.3.2 核心流程
1. 用户访问系统，前端检查登录状态
2. 未登录用户跳转至登录页面
3. 用户输入用户名和密码进行登录
4. 后端验证用户信息，生成登录凭证
5. 前端存储登录凭证，跳转到目标页面
6. 路由守卫验证登录状态，保护敏感页面

#### 4.3.3 关键类和组件
- `AccountingAuthController`: 处理认证相关请求
- `Login.vue`: 登录页面组件
- `router/index.js`: 路由守卫配置

#### 4.3.4 技术实现
- 登录状态通过localStorage持久化
- 路由守卫优化：验证信息缓存1小时，避免重复API调用
- 支持用户名/密码登录和新用户注册

### 4.4 个人记账与财务洞察模块

#### 4.4.1 功能概述
该模块提供账单管理、预算管理、财务分析和AI财务助手功能，帮助用户管理个人财务。

#### 4.4.2 核心功能
- 账单记录和分类
- 财务统计报表
- 预算管理
- 财务健康度分析
- AI财务助手
- 财务目标管理
- 财务风险分析

#### 4.4.3 关键类和组件
- `AccBillController`: 处理账单相关请求
- `AccBudgetController`: 处理预算相关请求
- `AccAiController`: 处理AI财务助手请求
- `BillList.vue`: 账单明细列表
- `BillAnalysis.vue`: 财务统计报表
- `BudgetManagement.vue`: 预算管理
- `AiChat.vue`: AI智能助手
- `InsightDashboard.vue`: 洞察中心仪表盘

#### 4.4.4 财务健康模型
计算逻辑位于`AccBillServiceImpl.getFinancialHealth`，基于消费稳定性、储蓄率、支出集中度等多维加权计算。

### 4.5 失物招领功能

#### 4.5.1 功能概述
该功能基于AI视觉识别技术，实现失物的自动检测、记录和认领管理。

#### 4.5.2 核心流程
1. YOLO视觉识别系统检测到失物
2. 自动生成失物记录并推送到后端
3. 后端存储失物信息
4. 前端展示失物列表
5. 用户可以认领失物
6. 系统更新失物状态

#### 4.5.3 关键类和组件
- `LFLostItemController`: 处理失物招领相关请求
- `LFLostItemServiceImpl`: 实现失物招领业务逻辑
- `LostFound.vue`: 失物招领页面组件

#### 4.5.4 核心特性
- 支持实时更新失物数量
- 支持部分认领功能
- 支持拖拽式YOLO子系统窗口
- 支持按教室、物品类型、状态、时间范围筛选
- 支持分页查询

### 4.6 座位预约功能

#### 4.6.1 功能概述
该功能实现图书馆座位的实时预约、签到、续约和签退管理，提高座位利用率。

#### 4.6.2 核心流程
1. 用户查看座位状态
2. 选择空闲座位进行预约
3. 系统生成预约记录
4. 用户到馆后签到
5. 临近结束时可以续约
6. 使用完毕后签退
7. 系统自动检查超时预约和使用结束

#### 4.6.3 关键类和组件
- `SeatController`: 处理座位预约相关请求
- `SysSeatServiceImpl`: 实现座位预约业务逻辑
- `SeatReservation.vue`: 座位预约页面组件

#### 4.6.4 核心特性
- 180个座位网格展示
- 实时时钟显示（指针式+数字式）
- 三种状态可视化（空闲/已预约/使用中）
- 预约15分钟缓冲期
- 重复提醒机制（10秒间隔）
- 自动释放机制（30秒后自动签退）
- 支持续约功能（30分钟/1小时/2小时）

### 4.7 食堂智能服务模块

#### 4.7.1 功能概述
该模块提供食堂3D选座、智能点餐和视觉特效界面，提升食堂服务体验。

#### 4.7.2 核心功能
- 3D选座：可视化选择食堂座位
- 智能点餐：浏览菜单、选择菜品、生成订单
- 视觉特效：GSAP动画和tsParticles粒子效果

#### 4.7.3 关键组件
- `CanteenManagement.vue`: 食堂智能服务主页面
- `CanteenSeating.vue`: 食堂3D选座页面
- `CanteenOrdering.vue`: 食堂智能点餐页面

#### 4.7.4 交互体验
- 鼠标移动产生火花效果
- 点击产生炸裂火花效果
- 卡片悬停3D倾斜效果
- 流畅的页面转场动画

## 5. 开发环境搭建

### 5.1 后端开发环境

#### 5.1.1 环境要求
- JDK 8
- Maven 3.6+
- MySQL 8.0

#### 5.1.2 搭建步骤
1. 安装JDK 8并配置环境变量
2. 安装Maven并配置环境变量
3. 安装MySQL 8.0并创建数据库
4. 克隆项目代码
5. 配置`application.yml`中的数据库连接信息
6. 运行`SmartCampusApplication.java`启动应用

### 5.2 前端开发环境

#### 5.2.1 环境要求
- Node.js 16+
- npm 8+

#### 5.2.2 搭建步骤
1. 安装Node.js和npm
2. 克隆项目代码
3. 进入`vue-demo`目录
4. 运行`npm install`安装依赖
5. 运行`npm run dev`启动开发服务器
6. 访问`http://localhost:5173`查看应用

### 5.3 AI视觉识别开发环境

#### 5.3.1 环境要求
- Python 3.8+
- pip
- 相关Python库：Flask, OpenCV, YOLOv11等

#### 5.3.2 搭建步骤
1. 安装Python 3.8+
2. 克隆项目代码
3. 进入`my_yolo_web`目录
4. 安装依赖：`pip install -r requirements.txt`
5. 运行`python app.py`启动服务
6. 访问`http://localhost:5000`查看应用

## 6. 开发流程规范

### 6.1 代码分支管理
- `main`: 主分支，用于发布稳定版本
- `develop`: 开发分支，用于集成各个功能分支
- `feature/*`: 功能分支，用于开发新功能
- `bugfix/*`:  bug修复分支，用于修复bug

### 6.2 开发流程
1. 从`develop`分支创建功能分支
2. 在功能分支上进行开发
3. 提交代码并推送到远程仓库
4. 创建合并请求到`develop`分支
5. 代码审查通过后合并到`develop`分支
6. 定期从`develop`分支合并到`main`分支发布版本

### 6.3 提交规范
- 提交信息应清晰描述所做的更改
- 提交信息格式：`类型: 描述`
  - 类型：feat(新功能)、fix(修复bug)、docs(文档更新)、style(代码样式)、refactor(代码重构)、test(测试)、chore(构建/工具)
  - 描述：简洁明了的更改描述

## 7. 部署说明

### 7.1 后端部署

#### 7.1.1 打包步骤
1. 进入`smart-campus-backend`目录
2. 运行`mvn clean package`打包应用
3. 生成的jar包位于`target`目录

#### 7.1.2 运行步骤
1. 确保MySQL数据库已启动
2. 运行`java -jar smart-campus-backend.jar`启动应用
3. 应用将在8080端口启动

### 7.2 前端部署

#### 7.2.1 打包步骤
1. 进入`vue-demo`目录
2. 运行`npm run build`打包应用
3. 生成的静态文件位于`dist`目录

#### 7.2.2 部署步骤
1. 将`dist`目录下的文件部署到Web服务器（如Nginx、Apache）
2. 配置Web服务器的反向代理，将API请求转发到后端服务

### 7.3 AI视觉识别部署

#### 7.3.1 打包步骤
1. 进入`my_yolo_web`目录
2. 安装依赖：`pip install -r requirements.txt`
3. 确保模型文件已放置在`models`目录

#### 7.3.2 运行步骤
1. 运行`python app.py`启动服务
2. 服务将在5000端口启动

## 8. 测试说明

### 8.1 测试类型
- 单元测试：测试单个类或方法的功能
- 集成测试：测试多个模块之间的交互
- 系统测试：测试整个系统的功能和性能
- 验收测试：验证系统是否满足需求

### 8.2 测试工具
- 后端：JUnit、Mockito
- 前端：Vitest、Vue Test Utils
- 接口测试：Postman、Swagger

### 8.3 测试流程
1. 编写测试用例
2. 运行测试
3. 分析测试结果
4. 修复发现的问题
5. 重新运行测试直到通过

## 9. 代码规范

### 9.1 后端代码规范
- 遵循Java编码规范
- 使用Lombok简化代码
- 方法和变量命名应清晰明了
- 类和方法应添加适当的注释
- 异常处理应合理

### 9.2 前端代码规范
- 遵循Vue编码规范
- 使用Composition API
- 组件命名采用PascalCase
- 变量和方法命名采用camelCase
- 模板中使用kebab-case
- 添加适当的注释

### 9.3 Python代码规范
- 遵循PEP 8编码规范
- 类命名采用PascalCase
- 变量和方法命名采用snake_case
- 添加适当的注释

## 10. 常见问题处理

### 10.1 数据库连接失败
- 检查数据库是否已启动
- 检查数据库连接配置是否正确
- 检查数据库用户权限是否正确

### 10.2 前端无法访问后端API
- 检查后端服务是否已启动
- 检查前端代理配置是否正确
- 检查浏览器控制台是否有跨域错误

### 10.3 YOLO模型加载失败
- 检查模型文件是否存在
- 检查模型文件路径是否正确
- 检查Python依赖是否安装正确

### 10.4 页面加载缓慢
- 优化前端代码，减少不必要的请求
- 优化图片资源，压缩图片大小
- 优化数据库查询，添加索引
- 考虑使用缓存机制

## 11. 系统集成关系

### 11.1 模块间通信
- 前端通过Axios调用后端API获取数据
- 前端通过iframe集成AI视觉识别系统
- 后端提供统一的数据访问接口
- AI视觉识别系统可独立运行，也可与主系统集成

### 11.2 数据流向
1. AI视觉识别系统采集数据
2. 将数据推送到后端服务
3. 后端存储数据并提供查询接口
4. 前端从后端获取数据并展示
5. 用户通过前端与系统交互

### 11.3 认证机制
- 所有模块共享用户认证信息
- 登录状态通过localStorage持久化
- 路由守卫保护敏感页面

## 12. UI/UX设计规范

### 12.1 颜色系统
- **主色调**: 蓝色系 (#409EFF, #00c6ff, #0072ff) - 代表科技、信任和专业
- **辅助色**: 紫色 (#9a65fd, #7b4af0) - 用于数据模拟和创新功能
- **强调色**: 橙红色 (#ff6b35) - 用于呼吸灯效果和重要提示
- **成功色**: #67C23A - 用于成功状态和操作
- **警告色**: #E6A23C - 用于警告状态和操作
- **危险色**: #F56C6C - 用于危险状态和操作

### 12.2 图标系统
- **图标库**: Iconify (已安装 Tabler, Material Design, Phosphor)
- **使用方式**: `<i-tabler-icon-name />`
- **推荐图标**:
  - 视觉识别: i-tabler-eye
  - 数据模拟: i-tabler-chart-line
  - 设置: i-tabler-settings
  - 分析: i-tabler-chart-bar
  - 主页: i-tabler-home
  - 菜单: i-tabler-menu
  - 搜索: i-tabler-search
  - 添加: i-tabler-plus
  - 编辑: i-tabler-edit
  - 删除: i-tabler-trash

### 12.3 动画规范
- **呼吸灯**: 2-3秒周期，ease-in-out缓动
- **悬停**: 0.3秒过渡，transform scale/shadow
- **拖拽**: 实时响应，cursor: move
- **脉冲**: 2秒周期，box-shadow扩散
- **页面转场**: 平滑的淡入淡出或滑动效果
- **加载状态**: 适当的加载动画，避免用户等待焦虑

### 12.4 布局原则
- **悬浮元素**: 右下三角布局，YOLO最小化居中
- **间距**: 40px/80px/10px三级间距系统
- **层级**: z-index 1000-2000，YOLO窗口最高
- **响应式**: 支持窗口调整大小，最小尺寸800x600px
- **可读性**: 确保文本和背景的对比度符合无障碍标准
- **易用性**: 重要操作按钮应易于发现和点击

## 13. 开发注意事项

### 13.1 后端开发注意事项
- 所有API返回格式统一使用`Result<T>`对象
- 新增实体类需同步创建对应的Mapper和Service
- 数据库操作优先使用MyBatis-Plus内置方法
- 失物招领模块注意状态(0未认领/1已认领)和数量的原子性操作
- 座位预约模块注意状态(0空闲/1已预约/2使用中)和时间管理
- 考虑并发场景，使用适当的锁机制

### 13.2 前端开发注意事项
- 全局样式遵循简约、玻璃拟态（Glassmorphism）和渐变色风格
- 新页面需在`router/index.js`中配置路由
- 新增API需在对应模块的api文件中封装
- 敏感页面需添加路由守卫保护
- 图标优先使用Tabler图标库(i-tabler-xxx)
- 动画效果注意性能，使用transform和opacity
- GSAP动画使用Timeline管理复杂动画序列，注意内存管理
- tsParticles合理配置粒子数量，避免性能问题
- 针对不同屏幕尺寸调整动画效果和粒子数量

### 13.3 AI视觉识别开发注意事项
- 模型文件需放置在`my_yolo_web/models/`目录
- 示例文件需放置在`my_yolo_web/example/`目录
- 检测结果自动保存到`runs/web_exp/`
- 多线程环境下注意使用Lock保证线程安全
- 考虑性能问题，合理配置检测参数

## 14. API文档说明

### 14.1 API路径前缀
- 用户认证: `/api/accounting/auth/`
- 教室管理: `/api/classroom/`
- 统计分析: `/api/stats/`
- 设备管理: `/api/device/`
- 个人记账: `/api/accounting/`
- 失物招领: `/api/lost-found/`
- 座位预约: `/api/seat/`
- AI 视觉识别: `http://localhost:5000/api/`

### 14.2 API设计原则
- 遵循RESTful API设计规范
- 使用适当的HTTP方法：GET(查询)、POST(创建)、PUT(更新)、DELETE(删除)
- 合理的URL设计，使用名词而非动词
- 统一的返回格式，包含状态码、消息和数据
- 适当的错误处理和错误信息返回

### 14.3 API文档工具
- 推荐使用Swagger或Postman生成API文档
- 文档应包含API路径、参数、返回值和示例

## 15. 数据库设计说明

### 15.1 数据库初始化
- 数据库初始化脚本位于`smart-campus-backend/sql/`目录
- 项目启动时按顺序自动执行：
  1. `schema.sql` - 创建数据库表结构
  2. `accounting_schema.sql` - 记账模块预置数据
  3. `lost_found_table.sql` - 失物招领表结构

### 15.2 核心表结构

#### 15.2.1 教室表 (sys_classroom)
- `id`: 教室ID
- `name`: 教室名称
- `type`: 教室类型
- `capacity`: 容纳人数
- `location`: 位置
- `status`: 状态

#### 15.2.2 座位表 (sys_seat)
- `id`: 座位ID
- `seat_no`: 座位编号
- `room_id`: 所属教室ID
- `status`: 状态(0空闲/1已预约/2使用中)
- `create_time`: 创建时间
- `update_time`: 更新时间

#### 15.2.3 失物招领表 (lf_lost_item)
- `id`: 失物ID
- `item_name`: 物品名称
- `item_type`: 物品类型
- `quantity`: 数量
- `room_id`: 发现教室ID
- `status`: 状态(0未认领/1已认领)
- `found_time`: 发现时间
- `claim_time`: 认领时间
- `claimer`: 认领人

#### 15.2.4 账单表 (acc_bill)
- `id`: 账单ID
- `user_id`: 用户ID
- `category_id`: 分类ID
- `amount`: 金额
- `type`: 类型(收入/支出)
- `date`: 日期
- `description`: 描述
- `create_time`: 创建时间
- `update_time`: 更新时间

#### 15.2.5 视觉统计日志表 (visual_stats_log)
- `id`: 日志ID
- `room_id`: 教室ID
- `people_count`: 人数
- `focus_rate`: 专注度
- `device_status`: 设备状态
- `log_time`: 日志时间

## 16. 系统监控与维护

### 16.1 系统监控
- 后端使用Spring Boot Actuator提供监控端点
- 可以集成Prometheus和Grafana进行监控可视化
- 监控指标包括CPU、内存、磁盘、网络、数据库连接等

### 16.2 日志管理
- 后端使用SLF4J + Logback进行日志管理
- 日志级别可配置：DEBUG、INFO、WARN、ERROR
- 日志文件按日期滚动，便于查询和管理

### 16.3 定期维护
- 定期备份数据库
- 清理过期日志文件
- 检查系统性能，优化配置
- 更新依赖库，修复安全漏洞

## 17. 未来规划

### 17.1 功能扩展
- 数据分析中心：校园运营数据综合分析
- 智能考勤系统：基于AI视觉识别的考勤管理
- 校园资产管理：资产的登记、盘点和维护
- 智能排班系统：教室和教师的智能排班

### 17.2 技术升级
- 后端升级到Spring Boot 3.x
- 前端考虑使用TypeScript提升代码质量
- 引入微服务架构，提高系统的可扩展性
- 考虑使用容器化部署，提高部署效率

### 17.3 性能优化
- 优化数据库查询，提高查询效率
- 引入缓存机制，减少数据库压力
- 优化前端性能，提高页面加载速度
- 考虑使用CDN加速静态资源访问

## 18. 结语

智学空间·校园智慧空间治理系统是一个功能全面、技术先进的校园管理系统，涵盖了校园管理的多个方面。本开发说明文档详细介绍了系统的技术栈、项目结构、核心模块设计、开发环境搭建、开发流程规范、部署说明等内容，为开发人员提供了全面的指导。

系统采用了前后端分离架构，支持多端访问，具有良好的可扩展性和可维护性。通过AI视觉识别、物联网技术、大数据分析等先进技术，实现了校园空间的智能化管理，提升了校园管理效率和服务质量。

未来，系统将继续扩展功能，升级技术，优化性能，为师生提供更加便捷、舒适的校园环境，助力智慧校园建设。

---

**文档版本**: 1.0
**编写日期**: 2026-01-25
**编写人员**: TRAECN开发团队
**适用范围**: 智学空间·校园智慧空间治理系统开发人员

---