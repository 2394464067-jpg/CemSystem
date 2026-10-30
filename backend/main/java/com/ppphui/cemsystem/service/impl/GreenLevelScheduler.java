package com.ppphui.cemsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ppphui.cemsystem.mapper.CarbonRecordMapper;
import com.ppphui.cemsystem.mapper.SupplierMapper;
import com.ppphui.cemsystem.pojo.CarbonRecord;
import com.ppphui.cemsystem.pojo.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class GreenLevelScheduler {

    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private CarbonRecordMapper recordMapper;

    // 每月1日凌晨2点执行
    @Scheduled(cron = "0 0 2 1 * ?")
    @Transactional
    public void calculateGreenLevels() {
        List<Supplier> suppliers = supplierMapper.selectList(null);
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        String startMonth = sixMonthsAgo.toString().substring(0, 7); // yyyy-MM

        for (Supplier supplier : suppliers) {
            // 1. 查询该供应商近6个月且状态为“审核通过(2)”的记录
            List<CarbonRecord> records = recordMapper.selectList(
                    new LambdaQueryWrapper<CarbonRecord>()
                            .eq(CarbonRecord::getSupplierId, supplier.getId())
                            .eq(CarbonRecord::getStatus, 2)
                            .ge(CarbonRecord::getReportMonth, startMonth)
            );

            if (records.isEmpty()) {
                continue; // 无数据不评级
            }

            // 2. 计算平均排放量
            BigDecimal sum = records.stream()
                    .map(CarbonRecord::getTotalEmission)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(BigDecimal.valueOf(records.size()), 4, RoundingMode.HALF_UP);

            // 3. 判定等级 (假设阈值: <100为A, <500为B, 否则C)
            String level;
            if (avg.compareTo(new BigDecimal("100")) < 0) {
                level = "A";
            } else if (avg.compareTo(new BigDecimal("500")) < 0) {
                level = "B";
            } else {
                level = "C";
            }

            // 4. 更新
            supplier.setAvgEmission(avg);
            supplier.setGreenLevel(level);
            supplierMapper.updateById(supplier);
        }
    }
}