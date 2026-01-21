import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/Login.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/',
      name: 'map',
      component: () => import('../views/MapView.vue'),
      meta: { title: '校园空间导航' }
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      meta: { title: '教室状态实时看板' }
    },
    {
      path: '/lost-found',
      name: 'lost-found',
      component: () => import('../views/LostFound.vue'),
      meta: { title: '失物招领智能辅助' }
    },

    {
      path: '/accounting',
      component: () => import('../views/accounting/AccountingLayout.vue'),
      children: [
        {
          path: 'bills',
          name: 'accounting-bills',
          component: () => import('../views/accounting/BillList.vue'),
          meta: { title: '账单明细' }
        },
        {
          path: 'analysis',
          name: 'accounting-analysis',
          component: () => import('../views/accounting/BillAnalysis.vue'),
          meta: { title: '统计报表' }
        },
        {
          path: 'treemap',
          name: 'accounting-treemap',
          component: () => import('../views/accounting/BillTreemap.vue'),
          meta: { title: '资金流向树' }
        },
        {
          path: 'calendar',
          name: 'accounting-calendar',
          component: () => import('../views/accounting/BillCalendar.vue'),
          meta: { title: '账单日历' }
        },
        {
          path: 'budget',
          name: 'accounting-budget',
          component: () => import('../views/accounting/BudgetManagement.vue'),
          meta: { title: '预算管理' }
        },
        {
          path: 'insight/dashboard',
          name: 'insight-dashboard',
          component: () => import('../views/accounting/insight/InsightDashboard.vue'),
          meta: { title: '洞察总览' }
        },
        {
          path: 'chat',
          name: 'accounting-chat',
          component: () => import('../views/accounting/AiChat.vue'),
          meta: { title: 'AI 智能助手' }
        },
        {
          path: 'insight/goal',
          name: 'insight-goal',
          component: () => import('../views/accounting/insight/InsightGoal.vue'),
          meta: { title: '财务目标' }
        },
        {
          path: 'insight/profile',
          name: 'insight-profile',
          component: () => import('../views/accounting/insight/InsightProfile.vue'),
          meta: { title: '用户画像' }
        },
        {
          path: 'insight/risk',
          name: 'insight-risk',
          component: () => import('../views/accounting/insight/InsightRisk.vue'),
          meta: { title: '风险评估' }
        },
        {
          path: 'insight/timeline',
          name: 'insight-timeline',
          component: () => import('../views/accounting/insight/InsightTimeline.vue'),
          meta: { title: '财务时间轴' }
        }
      ]
    },
    {
      path: '/seat',
      name: 'seat-reservation',
      component: () => import('../views/SeatReservation.vue'),
      meta: { title: '图书馆座位预约' }
    },
    {
      path: '/canteen',
      name: 'canteen',
      component: () => import('../views/CanteenManagement.vue'),
      meta: { title: '食堂智能服务' }
    },
    {
      path: '/canteen/seating',
      name: 'canteen-seating',
      component: () => import('../views/CanteenSeating.vue'),
      meta: { title: '食堂智能选座' }
    },
    {
      path: '/canteen/ordering',
      name: 'canteen-ordering',
      component: () => import('../views/CanteenOrdering.vue'),
      meta: { title: '食堂智能点餐' }
    }
  ]
})

// 全局路由守卫：仅允许 /login，其他页面需先获取当前用户
import { getCurrentUser } from '../api/accounting'

router.beforeEach(async (to, from, next) => {
  const startTime = performance.now()
  console.log('=== 路由守卫触发 ===', '时间戳:', startTime)
  console.log('目标路由:', to.path)
  console.log('来源路由:', from.path)

  if (to.path === '/login') {
    console.log('跳过路由守卫（登录页）', '耗时:', (performance.now() - startTime).toFixed(2), 'ms')
    return next()
  }

  // 首先检查 localStorage，如果存在 currentUser 则认为已登录
  try {
    const checkCacheStart = performance.now()
    const cached = localStorage.getItem('currentUser')
    console.log('检查localStorage耗时:', (performance.now() - checkCacheStart).toFixed(2), 'ms')
    console.log('localStorage中的用户数据:', cached ? '存在' : '不存在')

    if (cached) {
      const parseStart = performance.now()
      const userData = JSON.parse(cached)
      console.log('解析JSON耗时:', (performance.now() - parseStart).toFixed(2), 'ms')
      console.log('解析的用户数据:', userData)

      // 如果有验证标记且在1小时内，直接放行，不再调用API
      if (userData._verified && userData._timestamp) {
        const oneHour = 60 * 60 * 1000 // 1小时的毫秒数
        const timeDiff = Date.now() - userData._timestamp
        console.log('验证标记存在，时间差:', timeDiff, 'ms (阈值:', oneHour, 'ms)')

        if (timeDiff < oneHour) {
          console.log('验证通过，直接放行（不调用API）', '总耗时:', (performance.now() - startTime).toFixed(2), 'ms')
          return next()
        } else {
          console.log('验证标记已过期，需要重新验证')
        }
      } else {
        console.log('缺少验证标记，需要验证')
      }

      // 超过1小时或无验证标记，重新验证
      console.log('开始调用getCurrentUser() API...')
      try {
        const apiStart = performance.now()
        await getCurrentUser()
        console.log('getCurrentUser() API调用成功', 'API耗时:', (performance.now() - apiStart).toFixed(2), 'ms')
        console.log('总耗时:', (performance.now() - startTime).toFixed(2), 'ms')
        return next()
      } catch (e) {
        console.error('getCurrentUser() API调用失败:', e)
        // 缓存无效，清除并跳转到登录页
        localStorage.removeItem('currentUser')
      }
    }
  } catch (e) {
    console.error('读取localStorage时出错:', e)
  }

  // 如果没有缓存或缓存验证失败，尝试获取当前用户
  console.log('没有缓存或缓存无效，尝试获取当前用户')
  try {
    console.log('调用getCurrentUser() API...')
    const apiStart = performance.now()
    await getCurrentUser()
    console.log('getCurrentUser() API调用成功，允许跳转', 'API耗时:', (performance.now() - apiStart).toFixed(2), 'ms')
    console.log('总耗时:', (performance.now() - startTime).toFixed(2), 'ms')
    next()
  } catch (err) {
    console.error('getCurrentUser() API调用失败:', err)
    console.log('跳转到登录页', '总耗时:', (performance.now() - startTime).toFixed(2), 'ms')
    next('/login')
  }
})

export default router
