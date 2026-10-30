package com.ppphui.cemsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppphui.cemsystem.common.BusinessException;
import com.ppphui.cemsystem.common.ResultCode;
import com.ppphui.cemsystem.dto.*;
import com.ppphui.cemsystem.mapper.CarbonRecordMapper;
import com.ppphui.cemsystem.mapper.SupplierMapper;
import com.ppphui.cemsystem.mapper.UserMapper;
import com.ppphui.cemsystem.pojo.CarbonRecord;
import com.ppphui.cemsystem.pojo.Factor;
import com.ppphui.cemsystem.pojo.Supplier;
import com.ppphui.cemsystem.pojo.User;
import com.ppphui.cemsystem.security.LoginContext;
import com.ppphui.cemsystem.service.CarbonRecordService;
import com.ppphui.cemsystem.service.FactorService;
import com.ppphui.cemsystem.service.SupplierService;
import com.ppphui.cemsystem.util.ApprovalAction;
import com.ppphui.cemsystem.util.ApprovalStateMachine;
import com.ppphui.cemsystem.util.ApprovalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarbonRecordServiceImpl extends ServiceImpl<CarbonRecordMapper, CarbonRecord>
        implements CarbonRecordService {

    private final FactorService factorService;
    private final UserMapper userMapper;

    /* ==================== 分页查询 ==================== */
    @Override
    public IPage<CarbonRecordVO> page(CarbonRecordQueryDTO query, Integer currentUserId, boolean isAdmin) {
        Page<CarbonRecord> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<CarbonRecord> wrapper = new LambdaQueryWrapper<>();

        // 普通用户只能看自己的
        if (!isAdmin) {
            wrapper.eq(CarbonRecord::getUserId, currentUserId);
        } else{
            wrapper.ne(CarbonRecord::getStatus, 0);
            if (query.getUserId() != null) {
                wrapper.eq(CarbonRecord::getUserId, query.getUserId());
            }
        }


        if (StringUtils.hasText(query.getEnergyType())) {
            wrapper.like(CarbonRecord::getEnergyType, query.getEnergyType());
        }
        if (StringUtils.hasText(query.getReportMonth())) {
            wrapper.eq(CarbonRecord::getReportMonth, query.getReportMonth());
        }
        if (query.getStatus() != null) {
            wrapper.eq(CarbonRecord::getStatus, query.getStatus());
        }
        if (query.getSupplierId() != null) {
            wrapper.eq(CarbonRecord::getSupplierId, query.getSupplierId());
        }
        // 未绑定供应商：supplier_id IS NULL
        if (Boolean.TRUE.equals(query.getUnbound())) {
            wrapper.isNull(CarbonRecord::getSupplierId);
        }


        IPage<CarbonRecord> entityPage = baseMapper.selectPage(pageParam, wrapper);

        // 关联填充 supplierName / factoryName
        List<CarbonRecordVO> voList = entityPage.getRecords().stream()
                .map(this::toVOWithNames)
                .collect(Collectors.toList());

        Page<CarbonRecordVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /* ==================== 详情 ==================== */
    @Override
    public CarbonRecordVO getDetailById(Integer id) {
        CarbonRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        return toVOWithNames(record);
    }

    /* ==================== 保存草稿 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveDraft(CarbonRecordSaveDTO dto, Integer userId) {
        if (dto.getId() != null) {
            checkEditable(dto.getId(), userId);
        }
        CarbonRecord record = new CarbonRecord();
        BeanUtils.copyProperties(dto, record);
        record.setUserId(userId);
        record.setStatus(0); // 草稿
        // 草稿也尝试核算，方便前端展示
        try {
            fillEmission(record);
        } catch (Exception e) {
            log.warn("草稿核算失败：{}", e.getMessage());
        }
        return saveOrUpdate(record);
    }

    /* ==================== 删除 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, Integer currentUserId, boolean isAdmin) {
        CarbonRecord record = baseMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        if (!isAdmin && !record.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
        // 只有草稿(0)或驳回(3)允许删除
        if (record.getStatus() != 0 && record.getStatus() != 3) {
            throw new BusinessException(ResultCode.RECORD_STATUS_INVALID);
        }
        baseMapper.deleteById(id);
    }



    /* ==================== 近 N 月趋势 ==================== */
    @Override
    public List<CarbonRecordStatVO> monthlyTrend(int months) {
        LocalDate start = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        String startMonth = start.toString().substring(0, 7);

        List<CarbonRecord> records = baseMapper.selectList(
                new LambdaQueryWrapper<CarbonRecord>()
                        .eq(CarbonRecord::getStatus, 2)
                        .ge(CarbonRecord::getReportMonth, startMonth));

        // 按月聚合
        Map<String, BigDecimal> map = records.stream()
                .collect(Collectors.groupingBy(
                        CarbonRecord::getReportMonth,
                        Collectors.reducing(BigDecimal.ZERO,
                                r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission(),
                                BigDecimal::add)));

        // 补全缺失月份为 0
        List<CarbonRecordStatVO> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            String m = LocalDate.now().minusMonths(i).toString().substring(0, 7);
            CarbonRecordStatVO vo = new CarbonRecordStatVO();
            vo.setMonth(m);
            vo.setTotalEmission(map.getOrDefault(m, BigDecimal.ZERO));
            result.add(vo);
        }
        return result;
    }

    /* ==================== 可绑定记录 ==================== */
    @Override
    public List<CarbonRecordVO> listBindable(String reportMonth) {
        LambdaQueryWrapper<CarbonRecord> wrapper = new LambdaQueryWrapper<CarbonRecord>()
                .eq(CarbonRecord::getStatus, 2)
                .isNull(CarbonRecord::getSupplierId)
                .orderByDesc(CarbonRecord::getReportMonth);
        if (StringUtils.hasText(reportMonth)) {
            wrapper.eq(CarbonRecord::getReportMonth, reportMonth);
        }
        return baseMapper.selectList(wrapper).stream()
                .map(this::toVOWithNames)
                .collect(Collectors.toList());
    }

    /* ==================== 私有工具 ==================== */

    /** 校验记录是否可编辑：只有草稿/驳回，且必须是本人 */
    private void checkEditable(Integer id, Integer userId) {
        CarbonRecord exist = baseMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        if (exist.getStatus() != 0 && exist.getStatus() != 3) {
            throw new BusinessException(ResultCode.RECORD_STATUS_INVALID);
        }
        if (!exist.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.NO_PERMISSION);
        }
    }

    /** 核算：consumeAmount × factorValue */
    private void fillEmission(CarbonRecord record) {
        // 注意：getByEnergyName 返回 FactorVO
        FactorVO factor = factorService.getByEnergyName(record.getEnergyType());
        if (factor == null || factor.getFactorValue() == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }
        BigDecimal total = record.getConsumeAmount()
                .multiply(factor.getFactorValue())
                .setScale(4, RoundingMode.HALF_UP);
        record.setTotalEmission(total);
    }

    /** entity → VO，并填充工厂名 */
    private CarbonRecordVO toVOWithNames(CarbonRecord entity) {
        CarbonRecordVO vo = new CarbonRecordVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getUserId() != null) {
            User user = userMapper.selectById(entity.getUserId());
            if (user != null) {
                vo.setFactoryName(user.getFactoryName());
            }
        }
        return vo;
    }

    //提交
    @Override
    public void submit(int id){
        changeStatus(id, ApprovalAction.SUBMIT);

    }
    //审核通过
    @Override
    public void approve(int id){
        changeStatus(id, ApprovalAction.APPROVE);
    }
    //审核驳回
    @Override
    public void reject(int id){
        changeStatus(id, ApprovalAction.REJECT);
    }
    //重新提交
    @Override
    public void reSubmit(int id){
        CarbonRecord record=baseMapper.selectById(id);
        if(ApprovalStatus.of(record.getStatus()) == ApprovalStatus.REJECTED) {
            saveOrUpdate(record);
            changeStatus(id, ApprovalAction.RESUBMIT);
        }
    }
    //状态流转：校验+更新
    private void changeStatus(int id, ApprovalAction action){
        CarbonRecord record=baseMapper.selectById(id);
        ApprovalStatus current = ApprovalStatus.of(record.getStatus());

        ApprovalStatus next = ApprovalStateMachine.fire(current, action);
        record.setStatus(next.getCode());

        // 执行更新，MP 会自动在 WHERE 里加上 version 条件并让 version+1
        int rows = baseMapper.updateById(record);
        if (rows == 0) {
            throw new BusinessException(ResultCode.CONCURRENT_UPDATE);
        }
    }
    //保存并提交

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAndSubmit(CarbonRecordSaveAndSubmitDTO dto) {
        // 1. 根据能源类型查排放因子
        FactorVO factor = factorService.getByEnergyName(dto.getEnergyType());
        if (factor == null || factor.getFactorValue() == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }

        // 2. 后端核算碳排放量（前端预估仅展示，以后端为准）
        BigDecimal totalEmission = dto.getConsumeAmount()
                .multiply(factor.getFactorValue())
                .setScale(4, RoundingMode.HALF_UP);

        // 3. 组装实体
        CarbonRecord record = new CarbonRecord();
        record.setReportMonth(dto.getReportMonth());
        record.setEnergyType(dto.getEnergyType());
        record.setConsumeAmount(dto.getConsumeAmount());
        record.setTotalEmission(totalEmission);
        record.setStatus(1); // 待审核
        if (dto.getId() == null) {
            // 新增
            record.setUserId(LoginContext.get().getUserId());
            this.save(record);
        } else {
            // 编辑：校验记录存在、属于当前用户、且状态为草稿(0)或驳回(3)
            CarbonRecord old = this.getById(dto.getId());
            if (old == null) {
                throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
            }
            if (old.getStatus() != 0 && old.getStatus() != 3) {
                throw new BusinessException(ResultCode.RECORD_STATUS_INVALID);
            }
            // 按需校验归属：old.getUserId().equals(当前用户id)
            record.setId(dto.getId());
            this.updateById(record);
        }
    }

}