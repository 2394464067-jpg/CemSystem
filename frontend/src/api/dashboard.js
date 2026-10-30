import request from '@/utils/request'

/** 获取看板数据 */
export function getDashboardApi() {
    return request.get('/dashboard')
}