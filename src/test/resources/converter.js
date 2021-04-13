const path = require('path');
const convertCsvToXlsx = require('@aternus/csv-to-xlsx');
var Excel = require('exceljs');
var workbook = new Excel.Workbook();

let source = 'C:\\Users\\YZI2SGH\\Downloads\\APITestCase.csv';
let destination = 'C:\\Users\\YZI2SGH\\Downloads\\APITestCase.xlsx';

try {
  convertCsvToXlsx(source, destination);
} catch (e) {
  console.error(e.toString());
}