package com.ppphui.cemsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {
    @Schema(description = "用户名")
    @NotBlank(message="用户名不能为空")
    @Size(min=2,max=10,message="用户名长度在2到10个字符之间")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message="密码不能为空")
    @Size(min=6,max=20,message="密码长度在6到20个字符之间")
    private String password;

    @Schema(description = "确认密码")
    @NotBlank(message = "请确认密码")
    private String confirmPassword;

    @Schema(description = "工厂名称")
    @NotBlank(message="工厂名称不能为空")
    private String factoryName;
}
