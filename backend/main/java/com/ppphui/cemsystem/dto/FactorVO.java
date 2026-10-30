package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FactorVO {
    @Schema(description = "因子ID")
    private Integer id;

    @Schema(description = "能源类型")
    private String energyName;
    @Schema(description = "因子值")
    private BigDecimal factorValue;
    @Schema(description = "单位")
    private String unit;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
