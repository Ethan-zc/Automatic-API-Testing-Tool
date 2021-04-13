package automatic.testing.tool.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.testng.Reporter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ExcelProcess {

    private static Properties props;
    private static XSSFSheet excelSheet;
    private static Object[][] excelData;
    private static String excelPath;

    public static Object[][] processExcel(String filePath, int sheetId) throws IOException {

        try {
            props = new Properties();
            FileInputStream file = new FileInputStream(System.getProperty("user.dir") +
                    "\\src\\test\\resources\\config.properties");
            props.load(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        excelPath = props.getProperty("testData");

        // Read excel by data flow
        File file = new File(System.getProperty("user.dir")+filePath);
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        DataFormatter df = new DataFormatter();

        // Read certain sheet and count number of rows and cells
        excelSheet = wb.getSheetAt(sheetId);
        int numberOfRow = excelSheet.getPhysicalNumberOfRows();
        int numberOfCell = excelSheet.getRow(0).getLastCellNum();

        // Save sheet data into dtt object
        Object[][] dttData = new Object[numberOfRow][numberOfCell];
        for (int i = 0; i < numberOfRow; i++) {
            if (excelSheet.getRow(i) == null || "".equals(excelSheet.getRow(i).toString())) {
                continue;
            }

            for (int j = 0; j < numberOfCell; j++) {
                if ((excelSheet.getRow(i).getCell(j) == null) || excelSheet.getRow(i).getCell(j).toString().equals("")) {
                    continue;
                }
                XSSFCell cell = excelSheet.getRow(i).getCell(j);
                String cellInString = df.formatCellValue(cell);
                dttData[i][j] = cellInString;
            }
        }

        excelData = dttData;
        return dttData;

    }

    // Read all parameters for one single testing case.
    public static Object[][] readParameters(List<Integer> indexList) {
        Object[][] RequestInfo = new Object[ indexList.size() ][4];
        int InfoIndex = 0;
        for (Integer index:indexList) {
            // address
            RequestInfo[InfoIndex][0] = excelData[index][3].toString();
            // checkPoint
            RequestInfo[InfoIndex][1] = excelData[index][5].toString();
            // Acceptance Criteria
            RequestInfo[InfoIndex][2] = excelData[index][6].toString();
            // rowNum
            RequestInfo[InfoIndex][3] = index+"";
            InfoIndex = InfoIndex + 1;
        }

        return RequestInfo;
    }

    // Read key name and key value pairs and return them as a String
    public static String readKeyPairs(Integer row) {
        JSONObject keys = new JSONObject();
        String singleNum = null;

        for (int cell = 7; cell <= excelData[row].length-2; cell = cell+2) {
            // Not sure about the # of parameters, therefore doing not null judgement here.
            if ( excelData[row][cell] == null && excelData[row][cell+1] == null) {
                break;
            }
            if (excelData[row][cell+1] == null) {
                keys.put( excelData[row][cell].toString(),"" );
            } else {

                if (excelData[row][cell+1].toString().contains("[")) {
                    // Separate [] from the input Value
                    String inputValue = excelData[row][cell+1].toString().substring(1,
                            excelData[row][cell+1].toString().length() - 1);
                    System.out.println("This is input value: " + inputValue);
                    ArrayList<Object> finalInputValue = new ArrayList<>();
                    String[] singleJSON = inputValue.split(", ");
                    for (String s : singleJSON) {
                        JSONObject jsonData = JSONObject.parseObject(s);
                        finalInputValue.add(jsonData);
                    }
                    keys.put( excelData[row][cell].toString(), finalInputValue);
                } else if (excelData[row][cell+1].toString().contains("{")){
                    JSONObject jsonData = JSONObject.parseObject(excelData[row][cell+1].toString() );
                    keys.put( excelData[row][cell].toString(), jsonData);
                } else if (excelData[row][cell+1].toString().contains( "(single)") ){
                    singleNum = excelData[row][cell+1].toString().substring(excelData[row][cell+1].toString().indexOf(')')+1);
                }
                else {
                    keys.put( excelData[row][cell].toString(), excelData[row][cell+1] );
                }
            }
        }

        // send request through client and analyze response
        if (singleNum != null) {
            Reporter.log( "Input is: " + singleNum );
            return singleNum;
        } else {
            Reporter.log( "Input is: " + keys);
            return keys.toString();
        }
    }

    public static void copyRow(XSSFWorkbook workbook, XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {
        // Get the source / new row
        XSSFRow newRow = worksheet.getRow(destinationRowNum);
        XSSFRow sourceRow = worksheet.getRow(sourceRowNum);

        // If the row exist in destination, push down all rows by 1 else create a new row
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            XSSFCell oldCell = sourceRow.getCell(i);
            XSSFCell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                continue;
            }

            // Copy style from old cell and apply to new cell
            XSSFCellStyle newCellStyle = workbook.createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (oldCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
                case BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
        }

        // If there are are any merged regions in the source row, copy to new row
        for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
            CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
            if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
                CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(),
                        (newRow.getRowNum() +
                                (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()
                                )),
                        cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn());
                worksheet.addMergedRegion(newCellRangeAddress);
            }
        }

        int lastRowNum=worksheet.getPhysicalNumberOfRows();
        if( sourceRowNum >= 0 && sourceRowNum < lastRowNum )
            worksheet.shiftRows( sourceRowNum+1, lastRowNum,-1 );
        if( sourceRowNum == lastRowNum ){
            XSSFRow removingRow = worksheet.getRow( sourceRowNum );
            worksheet.removeRow( removingRow );
        }
    }


    public static void rearrangeCertainRows(List<Integer> rowIndexList) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(System.getProperty("user.dir")+excelPath));
        XSSFSheet sheet = workbook.getSheet("Sheet1");
        int numberOfRow = excelSheet.getPhysicalNumberOfRows();
        System.out.println("The number of rows is equal to: " + numberOfRow);
        int ASC_order = 0;
        for (Integer index: rowIndexList) {
            copyRow(workbook, sheet, index+1, 1+ASC_order);
            ASC_order++;
        }

        FileOutputStream out = new FileOutputStream(System.getProperty("user.dir")+excelPath);
        workbook.write(out);
        out.close();
    }

    public static void turnOffPassedCase(Integer numOfFailedCases) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(
                System.getProperty("user.dir")+excelPath));
        XSSFSheet sheet = workbook.getSheet("Sheet1");
        int numberOfRow = excelSheet.getPhysicalNumberOfRows();
        System.out.println("The number of rows is equal to: " + numberOfRow);
        for (int i = numOfFailedCases+1; i < numberOfRow; i++) {
            XSSFCell cell = sheet.getRow(i).getCell(2);
            cell.setCellValue(0);
        }
        if (numOfFailedCases != 0) {
            sheet.shiftRows(numberOfRow+1, numberOfRow + numOfFailedCases, -1);
        }
        FileOutputStream out = new FileOutputStream(
                System.getProperty("user.dir")+excelPath);
        workbook.write(out);
        out.close();
    }
}