package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserDTO {
    @Schema(description = "工厂名称")
    private String factoryName;
}
