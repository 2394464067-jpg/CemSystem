package com.ppphui.cemsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ppphui.cemsystem.dto.*;
import com.ppphui.cemsystem.mapper.CarbonRecordMapper;
import com.ppphui.cemsystem.mapper.SupplierMapper;
import com.ppphui.cemsystem.mapper.UserMapper;
import com.ppphui.cemsystem.pojo.CarbonRecord;
import com.ppphui.cemsystem.pojo.Supplier;
import com.ppphui.cemsystem.pojo.User;
import com.ppphui.cemsystem.service.CarbonRecordService;
import com.ppphui.cemsystem.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CarbonRecordMapper carbonRecordMapper;
    private final SupplierMapper supplierMapper;
    private final UserMapper userMapper;
    private final CarbonRecordService carbonRecordService;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 1. 近12月趋势（复用模块三方法）
        List<CarbonRecordStatVO> trend = carbonRecordService.monthlyTrend(12);
        vo.setMonthlyTrend(trend);

        // 2. 总排放（全部审核通过）
        List<CarbonRecord> allPassed = carbonRecordMapper.selectList(
                new LambdaQueryWrapper<CarbonRecord>().eq(CarbonRecord::getStatus, 2));
        BigDecimal total = allPassed.stream()
                .map(r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalEmission(total);

        // 3. 本月排放
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        BigDecimal current = allPassed.stream()
                .filter(r -> currentMonth.equals(r.getReportMonth()))
                .map(r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setCurrentMonthEmission(current);

        // 4. 环比
        String lastMonth = LocalDate.now().minusMonths(1).toString().substring(0, 7);
        BigDecimal last = allPassed.stream()
                .filter(r -> lastMonth.equals(r.getReportMonth()))
                .map(r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (last.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = current.subtract(last)
                    .divide(last, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            vo.setMonthOverMonthRate(rate);
        } else {
            vo.setMonthOverMonthRate(BigDecimal.ZERO);
        }

        // 5. 供应商总数
        vo.setSupplierCount(supplierMapper.selectCount(null));

        // 6. 待审核数
        vo.setPendingCount(carbonRecordMapper.selectCount(
                new LambdaQueryWrapper<CarbonRecord>().eq(CarbonRecord::getStatus, 1)));

        // 7. 各工厂排放占比
        Map<Integer, BigDecimal> factoryMap = allPassed.stream()
                .filter(r -> r.getUserId() != null)
                .collect(Collectors.groupingBy(
                        CarbonRecord::getUserId,
                        Collectors.reducing(BigDecimal.ZERO,
                                r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission(),
                                BigDecimal::add)));
        List<PieItemVO> factoryPie = new ArrayList<>();
        factoryMap.forEach((userId, val) -> {
            User u = userMapper.selectById(userId);
            String name = (u != null && u.getFactoryName() != null) ? u.getFactoryName() : "用户" + userId;
            factoryPie.add(new PieItemVO(name, val));
        });
        vo.setFactoryPie(factoryPie);

        // 8. 各供应商排放占比
        Map<Integer, BigDecimal> supplierMap = allPassed.stream()
                .filter(r -> r.getSupplierId() != null)
                .collect(Collectors.groupingBy(
                        CarbonRecord::getSupplierId,
                        Collectors.reducing(BigDecimal.ZERO,
                                r -> r.getTotalEmission() == null ? BigDecimal.ZERO : r.getTotalEmission(),
                                BigDecimal::add)));
        List<PieItemVO> supplierPie = new ArrayList<>();
        supplierMap.forEach((sid, val) -> {
            Supplier s = supplierMapper.selectById(sid);
            supplierPie.add(new PieItemVO(s != null ? s.getName() : "供应商" + sid, val));
        });
        vo.setSupplierPie(supplierPie);

        return vo;
    }
}