package com.ppphui.cemsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ppphui.cemsystem.common.BusinessException;
import com.ppphui.cemsystem.common.ResultCode;
import com.ppphui.cemsystem.dto.*;
import com.ppphui.cemsystem.pojo.User;
import com.ppphui.cemsystem.security.JwtUtil;
import com.ppphui.cemsystem.service.UserService;
import com.ppphui.cemsystem.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ppphui.cemsystem.security.LoginUser;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    public void register(RegisterDTO dto){
        if(!dto.getConfirmPassword().equals(dto.getPassword()))
            throw new BusinessException(ResultCode.PASSWORD_NOT_MATCH);

        Long count=userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUserName,dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        String hash=passwordEncoder.encode(dto.getPassword());
        User u=new User();
        u.setUserName(dto.getUsername());
        u.setPassword(hash);
        u.setRole("USER");
        u.setFactoryName(dto.getFactoryName());

        userMapper.insert(u);
    }

    public LoginVO login(LoginDTO dto){
        User user=userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserName,dto.getUsername()));

        if(user==null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if(!passwordEncoder.matches(dto.getPassword(),user.getPassword())){
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token=jwtUtil.generateToken(user.getId(),user.getUserName(),user.getRole(),user.getFactoryName());

        return new LoginVO().builder()
                .token(token)
                .username(user.getUserName())
                .role(user.getRole())
                .factoryName(user.getFactoryName())
                .build();
    }

    @Override
    public UserVO getUser(Integer id) {
        User u=userMapper.selectById(id);
        return UserVO.builder()
                .factoryName(u.getFactoryName())
                .build();
    }


    @Override
    public void updateProfile(Integer id,UserDTO dto) {
        User u=userMapper.selectById(id);
        u.setFactoryName(dto.getFactoryName());
        userMapper.updateById(u);
    }

    @Override
    public void updatePassword(Integer id,PwdDTO dto) {
        User u=userMapper.selectById(id);
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(u);
    }

    @Override
    public Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        // 情况1：principal 是 User 实体
        if (principal instanceof User user) {
            return user.getId();
        }
        // 情况2：principal 是 username 字符串 → 查库
        if (principal instanceof String username) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUserName, username));
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_FOUND);
            }
            return user.getId();
        }if (principal instanceof Integer userId) {
            return userId;
        }
        throw new BusinessException(ResultCode.UNAUTHORIZED);
    }

    @Override
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        // 方式1：看 authorities 里是否有 ROLE_ADMIN
        boolean hasAdminRole = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority())
                        || "ADMIN".equals(a.getAuthority()));
        if (hasAdminRole) {
            return true;
        }
        // 方式2：兜底查库
        try {
            Integer userId = getCurrentUserId();
            User user = userMapper.selectById(userId);
            return user != null && "ADMIN".equals(user.getRole());
        } catch (Exception e) {
            return false;
        }
    }
}
