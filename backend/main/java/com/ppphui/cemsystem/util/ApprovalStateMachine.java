package com.ppphui.cemsystem.util;

import com.ppphui.cemsystem.common.BusinessException;
import com.ppphui.cemsystem.common.ResultCode;

import java.util.HashMap;
import java.util.Map;

public class ApprovalStateMachine {
    private static final Map<ApprovalStatus,Map<ApprovalAction,ApprovalStatus>> TRANSACTIONS=new HashMap<>();

    static{
        TRANSACTIONS.put(ApprovalStatus.DRAFT,Map.of(ApprovalAction.SUBMIT,ApprovalStatus.PENDING));
        TRANSACTIONS.put(ApprovalStatus.PENDING,Map.of(ApprovalAction.APPROVE,ApprovalStatus.APPROVED,ApprovalAction.REJECT,ApprovalStatus.REJECTED));
        TRANSACTIONS.put(ApprovalStatus.REJECTED,Map.of(ApprovalAction.RESUBMIT,ApprovalStatus.PENDING));
        TRANSACTIONS.put(ApprovalStatus.APPROVED,Map.of());


    }
    public static ApprovalStatus fire(ApprovalStatus current,ApprovalAction action){
            Map<ApprovalAction,ApprovalStatus> nextMap=TRANSACTIONS.getOrDefault(current,Map.of());
            ApprovalStatus nextStatus=nextMap.get(action);
            if(nextStatus==null){
                throw new BusinessException(ResultCode.RECORD_STATUS_INVALID);
            }
            return nextStatus;
    }

}
