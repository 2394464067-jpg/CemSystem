package com.ppphui.cemsystem.log;

public interface LogType {
    // ===== 模块名（与前端菜单对齐）=====
    String MODULE_AUTH      = "认证权限";
    String MODULE_FACTOR    = "排放因子";
    String MODULE_CARBON    = "碳排上报";
    String MODULE_SUPPLIER  = "供应商";
    String MODULE_REPORT    = "可视化报表";

    // ===== 操作类型 =====
    String LOGIN = "登录";
    String ADD = "新增";
    String UPDATE = "修改";
    String DELETE = "删除";
    String SUBMIT = "提交";
    String AUDIT = "审核";
    String REJECT = "驳回";
    String EXPORT = "导出";
}
// 使用：@Log(module = "碳排上报", type = LogType.SUBMIT)