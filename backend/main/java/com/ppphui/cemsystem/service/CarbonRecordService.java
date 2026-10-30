package com.ppphui.cemsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ppphui.cemsystem.dto.*;
import com.ppphui.cemsystem.pojo.CarbonRecord;

import java.util.List;

public interface CarbonRecordService {

    /** 分页查询 */
    IPage<CarbonRecordVO> page(CarbonRecordQueryDTO query, Integer currentUserId, boolean isAdmin);
    /** 根据 id 查询详情 */
    CarbonRecordVO getDetailById(Integer id);

    /** 保存草稿（不核算，状态 0） */
    boolean saveDraft(CarbonRecordSaveDTO dto, Integer userId);

    void saveAndSubmit(CarbonRecordSaveAndSubmitDTO dto);
    /** 提交/保存并核算（状态 1） */
    void submit(int id);
    /** 管理员审核 */
    void approve(int id);
    /** 驳回 */
    void reject(int id);
    //重新提交
    void reSubmit(int id);
    /** 删除（逻辑删除，仅草稿/驳回可删） */
    void delete(Integer id, Integer currentUserId, boolean isAdmin);




    /** 统计近 N 个月趋势（大屏） */
    List<CarbonRecordStatVO> monthlyTrend(int months);
    /** 查询可绑定的记录（审核通过且未绑定） */
    List<CarbonRecordVO> listBindable(String reportMonth);
}