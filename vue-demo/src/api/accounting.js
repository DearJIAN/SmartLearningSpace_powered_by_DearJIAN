import request from '@/utils/request'

// 认证相关
export const login = (data) => request.post('/api/accounting/auth/login', data)
export const register = (data) => request.post('/api/accounting/auth/register', data)
export const logout = () => request.post('/api/accounting/auth/logout')
export const getCurrentUser = () => request.get('/api/accounting/auth/current')

// 账单相关
export const getBillPage = (params) => request.get('/api/accounting/bills', { params })
export const getBillStats = (params) => request.get('/api/accounting/bills/stats', { params })
export const saveBill = (data) => request.post('/api/accounting/bills', data)
export const deleteBill = (id) => request.delete(`/api/accounting/bills/${id}`)
export const clearBills = () => request.delete('/api/accounting/bills/clear')
export const generateBills = (count) => request.post('/api/accounting/bills/generate', null, { params: { count } })
export const getCategories = () => request.get('/api/accounting/bills/categories')
export const exportBillsUrl = (params) => {
    const p = new URLSearchParams()
    if (params) {
        Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
                p.append(key, params[key])
            }
        })
    }
    return `/api/accounting/bills/export?${p.toString()}`
}

// 统计相关
export const getAnalysisData = (params) => request.get('/api/accounting/analysis/trend', { params })
export const getTreeMapData = (params) => request.get('/api/accounting/analysis/treemap', { params })
export const getCalendarData = (params) => request.get('/api/accounting/calendar/events', { params })

// 预算相关
export const getBudgetStatus = (month) => request.get(`/api/accounting/budget/${month}`)
export const saveBudget = (data) => request.post('/api/accounting/budget', data)

// 洞察相关
export const getInsightDashboard = () => request.get('/api/accounting/insight/dashboard')
export const getFinancialProfile = () => request.get('/api/accounting/insight/profile')
export const getFinancialTimeline = () => request.get('/api/accounting/insight/timeline')
export const getRiskAlerts = () => request.get('/api/accounting/insight/risk')
export const getGoalTracking = () => request.get('/api/accounting/insight/goal')
export const updateGoal = (data) => request.post('/api/accounting/insight/goal', data)
