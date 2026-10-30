package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarbonRecordStatVO {

    @Schema(description = "月份 yyyy-MM")
    private String month;

    @Schema(description = "该月总排放")
    private BigDecimal totalEmission;
}