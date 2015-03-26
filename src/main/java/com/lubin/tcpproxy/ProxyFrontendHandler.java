
package com.lubin.tcpproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.net.InetSocketAddress;
import java.util.LinkedList;

import com.lubin.tcpproxy.TcpProxyServer.ProxyHost;

public class ProxyFrontendHandler extends ChannelInboundHandlerAdapter {
	
	private static final InternalLogger log = InternalLoggerFactory.getInstance(ProxyFrontendHandler.class);

	private Channel inboundChannel;
    private Channel outboundChannel;
    private LinkedList<Object> inboundMsgBuffer = new LinkedList<Object> ();
    
    enum ConnectionStatus{
        init,
        outBoundChnnlConnecting,      //inbound connected and outbound connecting  
        outBoundChnnlReady,           //inbound connected and outbound connected    
        closing                       //closing inbound and outbound connection
    }
    
    private ConnectionStatus connectStatus = ConnectionStatus.init;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	inboundChannel = ctx.channel();
        InetSocketAddress localAddress = (InetSocketAddress) inboundChannel.localAddress();
        int port = localAddress.getPort();
        final ProxyHost outboundRemoteHost = TcpProxyServer.getProxyHosts().get(port);
  
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
        	.channel(ctx.channel().getClass())
        	.handler(new BackendInitializer(this))
        	//.option(ChannelOption.SO_BACKLOG, TcpProxyServer.getConfig().getInt("tcpProxyServer.so_backlog"))
			.option(ChannelOption.SO_REUSEADDR, true)
			//.option(ChannelOption.SO_TIMEOUT, TcpProxyServer.getConfig().getInt("tcpProxyServer.so_timeout"))
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TcpProxyServer.getConfig().getInt("tcpProxyServer.connectTimeoutMillis"))
			.option(ChannelOption.SO_KEEPALIVE, true);
        
        ChannelFuture f = b.connect(outboundRemoteHost.getRemoteHost(), outboundRemoteHost.getRemotePort());
        connectStatus = ConnectionStatus.outBoundChnnlConnecting;
        outboundChannel = f.channel();
        f.addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    log.info("ProxyFrontendHandler|connection failed|"+outboundRemoteHost.getRemoteHost() + ":" + outboundRemoteHost.getRemotePort());
                    close();
                }
            }
         });
    }

    public Channel getInboundChannel() {
		return inboundChannel;
	}

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        switch(connectStatus){
        case outBoundChnnlReady:
            outboundChannel.writeAndFlush(msg);
            break;
        case closing:
            release(msg);
            break;
        case init:
            log.error("Bad connectStatus.");
            close();
            break;
        case outBoundChnnlConnecting:
        default:
            inboundMsgBuffer.add(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.debug("ProxyFrontendHandler|channelInactive");
    	close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	log.debug("ProxyFrontendHandler|exceptionCaught|remoteAddress="+ctx.channel().remoteAddress(), cause);
        close();
    }


    public void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void close() {
        connectStatus = ConnectionStatus.closing;
        for(Object obj : inboundMsgBuffer){
            release(obj);
        }
        inboundMsgBuffer.clear();
    	closeOnFlush(inboundChannel);
    	closeOnFlush(outboundChannel);
    }
    
	public void outBoundChannelReady() {
	    inboundChannel.config().setAutoRead(true);
	    connectStatus = ConnectionStatus.outBoundChnnlReady;
        for(Object obj : inboundMsgBuffer){
            outboundChannel.writeAndFlush(obj);
        }
        inboundMsgBuffer.clear();
	}
	
	private void release(Object obj){
	    if(obj instanceof ByteBuf){
            ((ByteBuf)obj).release();
        }
	}
}
