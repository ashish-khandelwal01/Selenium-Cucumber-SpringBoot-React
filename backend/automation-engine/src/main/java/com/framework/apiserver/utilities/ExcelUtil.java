package com.framework.apiserver.utilities;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ExcelUtil is a utility class for reading data from Excel files.
 * It provides methods to retrieve specific cell data and test case-related data from Excel sheets.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see FileInputStream
 * @see XSSFWorkbook
 * @see XSSFSheet
 * @see XSSFRow
 * @see XSSFCell
 * @see DataFormatter
 * @see HashMap
 * @see Iterator
 */
public class ExcelUtil extends BaseClass {

    /**
     * FileInputStream object to read the Excel file.
     */
    public static FileInputStream fileInputStream;

    /**
     * XSSFWorkbook object to represent the entire Excel workbook.
     */
    public static XSSFWorkbook workbook;

    /**
     * XSSFSheet object to represent a specific sheet in the workbook.
     */
    public static XSSFSheet sheet;

    /**
     * XSSFRow object to represent a specific row in the sheet.
     */
    public static XSSFRow row;

    /**
     * XSSFCell object to represent a specific cell in the row.
     */
    public static XSSFCell cell;

    /**
     * Retrieves the data from a specific cell in an Excel sheet.
     *
     * @param xlFile The path to the Excel file.
     * @param sheetName The name of the sheet to read from.
     * @param rowNum The row number of the cell (0-based index).
     * @param colNum The column number of the cell (0-based index).
     * @return The data from the specified cell as a String.
     */
    public static String getCellData(String xlFile, String sheetName, int rowNum, int colNum) {
        String cellData = "";
        try {
            fileInputStream = new FileInputStream(xlFile);
            workbook = new XSSFWorkbook(fileInputStream);
            sheet = workbook.getSheet(sheetName);
        } catch (Exception e) {
            System.out.println("Error opening Excel file: " + e.getMessage());
        }
        try {
            row = sheet.getRow(rowNum);
            cell = row.getCell(colNum);
            cellData = new DataFormatter().formatCellValue(cell);
        } catch (Exception e) {
            System.out.println("Error reading cell data: " + e.getMessage());
        }
        return cellData;
    }

    /**
     * Retrieves test case-related data from an Excel sheet as a key-value pair.
     *
     * @param xlPath The path to the Excel file.
     * @param sheetName The name of the sheet to read from.
     * @param testCaseName The name of the test case to retrieve data for.
     * @return A HashMap containing the test case data as key-value pairs.
     */
    public static HashMap<String, String> getData(String xlPath, String sheetName, String testCaseName) {
        HashMap<String, String> data = new HashMap<>();
        try {
            fileInputStream = new FileInputStream(xlPath);
            workbook = new XSSFWorkbook(fileInputStream);
            sheet = workbook.getSheet(sheetName);
            Iterator<Row> rowIterator = sheet.iterator();
            Row firstRow = rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (row.getCell(0).getStringCellValue().equalsIgnoreCase(testCaseName)) {
                    int i = 0;
                    for (int row_num = 1; row_num < row.getLastCellNum(); row_num++) {
                        Cell cell = row.getCell(row_num, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell == null) {
                            data.put(firstRow.getCell(i).getStringCellValue(), "");
                        } else if (cell.getCellType().equals(CellType.NUMERIC)) {
                            data.put(firstRow.getCell(i).getStringCellValue(), String.valueOf(cell.getNumericCellValue()));
                        } else {
                            data.put(firstRow.getCell(i).getStringCellValue(), cell.getStringCellValue());
                        }
                        i++;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Error reading Excel data: " + e.getMessage());
        } finally {
            try {
                workbook.close();
                fileInputStream.close();
            } catch (Exception e) {
                System.out.println("Error closing Excel file: " + e.getMessage());
            }
        }
        return data;
    }
}