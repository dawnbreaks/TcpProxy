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
		 	}]
	soBacklog =1000
	connectTimeoutMillis = 15000
	soTimeout = 15000
	ioThreadNum = 16
	debug = false
}
```
  

========
Please feel free to contact me(2005dawnbreaks@gmail.com) if you have any questions.
