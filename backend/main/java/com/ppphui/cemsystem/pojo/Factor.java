package com.ppphui.cemsystem.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_emission_factor")
public class Factor {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String energyName;      // 电力/天然气/柴油/蒸汽
    private BigDecimal factorValue; // 因子值（必须BigDecimal！）
    private String unit;            // kWh/m³/L/kg

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
