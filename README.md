TcpProxy
========

Yet another tcp proxy server.


Features
========

  * Simple, small code base
  * Very fast, high performance
  * Totally non-blocking IO operations.
  * Multi IO thread 

  
Simple tutorial
========
####1.Edit application.conf in the release folder to add proxy hosts.
```javascript
tcpProxyServer {
	hosts = [{
				localPort = 465
				remoteHost = smtp.gmail.com
				remotePort = 465
			},{
				localPort = 993
				remoteHost = imap.gmail.com
				remotePort = 993
			},{
				localPort = 995
				remoteHost = pop.gmail.com
				remotePort = 995
		 	},{
				localPort = 25
				remoteHost = smtp.qq.com
				remotePort = 25
			 },{
				localPort = 143
				remoteHost = imap.qq.com
				remotePort = 143
			 }]
	so_backlog = 1000
	connect_timeout_millis = 15000
	so_timeout = 15000
	ioThreadNum = 5
	debug = true
}
```
  
####2. startup server...
```shell
./tcpProxy.sh   or  ./tcpProxy.bat
```

Build
========

To build the JAR file of TcpProxy, you need to install Maven (http://maven.apache.org), then type the following command:

    $ mvn package

To generate project files (.project, .classpath) for Eclipse, do

    $ mvn eclipse:eclipse

then import the folder from your Eclipse.


========
Oh, that's all! Easy to understand, right? Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
