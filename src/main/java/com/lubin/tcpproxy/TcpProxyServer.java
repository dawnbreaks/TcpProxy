package com.lubin.tcpproxy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

public class TcpProxyServer {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TcpProxyServer.class);
    
	private static Config conf = ConfigFactory.load();
	
	private static HashMap<Integer, ProxyHost> proxyHosts = new HashMap<Integer, ProxyHost>();
	
	public static HashMap<Integer, ProxyHost> getProxyHosts() {
		return proxyHosts;
	}
	
	public static Config getConfig(){
		return conf;
	}
	
	private void run() throws Exception {

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup(TcpProxyServer.getConfig().getInt("tcpProxyServer.ioThreadNum"));
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new FrontendInitializer())
				.option(ChannelOption.SO_BACKLOG, TcpProxyServer.getConfig().getInt("tcpProxyServer.soBacklog"))
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TcpProxyServer.getConfig().getInt("tcpProxyServer.connectTimeoutMillis"))
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childOption(ChannelOption.AUTO_READ, false);

			ArrayList<Channel> allchannels =new ArrayList<Channel>();
			ArrayList<ProxyHost> hostList = getProxyHostList();
			log.info("TcpProxy config: ");
			for(ProxyHost host : hostList) {
				proxyHosts.put(host.localPort, host);
				log.info("local port = " + host.localPort + "|remote host=" + host.remoteHost + "|remote port=" + host.remotePort );
				Channel ch = b.bind(host.localPort).sync().channel();
				allchannels.add(ch);
			}
			
			log.info("TcpProxy server ready for connections...");
	
			for(Channel ch : allchannels){
				ch.closeFuture().sync();
			}
			
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static ArrayList<ProxyHost> getProxyHostList(){
		ArrayList<ProxyHost> proxyHosts = new ArrayList<ProxyHost>();
		
		List<? extends ConfigObject> hosts = TcpProxyServer.getConfig().getObjectList("tcpProxyServer.hosts");
		for(ConfigObject host : hosts) {
			ConfigValue localPort = host.get("localPort");
			ConfigValue remoteHost = host.get("remoteHost");
			ConfigValue remotePort = host.get("remotePort");
			proxyHosts.add(new ProxyHost(Integer.parseInt(localPort.render()), (String) remoteHost.unwrapped(), Integer.parseInt(remotePort.render())));
		}
		return proxyHosts;
	}
	
	public static class ProxyHost{
		int localPort;
		String remoteHost;
		int remotePort;
		
		public int getRemotePort() {
			return remotePort;
		}
		public void setRemotePort(int remotePort) {
			this.remotePort = remotePort;
		}
		public ProxyHost(int localPort, String remoteHost, int remotePort) {
			super();
			this.localPort = localPort;
			this.remoteHost = remoteHost;
			this.remotePort = remotePort;
		}
		public int getLocalPort() {
			return localPort;
		}
		public void setLocalPort(int localPort) {
			this.localPort = localPort;
		}
		public String getRemoteHost() {
			return remoteHost;
		}
		public void setRemoteHost(String remoteHost) {
			this.remoteHost = remoteHost;
		}
	}

	public static void main(String[] args) throws Exception {
		new TcpProxyServer().run();
	}
	
	public static boolean isDebug(){
		boolean debug = TcpProxyServer.getConfig().getBoolean("tcpProxyServer.debug");
    	return debug;
	}
}
