import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router/router.js'

// 创建 axios 实例
const request = axios.create({
    baseURL: '/api',    //所有请求自动加 /api 前缀，结合vite.config.js中的配置，将前端发出的请求转发给后端，防止跨域
    timeout: 10000
})

// ========== 请求拦截器：自动加 token ==========
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// ========== 响应拦截器：统一处理错误 ==========
request.interceptors.response.use(
    response => {
        // 图片、PDF、Excel、音视频等非文本内容blob 直接返回原始 data
        if (response.config.responseType === 'blob') {
            return response.data
        }
        const res = response.data

        // 业务成功
        if (res.code === 200) {
            return res
        }

        // 业务失败：弹出后端返回的 msg
        ElMessage.error(res.msg || '操作失败')
        return Promise.reject(res)
    },
    error => {
        // 网络/HTTP 错误
        const status = error.response?.status

        if (status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.clear()
            router.push('/login')
        } else if (status === 403) {
            ElMessage.error('没有权限访问')
        } else {
            ElMessage.error(error.response?.data?.msg || '网络异常')
        }

        return Promise.reject(error)
    }
)

export default request