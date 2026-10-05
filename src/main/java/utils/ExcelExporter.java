package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExporter {

    public static boolean exportToExcel(JTable table, String filePath, String sheetTitle) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetTitle);
            
            // Create fonts
            Font titleFont = workbook.createFont();
            titleFont.setFontName("Arial");
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setBold(true);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());

            Font headerFont = workbook.createFont();
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font dataFont = workbook.createFont();
            dataFont.setFontName("Arial");
            dataFont.setFontHeightInPoints((short) 10);

            // Create styles
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            
            TableModel model = table.getModel();
            int rowCount = model.getRowCount();
            int colCount = model.getColumnCount();
            
            // 1. Add Title Block
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(sheetTitle);
            titleCell.setCellStyle(titleStyle);
            
            // 2. Add Empty Row
            sheet.createRow(1);
            
            // 3. Add Headers
            Row headerRow = sheet.createRow(2);
            for (int col = 0; col < colCount; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(model.getColumnName(col));
                cell.setCellStyle(headerStyle);
            }
            
            // 4. Add Data Rows
            for (int row = 0; row < rowCount; row++) {
                Row excelRow = sheet.createRow(row + 3);
                for (int col = 0; col < colCount; col++) {
                    Cell cell = excelRow.createCell(col);
                    Object val = model.getValueAt(row, col);
                    if (val != null) {
                        cell.setCellValue(val.toString());
                    } else {
                        cell.setCellValue("");
                    }
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // 5. Auto size columns
            for (int col = 0; col < colCount; col++) {
                sheet.autoSizeColumn(col);
                // Set slightly wider than auto-size
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, currentWidth + 1200);
            }
            
            // 6. Save file
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting to Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
