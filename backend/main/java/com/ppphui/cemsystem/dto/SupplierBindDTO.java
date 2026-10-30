package com.ppphui.cemsystem.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SupplierBindDTO {
    @Schema(description = "供应商 id 不能为空")
    @NotNull(message = "供应商 id 不能为空")
    private Integer supplierId;

    @Schema(description = "要绑定的碳排记录 id 列表")
    @NotEmpty(message = "请选择要绑定的碳排记录")
    private List<Integer> recordIds;
}
