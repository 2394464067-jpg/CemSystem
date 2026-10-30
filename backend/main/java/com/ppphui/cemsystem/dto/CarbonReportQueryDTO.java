package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CarbonReportQueryDTO {
    @Schema(description = "起始月份 yyyy-MM（必填）",requiredMode = Schema.RequiredMode.REQUIRED,example = "2023-01")
    private String startMonth;

    @Schema(description = "结束月份 yyyy-MM（可选，默认等于起始月）")
    private String endMonth;

    @Schema(description = "工厂名（可选）")
    private String factoryName;

    @Schema(description = "供应商 id（可选）")
    private Integer supplierId;

    @Schema(description = "状态（可选，默认只导审核通过=2）")
    private Integer status;
}