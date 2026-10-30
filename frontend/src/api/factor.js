import request from '@/utils/request'

/** 分页查询 */
export function getFactorPageApi(params) {
    return request.get('/factor/page', { params })
}

/** 根据 id 查询 */
export function getFactorByIdApi(id) {
    return request.get(`/factor/${id}`)
}
/** 根据因子名称（能源类型）查询 */
export function getFactorByEnergyNameApi(energyName) {
    return request.get('/factor/name', { params: { energyName } })
}
/** 新增 */
export function saveFactorApi(data) {
    return request.post('/factor', data)
}

/** 修改 */
export function updateFactorApi(data) {
    return request.put('/factor', data)
}

/** 删除 */
export function deleteFactorApi(id) {
    return request.delete(`/factor/${id}`)
}