let id=1;
const exec = require('child_process').execSync
app.post("/api/addorginfo", function (req, res) {
    console.info('模拟用户调用shell脚本')
    //自增值
    var username=req.body.username;
    var channelname=username+'channel'
    id++
    // 执行，test.sh脚本
    exec('bash test.sh '+id+'' )
}