package com.wjz.remoting.transport.netty4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

public class NettyServer {

    private Channel channel;
    private InetSocketAddress bindAddress;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(String bindIp, int bindPort) {
        this.bindAddress = new InetSocketAddress(bindIp, bindPort);
    }

    public void doOpen() {
        bossGroup = eventLoopGroup(1, "NettyServerBoss");
        workerGroup = eventLoopGroup(5, "NettyServerWorker");
        final NettyServerHandler nettyServerHandler = new NettyServerHandler();
        bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // 设置帧分隔符解码器
                        ByteBuf delimiter = Unpooled.copiedBuffer("|".getBytes());
                        pipeline.addLast(new DelimiterBasedFrameDecoder(1000, delimiter));
                        // 设置消息内容自动转换为String的解码器到管线
                        pipeline.addLast(new StringDecoder());
                        // 设置字符串消息自动进行编码的编码器到管线
                        pipeline.addLast(new StringEncoder());
                        // 添加业务hander到管线
                        pipeline.addLast(nettyServerHandler);
                    }
                });
        ChannelFuture channelFuture = bootstrap.bind(bindAddress);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
    }

    public void doClose() {
        if (channel != null) {
            channel.close();
        }
        if (bootstrap != null) {
            bossGroup.shutdownGracefully().syncUninterruptibly();
            workerGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    private EventLoopGroup eventLoopGroup(int threads, String threadFactoryName) {
        ThreadFactory threadFactory = new DefaultThreadFactory(threadFactoryName, true);
        return new NioEventLoopGroup(threads, threadFactory);
    }

    public Channel getChannel() {
        return channel;
    }
}
