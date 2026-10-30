package com.ppphui.cemsystem.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_supplier")
public class Supplier {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String contact;
    private String industry;
    private String greenLevel;      // A/B/C
    private BigDecimal avgEmission; // 近6月平均碳排

    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

