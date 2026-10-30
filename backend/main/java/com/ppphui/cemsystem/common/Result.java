package com.ppphui.cemsystem.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data){ //Controller 返回数据给前端
        Result<T> r=new Result<>();
        r.code=ResultCode.SUCCESS.getCode();
        r.msg=ResultCode.SUCCESS.getMsg();
        r.data=data;
        return r;
    }

    public static Result<Void> success() {
        return success(null);
    }


    public static <T> Result<T> error(String msg){  //业务失败时返回错误
        Result<T> r=new Result<>();
        r.code=ResultCode.FAILED.getCode();
        r.msg=msg;
        return r;
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMsg(resultCode.getMsg());
        return r;
    }
}
