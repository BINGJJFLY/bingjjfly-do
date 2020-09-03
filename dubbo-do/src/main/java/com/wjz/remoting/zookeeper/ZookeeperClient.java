package com.wjz.remoting.zookeeper;

import java.util.List;

public interface ZookeeperClient {

    void create(String path, boolean ephemeral);

    void create(String path, String content, boolean ephemeral);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);
}
