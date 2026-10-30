package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginVO {

    @Schema(description = "token")
    private String token;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "角色")         // ADMIN / USER
    private String role;
    @Schema(description = "工厂名称")
    private String factoryName;
}
