package com.wjz.remoting.transport.netty4;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NettyClientTest {

    @Test
    public void test() throws UnknownHostException, ExecutionException, InterruptedException, TimeoutException {
        String ip = "127.0.0.1";
        NettyClient client = new NettyClient(ip, 12880);
        client.doOpen();
        CompletableFuture future = new CompletableFuture();
        // 发起NIO网络请求，马上返回
        String reqId = "1";
        client.send("hello server " + reqId);
        FutureMapUtil.put(reqId, future);
        future.get(5000, TimeUnit.MILLISECONDS);
    }
}
