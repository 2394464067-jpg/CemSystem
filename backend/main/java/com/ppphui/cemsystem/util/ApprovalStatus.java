package com.ppphui.cemsystem.util;

import lombok.Getter;

@Getter
public enum ApprovalStatus {
    DRAFT(0,"草稿"),
    PENDING(1,"待审核"),
    APPROVED(2,"已审核"),
    REJECTED(3,"驳回");

    private int code;
    private String msg;

    ApprovalStatus(int code,String msg){
        this.code = code;
        this.msg = msg;
    }


    public static ApprovalStatus of(int code){
        for (ApprovalStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知状态: " + code);
    }
}
