#!/bin/bash

currentDir=`readlink -m  $(dirname $0)`
targetDir=$currentDir/../target
jarFileName="TcpProxy-0.0.1-SNAPSHOT.jar"

pid=`ps -ef|grep java|grep TcpProxyServer|awk '{print $2}'`
if [ "x$pid" != "x" ];
then
    echo  "Killing service process." 
    kill  -9 $pid
    sleep  4
fi


if [ -f $targetDir/$jarFileName ];
then
  rm -f $currentDir/$jarFileName
  cp -f $targetDir/$jarFileName $currentDir
fi

echo "Starting service....."
nohup java  -server -XX:PermSize=24M -XX:MaxPermSize=64m -Xms128m -Xmx448m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:NewRatio=3 -cp $currentDir/dependency/*:$currentDir/*:$currentDir/config   com.lubin.tcpproxy.TcpProxyServer  2>&1 >> $currentDir/tcpProxy.log  &
echo "Done....."
