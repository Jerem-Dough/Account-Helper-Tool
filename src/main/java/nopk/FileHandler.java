package nopk;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class FileHandler {

    public List<String[]> readExcelFile(String filePath) throws IOException {
        List<String[]> records = new ArrayList<>();
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new HSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                rowData.add(cell.toString());
            }
            records.add(rowData.toArray(new String[0]));
        }

        fis.close();
        return records;
    }

    public void writeExcelFile(String filePath, List<String[]> data) throws IOException {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Processed Data");

        int rowIndex = 0;
        for (String[] rowData : data) {
            Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            for (String cellData : rowData) {
                row.createCell(cellIndex++).setCellValue(cellData);
            }
        }

        FileOutputStream fos = new FileOutputStream(filePath);
        workbook.write(fos);
        fos.close();
    }
}
