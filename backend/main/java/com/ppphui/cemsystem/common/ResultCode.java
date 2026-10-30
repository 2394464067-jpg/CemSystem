package com.ppphui.cemsystem.common;

import lombok.Getter;

@Getter
public enum ResultCode {

    /* ========== 通用 ========== */
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),

    /* ========== 参数/客户端错误 4xx ========== */
    VALIDATE_FAILED(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    PARAM_ERROR(40000, "参数错误"),

    /* ========== 业务相关（1xxx 起） ========== */
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PASSWORD_NOT_MATCH(1004, "两次密码不一致"),
    OLD_PASSWORD_ERROR(1005, "原密码不正确"),
    ROLE_INVALID(1006, "角色不合法"),
    /* ========== 模块二：排放因子（2xxx 起） ========== */
    FACTOR_NOT_FOUND(2001, "排放因子不存在"),
    FACTOR_VALUE_INVALID(2002, "排放因子必须为正数"),
    FACTOR_NAME_EXISTS(2003, "该能源类型已存在"),

    /* ========== 模块三：碳排上报（3xxx 起） ========== */

    RECORD_NOT_FOUND(3001, "碳排记录不存在"),
    RECORD_STATUS_INVALID(3002, "当前记录状态不允许该操作"),
    RECORD_NOT_APPROVED(3003, "碳排记录未审核通过，不能绑定"),
    REJECT_REASON_REQUIRED(3005, "驳回时必须填写驳回原因"),
    NO_PERMISSION(3007, "无权限操作"),

    CONCURRENT_UPDATE(3008, "出现了并发冲突，请稍后重试"),

    /* ========== 模块四：供应商（4xxx 起） ========== */
    SUPPLIER_NOT_FOUND(4001, "供应商不存在"),
    SUPPLIER_NAME_EXISTS(4003, "供应商名称已存在"),
    RECORD_ALREADY_BOUND(4002, "记录已绑定供应商"),

    /* ========== 服务端错误 5xx ========== */
    SYSTEM_ERROR(5000, "系统异常，请稍后重试");



    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}