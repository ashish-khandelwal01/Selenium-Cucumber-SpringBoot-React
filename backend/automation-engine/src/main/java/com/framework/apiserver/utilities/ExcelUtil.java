package com.framework.apiserver.utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ExcelUtil is a Spring-managed bean for reading Excel files.
 * It provides methods to read specific cell data or entire rows as key-value pairs.
 * Extends BaseClass to utilize logging functionality.
 * <p>
 * @see FileInputStream
 * @see XSSFWorkbook
 * @see DataFormatter
 * @see HashMap
 * @see Iterator
 * <p>
 * <p>Author: ashish-khandelwal01</p>
 */
@Component
public class ExcelUtil {

    @Autowired
    private BaseClass baseClass;

    /**
     * Reads the content of a specific cell from an Excel file.
     *
     * @param xlFile     Path to the Excel file.
     * @param sheetName  Name of the sheet.
     * @param rowNum     Zero-based row index.
     * @param colNum     Zero-based column index.
     * @return Cell value as String.
     */
    public String getCellData(String xlFile, String sheetName, int rowNum, int colNum) {
        try (FileInputStream fis = new FileInputStream(xlFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            Row row = sheet.getRow(rowNum);
            Cell cell = row.getCell(colNum);
            return new DataFormatter().formatCellValue(cell);
        } catch (Exception e) {
            baseClass.failLog("Error reading cell data: " + e.getMessage());
            return "";
        }
    }

    /**
     * Retrieves data from a row identified by a test case name as a map.
     *
     * @param xlFile        Path to the Excel file.
     * @param sheetName     Name of the sheet.
     * @param testCaseName  Name in the first column to match.
     * @return Key-value pairs of test data.
     */
    public HashMap<String, String> getData(String xlFile, String sheetName, String testCaseName) {
        HashMap<String, String> data = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(xlFile);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getCell(0).getStringCellValue().equalsIgnoreCase(testCaseName)) {
                    for (int i = 1; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        String key = headerRow.getCell(i).getStringCellValue();
                        if (cell == null) {
                            data.put(key, "");
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            data.put(key, String.valueOf(cell.getNumericCellValue()));
                        } else {
                            data.put(key, cell.getStringCellValue());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            baseClass.failLog("Error reading Excel data: " + e.getMessage());
        }
        return data;
    }
}
