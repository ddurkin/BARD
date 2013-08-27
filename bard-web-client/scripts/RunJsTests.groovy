#!/usr/bin/env groovy

def ant = new AntBuilder()
ant.mkdir(dir: 'target/test-reports')

//Update the codebase from GitHub
//ant.exec(executable:'./xvfbdscript.sh', dir: "test/jasmine") {
//    arg(value:'start')
//}
ant.exec(executable: '!/bin/bash', dir: 'test/jasmine', failonerror: true){
    arg(value: 'jsTestDriver.sh')
}

//ant.exec(executable:'./xvfbdscript.sh', dir: "test/jasmine") {
//    arg(value:'stop')
//}