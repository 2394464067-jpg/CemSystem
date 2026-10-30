import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

//1.配置路由规则
const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录', public: true }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '注册', public: true }
    },
    {
        path: '/',
        component: () => import('@/layout/MainLayout.vue'),
        redirect: '/carbon',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '可视化看板', icon: 'DataLine', roles: ['ADMIN'] }
            },
            {
                path: 'factor',
                name: 'Factor',
                component: () => import('@/views/Factor.vue'),
                meta: { title: '排放因子管理', icon: 'Odometer', roles: ['ADMIN'] }
            },
            {
                path: 'carbon',
                name: 'Carbon',
                component: () => import('@/views/CarbonRecord.vue'),
                meta: { title: '碳排数据上报', icon: 'Document', roles: ['ADMIN', 'USER'] }
            },
            {
                path: 'supplier',
                name: 'Supplier',
                component: () => import('@/views/Supplier.vue'),
                meta: { title: '供应商管理', icon: 'OfficeBuilding', roles: ['ADMIN'] }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: { title: '个人中心', icon: 'User', roles: ['ADMIN', 'USER'] }
            }
        ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/carbon' }     //当用户访问任何未定义/不存在的路径时，自动跳转到 /carbon。相当于一个简易的 404 处理
]

//2.创建路由器
const router = createRouter({
    history: createWebHistory(), //路由工作模式
    routes
})

// ========== 全局前置守卫 ==========
router.beforeEach((to, from, next) => {
    document.title = `${to.meta.title || '碳排系统'} - 碳排数据管理系统` //动态设置浏览器标签页标题，格式为 页面标题 - 碳排数据管理系统，当页面没有配置标题时，回退显示 碳排系统。

    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role')

    // 1. 公开页面直接放行
    if (to.meta.public) {
        // 已登录用户访问登录页 → 跳首页
        if (token && (to.path === '/login' || to.path === '/register')) {
            return next('/dashboard')
        }
        return next() //放行，正常进入 to 路由
    }

    // 2. 未登录访问受保护页面 → 跳登录
    if (!token) {
        ElMessage.warning('请先登录')
        return next({ path: '/login', query: { redirect: to.fullPath } })  //拦截并改道
    }

    // 3. 角色权限校验
    if (to.meta.roles && !to.meta.roles.includes(role)) {
        ElMessage.error('没有权限访问该页面')
        return next(role === 'ADMIN' ? '/dashboard' : '/carbon')
    }

    next()
})

export default router