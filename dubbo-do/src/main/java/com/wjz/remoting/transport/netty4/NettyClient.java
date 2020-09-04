package com.wjz.remoting.transport.netty4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyClient {

    private Channel channel;
    private Bootstrap bootstrap;
    private EventLoopGroup worker;
    private InetSocketAddress connectAddress;

    public NettyClient(String bindIp, int bindPort) {
        this.connectAddress = new InetSocketAddress(bindIp, bindPort);
    }

    public void doOpen() {
        worker = eventLoopGroup(5, "NettyClientWorker");
        NettyClientHandler nettyClientHandler = new NettyClientHandler();
        bootstrap = new Bootstrap()
                .group(worker)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(new ChannelInitializer<SocketChannel>() {
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
                        pipeline.addLast(nettyClientHandler);
                    }
                });
    }

    public void doConnect() {
        ChannelFuture channelFuture = bootstrap.connect(connectAddress);
        boolean ret = channelFuture.awaitUninterruptibly(3000, TimeUnit.MILLISECONDS);
        if (ret && channelFuture.isSuccess()) {
            Channel newChannel = channelFuture.channel();
            Channel oldChannel = channel;
            if (oldChannel != null) {
                oldChannel.close();
            }
            channel = newChannel;
        }
    }

    public void doDisConnect() {
        if (channel != null) {
            channel.close();
        }
    }

    public void doClose() {
        doDisConnect();
    }

    private EventLoopGroup eventLoopGroup(int threads, String threadFactoryName) {
        ThreadFactory threadFactory = new DefaultThreadFactory(threadFactoryName, true);
        return new NioEventLoopGroup(threads, threadFactory);
    }

    public void send(String msg) {
        doConnect();
        if (channel != null) {
            channel.writeAndFlush(msg);
        }
    }
}
