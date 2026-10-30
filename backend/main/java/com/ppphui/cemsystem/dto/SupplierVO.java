package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SupplierVO {

    @Schema(description = "供应商 id")
    private Integer id;

    @Schema(description = "供应商名称")
    private String name;

    @Schema(description = "供应商联系方式")
    private String contact;

    @Schema(description = "供应商行业")
    private String industry;

    @Schema(description = "绿色等级 A/B/C，未评级为 null")
    private String greenLevel;

    @Schema(description = "近 6 个月平均排放量")
    private BigDecimal avgEmission;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}