package com.app.server.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ExcelExporter {
    public void writeHeaderLine(
            XSSFWorkbook workbook,
            XSSFSheet sheet, List<String> headerLines) {
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        AtomicInteger columnCount = new AtomicInteger();
        headerLines.stream().forEach(a -> {
            createCell(sheet, row, columnCount.getAndIncrement(), a, style);
        });

    }

    public void createCell(XSSFSheet sheet, Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Float) {
            cell.setCellValue((Float) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    public void writeDataLines(XSSFWorkbook workbook, XSSFSheet sheet, List<Object> datas, int rowCount) {

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        Row row = sheet.createRow(rowCount);
        AtomicInteger columnCount = new AtomicInteger();
        datas.forEach(data -> {
            createCell(sheet, row, columnCount.getAndIncrement(), data, style);
        });
    }
}