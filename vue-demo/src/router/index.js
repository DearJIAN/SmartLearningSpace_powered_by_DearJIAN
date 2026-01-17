import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
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
    // 记账模块路由
    {
      path: '/accounting/login',
      name: 'accounting-login',
      component: () => import('../views/accounting/AccountingLogin.vue'),
      meta: { title: '记账登录' }
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
    }
  ]
})

export default router
