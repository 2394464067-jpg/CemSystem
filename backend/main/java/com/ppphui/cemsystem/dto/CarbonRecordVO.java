package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CarbonRecordVO {

    @Schema(description = "记录编号")
    private Integer id;
    @Schema(description = "用户编号")
    private Integer userId;

    @Schema(description = "工厂名称")
    private String factoryName;

    @Schema(description = "能源类型")
    private String energyType;
    @Schema(description = "消耗量")
    private BigDecimal consumeAmount;
    @Schema(description = "总排放量")
    private BigDecimal totalEmission;
    @Schema(description = "上报月份")
    private String reportMonth;
    @Schema(description = "状态 0-3")
    private Integer status;

}