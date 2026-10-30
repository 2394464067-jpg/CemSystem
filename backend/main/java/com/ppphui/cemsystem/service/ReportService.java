package com.ppphui.cemsystem.service;

import com.ppphui.cemsystem.dto.CarbonReportQueryDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface ReportService {
    void exportCarbonReport(CarbonReportQueryDTO query, HttpServletResponse response);
}