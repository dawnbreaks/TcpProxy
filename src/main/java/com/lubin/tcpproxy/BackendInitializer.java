
package com.lubin.tcpproxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class BackendInitializer extends ChannelInitializer<SocketChannel> {

    ProxyFrontendHandler proxyFrondtendHandle;
    public  BackendInitializer(ProxyFrontendHandler proxyFrondtendHandle){
        super();
        this.proxyFrondtendHandle = proxyFrondtendHandle;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {

        if(TcpProxyServer.isDebug()){
            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
        }
        ch.pipeline().addLast(new ProxyBackendHandler(proxyFrondtendHandle));

    }
}
