package com.pharma.application.report;

public interface ExportService {

    byte[] exportSalesToExcel(SalesReportDto report);

    byte[] exportOrdersToExcel(OrdersReportDto report);

    byte[] exportWriteOffToExcel(WriteOffReportDto report);

    byte[] exportMinzdravToExcel(MinzdravReportDto report);

    byte[] exportSalesToWord(SalesReportDto report);
}
