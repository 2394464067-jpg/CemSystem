package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SupplierRecordItemVO {

    @Schema(description = "供应商 id")
    private Integer id;

    @Schema(description = "能源类型")
    private String energyType;

    @Schema(description = "消耗量")
    private BigDecimal consumeAmount;

    @Schema(description = "总排放量")
    private BigDecimal totalEmission;

    @Schema(description = "上报月份 yyyy-MM")
    private String reportMonth;

    @Schema(description = "状态 0-草稿 1-待审核 2-通过 3-驳回")
    private Integer status;
}