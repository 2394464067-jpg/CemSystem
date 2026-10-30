package com.ppphui.cemsystem.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarbonReportExcelVO {

    @Schema(description = "序号")
    @ExcelProperty("序号")
    @ColumnWidth(8)
    private Integer seq;

    @Schema(description = "上报月份")
    @ExcelProperty("上报月份")
    @ColumnWidth(12)
    private String reportMonth;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂名称")
    @ColumnWidth(20)
    private String factoryName;

    @Schema(description = "供应商")
    @ExcelProperty("供应商")
    @ColumnWidth(20)
    private String supplierName;

    @Schema(description = "能源类型")
    @ExcelProperty("能源类型")
    @ColumnWidth(12)
    private String energyType;

    @Schema(description = "消耗量")
    @ExcelProperty("消耗量")
    @ColumnWidth(14)
    private BigDecimal consumeAmount;

    @Schema(description = "排放量(tCO₂e)")
    @ExcelProperty("排放量(tCO₂e)")
    @ColumnWidth(16)
    private BigDecimal totalEmission;

    @Schema(description = "状态")
    @ExcelProperty("状态")
    @ColumnWidth(10)
    private String statusText;
}