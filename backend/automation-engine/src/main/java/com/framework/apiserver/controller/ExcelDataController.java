package com.framework.apiserver.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*")
public class ExcelDataController {

    @Value("${dataprovider.file-path}")
    private String dataSheetPath;

    /**
     * Writes data to the specified Excel sheet.
     *
     * @param sheetName the name of the sheet to write to
     * @param rows the data to write, as a list of rows
     * @throws IOException if the file cannot be written
     */
    @Operation(
        summary = "Write data to Excel sheet",
        description = "Writes the provided rows to the specified sheet in the Excel file."
    )
    @PostMapping("/excel/{sheetName}")
    public void writeExcel(@PathVariable String sheetName, @RequestBody List<List<String>> rows) throws IOException {
        try (FileInputStream fis = new FileInputStream(dataSheetPath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            int rowIndex = 0;
            for (List<String> rowData : rows) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                int cellIndex = 0;
                for (String value : rowData) {
                    Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellValue(value);
                    cellIndex++;
                }
                rowIndex++;
            }
            try (FileOutputStream fos = new FileOutputStream(dataSheetPath)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Reads all data from the specified Excel sheet.
     *
     * @param sheetName the name of the sheet to read from
     * @return a list of rows, each row is a list of cell values
     * @throws IOException if the file cannot be read
     */
    @Operation(
        summary = "Read data from Excel sheet",
        description = "Returns all rows and cells from the specified sheet in the Excel file."
    )
    @GetMapping("/excel/{sheetName}")
    public List<List<String>> readExcel(@PathVariable String sheetName) throws IOException {
        try (FileInputStream fis = new FileInputStream(dataSheetPath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            List<List<String>> data = new ArrayList<>();
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(cell.toString());
                }
                data.add(rowData);
            }
            return data;
        }
    }

    /**
     * Lists all sheet names in the Excel file.
     *
     * @return a list of sheet names
     * @throws IOException if the file cannot be read
     */
    @Operation(
        summary = "List Excel sheet names",
        description = "Returns a list of all sheet names in the Excel file."
    )
    @GetMapping("/excel/sheets")
    public List<String> listSheets() throws IOException {
        try (FileInputStream fis = new FileInputStream(dataSheetPath);
             Workbook workbook = new XSSFWorkbook(fis)) {
            List<String> sheetNames = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            return sheetNames;
        }
    }


}
