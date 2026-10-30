import request from '@/utils/request'

/** 登录 */
export function loginApi(data) {
    return request.post('/auth/login', data)
}

/** 注册 */
export function registerApi(data) {
    return request.post('/auth/register', data)
}

/** 获取当前用户信息 */
export function getCurrentUserApi() {
    return request.get('/user/me')
}

/** 修改密码 */
export function updatePasswordApi(data) {
    return request.put('/user/password', data)
}

/** 修改个人信息 */
export function updateProfileApi(data) {
    return request.put('/user/profile', data)
}