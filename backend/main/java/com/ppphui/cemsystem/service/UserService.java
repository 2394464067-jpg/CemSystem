package com.ppphui.cemsystem.service;

import com.ppphui.cemsystem.dto.*;

public interface UserService {
    public LoginVO login(LoginDTO logindto);
    public void register(RegisterDTO registerdto);

    public UserVO getUser(Integer id);
    public void updateProfile(Integer id, UserDTO userDTO);
    public void updatePassword(Integer id, PwdDTO pwdDTO);

    /** 获取当前登录用户 id */
    Integer getCurrentUserId();

    /** 当前用户是否管理员 */
    boolean isAdmin();
}
