package com.wjz.remoting.transport.netty4;

import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NettyServerTest {

    @Test
    public void test() throws UnknownHostException, InterruptedException {
        String ip = "127.0.0.1";
        NettyServer server = new NettyServer(ip, 12880);
        server.doOpen();
        System.out.println("NettyServer opened.");
        // 等待服务监听套接字关闭
        server.getChannel().closeFuture().sync();
    }
}
