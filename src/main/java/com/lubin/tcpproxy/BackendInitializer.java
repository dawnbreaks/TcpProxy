
package com.lubin.tcpproxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {

	ProxyFrontendHandler proxyFrondtendHandle;
	public  BackendInitializer(ProxyFrontendHandler proxyFrondtendHandle){
		super();
		this.proxyFrondtendHandle = proxyFrondtendHandle;
	}
	
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new LoggingHandler(TcpProxyServer.getIoLogLevel()),
                new ProxyBackendHandler(proxyFrondtendHandle));
    }
}
