package com.wjz.remoting.transport.netty4;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.*;

public class NettyClientHandler extends ChannelDuplexHandler {

    private Executor executor = Executors.newFixedThreadPool(5);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 异步执行释放IO线程
        executor.execute(() -> {
            System.out.println("NettyClient channelRead msg: " + msg);
            String content = ((String) msg).split(":")[0];
            String reqId = ((String) msg).split(":")[1];
            CompletableFuture future = FutureMapUtil.remove(reqId);
            if (future != null) {
                future.complete(content);
            }
        });
    }
}
