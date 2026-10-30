package com.ppphui.cemsystem.controller;

import com.ppphui.cemsystem.common.Result;
import com.ppphui.cemsystem.dto.LoginDTO;
import com.ppphui.cemsystem.dto.LoginVO;
import com.ppphui.cemsystem.dto.RegisterDTO;
import com.ppphui.cemsystem.log.Log;
import com.ppphui.cemsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name="认证相关",description = "登陆注册")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @Log(module = "认证权限", type = "注册")
    @Operation(summary = "注册", description = "注册一个新用户")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }
    @Log(module = "认证权限", type = "登录")
    @Operation(summary = "登录", description = "用户登录并获得登录凭证")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        LoginVO vo = userService.login(dto);
        return Result.success(vo);
    }
}

