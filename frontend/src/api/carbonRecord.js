import request from '@/utils/request'

// 分页查询
export function listCarbon(params) {
    return request({ url: '/carbon-record/page', method: 'get', params })
}

// 详情
export function getCarbon(id) {
    return request({ url: `/carbon-record/${id}`, method: 'get' })
}

// 保存草稿（状态 0）
export function saveDraftApi(data) {
    return request({ url: '/carbon-record/draft', method: 'post', data })
}
//保存并提交
export function saveAndSubmitApi(data) {
    return request({ url: '/carbon-record/saveAndSubmit', method: 'post', data })
}
// 提交审核（自动核算，状态 1）
export function submitApi(id) {
    return request({ url: `/carbon-record/submit/${id}`, method: 'post' })
}

export function approveApi(id) {
    return request({ url: `/carbon-record/approve/${id}`, method: 'post' })
}
export function rejectApi(id) {
    return request({ url: `/carbon-record/reject/${id}`, method: 'post' })
}
export function reSubmitApi(id) {
    return request({ url: `/carbon-record/resubmit/${id}`, method: 'post' })
}

// 删除
export function deleteCarbon(id) {
    return request({ url: `/carbon-record/${id}`, method: 'delete' })
}

// 可绑定记录
export function listBindable(reportMonth) {
    return request({ url: '/carbon-record/bindable', method: 'get', params: { reportMonth } })
}

// 趋势
export function getTrend(months = 12) {
    return request({ url: '/carbon-record/trend', method: 'get', params: { months } })
}