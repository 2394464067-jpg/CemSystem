import request from '@/utils/request'


/** 分页查询供应商 */
export function getSupplierPageApi(params) {
    return request.get('/supplier/page', { params })
}

/** 根据 id 查询供应商详情（含绑定的碳排记录） */
export function getSupplierByIdApi(id) {
    return request.get(`/supplier/${id}`)
}

/** 新增供应商 */
export function saveSupplierApi(data) {
    return request.post('/supplier', data)
}

/** 修改供应商 */
export function updateSupplierApi(data) {
    return request.put('/supplier', data)
}

/** 删除供应商 */
export function deleteSupplierApi(id) {
    return request.delete(`/supplier/${id}`)
}

/** 批量绑定碳排记录到供应商 */
export function bindSupplierRecordsApi(data) {
    return request.post('/supplier/bind', data)
}

/** 解绑碳排记录 */
export function unbindSupplierRecordApi(recordId) {
    return request.delete(`/supplier/unbind/${recordId}`)
}

/** 手动触发绿色等级计算 */
export function recalculateGreenLevelApi() {
    return request.post('/supplier/recalculate-level')
}