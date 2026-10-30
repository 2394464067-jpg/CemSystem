package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SupplierDetailVO {

    @Schema(description = "供应商 id")
    private Integer id;

    @Schema(description = "供应商名称")
    private String name;

    @Schema(description = "供应商联系方式")
    private String contact;

    @Schema(description = "供应商行业")
    private String industry;

    @Schema(description = "供应商绿色等级")
    private String greenLevel;

    @Schema(description = "供应商平均排放量")
    private BigDecimal avgEmission;

    @Schema(description = "供应商创建时间")
    private LocalDateTime createTime;

    @Schema(description = "供应商更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "该供应商绑定的碳排记录（近 6 个月）")
    private List<SupplierRecordItemVO> records;
}