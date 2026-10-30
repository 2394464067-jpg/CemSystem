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
import com.ppphui.cemsystem.pojo.CarbonRecord;
import com.ppphui.cemsystem.pojo.Supplier;
import com.ppphui.cemsystem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements SupplierService {

    private final CarbonRecordMapper carbonRecordMapper;

    /* ==================== 分页 ==================== */
    @Override
    public IPage<SupplierVO> page(SupplierQueryDTO query) {
        Page<Supplier> pageParam = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(Supplier::getName, query.getName());
        }
        if (StringUtils.hasText(query.getIndustry())) {
            wrapper.eq(Supplier::getIndustry, query.getIndustry());
        }
        if (StringUtils.hasText(query.getGreenLevel())) {
            wrapper.eq(Supplier::getGreenLevel, query.getGreenLevel());
        }
        wrapper.orderByDesc(Supplier::getCreateTime);

        IPage<Supplier> entityPage = baseMapper.selectPage(pageParam, wrapper);

        List<SupplierVO> voList = entityPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        Page<SupplierVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /* ==================== 详情 ==================== */
    @Override
    public SupplierDetailVO getDetailById(Integer id) {
        Supplier supplier = baseMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ResultCode.SUPPLIER_NOT_FOUND);
        }

        SupplierDetailVO vo = new SupplierDetailVO();
        BeanUtils.copyProperties(supplier, vo);

        // 近6个月审核通过的记录
        String startMonth = LocalDate.now().minusMonths(6).toString().substring(0, 7);
        List<CarbonRecord> records = carbonRecordMapper.selectList(
                new LambdaQueryWrapper<CarbonRecord>()
                        .eq(CarbonRecord::getSupplierId, id)
                        .eq(CarbonRecord::getStatus, 2)
                        .ge(CarbonRecord::getReportMonth, startMonth)
                        .orderByDesc(CarbonRecord::getReportMonth));

        vo.setRecords(records.stream().map(r -> {
            SupplierRecordItemVO item = new SupplierRecordItemVO();
            BeanUtils.copyProperties(r, item);
            return item;
        }).collect(Collectors.toList()));

        return vo;
    }

    /* ==================== 新增 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SupplierSaveDTO dto) {
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Supplier>().eq(Supplier::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException(ResultCode.SUPPLIER_NAME_EXISTS);
        }
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);//拷贝同名属性
        baseMapper.insert(supplier);
    }

    /* ==================== 修改 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SupplierSaveDTO dto) {
        Supplier old = baseMapper.selectById(dto.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.SUPPLIER_NOT_FOUND);
        }
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Supplier>()
                        .eq(Supplier::getName, dto.getName())
                        .ne(Supplier::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException(ResultCode.SUPPLIER_NOT_FOUND);
        }
        Supplier supplier = new Supplier();
        BeanUtils.copyProperties(dto, supplier);
        baseMapper.updateById(supplier);
    }

    /* ==================== 删除 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id) {
        Supplier supplier = baseMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException(ResultCode.SUPPLIER_NOT_FOUND);
        }
        baseMapper.deleteById(id);
    }

    /* ==================== 绑定碳排记录（更新纪录实体类的供应商id） ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindRecords(SupplierBindDTO dto) {
        Supplier supplier = baseMapper.selectById(dto.getSupplierId());
        if (supplier == null) {
            throw new BusinessException(ResultCode.SUPPLIER_NOT_FOUND);
        }
        for (Integer recordId : dto.getRecordIds()) {
            CarbonRecord record = carbonRecordMapper.selectById(recordId);
            if (record == null) {
                throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
            }
            if (record.getStatus() != 2) {
                throw new BusinessException(ResultCode.RECORD_NOT_APPROVED);
            }
            if (record.getSupplierId() != null && !record.getSupplierId().equals(dto.getSupplierId())) {
                throw new BusinessException(ResultCode.RECORD_ALREADY_BOUND);
            }
            record.setSupplierId(dto.getSupplierId());
            carbonRecordMapper.updateById(record);
        }
    }

    /* ==================== 解绑 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindRecord(Integer recordId) {
        CarbonRecord record = carbonRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.RECORD_NOT_FOUND);
        }
        record.setSupplierId(null);
        carbonRecordMapper.updateById(record);
    }

    /* ==================== 绿色等级计算定时任务==================== */
    @Override
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void recalculateGreenLevels() {
        List<Supplier> suppliers = baseMapper.selectList(null);
        String startMonth = LocalDate.now().minusMonths(6).toString().substring(0, 7);

        for (Supplier supplier : suppliers) {
            List<CarbonRecord> records = carbonRecordMapper.selectList(
                    new LambdaQueryWrapper<CarbonRecord>()
                            .eq(CarbonRecord::getSupplierId, supplier.getId())
                            .eq(CarbonRecord::getStatus, 2)
                            .ge(CarbonRecord::getReportMonth, startMonth));

            if (records.isEmpty()) {
                if (supplier.getGreenLevel() != null || supplier.getAvgEmission() != null) {
                    supplier.setGreenLevel(null);
                    supplier.setAvgEmission(null);
                    baseMapper.updateById(supplier);
                }
                continue; // 无数据不评级
            }

            BigDecimal sum = records.stream()
                    .map(CarbonRecord::getTotalEmission)
                    .reduce(BigDecimal.ZERO, BigDecimal::add); //累加
            BigDecimal avg = sum.divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

            String level;
            if (avg.compareTo(new BigDecimal("100")) < 0) {
                level = "A";
            } else if (avg.compareTo(new BigDecimal("500")) < 0) {
                level = "B";
            } else {
                level = "C";
            }

            supplier.setAvgEmission(avg);
            supplier.setGreenLevel(level);
            baseMapper.updateById(supplier);
        }
        log.info("绿色等级计算完成，共处理 {} 个供应商", suppliers.size());
    }

    /* ==================== 工具 ==================== */
    private SupplierVO toVO(Supplier entity) {
        SupplierVO vo = new SupplierVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}