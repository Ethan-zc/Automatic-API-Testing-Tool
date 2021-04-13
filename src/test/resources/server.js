var express = require('express');
var app = express();
const child_process = require('child_process');

app.use(express.static(__dirname + '\\public'));

app.get('/index.html', function (req, res) {
   res.sendFile( __dirname + "/" + "index.html" );
})

app.get('/execute', function(req,res) {
    
  child_process.execFile("resource_control.bat",null,{cwd:'C:\\Users\\YZI2SGH\\Desktop\\DCHR\\AutomaticTestingTool'},function(error,stdout,stderr){
    console.log("Starting the testing...");
    if(error !== null){
      console.log("exec error"+error)
    }
    else console.log("成功")
    console.log('stdout: ' + stdout);
    console.log('stderr: ' + stderr);
  })
    res.send(200);
})


var server = app.listen(8081, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("The address is: http://%s:%s", host, port)

})
