package com.pharma.application.report;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ExportServiceImpl implements ExportService {

    @Override
    public byte[] exportSalesToExcel(SalesReportDto report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Sales");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Drug");
            header.createCell(1).setCellValue("Quantity");
            header.createCell(2).setCellValue("Revenue");

            int rowNum = 1;
            for (var item : report.items()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.drugName());
                row.createCell(1).setCellValue(item.totalQuantity());
                row.createCell(2).setCellValue(item.totalRevenue().doubleValue());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации Excel", e);
        }
    }

    @Override
    public byte[] exportOrdersToExcel(OrdersReportDto report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Orders");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Order ID");
            header.createCell(1).setCellValue("Supplier");
            header.createCell(2).setCellValue("Status");

            int rowNum = 1;
            for (var item : report.orders()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.orderId());
                row.createCell(1).setCellValue(item.supplierName());
                row.createCell(2).setCellValue(item.status().name());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации Excel", e);
        }
    }

    @Override
    public byte[] exportWriteOffToExcel(WriteOffReportDto report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("WriteOffs");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Drug");
            header.createCell(1).setCellValue("Reason");
            header.createCell(2).setCellValue("Quantity");

            int rowNum = 1;
            for (var item : report.items()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.drugName());
                row.createCell(1).setCellValue(item.reason().name());
                row.createCell(2).setCellValue(item.quantity());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации Excel", e);
        }
    }

    @Override
    public byte[] exportMinzdravToExcel(MinzdravReportDto report) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Minzdrav");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Category");
            header.createCell(1).setCellValue("Country");
            header.createCell(2).setCellValue("Quantity");
            header.createCell(3).setCellValue("Total");

            int rowNum = 1;
            for (var item : report.items()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.categoryName());
                row.createCell(1).setCellValue(item.country());
                row.createCell(2).setCellValue(item.quantity());
                row.createCell(3).setCellValue(item.totalAmount().doubleValue());
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации Excel", e);
        }
    }

    @Override
    public byte[] exportSalesToWord(SalesReportDto report) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph title = document.createParagraph();
            title.createRun().setText("Sales report");
            XWPFParagraph summary = document.createParagraph();
            summary.createRun().setText("Total revenue: " + report.totalRevenue() + ", total items: " + report.totalItemsSold());
            for (var item : report.items()) {
                XWPFParagraph p = document.createParagraph();
                p.createRun().setText(item.drugName() + " | qty=" + item.totalQuantity() + " | revenue=" + item.totalRevenue());
            }
            document.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Ошибка генерации Word", e);
        }
    }
}
