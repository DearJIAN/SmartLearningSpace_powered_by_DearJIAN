# 智学空间·校园智慧空间治理系统 - 开发说明文档

## 📚 文档说明

本文档是《智学空间·校园智慧空间治理系统》的完整开发说明文档，涵盖了项目的技术架构、功能模块、开发环境搭建、API接口、数据库设计、开发规范等全方位内容。

**文档版本**: v2.0.0
**最后更新**: 2026-05-03
**项目状态**: 数字人 Live2D + 语音交互已完成集成

---

## 🌟 个人水印

本项目为 **DearJIAN** 创制的校园智慧空间治理平台。

### 个人信息

| 标识 | 说明 |
|------|------|
| 开发者 | **DearJIAN** |
| 开发者主页 | [https://github.com/DearJIAN](https://github.com/DearJIAN) |
| 项目地址 | [SmartLearningSpace_powered_by_DearJIAN](https://github.com/DearJIAN/SmartLearningSpace_powered_by_DearJIAN) |

### 水印展示

项目在以下位置展示个人水印，接受用户监督：

| 位置 | 文件 | 说明 |
|------|------|------|
| 登录页底部 | `vue-demo/src/views/Login.vue` | 渐变文字水印，显示 DearJIAN + 项目名 |
| 全局右下角 | `vue-demo/src/App.vue` | 固定浮层，所有已登录页面可见 |
| 组件源码 | `vue-demo/src/components/BrandingFooter.vue` | 可复用个人水印组件 |

### 水印设计说明

- **视觉风格**：Flat Design，渐变色文字，低调不抢主视觉
- **配色**：DearJIAN 用紫蓝渐变（`#a78bfa → #818cf8`），项目名用翠绿渐变（`#34d399 → #6ee7b7`）
- **交互**：hover 时轻微放大，带平滑过渡动画
- **无侵扰性**：字号极小（11-12px），不干扰正常业务流程

---

## 📑 目录

- [📚 文档说明](#📚-文档说明)
- [🌟 个人水印](#🌟-个人水印)
- [🎯 项目概述](#🎯-项目概述)
  - [项目简介](#项目简介)
  - [核心价值](#核心价值)
  - [技术亮点](#技术亮点)
- [🛠️ 技术栈](#🛠️-技术栈)
  - [后端技术栈](#后端技术栈)
  - [前端技术栈](#前端技术栈)
  - [AI视觉与数字人技术栈](#ai视觉与数字人技术栈)
- [📂 项目结构](#📂-项目结构)
  - [整体目录结构](#整体目录结构)
  - [后端详细结构](#后端详细结构)
  - [前端详细结构](#前端详细结构)
- [🚀 开发环境搭建](#🚀-开发环境搭建)
  - [环境要求](#环境要求)
  - [后端环境搭建](#后端环境搭建)
  - [前端环境搭建](#前端环境搭建)
  - [AI视觉与数字人子系统环境搭建](#ai视觉与数字人子系统环境搭建)
- [📦 核心功能模块详解](#📦-核心功能模块详解)
  - [1. 用户认证系统](#1-用户认证系统)
  - [2. 教室状态实时监控](#2-教室状态实时监控)
  - [3. 校园空间导航](#3-校园空间导航)
  - [4. AI视觉识别与数字人子系统](#4-ai视觉识别与数字人子系统)
  - [5. 个人记账与财务洞察](#5-个人记账与财务洞察)
  - [6. 失物招领功能](#6-失物招领功能)
  - [7. 图书馆座位预约](#7-图书馆座位预约)
  - [8. 食堂智能服务](#8-食堂智能服务)
- [📊 数据库设计](#📊-数据库设计)
  - [数据库表结构](#数据库表结构)
  - [数据库初始化](#数据库初始化)
- [🔌 API接口文档](#🔌-api接口文档)
  - [统一返回格式](#统一返回格式)
  - [用户认证API](#用户认证api)
  - [教室管理API](#教室管理api)
  - [统计分析API](#统计分析api)
  - [账单管理API](#账单管理API)
  - [失物招领API](#失物招领API)
  - [座位预约API](#座位预约API)
  - [AI视觉与数字人API](#ai视觉与数字人API)
- [🎨 UI/UX设计规范](#🎨-uiux设计规范)
  - [颜色系统](#颜色系统)
  - [图标系统](#图标系统)
  - [动画规范](#动画规范)
  - [布局原则](#布局原则)
  - [响应式设计](#响应式设计)
- [📝 开发规范](#📝-开发规范)
  - [前端开发规范](#前端开发规范)
  - [后端开发规范](#后端开发规范)
  - [数据库操作规范](#数据库操作规范)
- [🚢 部署指南](#🚢-部署指南)
  - [后端部署](#后端部署)
  - [前端部署](#前端部署)
  - [AI视觉与数字人子系统部署](#ai视觉与数字人子系统部署)
  - [Docker部署](#docker部署)
- [🔄 版本控制与 .gitignore 说明](#🔄-版本控制与-gitignore-说明)
- [🔧 常见问题与解决方案](#🔧-常见问题与解决方案)
  - [后端常见问题](#后端常见问题)
  - [前端常见问题](#前端常见问题)
  - [AI与数字人子系统常见问题](#ai与数字人子系统常见问题)
  - [部署常见问题](#部署常见问题)
- [📞 技术支持](#📞-技术支持)
- [📝 更新日志](#📝-更新日志)
- [📄 许可证](#📄-许可证)
- [🤖 数字人 Live2D + 语音交互集成说明](#数字人-live2d--语音交互集成说明)

------

## 🎯 项目概述

### 项目简介

《智学空间·校园智慧空间治理系统》是一个集成了多种智能功能的校园综合管理平台，通过人工智能、物联网、大数据分析等技术，实现校园空间的智能化管理和优化。

### 核心价值

1. **智能监控**: 基于AI视觉识别的教室使用情况实时监控
2. **资源优化**: 智能座位预约、失物招领，提高校园资源利用率
3. **数据分析**: 多维度数据统计和可视化分析
4. **用户体验**: 现代化UI设计，流畅的交互体验
5. **AI赋能**: 集成AI助手，提供智能对话和决策支持

### 技术亮点

- **前后端分离架构**: Vue 3 + Spring Boot + Flask
- **AI视觉识别**: YOLOv11 目标检测
- **实时数据流**: WebSocket + 轮询机制
- **数字人Live2D**: 全局AI数字人助手"火花" + Live2D形象 + 语音交互
- **智能对话**: 集成火山方舟 ARK SDK（豆包大模型）
- **现代化UI**: 玻璃拟态设计 + GSAP动画 + 粒子效果
- **多端适配**: 响应式设计，支持多种屏幕尺寸

---

## 🛠️ 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 8 | 编程语言 |
| Spring Boot | 2.7.18 | Web框架 |
| MyBatis-Plus | 3.5.3.1 | ORM框架 |
| MySQL | 8.0 | 数据库 |
| Druid | 1.2.16 | 数据库连接池 |
| Lombok | 最新版 | 代码简化工具 |
| Apache POI | 5.2.3 | Excel导出 |
| OkHttp | 4.10.0 | HTTP客户端(AI集成) |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.26 | 前端框架 |
| Vite | 7.3.0 | 构建工具 |
| Element Plus | 2.13.1 | UI组件库 |
| ECharts | 6.0.0 | 数据可视化 |
| Axios | 1.13.2 | HTTP客户端 |
| Vue Router | 4.6.4 | 路由管理 |
| GSAP | 最新版 | 动画库 |
| tsParticles | 3.9.1 | 粒子效果 |
| Three.js | 0.182.0 | 3D渲染 |
| Iconify | 最新版 | 图标系统 |

### AI视觉与数字人技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Python | 3.8+ | 编程语言（newyolo conda 环境） |
| Flask | 最新版 | Web框架 + YOLO 检测 + AI 对话 + 语音服务 |
| YOLOv11 | 11n/11m | 目标检测模型 |
| OpenCV | 最新版 | 图像处理 |
| 火山方舟 ARK SDK | 最新版 | AI 大模型调用（豆包 doubao-seed-1-6） |
| Live2D Cubism 5 | 最新版 | 数字人形象渲染 |
| faster-whisper | 1.2.1 | 本地语音识别（ASR） |
| 火山 TTS | 最新版 | 语音合成 |

---

## 📂 项目结构

### 整体目录结构

```
E:\LEAR-CODE\
├── smart-campus-backend/        # 后端项目
│   ├── src/main/java/com/smartcampus/
│   │   ├── config/              # 配置类
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # 数据访问层
│   │   └── common/             # 通用组件
│   ├── sql/                    # 数据库脚本
│   └── src/main/resources/     # 配置文件
│
├── vue-demo/                    # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── views/              # 页面组件
│   │   ├── components/         # 通用组件
│   │   ├── router/             # 路由配置
│   │   ├── utils/              # 工具类
│   │   └── assets/             # 静态资源
│   ├── public/                 # 公共资源
│   └── package.json            # 依赖配置
│
└── my_yolo_web/                 # AI视觉与数字人子系统
    ├── app.py                   # Flask主应用（YOLO + AI + 语音）
    ├── web_inference.py         # YOLO检测逻辑
    ├── yolo_io_utils.py         # IO工具类
    ├── ai_api.py                # [已废弃] 旧版AI对话接口
    ├── .env                     # 环境变量配置（不纳入版本控制）
    ├── .env.example             # 环境变量示例文件
    ├── backend/services/        # AI数字人服务
    │   ├── langchain_service.py       # ARK大模型服务
    │   ├── tts_service.py             # TTS语音合成
    │   ├── volc_realtime_bridge.py    # 火山实时语音桥接
    │   └── volc_realtime_protocol.py  # 实时语音协议
    ├── models/                  # YOLO模型文件（.pt，需自行下载）
    ├── static/                  # 静态资源
    ├── templates/               # HTML模板
    ├── example/                 # 示例文件（图片/视频）
    └── runs/                    # 检测结果输出（不纳入版本控制）
```

### 后端详细结构

```
smart-campus-backend/src/main/java/com/smartcampus/
├── SmartCampusApplication.java  # 启动类
│
├── config/
│   ├── MybatisPlusConfig.java   # MyBatis-Plus配置
│   └── WebConfig.java           # Web配置
│
├── controller/                  # 控制器层
│   ├── AccAiController.java     # AI助手控制器
│   ├── AccAnalysisController.java # 财务分析控制器
│   ├── AccBillController.java   # 账单控制器
│   ├── AccBudgetController.java # 预算控制器
│   ├── AccCalendarController.java # 日历控制器
│   ├── AccInsightController.java # 洞察控制器
│   ├── AccountingAuthController.java # 认证控制器
│   ├── AccSetupController.java  # 设置控制器
│   ├── ClassroomController.java # 教室管理控制器
│   ├── DeviceController.java    # 设备管理控制器
│   ├── LFLostItemController.java # 失物招领控制器
│   ├── SeatController.java      # 座位预约控制器
│   └── StatsController.java     # 统计分析控制器
│
├── service/                     # 服务层
│   ├── impl/                    # 服务实现
│   ├── AccAiService.java        # AI服务接口
│   ├── AccBillService.java      # 账单服务
│   ├── AccBudgetService.java    # 预算服务
│   ├── AccCategoryService.java # 分类服务
│   ├── AccFinancialGoalService.java # 财务目标服务
│   ├── LFLostItemService.java   # 失物招领服务
│   └── SeatBookingService.java  # 座位预约服务
│
├── entity/                      # 实体类
│   ├── AccBill.java            # 账单实体
│   ├── AccBudget.java          # 预算实体
│   ├── AccCategory.java        # 分类实体
│   ├── AccFinancialGoal.java    # 财志目标实体
│   ├── LFLostItem.java         # 失物招领实体
│   ├── SeatBooking.java        # 座位预约实体
│   ├── SysClassroom.java       # 教室实体
│   ├── SysSeat.java            # 座位实体
│   ├── SysUser.java            # 用户实体
│   └── VisualStatsLog.java     # 可视化统计日志实体
│
├── mapper/                      # 数据访问层
│   ├── AccBillMapper.java
│   ├── AccBudgetMapper.java
│   ├── AccCategoryMapper.java
│   ├── AccFinancialGoalMapper.java
│   ├── LFLostItemMapper.java
│   ├── SeatBookingMapper.java
│   ├── SysClassroomMapper.java
│   ├── SysSeatMapper.java
│   ├── SysUserMapper.java
│   └── VisualStatsLogMapper.java
│
└── common/                      # 通用组件
    └── Result.java             # 统一返回结果
```

### 前端详细结构

```
vue-demo/src/
├── api/                        # API接口封装
│   ├── accounting.js           # 记账模块API
│   ├── seat.js                 # 座位预约API
│   └── accounting.js           # 认证API
│
├── views/                      # 页面组件
│   ├── Login.vue               # 登录页面
│   ├── DashboardView.vue       # 教室监控看板
│   ├── MapView.vue             # 校园空间导航
│   ├── LostFound.vue           # 失物招领
│   ├── SeatReservation.vue     # 座位预约
│   ├── CanteenManagement.vue    # 食堂服务主页面
│   ├── CanteenSeating.vue      # 食堂选座
│   ├── CanteenOrdering.vue     # 食堂点餐
│   │
│   └── accounting/             # 记账模块
│       ├── AccountingLayout.vue # 布局组件
│       ├── BillList.vue        # 账单明细
│       ├── BillAnalysis.vue    # 统计报表
│       ├── BillTreemap.vue     # 资金流向树
│       ├── BillCalendar.vue    # 账单日历
│       ├── BudgetManagement.vue # 预算管理
│       ├── AiChat.vue          # AI助手
│       └── insight/            # 洞察中心
│           ├── InsightDashboard.vue # 洞察总览
│           ├── InsightGoal.vue     # 财务目标
│           ├── InsightProfile.vue  # 用户画像
│           ├── InsightRisk.vue     # 风险评估
│           └── InsightTimeline.vue # 财务时间轴
│
├── components/                 # 通用组件
│   ├── FocusTrendChart.vue     # 专注度趋势图表
│   └── FloatingSimulator.vue   # 数据模拟器
│
├── router/                     # 路由配置
│   └── index.js                # 路由定义
│
├── utils/                      # 工具类
│   └── request.js              # HTTP请求封装
│
├── assets/                     # 静态资源
│   ├── images/                 # 图片资源
│   └── ...                     # 其他资源
│
├── App.vue                     # 根组件
└── main.js                     # 入口文件
```

---

## 🚀 开发环境搭建

### 环境要求

| 组件 | 版本要求 |
|------|---------|
| JDK | 8+ |
| Node.js | 20.19.0+ 或 22.12.0+ |
| Python | 3.8+ |
| MySQL | 8.0+ |
| Maven | 3.6+ |

### 后端环境搭建

#### 1. 安装JDK 8

```bash
# 下载并安装JDK 8
# 配置JAVA_HOME环境变量
```

#### 2. 安装Maven

```bash
# 下载并安装Maven 3.6+
# 配置MAVEN_HOME环境变量
```

#### 3. 配置MySQL数据库

```sql
-- 创建数据库
CREATE DATABASE smart_campus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER 'smartcampus'@'localhost' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON smart_campus.* TO 'smartcampus'@'localhost';
FLUSH PRIVILEGES;
```

#### 4. 配置数据库连接

修改 `smart-campus-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_campus?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root          # 根据实际情况修改
    password: 123456        # 根据实际情况修改
```

#### 5. 初始化数据库

数据库初始化脚本按以下顺序自动执行：
1. `schema.sql` - 建库/表
2. `accounting_schema.sql` - 记账预置数据
3. `lost_found_table.sql` - 失物招领表结构

如需手动执行：

```bash
# 连接MySQL
mysql -u root -p

# 选择数据库
USE smart_campus;

# 按顺序执行脚本
SOURCE E:/LEAR-CODE/smart-campus-backend/sql/schema.sql;
SOURCE E:/LEAR-CODE/smart-campus-backend/sql/accounting_schema.sql;
SOURCE E:/LEAR-CODE/smart-campus-backend/sql/lost_found_table.sql;
```

#### 6. 启动后端服务

```bash
cd E:/LEAR-CODE/smart-campus-backend

# 使用Maven编译并运行
mvn spring-boot:run

# 或者先编译再运行
mvn clean package
java -jar target/smart-campus-backend-1.0.0.jar
```

服务启动后访问: http://localhost:8080

### 前端环境搭建

#### 1. 安装Node.js

```bash
# 下载并安装Node.js 20.19.0+ 或 22.12.0+
# 验证安装
node --version
npm --version
```

#### 2. 安装依赖

```bash
cd E:/LEAR-CODE/vue-demo

# 安装依赖（使用淘宝镜像加速）
npm install --registry=https://registry.npmmirror.com

# 或使用pnpm
pnpm install
```

#### 3. 启动开发服务器

```bash
npm run dev

# 或
pnpm dev
```

前端服务启动后访问: http://localhost:5173

#### 4. 构建生产版本

```bash
npm run build

# 构建产物位于 dist/ 目录
```

### AI视觉子系统环境搭建

#### 1. 安装Python 3.8+

```bash
# 下载并安装Python 3.8+
# 验证安装
python --version
```

#### 2. 安装依赖

```bash
cd E:/LEAR-CODE/my_yolo_web

# 创建虚拟环境（推荐）
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt

# 如果requirements.txt不存在，手动安装核心依赖
pip install flask opencv-python ultralytics numpy requests pillow
```

#### 3. 下载YOLO模型

```bash
# 模型文件放置在 my_yolo_web/models/ 目录
# 需要的模型文件:
# - yolo11n.pt (5.3MB)
# - yolo11m.pt (38.8MB)
# - 自定义教室模型 (custom_classroom.pt)

# 下载地址: https://github.com/ultralytics/ultralytics/releases
```

#### 4. 启动AI服务

```bash
cd E:/LEAR-CODE/my_yolo_web

# 启动Flask应用
python app.py
```

AI服务启动后访问: http://localhost:5000

---

## 📦 核心功能模块详解

### 1. 用户认证系统

#### 功能概述

提供企业级单点登录、注册、登出功能，支持用户权限管理。

#### 技术实现

**后端实现**:
- 控制器: `AccountingAuthController`
- 实体: `SysUser`
- Mapper: `SysUserMapper`
- 路径: `/api/accounting/auth/`

**前端实现**:
- 页面: `Login.vue`
- API: `api/accounting.js`
- 路由守卫: `router/index.js`

#### 核心功能

1. **用户注册**
   - 用户名/邮箱注册
   - 密码加密存储
   - 重复注册检测

2. **用户登录**
   - 用户名/密码登录
   - JWT Token生成
   - 登录状态持久化

3. **路由守卫**
   - 保护敏感页面（`/dashboard`, `/accounting/*`等）
   - 未登录用户自动跳转到登录页
   - 验证信息缓存1小时，提升加载速度

4. **登出功能**
   - 清除登录状态
   - 跳转到登录页

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | `/api/accounting/auth/register` | 注册新用户 |
| 用户登录 | POST | `/api/accounting/auth/login` | 用户登录 |
| 获取当前用户 | GET | `/api/accounting/auth/current` | 获取当前登录用户信息 |
| 用户登出 | POST | `/api/accounting/auth/logout` | 用户登出 |

#### 开发注意事项

1. 登录状态通过 `localStorage` 持久化
2. 路由守卫配置在 `router/index.js` 的 `beforeEach` 钩子
3. 登录/注册成功后默认跳转到校园空间导航 (`/`)
4. 验证信息缓存机制: `_verified` 标记 + 1小时时效

---

### 2. 教室状态实时监控

#### 功能概述

基于AI视觉识别的教室人数统计、专注度分析、设备管理，提供实时监控看板。

#### 技术实现

**后端实现**:
- 控制器: `ClassroomController`, `StatsController`
- 实体: `SysClassroom`, `VisualStatsLog`
- Mapper: `SysClassroomMapper`, `VisualStatsLogMapper`
- 路径: `/api/classroom/`, `/api/stats/`

**前端实现**:
- 页面: `DashboardView.vue`
- 组件: `FocusTrendChart.vue`, `FloatingSimulator.vue`
- AI集成: 通过iframe嵌入YOLO子系统

#### 核心功能

1. **实时监控**
   - 教室人数统计
   - 专注度分析
   - 设备状态监控
   - 24小时数据趋势

2. **智能数据生成**
   - 根据课表规律自动调整人数和专注度
   - 上课时间自动触发高峰期
   - 休息时间自动降低使用率

3. **AI视觉集成**
   - 实时视频流检测
   - 目标识别和统计
   - 检测结果可视化

4. **悬浮控制面板**
   - AI视觉入口
   - 数据模拟按钮
   - YOLO子系统窗口（支持拖拽、调整大小、最小化）

#### 数据模拟逻辑

```javascript
// 课表时间判断
function getClassScheduleStatus() {
  const hour = new Date().getHours();
  if (hour >= 8 && hour < 12) return 'morning';    // 上午上课
  if (hour >= 14 && hour < 18) return 'afternoon'; // 下午上课
  if (hour >= 19 && hour < 22) return 'evening';  // 晚自习
  return 'break';                                   // 休息时间
}

// 根据时间调整人数和专注度
function adjustDataBySchedule() {
  const schedule = getClassScheduleStatus();
  if (schedule === 'morning' || schedule === 'afternoon') {
    // 上课时间：人数多，专注度高
    personCount = random(60, 100);
    focusScore = random(75, 95);
  } else if (schedule === 'evening') {
    // 晚自习：人数中等，专注度中等
    personCount = random(40, 70);
    focusScore = random(65, 85);
  } else {
    // 休息时间：人数少，专注度低
    personCount = random(5, 20);
    focusScore = random(40, 60);
  }
}
```

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有教室 | GET | `/api/classroom/list` | 获取教室列表 |
| 获取教室详情 | GET | `/api/classroom/{id}` | 获取指定教室信息 |
| 获取实时统计 | GET | `/api/stats/realtime` | 获取实时统计数据 |
| 获取历史趋势 | GET | `/api/stats/trend` | 获取24小时趋势数据 |
| 模拟数据 | POST | `/api/stats/simulate` | 生成模拟数据 |

#### UI设计规范

**悬浮按钮布局**:
- YOLO最小化窗口: `bottom: 40px; left: 50%; transform: translateX(-50%)`
- AI视觉按钮: `bottom: 130px; right: 40px`
- 数据模拟按钮: `bottom: 40px; right: 40px`

**颜色方案**:
- 紫色数据模拟按钮: `linear-gradient(135deg, #9a65fd, #7b4af0)`
- 橙红色呼吸灯: `#ff6b35`
- 蓝色AI视觉按钮: `linear-gradient(135deg, #00c6ff, #0072ff)`

**动画效果**:
- 呼吸灯: 2-3秒周期，`ease-in-out` 缓动
- 脉冲: 2秒周期，`box-shadow` 扩散
- 悬停: 0.3秒过渡，`transform scale/shadow`

---

### 3. 校园空间导航

#### 功能概述

校园地图展示和空间可视化，提供直观的校园导航服务。

#### 技术实现

**前端实现**:
- 页面: `MapView.vue`
- 静态资源: `src/assets/images/`

#### 核心功能

1. **地图展示**
   - 校园平面地图
   - 建筑物标注
   - 空间位置指示

2. **交互功能**
   - 点击查看详情
   - 缩放和平移
   - 路径规划（预留）

3. **数据联动**
   - 与教室监控联动
   - 显示实时状态
   - 空间使用率统计

#### 开发说明

当前版本使用静态地图资源，未来可扩展为：
- 在线地图集成（高德/百度地图）
- 3D校园模型
- 实时导航路径规划

---

### 4. AI视觉识别子系统

#### 功能概述

基于YOLOv11的目标检测系统，支持实时视频流分析和AI对话功能。

#### 技术实现

**后端实现**:
- 主应用: `app.py` (Flask)
- 检测逻辑: `web_inference.py` (YOLO检测)
- AI接口: `ai_api.py` (豆包API)
- 工具类: `yolo_io_utils.py` (IO操作)

**前端集成**:
- 通过iframe嵌入到主系统
- 实时接收检测结果
- 支持交互式操作

#### 核心功能

1. **目标检测**
   - 支持多模型切换 (yolo11n.pt, yolo11m.pt, 自定义模型)
   - 实时视频流检测
   - 图片/视频文件检测
   - 多线程处理 + 锁机制

2. **参数配置**
   - 置信度阈值 (conf_thres)
   - IOU阈值 (iou_thres)
   - 输入尺寸 (imgsz)
   - 输入源选择 (摄像头/文件)

3. **结果持久化**
   - 自动保存检测视频/图片到 `runs/web_exp/`
   - 结果数据统计
   - 检测日志记录

4. **AI对话**
   - 集成豆包API
   - 流式响应
   - 支持自然语言交互

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 开始检测 | POST | `/api/start` | 启动检测 |
| 停止检测 | POST | `/api/stop` | 停止检测 |
| 获取检测结果 | GET | `/api/results` | 获取检测结果 |
| 上传文件 | POST | `/api/upload` | 上传检测文件 |
| AI对话 | POST | `/api/chat` | AI对话接口 |

#### 模型说明

**模型文件**:
```
my_yolo_web/models/
├── yolo11n.pt      # 轻量级模型 (5.3MB)
├── yolo11m.pt      # 中型模型 (38.8MB)
└── custom_classroom.pt  # 自定义教室模型
```

**模型对比**:

| 模型 | 大小 | 速度 | 精度 | 适用场景 |
|------|------|------|------|---------|
| yolo11n.pt | 5.3MB | 快 | 中 | 实时检测、移动端 |
| yolo11m.pt | 38.8MB | 中 | 高 | 高精度检测 |
| custom_classroom.pt | - | - | 高 | 教室场景专用 |

#### 多线程安全

使用 `threading.Lock` 保证多线程环境下的数据安全：

```python
from threading import Lock

detection_lock = Lock()

def safe_detect(image):
    with detection_lock:
        result = model(image)
    return result
```

---

### 5. 个人记账与财务洞察

#### 功能概述

完整的个人财务管理系统，包括账单管理、多维统计、预算管理、AI财务助手、财务洞察分析。

#### 技术实现

**后端实现**:
- 控制器: `AccBillController`, `AccAnalysisController`, `AccBudgetController`, `AccInsightController`
- 实体: `AccBill`, `AccCategory`, `AccBudget`, `AccFinancialGoal`
- Mapper: 对应Mapper接口
- 路径: `/api/accounting/`

**前端实现**:
- 布局: `AccountingLayout.vue`
- 页面: `BillList.vue`, `BillAnalysis.vue`, `BillTreemap.vue`, `BillCalendar.vue`
- 预算: `BudgetManagement.vue`
- AI助手: `AiChat.vue`
- 洞察: `insight/` 目录下的洞察组件

#### 核心功能

1. **账单管理**
   - 账单增删改查
   - 多维度筛选
   - 账单分类管理
   - 导出Excel

2. **统计分析**
   - 收支统计
   - 分类占比
   - 月度/年度趋势
   - 资金流向树状图

3. **预算管理**
   - 预算设置
   - 预算执行监控
   - 超支提醒
   - 预算进度可视化

4. **AI财务助手**
   - 智能问答
   - 财务建议
   - 消费分析
   - 风险预警

5. **财务洞察**
   - **洞察总览** (`InsightDashboard.vue`): 综合财务健康度评估
   - **财务目标** (`InsightGoal.vue`): 目标达成度分析
   - **用户画像** (`InsightProfile.vue`): 消费习惯分析
   - **风险评估** (`InsightRisk.vue`): 消费风险识别
   - **财务时间轴** (`InsightTimeline.vue`): 财务事件时间线

#### 财务健康模型

基于多维度加权计算财务健康度：

```java
public FinancialHealth getFinancialHealth(Long userId) {
    // 1. 消费稳定性分析
    double stability = calculateStability(userId);

    // 2. 储蓄率计算
    double savingsRate = calculateSavingsRate(userId);

    // 3. 支出集中度
    double concentration = calculateConcentration(userId);

    // 4. 预算执行率
    double budgetExecution = calculateBudgetExecution(userId);

    // 5. 加权计算综合得分
    double healthScore = stability * 0.25 +
                         savingsRate * 0.30 +
                         concentration * 0.20 +
                         budgetExecution * 0.25;

    return new FinancialHealth(healthScore, getHealthLevel(healthScore));
}
```

#### AI对话增强

对话时注入"实时数据库快照"，提供上下文感知：

```java
public String getAiPromptWithSnapshot(Long userId) {
    // 获取实时数据快照
    double currentBudget = getCurrentBudget(userId);
    double recentBalance = getRecentBalance(userId);
    int riskLevel = assessRisk(userId);

    // 构建上下文提示
    String prompt = String.format(
        "用户当前预算余额: %.2f元, 近期结余: %.2f元, 风险等级: %d. " +
        "请基于此数据回答用户问题。",
        currentBudget, recentBalance, riskLevel
    );

    return prompt;
}
```

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取账单列表 | GET | `/api/accounting/bills` | 分页获取账单 |
| 创建账单 | POST | `/api/accounting/bills` | 创建新账单 |
| 更新账单 | PUT | `/api/accounting/bills/{id}` | 更新账单 |
| 删除账单 | DELETE | `/api/accounting/bills/{id}` | 删除账单 |
| 导出Excel | GET | `/api/accounting/bills/export` | 导出账单Excel |
| 获取统计 | GET | `/api/accounting/stats` | 获取统计数据 |
| AI对话 | POST | `/api/accounting/ai/chat` | AI助手对话 |
| 获取洞察 | GET | `/api/accounting/insight` | 获取财务洞察 |

---

### 6. 失物招领功能

#### 功能概述

基于AI视觉识别的失物检测、认领管理和记录更新，提供智能化的失物招领服务。

#### 技术实现

**后端实现**:
- 控制器: `LFLostItemController`
- 服务: `LFLostItemService`
- 实体: `LFLostItem`
- Mapper: `LFLostItemMapper`
- XML配置: `LFLostItemMapper.xml`
- 路径: `/api/lost-found/`

**前端实现**:
- 页面: `LostFound.vue`
- AI集成: 通过iframe嵌入YOLO子系统

#### 核心功能

1. **失物检测**
   - AI视觉自动检测
   - 自动创建失物记录
   - 实时更新失物数量

2. **认领管理**
   - 失物列表展示
   - 支持全部认领
   - 支持部分认领（指定数量）
   - 认领状态更新

3. **筛选功能**
   - 按教室筛选
   - 按物品类型筛选
   - 按状态筛选（未认领/已认领）
   - 按时间范围筛选

4. **数据同步**
   - 1秒间隔自动刷新
   - 实时数量统计
   - 动态物品类型提取

#### 数据流程

```
YOLO检测
    ↓
识别到失物
    ↓
POST /api/lost-found/detect
    ↓
后端处理
    ↓
检查是否已存在
    ├─ 存在 → 更新数量
    └─ 不存在 → 创建新记录
    ↓
前端自动刷新
    ↓
更新失物列表
```

#### 核心逻辑

**自动生成失物记录**:

```java
public LFLostItem autoGenerateLostItem(
    String classroomId,
    String itemType,
    int quantity,
    String imageUrl
) {
    // 检查是否已存在相同失物
    LFLostItem existing = getByClassroomAndItem(classroomId, itemType);

    if (existing != null) {
        // 更新数量
        existing.setQuantity(existing.getQuantity() + quantity);
        updateById(existing);
        return existing;
    } else {
        // 创建新记录
        LFLostItem newItem = new LFLostItem();
        newItem.setClassroomId(classroomId);
        newItem.setItemType(itemType);
        newItem.setQuantity(quantity);
        newItem.setImageUrl(imageUrl);
        newItem.setStatus(0); // 0: 未认领
        newItem.setFoundTime(new Date());
        save(newItem);
        return newItem;
    }
}
```

**部分认领功能**:

```java
public boolean claimLostItem(Long itemId, int claimQuantity) {
    LFLostItem item = getById(itemId);

    if (item == null || item.getStatus() != 0) {
        return false;
    }

    if (claimQuantity >= item.getQuantity()) {
        // 全部认领
        item.setStatus(1); // 已认领
        item.setClaimTime(new Date());
    } else {
        // 部分认领
        item.setQuantity(item.getQuantity() - claimQuantity);
    }

    updateById(item);
    return true;
}
```

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取失物列表 | GET | `/api/lost-found/list` | 分页获取失物 |
| 自动创建失物 | POST | `/api/lost-found/detect` | AI检测后自动创建 |
| 认领失物 | POST | `/api/lost-found/claim/{id}` | 认领失物 |
| 更新失物 | PUT | `/api/lost-found/{id}` | 更新失物信息 |
| 删除失物 | DELETE | `/api/lost-found/{id}` | 删除失物 |
| 清空失物 | DELETE | `/api/lost-found/clear` | 清空所有失物 |

#### UI设计

**拖拽式YOLO窗口**:
- 支持拖拽移动
- 支持调整大小
- 支持最小化/恢复/关闭
- 默认位置: 右下角

**失物卡片**:
- 失物图片展示
- 物品类型和数量
- 教室位置
- 发现时间
- 认领按钮

---

### 7. 图书馆座位预约

#### 功能概述

图书馆座位实时预约、签到、续约、签退管理，提供智能化的座位资源调度。

#### 技术实现

**后端实现**:
- 控制器: `SeatController`
- 服务: `SysSeatService` (实现类)
- 实体: `SysSeat`, `SeatBooking`
- Mapper: `SysSeatMapper`, `SeatBookingMapper`
- 路径: `/api/seat/`

**前端实现**:
- 页面: `SeatReservation.vue`
- API: `api/seat.js`

#### 核心功能

1. **座位展示**
   - 180个座位网格展示
   - 三种状态可视化（空闲/已预约/使用中）
   - 座位编号（S001-S180）

2. **预约管理**
   - 实时预约
   - 自定义开始时间
   - 使用时长选择（30分钟/1小时/2小时）
   - 15分钟缓冲期

3. **签到续约**
   - 签到入座
   - 续约延长
   - 自动释放机制
   - 重复提醒

4. **实时监控**
   - 2秒间隔自动刷新
   - 模拟时钟显示（指针式+数字式）
   - 通知中心（超时/未守约/自动释放）

#### 座位状态

| 状态 | 值 | 说明 |
|------|-----|------|
| 空闲 | 0 | 可预约 |
| 已预约 | 1 | 已预约未签到 |
| 使用中 | 2 | 已签到使用 |

#### 核心逻辑

**预约座位**:

```java
public boolean reserve(Long seatId, Date startTime, int durationMinutes) {
    SysSeat seat = getById(seatId);

    if (seat.getStatus() != 0) {
        throw new BusinessException("座位不可预约");
    }

    // 创建预约记录
    SeatBooking booking = new SeatBooking();
    booking.setSeatId(seatId);
    booking.setUserId(getCurrentUserId());
    booking.setStartTime(startTime);
    booking.setDurationMinutes(durationMinutes);
    booking.setEndTime(calculateEndTime(startTime, durationMinutes));
    booking.setStatus(1); // 已预约

    bookingMapper.insert(booking);

    // 更新座位状态
    seat.setStatus(1);
    updateById(seat);

    return true;
}
```

**签到入座**:

```java
public boolean checkIn(Long seatId) {
    SysSeat seat = getById(seatId);
    SeatBooking booking = getActiveBooking(seatId);

    if (booking == null || booking.getStatus() != 1) {
        throw new BusinessException("没有有效的预约记录");
    }

    // 检查是否超时
    if (isExpired(booking.getStartTime())) {
        throw new BusinessException("预约已超时");
    }

    // 更新预约状态
    booking.setStatus(2); // 使用中
    booking.setCheckInTime(new Date());
    bookingMapper.updateById(booking);

    // 更新座位状态
    seat.setStatus(2);
    updateById(seat);

    return true;
}
```

**续约座位**:

```java
public boolean renew(Long seatId, int extendMinutes) {
    SysSeat seat = getById(seatId);
    SeatBooking booking = getActiveBooking(seatId);

    if (booking == null || booking.getStatus() != 2) {
        throw new BusinessException("座位不在使用中");
    }

    // 延长使用时间
    Date newEndTime = new Date(
        booking.getEndTime().getTime() + extendMinutes * 60 * 1000
    );
    booking.setEndTime(newEndTime);
    bookingMapper.updateById(booking);

    return true;
}
```

**定时检查任务**:

```java
@Scheduled(fixedRate = 2000) // 每2秒执行
public void checkExpiration() {
    List<SysSeat> seats = list();

    for (SysSeat seat : seats) {
        SeatBooking booking = getActiveBooking(seat.getId());

        if (booking == null) continue;

        // 检查预约超时（15分钟未签到）
        if (booking.getStatus() == 1 && isExpired(booking.getStartTime(), 15)) {
            // 自动释放
            cancelBooking(booking);
        }

        // 检查使用结束
        if (booking.getStatus() == 2 && isExpired(booking.getEndTime())) {
            // 自动签退
            checkOut(seat.getId());
        }
    }
}
```

#### API接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取所有座位 | GET | `/api/seat/list` | 获取座位列表 |
| 预约座位 | POST | `/api/seat/reserve/{id}` | 预约座位 |
| 签到 | POST | `/api/seat/checkin/{id}` | 签到入座 |
| 续约 | POST | `/api/seat/renew/{id}` | 续约座位 |
| 签退 | POST | `/api/seat/checkout/{id}` | 签退座位 |
| 重置所有 | POST | `/api/seat/reset` | 重置所有座位 |
| 模拟数据 | POST | `/api/seat/simulate` | 模拟预约场景 |

#### 智能提醒机制

1. **超时提醒**: 预约15分钟未签到，每10秒提醒一次
2. **自动释放**: 提醒30秒后自动释放座位
3. **使用结束提醒**: 使用时间结束前5分钟提醒
4. **续约提醒**: 使用时间结束提醒续约

#### 时钟显示

```javascript
// 模拟时钟（指针式）
const clockData = ref({
  hour: 0,
  minute: 0,
  second: 0
});

// 实时更新
setInterval(() => {
  const now = new Date();
  clockData.value = {
    hour: now.getHours(),
    minute: now.getMinutes(),
    second: now.getSeconds()
  };
}, 1000);

// 计算指针角度
const hourAngle = computed(() =>
  (clockData.value.hour % 12) * 30 + clockData.value.minute * 0.5
);
const minuteAngle = computed(() => clockData.value.minute * 6);
const secondAngle = computed(() => clockData.value.second * 6);
```

---

### 8. 食堂智能服务

#### 功能概述

3D选座、智能点餐、视觉特效界面，提供沉浸式的食堂服务体验。

#### 技术实现

**前端实现**:
- 主页面: `CanteenManagement.vue` (GSAP动画 + tsParticles粒子效果)
- 选座页面: `CanteenSeating.vue` (Three.js 3D渲染)
- 点餐页面: `CanteenOrdering.vue` (智能点餐)

#### 核心功能

1. **视觉特效**
   - GSAP动画（入场动画、3D卡片倾斜、火花粒子）
   - tsParticles粒子效果（背景粒子、鼠标交互）
   - 数据流背景动画
   - 全息服务指引

2. **3D选座**
   - Three.js 3D场景
   - 交互式座位选择
   - 实时座位状态

3. **智能点餐**
   - 菜品展示
   - 购物车管理
   - 订单提交
   - 多种菜品图片资源

#### GSAP动画效果

```javascript
import gsap from 'gsap';

// 元素入场动画
gsap.from('.service-card', {
  y: 100,
  opacity: 0,
  duration: 1,
  stagger: 0.2,
  ease: 'power3.out'
});

// 3D卡片倾斜效果
gsap.to('.card-container', {
  rotationX: 10,
  rotationY: 10,
  duration: 0.5,
  ease: 'power2.out'
});

// 火花粒子系统
function createSparkParticles(x, y) {
  for (let i = 0; i < 20; i++) {
    const particle = document.createElement('div');
    particle.className = 'spark-particle';
    gsap.set(particle, {
      x: x,
      y: y,
      opacity: 1
    });
    gsap.to(particle, {
      x: x + random(-50, 50),
      y: y + random(-50, 50),
      opacity: 0,
      duration: 1,
      onComplete: () => particle.remove()
    });
  }
}
```

#### tsParticles配置

```javascript
import tsParticles from '@tsparticles/vue3';

const particlesInit = async (engine) => {
  await engine.load('https://cdn.jsdelivr.net/npm/tsparticles@3.0.0/tsparticles.bundle.min.js');
};

const particlesOptions = {
  background: {
    color: '#0a0e27'
  },
  particles: {
    number: {
      value: 80
    },
    size: {
      value: 3
    },
    move: {
      enable: true,
      speed: 1
    }
  },
  interactivity: {
    events: {
      onHover: {
        enable: true,
        mode: 'grab'
      }
    }
  }
};
```

#### Three.js 3D场景

```javascript
import * as THREE from 'three';

// 创建场景
const scene = new THREE.Scene();

// 创建相机
const camera = new THREE.PerspectiveCamera(
  75,
  window.innerWidth / window.innerHeight,
  0.1,
  1000
);
camera.position.z = 5;

// 创建渲染器
const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(window.innerWidth, window.innerHeight);
document.getElementById('canvas-container').appendChild(renderer.domElement);

// 创建座位
function createSeat(x, y, occupied) {
  const geometry = new THREE.BoxGeometry(1, 1, 1);
  const material = new THREE.MeshPhongMaterial({
    color: occupied ? 0xff6b6b : 0x6bcf63
  });
  const seat = new THREE.Mesh(geometry, material);
  seat.position.set(x, y, 0);
  return seat;
}

// 渲染循环
function animate() {
  requestAnimationFrame(animate);
  renderer.render(scene, camera);
}
animate();
```

#### 菜品图片资源

```
vue-demo/src/assets/images/dishes/
├── rice.png           # 米饭
├── noodles.png        # 面条
├── dumplings.png      # 饺子
├── chicken.png        # 鸡肉
├── beef.png           # 牛肉
├── pork.png           # 猪肉
├── vegetables.png     # 蔬菜
├── soup.png           # 汤
└── ...
```

#### 交互体验

1. **鼠标移动**: 产生火花效果
2. **点击**: 产生炸裂火花效果
3. **卡片悬停**: 3D倾斜效果
4. **页面转场**: 流畅的过渡动画

---

## 📊 数据库设计

### 数据库表结构

#### 1. sys_classroom (教室表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| classroom_name | VARCHAR(50) | 教室名称 |
| building | VARCHAR(50) | 所在建筑 |
| floor | INT | 楼层 |
| capacity | INT | 容量 |
| status | INT | 状态 (0-正常, 1-维护) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 2. visual_stats_log (可视化统计日志表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| classroom_id | BIGINT | 教室ID |
| person_count | INT | 人数 |
| focus_score | DECIMAL(5,2) | 专注度分数 |
| device_status | INT | 设备状态 |
| record_time | DATETIME | 记录时间 |
| create_time | DATETIME | 创建时间 |

#### 3. sys_user (用户表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(100) | 密码 |
| email | VARCHAR(100) | 邮箱 |
| phone | VARCHAR(20) | 手机号 |
| role | INT | 角色 (0-普通用户, 1-管理员) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 4. acc_bill (账单表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| category_id | BIGINT | 分类ID |
| amount | DECIMAL(10,2) | 金额 |
| type | INT | 类型 (0-支出, 1-收入) |
| description | VARCHAR(200) | 描述 |
| transaction_date | DATE | 交易日期 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 5. acc_category (账单分类表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(50) | 分类名称 |
| icon | VARCHAR(50) | 图标 |
| color | VARCHAR(20) | 颜色 |
| type | INT | 类型 (0-支出, 1-收入) |
| sort_order | INT | 排序 |
| create_time | DATETIME | 创建时间 |

#### 6. acc_budget (预算表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| category_id | BIGINT | 分类ID |
| amount | DECIMAL(10,2) | 预算金额 |
| period | VARCHAR(20) | 周期 (month/quarter/year) |
| start_date | DATE | 开始日期 |
| end_date | DATE | 结束日期 |
| status | INT | 状态 (0-正常, 1-已关闭) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 7. acc_financial_goal (财务目标表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| goal_name | VARCHAR(100) | 目标名称 |
| target_amount | DECIMAL(10,2) | 目标金额 |
| current_amount | DECIMAL(10,2) | 当前金额 |
| deadline | DATE | 截止日期 |
| status | INT | 状态 (0-进行中, 1-已完成, 2-已逾期) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 8. lf_lost_item (失物招领表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| classroom_id | VARCHAR(20) | 教室ID |
| item_type | VARCHAR(50) | 物品类型 |
| quantity | INT | 数量 |
| description | VARCHAR(200) | 描述 |
| image_url | VARCHAR(500) | 图片URL |
| status | INT | 状态 (0-未认领, 1-已认领) |
| found_time | DATETIME | 发现时间 |
| claim_time | DATETIME | 认领时间 |
| claim_user_id | BIGINT | 认领用户ID |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 9. sys_seat (座位表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| seat_no | VARCHAR(20) | 座位编号 |
| area | VARCHAR(50) | 区域 |
| floor | INT | 楼层 |
| status | INT | 状态 (0-空闲, 1-已预约, 2-使用中) |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### 10. seat_booking (座位预约表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| seat_id | BIGINT | 座位ID |
| user_id | BIGINT | 用户ID |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| duration_minutes | INT | 时长（分钟） |
| status | INT | 状态 (1-已预约, 2-使用中, 3-已取消) |
| check_in_time | DATETIME | 签到时间 |
| check_out_time | DATETIME | 签退时间 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 数据库初始化

数据库初始化脚本位于 `smart-campus-backend/sql/` 目录：

1. **schema.sql**: 建库/表
2. **accounting_schema.sql**: 记账预置数据
3. **lost_found_table.sql**: 失物招领表结构

项目启动时自动按顺序执行这些脚本。

---

## 🔌 API接口文档

### 统一返回格式

所有API接口统一使用 `Result<T>` 对象返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 说明 |
|------|------|
| code | 状态码 (200-成功, 400-请求错误, 500-服务器错误) |
| message | 消息 |
| data | 数据 |

### 用户认证API

#### 注册新用户

```http
POST /api/accounting/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "email": "test@example.com",
  "phone": "13800138000"
}
```

#### 用户登录

```http
POST /api/accounting/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

#### 获取当前用户

```http
GET /api/accounting/auth/current
Authorization: Bearer {token}
```

#### 用户登出

```http
POST /api/accounting/auth/logout
Authorization: Bearer {token}
```

### 教室管理API

#### 获取所有教室

```http
GET /api/classroom/list
```

#### 获取教室详情

```http
GET /api/classroom/{id}
```

### 统计分析API

#### 获取实时统计

```http
GET /api/stats/realtime?classroomId={classroomId}
```

#### 获取历史趋势

```http
GET /api/stats/trend?classroomId={classroomId}&hours=24
```

#### 模拟数据

```http
POST /api/stats/simulate
```

### 账单管理API

#### 获取账单列表

```http
GET /api/accounting/bills?page=1&size=10&type=0&categoryId=1
```

#### 创建账单

```http
POST /api/accounting/bills
Content-Type: application/json

{
  "categoryId": 1,
  "amount": 100.00,
  "type": 0,
  "description": "午餐",
  "transactionDate": "2026-01-25"
}
```

#### 更新账单

```http
PUT /api/accounting/bills/{id}
Content-Type: application/json

{
  "amount": 120.00,
  "description": "午餐+饮料"
}
```

#### 删除账单

```http
DELETE /api/accounting/bills/{id}
```

#### 导出Excel

```http
GET /api/accounting/bills/export
```

### 失物招领API

#### 获取失物列表

```http
GET /api/lost-found/list?page=1&size=10&classroomId=A101&status=0
```

#### 自动创建失物

```http
POST /api/lost-found/detect
Content-Type: application/json

{
  "classroomId": "A101",
  "itemType": "water_bottle",
  "quantity": 1,
  "imageUrl": "http://example.com/image.jpg"
}
```

#### 认领失物

```http
POST /api/lost-found/claim/{id}
Content-Type: application/json

{
  "quantity": 1
}
```

### 座位预约API

#### 获取所有座位

```http
GET /api/seat/list
```

#### 预约座位

```http
POST /api/seat/reserve/{id}
Content-Type: application/json

{
  "startTime": "2026-01-25 14:00:00",
  "durationMinutes": 60
}
```

#### 签到

```http
POST /api/seat/checkin/{id}
```

#### 续约

```http
POST /api/seat/renew/{id}
Content-Type: application/json

{
  "extendMinutes": 30
}
```

#### 签退

```http
POST /api/seat/checkout/{id}
```

#### 重置所有座位

```http
POST /api/seat/reset
```

#### 模拟数据

```http
POST /api/seat/simulate
```

### AI视觉API

#### 开始检测

```http
POST http://localhost:5000/api/start
Content-Type: application/json

{
  "model": "yolo11n.pt",
  "source": 0,
  "conf_thres": 0.25,
  "iou_thres": 0.45,
  "imgsz": 640
}
```

#### 停止检测

```http
POST http://localhost:5000/api/stop
```

#### 获取检测结果

```http
GET http://localhost:5000/api/results
```

#### 上传文件

```http
POST http://localhost:5000/api/upload
Content-Type: multipart/form-data

file: {file}
```

#### AI对话

```http
POST http://localhost:5000/api/chat
Content-Type: application/json

{
  "message": "你好"
}
```

---

## 🎨 UI/UX设计规范

### 颜色系统

#### 主色调

| 颜色 | 用途 | Hex |
|------|------|-----|
| 蓝色 | 主色调 | #409EFF |
| 浅蓝 | 辅助色 | #00c6ff |
| 深蓝 | 强调色 | #0072ff |

#### 辅助色

| 颜色 | 用途 | Hex |
|------|------|-----|
| 紫色 | 数据模拟 | #9a65fd |
| 深紫 | 辅助色 | #7b4af0 |
| 橙红 | 呼吸灯 | #ff6b35 |

#### 功能色

| 颜色 | 用途 | Hex |
|------|------|-----|
| 成功 | 成功提示 | #67C23A |
| 警告 | 警告提示 | #E6A23C |
| 危险 | 危险/删除 | #F56C6C |
| 信息 | 信息提示 | #909399 |

### 图标系统

#### 图标库

使用 **Iconify** 系统，集成多个图标库：
- **Tabler Icons**: 主要图标库
- **Material Design Icons**: 备选图标库
- **Phosphor Icons**: 辅助图标库

#### 使用方式

```vue
<template>
  <i-tabler-eye />           <!-- 视觉识别 -->
  <i-tabler-chart-line />    <!-- 数据分析 -->
  <i-tabler-settings />      <!-- 设置 -->
  <i-tabler-chart-bar />     <!-- 统计 -->
  <i-mdi:user />             <!-- 用户 -->
  <i-mdi:home />             <!-- 首页 -->
  <i-ph:calendar />          <!-- 日历 -->
</template>
```

#### 推荐图标

| 功能 | 图标 | 说明 |
|------|------|------|
| 视觉识别 | `i-tabler-eye` | AI视觉入口 |
| 数据模拟 | `i-tabler-chart-line` | 数据模拟器 |
| 设置 | `i-tabler-settings` | 设置页面 |
| 分析 | `i-tabler-chart-bar` | 统计分析 |
| 登录 | `i-tabler-login` | 登录页面 |
| 注册 | `i-tabler-user-plus` | 注册页面 |
| 退出 | `i-tabler-logout` | 退出登录 |

### 动画规范

#### 呼吸灯效果

```css
@keyframes breathe {
  0%, 100% {
    opacity: 1;
    box-shadow: 0 0 10px rgba(255, 107, 53, 0.5);
  }
  50% {
    opacity: 0.7;
    box-shadow: 0 0 20px rgba(255, 107, 53, 0.8);
  }
}

.breathing {
  animation: breathe 2s ease-in-out infinite;
}
```

#### 脉冲动画

```css
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 198, 255, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(0, 198, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 198, 255, 0);
  }
}

.pulsing {
  animation: pulse 2s infinite;
}
```

#### 悬停效果

```css
.hover-effect {
  transition: all 0.3s ease;
}

.hover-effect:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
```

### 布局原则

#### 悬浮元素布局

| 元素 | 位置 | 说明 |
|------|------|------|
| YOLO最小化窗口 | `bottom: 40px; left: 50%; transform: translateX(-50%)` | 底部居中 |
| AI视觉按钮 | `bottom: 130px; right: 40px` | 右下角 |
| 数据模拟按钮 | `bottom: 40px; right: 40px` | 右下角 |

#### 间距系统

| 间距 | 大小 | 用途 |
|------|------|------|
| xs | 8px | 紧凑元素 |
| sm | 16px | 小元素 |
| md | 24px | 中等元素 |
| lg | 32px | 大元素 |
| xl | 48px | 超大元素 |

#### 层级管理

| 层级 | z-index | 用途 |
|------|---------|------|
| 正常 | 0-100 | 普通内容 |
| 悬浮 | 100-1000 | 悬浮按钮、卡片 |
| 模态 | 1000-2000 | 弹窗、对话框 |
| 顶层 | 2000+ | YOLO窗口、消息提示 |

### 响应式设计

#### 断点

| 断点 | 屏幕宽度 | 设备 |
|------|---------|------|
| xs | < 576px | 手机 |
| sm | ≥ 576px | 大屏手机 |
| md | ≥ 768px | 平板 |
| lg | ≥ 992px | 小屏笔记本 |
| xl | ≥ 1200px | 桌面显示器 |

#### 适配策略

```css
/* 移动端 */
@media (max-width: 576px) {
  .container {
    padding: 0 12px;
  }
}

/* 平板 */
@media (min-width: 576px) and (max-width: 768px) {
  .container {
    padding: 0 24px;
  }
}

/* 桌面端 */
@media (min-width: 992px) {
  .container {
    padding: 0 48px;
  }
}
```

---

## 📝 开发规范

### 前端开发规范

#### 命名规范

**文件命名**:
- 组件文件: PascalCase, 例如 `DashboardView.vue`
- 工具文件: camelCase, 例如 `request.js`
- 样式文件: kebab-case, 例如 `main.css`

**变量命名**:
- 组件内变量: camelCase, 例如 `userName`, `isLoading`
- 常量: UPPER_SNAKE_CASE, 例如 `API_BASE_URL`, `MAX_RETRY`

**组件命名**:
- 组件名使用 PascalCase
- 多单词组件名使用完整单词, 例如 `UserProfile` 而非 `UserProf`

#### 代码组织

**Vue组件结构**:

```vue
<template>
  <!-- 模板 -->
</template>

<script setup>
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'

// 2. Props和Emits
const props = defineProps({
  title: String
})

const emit = defineEmits(['update'])

// 3. 响应式数据
const count = ref(0)
const list = ref([])

// 4. 计算属性
const doubled = computed(() => count.value * 2)

// 5. 方法
const increment = () => {
  count.value++
}

// 6. 生命周期
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
/* 样式 */
</style>
```

#### 样式规范

**优先级**:
1. 使用 Element Plus 内置样式
2. 使用全局样式变量
3. 使用组件内 `scoped` 样式
4. 使用 CSS Modules

**命名空间**:
- 使用 BEM 命名规范
- 例如: `.block__element--modifier`

```css
.card {
  /* block */
}

.card__header {
  /* element */
}

.card__header--active {
  /* modifier */
}
```

#### 注释规范

**文件注释**:
```vue
<!--
  @file ComponentName.vue
  @description 组件描述
  @author 作者名
  @date 2026-01-25
-->
```

**代码注释**:
```javascript
// 单行注释

/**
 * 多行注释
 * @param {String} name 参数描述
 * @returns {Boolean} 返回值描述
 */
function validate(name) {
  return name.length > 0
}
```

### 后端开发规范

#### 命名规范

**包命名**:
- 全部小写
- 使用反向域名, 例如 `com.smartcampus.controller`

**类命名**:
- 类名: PascalCase, 例如 `UserController`
- 接口名: PascalCase + I前缀, 例如 `IUserService`
- 实现类: PascalCase + Impl后缀, 例如 `UserServiceImpl`

**方法命名**:
- 动词开头, 例如 `getUserById`, `createUser`, `updateUser`
- 使用驼峰式命名

**变量命名**:
- 成员变量: camelCase, 例如 `userName`, `userAge`
- 常量: UPPER_SNAKE_CASE, 例如 `MAX_RETRY_COUNT`, `API_BASE_URL`

#### 代码组织

**Controller层**:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> list(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return Result.success(userService.list(page, size));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}
```

**Service层**:

```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<User> list(int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        return userMapper.selectPage(pageParam, null).getRecords();
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
```

**Entity层**:

```java
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;
}
```

#### 注解规范

**Lombok注解**:
```java
@Data           // Getter/Setter/toString/equals/hashCode
@NoArgsConstructor // 无参构造
@AllArgsConstructor // 全参构造
@Builder        // 建造者模式
@Slf4j          // 日志
```

**MyBatis-Plus注解**:
```java
@TableName("table_name")         // 表名
@TableId(type = IdType.AUTO)     // 主键
@TableField("column_name")       // 字段名
@TableLogic                      // 逻辑删除
```

**Spring注解**:
```java
@RestController                 // REST控制器
@RequestMapping("/api")          // 请求映射
@GetMapping, @PostMapping,      // HTTP方法
@PutMapping, @DeleteMapping
@Autowired                       // 依赖注入
@Service                         // 服务层
@Component                       // 组件
```

#### 异常处理

**统一异常处理**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常: {}", e.getMessage());
        return Result.error(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error("系统错误，请联系管理员");
    }
}
```

### 数据库操作规范

#### SQL规范

**表名**:
- 使用小写字母
- 使用下划线分隔, 例如 `sys_user`, `acc_bill`

**字段名**:
- 使用小写字母
- 使用下划线分隔, 例如 `user_name`, `create_time`

**索引命名**:
- 普通索引: `idx_字段名`, 例如 `idx_user_name`
- 唯一索引: `uk_字段名`, 例如 `uk_username`
- 联合索引: `idx_字段1_字段2`, 例如 `idx_user_name_age`

#### 查询优化

1. **避免SELECT ***
```sql
-- 不推荐
SELECT * FROM sys_user;

-- 推荐
SELECT id, username, email FROM sys_user;
```

2. **使用索引**
```sql
-- 确保查询条件使用索引
SELECT * FROM acc_bill WHERE user_id = 1 AND create_time > '2026-01-01';
```

3. **分页查询**
```sql
-- 使用MyBatis-Plus分页插件
Page<AccBill> page = new Page<>(1, 10);
userMapper.selectPage(page, null);
```

4. **批量操作**
```sql
-- 批量插入
INSERT INTO acc_bill (user_id, amount) VALUES
(1, 100.00),
(2, 200.00),
(3, 300.00);
```

---

## 🚢 部署指南

### 后端部署

#### 1. 打包应用

```bash
cd E:/LEAR-CODE/smart-campus-backend

# 使用Maven打包
mvn clean package -DskipTests

# 打包产物位于 target/smart-campus-backend-1.0.0.jar
```

#### 2. 配置生产环境

修改 `application-prod.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://生产数据库地址:3306/smart_campus?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    druid:
      initial-size: 10
      min-idle: 10
      max-active: 50

# 日志配置
logging:
  level:
    com.smartcampus: INFO
  file:
    name: /var/log/smart-campus/application.log
```

#### 3. 启动应用

```bash
# 使用生产环境配置启动
java -jar target/smart-campus-backend-1.0.0.jar --spring.profiles.active=prod

# 或使用nohup后台运行
nohup java -jar target/smart-campus-backend-1.0.0.jar --spring.profiles.active=prod > /dev/null 2>&1 &
```

#### 4. 使用Systemd管理服务

创建 `/etc/systemd/system/smart-campus.service`:

```ini
[Unit]
Description=Smart Campus Backend
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/smart-campus-backend
ExecStart=/usr/bin/java -jar smart-campus-backend-1.0.0.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
# 重新加载systemd
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start smart-campus

# 开机自启
sudo systemctl enable smart-campus

# 查看状态
sudo systemctl status smart-campus
```

### 前端部署

#### 1. 构建生产版本

```bash
cd E:/LEAR-CODE/vue-demo

# 安装依赖
npm install

# 构建生产版本
npm run build

# 构建产物位于 dist/ 目录
```

#### 2. 配置Nginx

创建 `/etc/nginx/sites-available/smart-campus`:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/smart-campus/dist;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用站点:

```bash
# 创建软链接
sudo ln -s /etc/nginx/sites-available/smart-campus /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启Nginx
sudo systemctl restart nginx
```

#### 3. 使用PM2管理Node服务（如果需要）

```bash
# 安装PM2
npm install -g pm2

# 启动服务
pm2 start npm --name "smart-campus-frontend" -- run dev

# 查看状态
pm2 status

# 查看日志
pm2 logs smart-campus-frontend

# 设置开机自启
pm2 startup
pm2 save
```

### AI视觉子系统部署

#### 1. 配置生产环境

修改 `app.py`:

```python
# 生产环境配置
app.run(
    host='0.0.0.0',
    port=5000,
    debug=False
)
```

#### 2. 使用Gunicorn部署

```bash
# 安装Gunicorn
pip install gunicorn

# 启动服务
gunicorn -w 4 -b 0.0.0.0:5000 app:app
```

#### 3. 使用Systemd管理服务

创建 `/etc/systemd/system/ai-vision.service`:

```ini
[Unit]
Description=AI Vision Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/var/www/my_yolo_web
Environment="PATH=/var/www/my_yolo_web/venv/bin"
ExecStart=/var/www/my_yolo_web/venv/bin/gunicorn -w 4 -b 0.0.0.0:5000 app:app
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
sudo systemctl daemon-reload
sudo systemctl start ai-vision
sudo systemctl enable ai-vision
sudo systemctl status ai-vision
```

### Docker部署

#### 1. 创建后端Dockerfile

```dockerfile
FROM openjdk:8-jre-alpine

WORKDIR /app

COPY target/smart-campus-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 2. 创建前端Dockerfile

```dockerfile
FROM node:20-alpine as builder

WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

#### 3. 创建AI子系统Dockerfile

```dockerfile
FROM python:3.8-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "app:app"]
```

#### 4. 创建docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: smart_campus
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql

  backend:
    build: ./smart-campus-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/smart_campus
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=123456

  frontend:
    build: ./vue-demo
    ports:
      - "80:80"
    depends_on:
      - backend

  ai-vision:
    build: ./my_yolo_web
    ports:
      - "5000:5000"

volumes:
  mysql-data:
```

#### 5. 启动所有服务

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

---

## 🔧 常见问题与解决方案

### 后端常见问题

#### 1. 数据库连接失败

**问题**:
```
Could not create connection to database server
```

**解决方案**:
1. 检查MySQL服务是否启动
2. 检查数据库地址、端口、用户名、密码是否正确
3. 检查防火墙是否允许3306端口

#### 2. 端口被占用

**问题**:
```
Port 8080 is already in use
```

**解决方案**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F

# Linux/Mac
lsof -i :8080
kill -9 <进程ID>
```

#### 3. 内存溢出

**问题**:
```
java.lang.OutOfMemoryError: Java heap space
```

**解决方案**:
```bash
# 增加JVM堆内存
java -Xms512m -Xmx2048m -jar app.jar
```

### 前端常见问题

#### 1. 跨域问题

**问题**:
```
Access to XMLHttpRequest has been blocked by CORS policy
```

**解决方案**:
在后端配置CORS:
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*");
    }
}
```

#### 2. 路由404

**问题**:
刷新页面后出现404

**解决方案**:
配置Nginx:
```nginx
location / {
    try_files $uri $uri/ /index.html;
}
```

#### 3. 依赖安装失败

**问题**:
```
npm ERR! code ELIFECYCLE
```

**解决方案**:
```bash
# 清除缓存
npm cache clean --force

# 删除node_modules重新安装
rm -rf node_modules package-lock.json
npm install

# 使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

### AI子系统常见问题

#### 1. 模型加载失败

**问题**:
```
FileNotFoundError: [Errno 2] No such file or directory: 'models/yolo11n.pt'
```

**解决方案**:
1. 检查模型文件是否存在于 `models/` 目录
2. 检查模型文件路径配置
3. 下载正确的模型文件

#### 2. 摄像头无法打开

**问题**:
```
cv2.VideoCapture(0).read() 返回 False
```

**解决方案**:
1. 检查摄像头是否被占用
2. 检查摄像头驱动是否安装
3. 尝试其他摄像头索引 (0, 1, 2...)

#### 3. 内存不足

**问题**:
```
RuntimeError: CUDA out of memory
```

**解决方案**:
1. 使用较小的模型 (yolo11n.pt)
2. 减小输入图片尺寸
3. 批量处理时减小batch size

### 部署常见问题

#### 1. 端口被占用

**问题**:
部署时端口冲突

**解决方案**:
修改配置文件中的端口号:
```yaml
# application.yml
server:
  port: 8081  # 改为其他端口
```

#### 2. 权限不足

**问题**:
```
Permission denied
```

**解决方案**:
```bash
# 修改文件权限
chmod +x script.sh

# 修改目录所有者
chown -R www-data:www-data /var/www/smart-campus
```

#### 3. 服务无法启动

**问题**:
Systemd服务启动失败

**解决方案**:
```bash
# 查看服务日志
sudo journalctl -u smart-campus -n 100

# 检查配置文件
sudo nginx -t

# 重启服务
sudo systemctl restart smart-campus
```

---

---

## 🔄 版本控制与 .gitignore 说明

### .gitignore 规则说明

本项目有 **两个** `.gitignore` 文件，分别管理不同子系统的忽略规则：

#### 根目录 `.gitignore`（管理整个项目）

| 类别 | 规则 | 对应实际文件/目录 | 是否正确 |
|------|------|------------------|---------|
| 系统垃圾 | `nul`, `**/nul` | Windows NUL 幽灵文件（可能存在） | ✅ |
| 系统垃圾 | `.DS_Store`, `Thumbs.db` | macOS/Windows 系统文件 | ✅ |
| IDE 配置 | `.idea/`, `*.iws`, `*.iml`, `**/out/` | JetBrains IDE 本地配置（可能存在） | ✅ |
| IDE 配置 | `.vscode/`, `*.code-workspace` | VS Code 本地配置（可能存在） | ✅ |
| IDE 配置 | `.trae/` | Trae IDE 本地配置（可能存在） | ✅ |
| Java 构建 | `**/target/`, `**/*.class`, `**/*.jar`, `*.war` | Maven/Gradle 编译产物目录 | ✅ |
| 前端构建 | `**/node_modules/` | npm 依赖目录 | ✅ |
| 前端构建 | `**/dist/` | Vite/Webpack 构建产物 | ✅ |
| Python | `**/__pycache__/`, `*.pyc` | Python 字节码缓存 | ✅ |
| Python | `.venv/`, `venv/`, `env/` | Python 虚拟环境 | ✅ |
| 敏感配置 | `.env` | Flask 环境变量文件（含真实密钥） | ✅ |
| YOLO 模型 | `*.pt`, `*.onnx` | YOLO 模型文件（需自行下载） | ✅ |
| YOLO 模型 | `**/runs/` | YOLO 推理输出目录 | ✅ |

#### `my_yolo_web/.gitignore`（管理 Flask 子系统）

| 类别 | 规则 | 对应实际文件/目录 | 是否正确 |
|------|------|------------------|---------|
| IDE 配置 | `.idea/` | JetBrains IDE 配置（可能存在） | ✅ |
| IDE 配置 | `out/` | 编译输出目录（可能存在） | ✅ |
| Python | `__pycache__/`, `*.pyc` | Python 字节码缓存 | ✅ |
| YOLO | `runs/` | YOLO 推理结果目录 | ✅ |
| 敏感文件 | `ai_api.py` | 含明文 API Key 的旧版文件 | ✅（已清空 Key，但仍保留忽略） |

### 环境变量管理策略

**安全策略**：
- `.env` 文件包含真实 API Key / Token / Secret，**绝不提交到 Git**
- 代码中不硬编码任何密钥
- README 中只写变量名、用途和配置说明，不写真实密钥值
- 日志和提交说明中不包含敏感信息

**配置模板**：
- 项目提供了 `my_yolo_web/.env.example` 作为配置模板
- 新开发者克隆项目后，执行以下操作即可：

```bash
# 复制示例配置
cd my_yolo_web
cp .env.example .env

# 编辑 .env 填入真实密钥
# ARK_API_KEY=你的真实KEY
# VOICE_REALTIME_TOKEN=你的真实TOKEN
# 等等...
```

### 明确需要提交的文件类型

| 类型 | 是否提交 | 示例 |
|------|---------|------|
| 源代码 (.java/.vue/.py/.js/.css) | ✅ 提交 | 全部 |
| 配置文件模板 (.env.example) | ✅ 提交 | `my_yolo_web/.env.example` |
| Live2D 模型资源 (.moc3/.json/.png) | ✅ 提交 | `vue-demo/public/live2d/` |
| 前端静态资源 (图片/字体) | ✅ 提交 | `vue-demo/src/assets/`, `vue-demo/public/images/` |
| YOLO 示例文件 (图片/视频) | ✅ 提交 | `my_yolo_web/example/` |
| 数据库初始化脚本 (.sql) | ✅ 提交 | `smart-campus-backend/sql/` |
| 项目文档 (.md) | ✅ 提交 | `README.md` 等 |
| 敏感配置 (.env) | ❌ 不提交 | `my_yolo_web/.env` |
| 依赖目录 (node_modules) | ❌ 不提交 | `vue-demo/node_modules/` |
| 构建产物 (target/dist) | ❌ 不提交 | `**/target/`, `**/dist/` |
| YOLO 模型文件 (.pt) | ❌ 不提交 | 需从 ultralytics 官方下载 |
| 检测结果 (runs) | ❌ 不提交 | `my_yolo_web/runs/` |
| IDE 配置 (.idea/.vscode/.trae) | ❌ 不提交 | 本地 IDE 设置 |

---

## 📞 技术支持

### 联系方式

- **项目地址**: [GitHub Repository]
- **问题反馈**: [Issue Tracker]
- **技术交流**: [Discussion Board]

### 相关资源

- **Vue 3文档**: https://vuejs.org/
- **Spring Boot文档**: https://spring.io/projects/spring-boot
- **YOLOv11文档**: https://github.com/ultralytics/ultralytics
- **Element Plus文档**: https://element-plus.org/

---

## 📝 更新日志

> **版本号格式说明**：主版本.次版本.修订号
> 版本变更记录按以下分类记录，后续版本请参照此格式：
> - **新增 (Added)**：新增的功能模块、页面、组件、接口
> - **修改 (Changed)**：已有功能的改造、替换、升级
> - **修复 (Fixed)**：BUG 修复
> - **删除 (Removed)**：已移除的功能、文件、依赖
> - **优化 (Optimized)**：性能优化、代码重构、配置调整
> - **安全 (Security)**：安全相关修复或加固
> - **文档 (Documentation)**：README、注释等文档变更

---

### v2.0.0 (2026-05-03)

> **主题**：数字人 Live2D + 语音交互集成（Golden Release）

#### 新增 (Added)

| 分类 | 文件/模块 | 说明 |
|------|-----------|------|
| Flask 后端 | `my_yolo_web/app.py`（重写） | 融合原 YOLO 视觉检测能力 + my_huahuo 的 ARK AI 大模型调用、流式聊天、实时语音对话、ASR 语音识别、TTS 语音合成等数字人能力 |
| Flask 后端 | `my_yolo_web/backend/services/*` | 迁入火山方舟实时语音桥接 (`volc_realtime_bridge.py`)、TTS 语音合成服务 (`tts_service.py`)、LangChain/ARK 大模型服务 (`langchain_service.py`) |
| Flask 后端 | `my_yolo_web/.env` | 新建环境变量配置文件，统一管理 ARK_API_KEY、ARK_MODEL、VOICE_REALTIME_*、DOUBAO_ASR_* 等外部 API 密钥和参数 |
| 前端组件 | `vue-demo/src/components/DigitalHumanAssistant.vue` | 全局数字人助手面板：Live2D 加载、文本/语音对话、流式AI回复、浏览器语音播报、暂停/停止、静音、新会话、展开/收起 |
| 前端 API | `vue-demo/src/api/assistant.js` | 数字人 Flask API 封装层：健康检查、配置读取、流式聊天、ASR 语音上传、对话消息管理 |
| 前端资源 | `vue-demo/public/live2d/` | 火花 Live2D 数字人模型资源（贴图、表情、动作） |
| 前端资源 | `vue-demo/public/live2d-widget-dist/` | Live2D Widget 渲染脚本和样式文件 |
| Flask 接口 | `/api/health` | AI/语音/YOLO 综合健康状态 |
| Flask 接口 | `/api/chat/stream` | 流式 AI 对话 SSE 端点（前端核心依赖） |
| Flask 接口 | `/api/voice/config` | 语音配置状态查询 |
| Flask 接口 | `/api/voice/chat` | 实时语音文本对话入口 |
| Flask 接口 | `/api/asr` | 音频上传语音识别（支持 doubao / faster-whisper 双提供方） |
| Flask 接口 | `/api/tts/synthesize`、`/api/tts/audio/<filename>` | 服务端 TTS 语音合成和音频下载 |
| Flask 接口 | `/api/debug/env` | 调试用：查看环境变量加载状态 |

#### 修改 (Changed)

| 分类 | 文件 | 说明 |
|------|------|------|
| 前端入口 | `vue-demo/src/App.vue` | 集成全局数字人助手 `<DigitalHumanAssistant>`，在路由视图外全局悬浮挂载 |
| 前端代理 | `vue-demo/vite.config.js` | 新增 `/flask-api` 代理 -> Flask `localhost:5000`，保持 `/api` 代理 Spring Boot `localhost:8080` |
| 前端路由 | `vue-demo/src/router/index.js` | 配合数字人助手路由逻辑 |
| 前端页面 | `vue-demo/src/views/accounting/AiChat.vue` | 改造“AI 智能助手”页面，消息发送通过自定义事件转发给全局数字人助手统一处理 |
| Spring Boot | `AccAiServiceImpl.java` | 财务 AI 对话逻辑迁移：不再直连火山方舟，改为注入财务上下文后转发 Flask `/api/chat/stream` |
| Spring Boot | `AccAiController.java` | 新增 `/api/accounting/ai/chat` 流式端点，支持 `text/event-stream` 响应 |
| Spring Boot | `application.yml` | 新增 Flask AI 服务地址配置 (`ai.flask.*`) |
| Flask 后端 | `requirements.txt` | 新增 volcengine-python-sdk、langchain、faster-whisper、opencc、websockets 等依赖 |

#### 修复 (Fixed)

| 分类 | 说明 |
|------|------|
| Flask 后端 | 修复原 `app.py` 中 `ai_api.py` 硬编码 ARK_API_KEY 的安全问题，改为通过 `.env` 统一加载 |
| Flask 后端 | 修复原 `app.py` 缺少会话管理、流式聊天和不支持多场景对话的问题 |

#### 删除 (Removed)

| 分类 | 文件/模块 | 说明 |
|------|-----------|------|
| Flask 后端 | `my_yolo_web/ai_api.py`（旧版调用逻辑） | 不再被新 `app.py` 导入，AI 调用统一使用 ARK SDK（`volcenginesdkarkruntime`） |
| 不迁移 | my_huahuo OOA&D 信息展示页 | 不迁移 |
| 不迁移 | my_huahuo 各模块自动讲解功能 | 不迁移 |

#### 优化 (Optimized)

| 分类 | 说明 |
|------|------|
| Flask 后端 | 会话管理：6 小时 TTL，每会话最多保留 12 条历史消息 |
| Flask 后端 | 流式输出：后台线程 + Queue + `stream_with_context`，支持长文本分块 |
| Flask 后端 | ASR 双提供方：优先 doubao（WebSocket），失败自动回退 faster-whisper（本地 CPU） |
| 前端 | 浏览器检测：Chrome/Edge 使用浏览器原生 Web Speech API（零延迟），Firefox 走 Flask `/api/asr` |
| 前端 | 语音播报：使用浏览器 `speechSynthesis`，无需服务端 TTS（降低延迟） |

#### 安全 (Security)

| 分类 | 说明 |
|------|------|
| 全局 | 所有 API Key / Token 统一从 `.env` 加载，不在代码、README、提交说明中明文暴露 |
| README | README 只写变量名、用途和配置说明，不写真实密钥值 |

#### 修复 (Fixed - 2026-05-03 补充)

| 分类 | 说明 |
|------|------|
| .gitignore | 修复 *.trae/ 语法错误为 .trae/，移除对 my_yolo_web/example/ 的错误忽略（示例文件为 YOLO 功能所需），清理无效忽略项 |
| 环境配置 | 修复 .env 中变量名不一致问题：VOLCANO_* → VOICE_REALTIME_*，与 olc_realtime_bridge.py / 	ts_service.py 读取的变量名匹配 |
| 配置管理 | 新建 my_yolo_web/.env.example 模板文件，解决 .env 不提交后新开发者无法获知配置项的问题 |

#### 文档 (Documentation - 2026-05-03 补充)

| 分类 | 说明 |
|------|------|
| README.md | 新增「版本控制与 .gitignore 说明」章节，详细说明忽略规则、环境变量管理策略、配置模板使用方式、需提交/不提交的文件类型 |
| README.md | 新增「Live2D 表情系统」章节，列出全部 12 种表情，标注「月卡」「水印」为共存表情并说明其叠加渲染特性 |
| README.md | 技术栈章节更新：AI视觉 → AI视觉与数字人，补充 ARK SDK / Live2D / faster-whisper / 火山 TTS 等技术 |
| README.md | 项目结构章节更新：补充 ackend/services/、.env、.env.example 等新增/变更文件 |
| README.md | 项目概述补充数字人 Live2D + 语音交互能力，前后端分离标注为 Vue 3 + Spring Boot + Flask |
| README.md | 验证步骤补充 Live2D 表情和 AI 助手名「火花」确认项 |

| 分类 | 说明 |
|------|------|
| README.md | 文档版本升至 v2.0.0，补充完整的"数字人 Live2D + 语音交互集成说明"章节，含前端/Spring Boot/Flask 三层职责、接口表、变量表、启动顺序、验证步骤 |

#### 安全 (Security - 2026-05-03 第二轮补充)

| 分类 | 说明 |
|------|------|
| 敏感信息 | 清空 `ai_api.py` 中的明文 API Key，文件标注为废弃，防止意外提交到 Git |
| .gitignore | 根目录 `.gitignore` 与实际文件对比：全部 13 条规则均与项目文件对应正确 |
| .gitignore | `my_yolo_web/.gitignore` 与实际文件对比：全部 5 条规则均正确（`ai_api.py` 含历史明文 Key 仍保留忽略） |
| 命名统一 | AI 助手名统一为「火花」：涉及 `DigitalHumanAssistant.vue`、`AccAiController.java`、`app.py`、`.env`、`.env.example`、`README.md` 共 8 处 |
| 个人水印 | 新增个人水印组件 `BrandingFooter.vue`，显示"Powered by DearJIAN"和项目名，嵌入登录页底部和全局右下角浮层 |

#### 文档 (Documentation - 2026-05-03 第二轮补充)

| 分类 | 说明 |
|------|------|
| README.md | 目录（TOC）新增「🤖 数字人 Live2D + 语音交互集成说明」可跳转链接，更新 8 处过时子标题名称（AI视觉 → AI视觉与数字人） |
| README.md | 版本控制章节升级：详细列出根目录 + `my_yolo_web/.gitignore` 共 2 个文件的所有规则，逐一对应实际文件/目录，并标注是否正确 |
| my_yolo_web/README.md | 全面重写：从旧架构（ai_api.py 模板）更新为新架构（ARK SDK + backend/services/），补充全部 AI 数字人接口、`.env.example` 说明 |


---

### v1.0.0 (2026-01-25)

> **主题**：核心功能开发完成

#### 新增 (Added)
- 用户认证系统
- 教室状态实时监控
- 校园空间导航
- 个人记账与财务洞察
- 失物招领功能
- 图书馆座位预约
- 食堂智能服务
- AI视觉识别子系统 (YOLOv11)

#### 技术特性
- 前后端分离架构 (Vue 3 + Spring Boot + Flask)
- AI 视觉集成 (YOLOv11 目标检测)
- 智能数据模拟
- 实时数据同步
- 现代化 UI 设计（玻璃拟态 + GSAP 动画 + 粒子效果）

---

## 📄 许可证

本项目采用 MIT 许可证，详情请参阅 LICENSE 文件。

---

---

## 数字人 Live2D + 语音交互集成说明

### 功能说明

本项目已将 AI 助手入口升级为统一的“数字人 Live2D 形象 + 语音交互”方式。数字人助手名为“火花”，在前端全局悬浮显示，支持文本提问、麦克风输入、AI 回复、浏览器语音播报、停止朗读、静音/取消静音、新会话、展开/收起。

本次只迁移参考项目 `my_huahuo` 中的 Live2D 形象、语音交互、AI 对话调用、ASR/TTS 能力和助手按钮交互。不迁移 `my_huahuo` 的 OOA&D 信息展示页，也不迁移各模块自动讲解功能。

### 主要文件和目录

| 类型 | 文件/目录 | 说明 |
|------|-----------|------|
| 前端组件 | `vue-demo/src/components/DigitalHumanAssistant.vue` | 全局数字人助手、语音输入、朗读、AI 对话面板 |
| 前端 API | `vue-demo/src/api/assistant.js` | Flask 数字人/ASR/AI 流式接口封装 |
| 前端入口 | `vue-demo/src/App.vue` | 全局挂载数字人助手 |
| 前端代理 | `vue-demo/vite.config.js` | `/api` 代理 Spring Boot，`/flask-api` 代理 Flask |
| Live2D 资源 | `vue-demo/public/live2d/` | 火花 Live2D 模型资源 |
| Live2D Widget | `vue-demo/public/live2d-widget-dist/` | Live2D widget 脚本和样式 |
| Flask 入口 | `my_yolo_web/app.py` | YOLO + 数字人 AI/语音 API |
| Flask 服务 | `my_yolo_web/backend/services/` | 火山实时语音、TTS、LangChain/ARK 相关服务 |
| Flask 依赖 | `my_yolo_web/requirements.txt` | Python 依赖 |
| Spring AI 服务 | `smart-campus-backend/src/main/java/com/smartcampus/service/impl/AccAiServiceImpl.java` | 财务 AI 转发到 Flask 统一模型服务 |
| Spring 配置 | `smart-campus-backend/src/main/resources/application.yml` | Spring、Flask AI 服务地址和模型变量占位 |

### Java Spring Boot 后端职责

Spring Boot 后端继续负责核心业务和数据库：

- 用户认证、Session、个人记账用户状态；
- 账单、预算、财务分析、财务洞察；
- 教室、设备、统计分析；
- 失物招领记录、YOLO 检测结果落库；
- 座位预约、食堂等业务接口；
- 财务 AI 的业务上下文注入和“生成账单”等业务指令拦截。

启动方式：

```bash
cd E:/LEAR-CODE/smart-campus-backend
mvn spring-boot:run
```

默认端口：`8080`。

主要接口：

- `/api/accounting/**`：认证、账单、预算、分析、洞察、财务 AI；
- `/api/stats/**`：教室统计；
- `/api/lost-found/**`：失物招领；
- `/api/seat/**`：座位预约；
- `/api/device/**`：设备数据。

Spring Boot 需要的环境变量：

| 变量名 | 用途 |
|--------|------|
| `AI_FLASK_BASE_URL` | Flask 数字人/AI 服务地址，默认 `http://127.0.0.1:5000` |
| `AI_FLASK_CHAT_STREAM_PATH` | Flask 流式聊天路径，默认 `/api/chat/stream` |
| `ARK_API_KEY` | 兼容旧配置，不建议 Spring 直接使用真实密钥 |
| `ARK_MODEL` | 兼容旧配置，模型名 |
| `ARK_CHAT_COMPLETIONS_URL` | 兼容旧配置，ARK chat completions 地址 |

### Python Flask 后端职责

Flask 后端负责 AI 能力和视觉能力：

- YOLO 视觉检测、视频流、模型列表、检测结果、实时统计；
- 统一 AI 大模型调用；
- 数字人通用聊天流式输出；
- Firefox/不支持浏览器原生语音识别时的 `/api/asr`；
- 服务端 TTS 音频合成；
- 火山实时语音配置读取。

启动方式必须使用 `newyolo` 环境：

```bash
cd E:/LEAR-CODE/my_yolo_web
D:/TOOLS/anaconda/envs/newyolo/python.exe -u app.py
```

默认端口：`5000`。

主要接口：

- `/api/health`：AI/语音/YOLO 状态；
- `/api/chat`：普通 AI 对话；
- `/api/chat/stream`：流式 AI 对话；
- `/api/voice/chat`：实时语音对话文本入口；
- `/api/voice/config`：语音配置状态；
- `/api/asr`：上传音频做语音识别；
- `/api/tts/synthesize`、`/api/tts/audio/<filename>`：语音合成和播放；
- `/api/start`、`/api/stop`、`/api/results`、`/api/stats`：YOLO 检测能力。

Flask 需要的环境变量：

| 变量名 | 用途 |
|--------|------|
| `ARK_API_KEY` | 火山方舟 API Key |
| `ARK_BASE_URL` | 火山方舟 API 基础地址 |
| `ARK_MODEL` | 火山方舟模型 ID（默认 `doubao-seed-1-6-251015`） |
| `VOICE_TEMPERATURE` | 文本模型温度（默认 `0.4`） |
| `VOICE_REALTIME_APP_ID` | 实时语音应用 ID |
| `VOICE_REALTIME_APP_KEY` | 实时语音应用密钥 |
| `VOICE_REALTIME_TOKEN` | 实时语音访问令牌，也用于 TTS |
| `VOICE_REALTIME_RESOURCE_ID` | 实时语音 Resource ID |
| `VOICE_REALTIME_UID` | 实时语音用户 ID |
| `VOICE_REALTIME_DIALOG_ADDRESS` | 实时语音 WebSocket 地址 |
| `VOICE_REALTIME_DIALOG_URI` | 实时语音 WebSocket URI |
| `VOICE_REALTIME_SPEAKER` | TTS 发音人 |
| `VOICE_REALTIME_BOT_NAME` | 语音机器人名称 |
| `VOICE_REALTIME_INPUT_MOD` | 实时语音输入模式 |
| `VOICE_REALTIME_SYSTEM_ROLE` | 实时语音系统角色 Prompt |
| `VOICE_REALTIME_SPEAKING_STYLE` | 实时语音说话风格描述 |
| `VOICE_REALTIME_RECV_TIMEOUT` | 实时语音接收超时（秒） |
| `ASR_PROVIDER` | ASR 提供方，`doubao` 或 `faster-whisper` |
| `DOUBAO_ASR_WS_URL` | 豆包 ASR WebSocket 地址 |
| `DOUBAO_ASR_APP_ID` | 豆包 ASR 应用 ID |
| `DOUBAO_ASR_ACCESS_TOKEN` | 豆包 ASR Access Token |
| `DOUBAO_ASR_RESOURCE_ID` | 豆包 ASR Resource ID |
| `DOUBAO_ASR_MODEL_NAME` | 豆包 ASR 模型名 |
| `FLASK_PORT` | Flask 端口，默认 `5000` |
| `FLASK_DEBUG` | Flask debug 开关 |

真实密钥应写入 `my_yolo_web/.env` 或系统环境变量，不要写入 README、前端代码或提交说明。

### 前端连接方式

前端由 Vite 开发服务器统一代理：

- `/api/**` → Spring Boot `http://localhost:8080/api/**`
- `/flask-api/**` → Flask `http://localhost:5000/api/**`

数字人助手链路：

| 能力 | 最终链路 |
|------|----------|
| Live2D 显示 | 前端 `vue-demo/public/live2d` + `live2d-widget-dist` |
| 文本 AI 对话 | 非记账页面：前端 → Flask `/api/chat/stream` |
| 财务 AI 对话 | 前端 → Spring `/api/accounting/ai/chat` → Flask `/api/chat/stream` |
| 财务业务指令 | 前端 → Spring 拦截执行，例如生成测试账单 |
| 语音输入 | Chrome/Edge 优先浏览器 Web Speech；其他浏览器走 Flask `/api/asr` |
| 语音播报 | 前端浏览器 `speechSynthesis`；服务端 TTS 可走 Flask `/api/tts/synthesize` |
| YOLO 视觉 | 前端 iframe/接口 → Flask YOLO 接口 |
| 业务数据 | 前端 → Spring Boot → MySQL |

### 本地开发完整启动顺序

1. 启动 MySQL，并确认 `smart_campus` 数据库可用。
2. 启动 Spring Boot：

```bash
cd E:/LEAR-CODE/smart-campus-backend
mvn spring-boot:run
```

3. 启动 Flask，必须使用 `newyolo`：

```bash
cd E:/LEAR-CODE/my_yolo_web
D:/TOOLS/anaconda/envs/newyolo/python.exe -u app.py
```

4. 启动前端：

```bash
cd E:/LEAR-CODE/vue-demo
npm run dev
```

5. 访问 `http://localhost:5173`。

### 如何使用和验证

1. 打开任意主页面，右下角会出现“数字人助手”按钮和 Live2D 火花形象（AI 助手名为“火花”）。
2. 点击“数字人助手”展开面板。
3. 输入文本并发送，验证 AI 回复是否流式出现。
4. 点击麦克风按钮，说话后停止，验证语音识别结果是否自动发送。
5. AI 回复后，验证浏览器语音播报是否开始。
6. 点击“停止”验证朗读中断。
7. 点击“静音/播报”验证自动朗读开关。
8. 进入“个人记账 → AI 智能助手”，点击“数字人语音助手”或快捷问题，验证财务 AI 仍能读取预算、风险和账单上下文。
9. 进入“教室状态监控”或“失物招领”，打开 AI 视觉窗口，验证 YOLO Flask 页面仍能显示。
10. 检查浏览器控制台没有 Live2D、语音、代理或跨域错误。

### Live2D 表情系统

Live2D 火花模型支持 **12 种表情**，包括：

| 表情名称 | 类型 | 说明 |
|---------|------|------|
| 01 黑脸 | 独立 | 普通表情 |
| 02 脸红爱心 | 独立 | 普通表情 |
| 03 生气 | 独立 | 普通表情 |
| 04 晕 | 独立 | 普通表情 |
| 05 ＞＜ | 独立 | 普通表情 |
| 06 0.0 | 独立 | 普通表情 |
| 07 星星眼 | 独立 | 普通表情 |
| 08 流泪 | 独立 | 普通表情 |
| 10 捧心 | 独立 | 普通表情 |
| 11 要饭 | 独立 | 普通表情 |
| **月卡** | **共存** | 可与任意其他表情叠加显示 |
| **水印** | **共存** | 可与任意其他表情叠加显示 |

> **共存表情说明**：“月卡”和“水印”是可以与其他表情**同时存在**的特殊表情。例如：可以同时显示“07 星星眼” + “月卡” + “水印”，三个表情同时叠加在模型上。这是 Live2D Cubism SDK 原生支持的多表情混合渲染能力。

### 验证命令

```bash
# Flask 语法检查
cd E:/LEAR-CODE/my_yolo_web
D:/TOOLS/anaconda/envs/newyolo/python.exe -m py_compile app.py ai_api.py backend/services/langchain_service.py backend/services/tts_service.py backend/services/volc_realtime_bridge.py backend/services/volc_realtime_protocol.py

# Spring Boot 打包
cd E:/LEAR-CODE/smart-campus-backend
mvn -DskipTests package

# 前端构建
cd E:/LEAR-CODE/vue-demo
npm run build
```

**文档结束**

© 2026 智学空间项目组. 保留所有权利。
