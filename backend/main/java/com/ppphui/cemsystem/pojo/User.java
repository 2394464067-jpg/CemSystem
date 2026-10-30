package com.ppphui.cemsystem.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_user")
public class User {

    @TableId(type=IdType.AUTO)
    private Integer id;
    @TableField("username")
    private String userName;
    private String password;        // BCrypt加密存储，返回前端时必须置null
    private String role;            // ADMIN / USER
    @TableField("factory_name")
    private String factoryName;

    @TableLogic
    private Integer deleted;

}

