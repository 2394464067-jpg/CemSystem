package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserVO {
    @Schema(description = "工厂名称")
    private String factoryName;
}
