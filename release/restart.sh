#!/bin/bash

currentDir=`readlink -m  $(dirname $0)`

pid=`ps -ef|grep java|grep TcpProxyServer|awk '{print $2}'`
if [ "x$pid" != "x" ];
then
    echo  "Killing service process." 
    kill  -9 $pid
    sleep  3
fi

$currentDir/tcpProxy.sh
