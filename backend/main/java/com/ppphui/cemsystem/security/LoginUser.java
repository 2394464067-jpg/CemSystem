package com.ppphui.cemsystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {
    private Integer userId;
    private String userName;
    private String role;
    private String factoryName;
}
