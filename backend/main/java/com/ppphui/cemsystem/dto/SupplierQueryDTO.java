package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SupplierQueryDTO {

    @Schema(description = "页码",defaultValue = "1")
    private Integer page = 1;
    @Schema(description = "每页大小",defaultValue = "10")
    private Integer size = 10;

    @Schema(description = "供应商名称模糊查询")
    private String name;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "绿色等级 A/B/C")
    private String greenLevel;
}