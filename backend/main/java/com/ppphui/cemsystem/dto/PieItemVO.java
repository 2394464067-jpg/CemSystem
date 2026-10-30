package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PieItemVO {
    @Schema(description = "名称")
    private String name;
    @Schema(description = "值")
    private BigDecimal value;

    public PieItemVO() {}
    public PieItemVO(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }
}