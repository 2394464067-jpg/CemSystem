package com.ppphui.cemsystem.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ppphui.cemsystem.common.BusinessException;
import com.ppphui.cemsystem.common.ResultCode;
import com.ppphui.cemsystem.dto.CarbonReportExcelVO;
import com.ppphui.cemsystem.dto.CarbonReportQueryDTO;
import com.ppphui.cemsystem.mapper.CarbonRecordMapper;
import com.ppphui.cemsystem.mapper.SupplierMapper;
import com.ppphui.cemsystem.mapper.UserMapper;
import com.ppphui.cemsystem.pojo.CarbonRecord;
import com.ppphui.cemsystem.pojo.Supplier;
import com.ppphui.cemsystem.pojo.User;
import com.ppphui.cemsystem.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CarbonRecordMapper carbonRecordMapper;
    private final SupplierMapper supplierMapper;
    private final UserMapper userMapper;

    @Override
    public void exportCarbonReport(CarbonReportQueryDTO query, HttpServletResponse response) {
        if (!StringUtils.hasText(query.getStartMonth())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        String endMonth = StringUtils.hasText(query.getEndMonth())
                ? query.getEndMonth() : query.getStartMonth();

        LambdaQueryWrapper<CarbonRecord> wrapper = new LambdaQueryWrapper<CarbonRecord>()
                .between(CarbonRecord::getReportMonth, query.getStartMonth(), endMonth)
                .orderByAsc(CarbonRecord::getReportMonth);

        // 默认只导审核通过
        if (query.getStatus() != null) {
            wrapper.eq(CarbonRecord::getStatus, query.getStatus());
        } else {
            wrapper.eq(CarbonRecord::getStatus, 2);
        }
        if (query.getSupplierId() != null) {
            wrapper.eq(CarbonRecord::getSupplierId, query.getSupplierId());
        }

        List<CarbonRecord> records = carbonRecordMapper.selectList(wrapper);

        // 工厂名过滤（内存过滤，数据量大可改成 join）
        if (StringUtils.hasText(query.getFactoryName())) {
            records.removeIf(r -> {
                User u = userMapper.selectById(r.getUserId());
                return u == null || u.getFactoryName() == null
                        || !u.getFactoryName().contains(query.getFactoryName());
            });
        }

        List<CarbonReportExcelVO> list = new ArrayList<>();
        int seq = 1;
        for (CarbonRecord r : records) {
            CarbonReportExcelVO row = new CarbonReportExcelVO();
            row.setSeq(seq++);
            row.setReportMonth(r.getReportMonth());
            row.setEnergyType(r.getEnergyType());
            row.setConsumeAmount(r.getConsumeAmount());
            row.setTotalEmission(r.getTotalEmission());
            row.setStatusText(statusText(r.getStatus()));

            if (r.getUserId() != null) {
                User u = userMapper.selectById(r.getUserId());
                row.setFactoryName(u != null ? u.getFactoryName() : "-");
            }
            if (r.getSupplierId() != null) {
                Supplier s = supplierMapper.selectById(r.getSupplierId());
                row.setSupplierName(s != null ? s.getName() : "-");
            }
            list.add(row);
        }

        // 写响应头
        try {
            String fileName = "碳排明细报表_" + query.getStartMonth()
                    + (endMonth.equals(query.getStartMonth()) ? "" : "_至_" + endMonth) + ".xlsx";
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + encoded);

            EasyExcel.write(response.getOutputStream(), CarbonReportExcelVO.class)
                    .sheet("碳排明细")
                    .doWrite(list);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.FAILED);
        }
    }

    private String statusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "待审核";
            case 2 -> "审核通过";
            case 3 -> "驳回";
            default -> "未知";
        };
    }
}