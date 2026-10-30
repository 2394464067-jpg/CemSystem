package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CarbonRecordQueryDTO {

    @Schema(description = "每页条数", defaultValue = "1")
    private Integer page = 1;
    @Schema(description = "页大小", defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "能源类型")
    private String energyType;

    @Schema(description = "上报月份")
    private String reportMonth;

    @Schema(description = "状态 0-3")
    private Integer status;

    @Schema(description = "是否只查未绑定供应商的记录（绑定弹窗用）")
    private Boolean unbound;

    @Schema(description = "供应商 id")
    private Integer supplierId;

    @Schema(description = "工厂用户 id（管理员可查所有，普通用户只查自己）")
    private Integer userId;
}