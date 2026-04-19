package com.pharma.application.service;

import com.pharma.application.dto.InventoryReportDto;
import com.pharma.application.dto.WriteOffReportDto;

public interface ReportExportService {

    byte[] exportWriteOffToExcel(WriteOffReportDto report);

    byte[] exportWriteOffToWord(WriteOffReportDto report);

    byte[] exportInventoryToExcel(InventoryReportDto report);

    byte[] exportInventoryToWord(InventoryReportDto report);
}
