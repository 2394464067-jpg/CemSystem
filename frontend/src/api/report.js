import request from '@/utils/request'

/** 导出碳排明细报表（返回 blob） */
export function exportCarbonReportApi(params) {
    return request.get('/report/carbon/export', {
        params,
        responseType: 'blob'
    })
}