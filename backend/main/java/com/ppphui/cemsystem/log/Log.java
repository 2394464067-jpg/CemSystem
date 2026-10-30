package com.ppphui.cemsystem.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    /** 操作模块，如：碳排数据上报、因子库管理 */
    String module() default "";
    /** 操作类型，如：新增、修改、删除、导出 */
    String type() default "";
}