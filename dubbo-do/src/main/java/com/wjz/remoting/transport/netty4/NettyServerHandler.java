package com.wjz.remoting.transport.netty4;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NettyServerHandler extends ChannelDuplexHandler {

    private Executor executor = Executors.newFixedThreadPool(5);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("NettyServer channelRead msg: " + msg);
        // 异步执行释放IO线程
        executor.execute(() -> {
            // 获取消息体，并且解析出请求id
            String str = (String) msg;
            String reqId = str.split(":")[1];
            // 拼接结果，请求id,协议帧分隔符(模拟服务端执行服务产生结果)
            String resp = CodeC.generatorFrame("NettyServer response hi client " + reqId, reqId);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 写回结果
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer(resp.getBytes()));
        });
    }
}
