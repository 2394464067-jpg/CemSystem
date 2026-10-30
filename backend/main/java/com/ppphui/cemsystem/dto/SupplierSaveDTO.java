package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierSaveDTO {
    @Schema(description = "供应商 id")
    private Integer id;

    @Schema(description = "供应商名称")
    @NotBlank(message = "供应商名称不能为空")
    private String name;

    @Schema(description = "供应商联系方式")
    private String contact;

    @Schema(description = "供应商行业")
    private String industry;

}