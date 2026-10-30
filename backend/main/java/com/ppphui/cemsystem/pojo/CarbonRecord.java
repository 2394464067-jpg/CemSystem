package com.ppphui.cemsystem.pojo;


import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_carbon_record")
public class CarbonRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("supplier_id")
    private Integer supplierId;

    @TableField("user_id")
    private Integer userId;
    private String energyType;
    private BigDecimal consumeAmount;
    private BigDecimal totalEmission;
    private String reportMonth;

    private int status;

    @TableLogic
    private Integer deleted;

    @Version
    private Integer version;
}

