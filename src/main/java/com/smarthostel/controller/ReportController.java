package com.smarthostel.controller;

import com.smarthostel.exception.DatabaseException;
import com.smarthostel.service.ReportService;

import java.util.Map;

/**
 * Controller mediating dashboard KPI reports and export summaries.
 */
public class ReportController {

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    public Map<String, Object> getMetrics() throws DatabaseException {
        return reportService.getDashboardMetrics();
    }

    public String getSummaryText() throws DatabaseException {
        return reportService.generateSummaryReportText();
    }
}
