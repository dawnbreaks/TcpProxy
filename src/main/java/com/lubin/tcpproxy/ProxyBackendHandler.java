
package com.lubin.tcpproxy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class ProxyBackendHandler extends ChannelInboundHandlerAdapter {

	 private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProxyBackendHandler.class);

	private ProxyFrontendHandler proxyFrondtendHandle;

    public ProxyBackendHandler(ProxyFrontendHandler proxyFrondtendHandle) {
        this.proxyFrondtendHandle=proxyFrondtendHandle;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	logger.info("ProxyBackendHandler|channelInactive");
    	proxyFrondtendHandle.outBoundChannelReady();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    	proxyFrondtendHandle.getInboundChannel().writeAndFlush(msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	logger.info("ProxyBackendHandler|channelInactive");
    	proxyFrondtendHandle.close();
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("ProxyBackendHandler|channelUnregistered");
        proxyFrondtendHandle.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	logger.info("ProxyBackendHandler|exceptionCaught|remoteAddress="+ctx.channel().remoteAddress(), cause);
        proxyFrondtendHandle.close();
    }
}
