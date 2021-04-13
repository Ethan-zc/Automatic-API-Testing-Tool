var runShellFile = function() {
    const { spawn } = require('child_process');
    const bat = spawn('cmd.exe', ['/c', 'testing.bat']);

    bat.stdout.on('data', (data) => {
        console.log(data.toString());
    });

    bat.stderr.on('data', (data) => {
      console.error(data.toString());
    });

    bat.on('exit', (code) => {
        console.log(`子进程退出，退出码 ${code}`);
    });
}
