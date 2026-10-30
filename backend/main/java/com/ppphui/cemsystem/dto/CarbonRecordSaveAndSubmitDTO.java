package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarbonRecordSaveAndSubmitDTO {

    @Schema(description = "记录编号，编辑时传，新增时不传")
    private Integer id;

    @Schema(description = "上报月份")
    @NotNull(message = "上报月份不能为空")
    private String reportMonth;   // 格式 YYYY-MM

    @Schema(description = "能源类型")
    @NotNull(message = "能源类型不能为空")
    private String energyType;

    @Schema(description = "消耗量")
    @NotNull(message = "消耗量不能为空")
    @DecimalMin(value = "0", message = "消耗量不能为负")
    private BigDecimal consumeAmount;
}