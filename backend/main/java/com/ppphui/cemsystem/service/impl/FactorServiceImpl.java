package com.ppphui.cemsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ppphui.cemsystem.common.BusinessException;
import com.ppphui.cemsystem.common.ResultCode;
import com.ppphui.cemsystem.dto.FactorSaveDTO;
import com.ppphui.cemsystem.mapper.FactorMapper;
import com.ppphui.cemsystem.pojo.Factor;
import com.ppphui.cemsystem.service.FactorService;
import com.ppphui.cemsystem.dto.FactorVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactorServiceImpl implements FactorService {

    private final FactorMapper factorMapper;

    /* ==================== 分页查询 ==================== */
    @Override
    public IPage<FactorVO> page(Integer page, Integer size, String energyName) {
        // 1. 分页查询
        Page<Factor> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Factor> wrapper = new LambdaQueryWrapper<>();

        // 2.能源类型模糊搜索
        if (StringUtils.hasText(energyName)) {
            wrapper.like(Factor::getEnergyName, energyName);
        }
        wrapper.orderByDesc(Factor::getCreateTime);

        IPage<Factor> entityPage = factorMapper.selectPage(pageParam, wrapper);

        // 3. 把 IPage.records 转成 vo列表
        List<FactorVO> voList = entityPage.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList());

        // 4. 返回 VO 分页
        Page<FactorVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    /* ==================== 新增 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(FactorSaveDTO dto) {
        // 1. 因子值必须为正数
        if (dto.getFactorValue() == null || dto.getFactorValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.FACTOR_VALUE_INVALID);
        }

        // 2. 能源类型唯一
        Long count = factorMapper.selectCount(
                new LambdaQueryWrapper<Factor>()
                        .eq(Factor::getEnergyName, dto.getEnergyName()));
        if (count > 0) {
            throw new BusinessException(ResultCode.FACTOR_NAME_EXISTS);
        }

        // 3. 组装实体
        Factor factor = new Factor();
        BeanUtils.copyProperties(dto, factor);
        factor.setId(null);   // 新增必须清空 id

        // 4. 入库
        factorMapper.insert(factor);
    }

    /* ==================== 修改 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(FactorSaveDTO dto) {
        // 1. 存在性校验
        Factor old = factorMapper.selectById(dto.getId());
        if (old == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }

        // 2. 因子值必须为正数
        if (dto.getFactorValue() == null || dto.getFactorValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.FACTOR_VALUE_INVALID);
        }

        // 3. 能源类型唯一（排除自己）
        Long count = factorMapper.selectCount(
                new LambdaQueryWrapper<Factor>()
                        .eq(Factor::getEnergyName, dto.getEnergyName())
                        .ne(Factor::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException(ResultCode.FACTOR_NAME_EXISTS);
        }

        // 4. 组装并更新
        Factor factor = new Factor();
        BeanUtils.copyProperties(dto, factor);
        factorMapper.updateById(factor);

        log.info("修改排放因子：id={}", dto.getId());
    }

    /* ==================== 删除 ==================== */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Factor factor = factorMapper.selectById(id);
        if (factor == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }

        // 逻辑删除：MP 会自动改成 UPDATE ... SET deleted=1
        factorMapper.deleteById(id);
        log.info("删除排放因子：id={}", id);
    }

    /* ==================== 根据 id 查询 ==================== */
    @Override
    public FactorVO getById(Long id) {
        Factor factor = factorMapper.selectById(id);
        if (factor == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }
        return toVO(factor);
    }

    /* ==================== 根据因子名称查询==================== */
    @Override
    public FactorVO getByEnergyName(String energyName) {
        if (!StringUtils.hasText(energyName)) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }

        Factor factor = factorMapper.selectOne(
                new LambdaQueryWrapper<Factor>()
                        .eq(Factor::getEnergyName, energyName));

        if (factor == null) {
            throw new BusinessException(ResultCode.FACTOR_NOT_FOUND);
        }
        return toVO(factor);
    }
    /* ==================== 工具：entity → vo ==================== */
    private FactorVO toVO(Factor entity) {
        FactorVO vo = new FactorVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
