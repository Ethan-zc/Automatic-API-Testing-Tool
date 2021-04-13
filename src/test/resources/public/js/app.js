var mySpreadsheet;

function selectFile() {
    document.getElementById('file').click();
}

// read local excel file
function readWorkbookFromLocalFile(file, callback) {
    var reader = new FileReader();
    reader.onload = function(e) {
        var data = e.target.result;
        var workbook = XLS.read(data, {type: 'binary'});
        if(callback) callback(workbook);
    };
    reader.readAsBinaryString(file);
}


// 读取 excel文件
function outputWorkbook(workbook) {
    // the set for workbooks name
    var sheetNames = workbook.SheetNames;
    sheetNames.forEach(name => {
        // get the workbook through its name
        var worksheet = workbook.Sheets[name];
        for(var key in worksheet) {
            // v is the original value from the sheet cell
            console.log(key, key[0] === '!' ? worksheet[key] : worksheet[key].v);
        }
    });
}

function readWorkbook(workbook) {
    // the set for workbooks name
    var sheetNames = workbook.SheetNames; 
    // read only the first sheet
    var worksheet = workbook.Sheets[sheetNames[0]];
    var csv = XLS.utils.sheet_to_csv(worksheet);
    // transfer the table to json format. 
    var json = XLS.utils.sheet_to_json(worksheet);
    var longest_idx = findLongestLength(json);
    console.log("This is the longest: " + longest_idx);
    var columnHead = [
        { type: 'text', title:'Test No.', width:100 },
        { type: 'text', title:'Test Name',width:350 },
        { type: 'checkbox', title:'Run Switch',width:100 },
        { type: 'text', title:'address',width:350 },
        { type: 'dropdown', title:'Request Type', width:100, source: ['post','put','delete','get'] },
        { type: 'text', title:'Check Point', width:200 },
        { type: 'text', title:'Acceptance Criteria', width:200 }
    ]
    var longest_row = json[longest_idx];
    var count = 0;
    for (var valueName in longest_row) {
        if (count >= 7) {
            columnHead.push({
                type: 'text', title: valueName, width: 150
            })
        }
        count += 1;
    }
    //document.getElementById('result').innerHTML = json2table(json);
    //json2table(json);
    mySpreadsheet = jspreadsheet(document.getElementById('spreadsheet1'), {
        data: json,
        columns: columnHead,
    })
}

function findLongestLength(json) {
    var longest_idx = 0;
    var longest = 0;
    var temp = 0;
    json.forEach(function(row,idx) {
        temp = 0;
        for (var valueName in row) {
            temp += 1;
        }
        if (temp > longest) {
            longest = temp;
            longest_idx = idx;
        }              
    })
    return longest_idx;
}

function json2table(json) {
    var html = '<table class="table table-hover">';
    // read through the table and find the longest json to create head

    json.forEach(function(row, idx) {
        //console.log(row);
        if (idx == 0) {
            html += '<tr>';
            for(var valueName in row) {
                html += '<th>' + valueName + '</th>';
            }
            html += '</tr>';
        }
        else {
            html += '<tr>';
            for (var value of Object.values(row)){
                html += '<td>' + value + '</td>';
            } 
            html += '</tr>';
        }
    })
    html += '</table>';
    return html;
}

function table2csv(table) {
    var csv = [];
    $(table).find('tr').each(function() {
        var temp = [];	
        $(this).find('td').each(function() {
            temp.push($(this).html());
        })
        //delete the first one 
        console.log(temp);
        csv.push(temp.join(','));
    });
    csv.shift();
    return csv.join('\n');
}

// csv转sheet对象
function csv2sheet(csv) {
    var sheet = {}; // 将要生成的sheet
    //console.log(csv);
    csv = csv.split('\n');
    csv.forEach(function(row, i) {
        row = row.split(',');
        if(i == 0) sheet['!ref'] = 'A1:'+String.fromCharCode(65+row.length-1)+(csv.length-1);
        row.forEach(function(col, j) {
            sheet[String.fromCharCode(65+j)+(i+1)] = {v: col};
        });
    });
    return sheet;
}

// 将一个sheet转成最终的excel文件的blob对象，然后利用URL.createObjectURL下载
function sheet2blob(sheet, sheetName) {
    sheetName = sheetName || 'sheet1';
    var workbook = {
        SheetNames: [sheetName],
        Sheets: {}
    };
    workbook.Sheets[sheetName] = sheet;
    // 生成excel的配置项
    var wopts = {
        bookType: 'xlsx', // 要生成的文件类型
        bookSST: false, 
        type: 'binary'
    };
    var wbout = XLSX.write(workbook, wopts);
    var blob = new Blob([s2ab(wbout)], {type:"application/octet-stream"});
    // 字符串转ArrayBuffer
    function s2ab(s) {
        var buf = new ArrayBuffer(s.length);
        var view = new Uint8Array(buf);
        for (var i=0; i!=s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
        return buf;
    }
    return blob;
}

/**
* 通用的打开下载对话框方法，没有测试过具体兼容性
* @param url 下载地址，也可以是一个blob对象，必选
* @param saveName 保存文件名，可选
*/
function openDownloadDialog(url, saveName)
{
    if(typeof url == 'object' && url instanceof Blob)
    {
        url = URL.createObjectURL(url); // 创建blob地址
    }
    var aLink = document.createElement('a');
    aLink.href = url;
    aLink.download = saveName || ''; // HTML5新增的属性，指定保存文件名，可以不要后缀，注意，file:///模式下不会生效
    var event;
    if(window.MouseEvent) event = new MouseEvent('click');
    else
    {
        event = document.createEvent('MouseEvents');
        event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    }
    aLink.dispatchEvent(event);
}

$(function() {
    document.getElementById('file').addEventListener('change', function(e) {
        var files = e.target.files;
        if(files.length == 0) return;
        var f = files[0];
        //if(!/\.xls$/g.test(f.name)) {
        //    alert('仅支持读取xls格式！');
        //    return;
        //}
        readWorkbookFromLocalFile(f, function(workbook) {
            readWorkbook(workbook);
        });
    });
});

function exportExcel() {
    var csv = table2csv($('#spreadsheet1 table')[0]);
    var sheet = csv2sheet(csv);
    var blob = sheet2blob(sheet);
    openDownloadDialog(blob, '导出.xlsx');
}

function getJsonLength(json) {
    var jsonLength = 0;
    for (var i in json) {
        jsonLength++;
    }
    return jsonLength;
}

// function runShellFile() {
//     const { spawn } = require('child_process');
//     const bat = spawn('cmd.exe', ['/c', 'testing.bat']);

//     bat.stdout.on('data', (data) => {
//         console.log(data.toString());
//     });

//     bat.stderr.on('data', (data) => {
//         console.error(data.toString());
//     });

//     bat.on('exit', (code) => {
//         console.log(`子进程退出，退出码 ${code}`);
//     });
// }