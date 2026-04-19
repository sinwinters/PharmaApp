package com.pharma.application.service;

import com.pharma.application.dto.InventoryReportDto;
import com.pharma.application.dto.WriteOffReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StubReportExportService implements ReportExportService {

    @Override
    public byte[] exportWriteOffToExcel(WriteOffReportDto report) {
        log.info("Stub export write-off report to Excel: {} rows", report.items().size());
        return new byte[0];
    }

    @Override
    public byte[] exportWriteOffToWord(WriteOffReportDto report) {
        log.info("Stub export write-off report to Word: {} rows", report.items().size());
        return new byte[0];
    }

    @Override
    public byte[] exportInventoryToExcel(InventoryReportDto report) {
        log.info("Stub export inventory report to Excel: {} rows", report.items().size());
        return new byte[0];
    }

    @Override
    public byte[] exportInventoryToWord(InventoryReportDto report) {
        log.info("Stub export inventory report to Word: {} rows", report.items().size());
        return new byte[0];
    }
}
