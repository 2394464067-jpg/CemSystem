package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarbonRecordSaveDTO {

    @Schema(description = "记录编号，编辑时传，新增时不传")
    private Integer id;

    @Schema(description = "能源类型")
    @NotBlank(message = "能源类型不能为空")
    private String energyType;

    @Schema(description = "消耗量")
    @NotNull(message = "消耗量不能为空")
    @DecimalMin(value = "0.0001", message = "消耗量必须为正数")
    private BigDecimal consumeAmount;
    @Schema(description = "上报月份")

    @NotBlank(message = "上报月份不能为空")
    private String reportMonth;

    @Schema(description = "可选：草稿保存时不提交审核")
    private Boolean submit;

    @Schema(description = "可选：关联供应商")
    private Integer supplierId;
}