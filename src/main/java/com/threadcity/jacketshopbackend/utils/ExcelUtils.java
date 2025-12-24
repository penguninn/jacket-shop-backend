package com.threadcity.jacketshopbackend.utils;

import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class ExcelUtils {

    public static <T> List<T> readExcel(MultipartFile file, BiFunction<Row, Integer, T> rowMapper) throws IOException {
        List<T> result = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowNumber = 0;
            for (Row row : sheet) {
                
                if (isRowEmpty(row)) {
                    continue;
                }

                T item = rowMapper.apply(row, rowNumber);
                if (item != null) {
                    result.add(item);
                }
                rowNumber++;
            }
        }
        return result;
    }

    public static String getCellValueAsString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    // Convert numeric to string, removing ".0" for integers if applicable
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        yield String.format("%d", (long) val);
                    }
                    yield String.valueOf(val);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula(); // Or evaluate formula
            default -> "";
        };
    }
    
    public static Double getCellValueAsDouble(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> Double.parseDouble(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static Integer getCellValueAsInt(Row row, int cellIndex) {
        Double val = getCellValueAsDouble(row, cellIndex);
        return val != null ? val.intValue() : null;
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(row, c).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
