package com.wjz.remoting.zookeeper.curator;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ICuratorZookeeperClientTest {

    @Test
    public void test() throws IOException {
        ICuratorZookeeperClient client = new ICuratorZookeeperClient("127.0.0.1:2181", 5 * 1000, 60 * 1000);
        client.create("/curator", false);
        client.create("/curator/client", true);
        System.in.read();
    }

    @Test
    public void test2() throws IOException {
        ICuratorZookeeperClient client = new ICuratorZookeeperClient("127.0.0.1:2181", 60 * 1000, 60 * 1000);
        client.create("/curator/client/test", true);
        client.create("/curator/client/test2", true);
        System.out.println(client.addTargetChildListener("/curator/client", client.createChildListener("/curator/client")));
        client.create("/curator/client/test3", true);
        System.in.read();
    }

    @Test
    public void test3() throws IOException {
        ICuratorZookeeperClient client = new ICuratorZookeeperClient("127.0.0.1:2181", 60 * 1000, 60 * 1000);
        client.create("/curator/client", true);
        client.addDataListener("/curator/client", client.createDataListener());
        client.create("/curator/client", "1", true);
        System.in.read();
    }

    /**
     * 动态获取更新服务列表
     */
    @Test
    public void test4() throws InterruptedException {
        ICuratorZookeeperClient client = new ICuratorZookeeperClient("127.0.0.1:2181", 60 * 1000, 60 * 1000);
        client.create("/curator/client/test", true);
        client.create("/curator/client/test2", true);
        List<String> services = client.getChildren("/curator/client");
        client.addChildListener("/curator/client", (p, s) -> notify(s));
        notify(services);
        TimeUnit.MILLISECONDS.sleep(3000);
        client.create("/curator/client/test3", true);
        client.create("/curator/client/test4", true);
        TimeUnit.MILLISECONDS.sleep(1000);
    }

    private void notify(List<String> services) {
        System.out.println(services);
    }
}
