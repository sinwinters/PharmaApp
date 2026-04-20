package com.pharma.application.report;

public interface ReportService {

    SalesReportDto getSalesReport(ReportFilterDto filter);

    OrdersReportDto getOrdersReport(ReportFilterDto filter);

    WriteOffReportDto getWriteOffReport(ReportFilterDto filter);

    MinzdravReportDto getMinzdravReport(ReportFilterDto filter);

    ChartDto getSalesChart(ReportFilterDto filter);
}
