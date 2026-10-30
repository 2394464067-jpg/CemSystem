package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FactorSaveDTO {

    @Schema(description = "因子ID")
    private Integer id;

    @Schema(description = "能源名称")
    @NotBlank(message = "能源类型不能为空")
    private String energyName;

    @Schema(description = "因子值")
    @NotNull(message = "因子值不能为空")
    @DecimalMin(value = "0.0001", message = "因子值必须为正数")
    private BigDecimal factorValue;

    @Schema(description = "单位")
    @NotBlank(message = "单位不能为空")
    private String unit;

    @Schema(description = "备注")
    private String remark;
}
