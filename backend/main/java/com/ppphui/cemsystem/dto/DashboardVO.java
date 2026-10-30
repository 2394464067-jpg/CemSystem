package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class DashboardVO {

    @Schema(description = "总排放量（全部审核通过）")
    private BigDecimal totalEmission;

    @Schema(description = "本月排放量")
    private BigDecimal currentMonthEmission;

    @Schema(description = "环比增长率（%）")
    private BigDecimal monthOverMonthRate;

    @Schema(description = "供应商总数")
    private Long supplierCount;

    @Schema(description = "待审核记录数")
    private Long pendingCount;

    @Schema(description = "近12月趋势")
    private List<CarbonRecordStatVO> monthlyTrend;

    @Schema(description = "各工厂排放占比")
    private List<PieItemVO> factoryPie;

    @Schema(description = "各供应商排放占比")
    private List<PieItemVO> supplierPie;
}