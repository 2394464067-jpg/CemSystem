package com.ppphui.cemsystem.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志表
 * 注意：日志表一般不做逻辑删除，也不自动填充 create_time（由切面手动 set），
 * 这里为了演示统一，仍用自动填充。
 */
@Data
@TableName("t_sys_log")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作模块 */
    private String module;

    /** 操作类型 */
    private String type;

    /** 操作人用户名（从 JWT 解析） */
    private String operator;

    /** 请求方式 GET/POST/PUT/DELETE */
    private String requestMethod;

    /** 请求 URL */
    private String requestUrl;

    /** 请求参数（JSON 字符串） */
    private String requestParams;

    /** 目标数据 ID（从参数中提取，可为空） */
    private Long targetId;

    /** 操作 IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 状态：1-成功 0-失败 */
    private Integer status;

    /** 异常信息（失败时记录） */
    private String errorMsg;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
