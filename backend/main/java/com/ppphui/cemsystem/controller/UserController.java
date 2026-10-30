package com.ppphui.cemsystem.controller;

import com.ppphui.cemsystem.common.Result;
import com.ppphui.cemsystem.dto.PwdDTO;
import com.ppphui.cemsystem.dto.UserDTO;
import com.ppphui.cemsystem.dto.UserVO;
import com.ppphui.cemsystem.security.LoginContext;
import com.ppphui.cemsystem.security.LoginUser;
import com.ppphui.cemsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "个人中心相关",description = "获得当前用户信息，修改用户信息，修改用户密码")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "获得当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me() {
        LoginUser loginUser = LoginContext.get();
        UserVO vo = userService.getUser(loginUser.getUserId());
        return Result.success(vo);
    }

    @Operation(summary = "修改用户信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UserDTO dto) {
        LoginUser u=LoginContext.get();
        userService.updateProfile(u.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "修改用户密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody @Valid PwdDTO dto) {
        LoginUser loginUser = LoginContext.get();
        userService.updatePassword(loginUser.getUserId(),dto);
        return Result.success();
    }
}
