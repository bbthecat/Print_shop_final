package com.printflow.service;

import com.printflow.dto.response.ReportSummaryResponse;

import java.time.LocalDate;

public interface ReportService {

    /**
     * Summary for the date range (both days included).
     * If a date is null, the report covers the last 30 days up to today.
     */
    ReportSummaryResponse getSummary(LocalDate from, LocalDate to);
}
