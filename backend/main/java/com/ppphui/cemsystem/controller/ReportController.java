package com.ppphui.cemsystem.controller;

import com.ppphui.cemsystem.dto.CarbonReportQueryDTO;
import com.ppphui.cemsystem.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "报表导出相关", description = "报表导出")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "导出碳排明细报表", description = "导出碳排明细报表")
    @GetMapping("/carbon/export")
    public void exportCarbonReport(@ParameterObject CarbonReportQueryDTO query, HttpServletResponse response) {
        reportService.exportCarbonReport(query, response);
    }
}