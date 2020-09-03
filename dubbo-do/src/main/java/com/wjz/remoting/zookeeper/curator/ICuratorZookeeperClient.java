package com.wjz.remoting.zookeeper.curator;

import com.wjz.remoting.zookeeper.ChildListener;
import com.wjz.remoting.zookeeper.ZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ICuratorZookeeperClient implements ZookeeperClient {

    static final Charset CHARSET = Charset.forName("UTF-8");
    private CuratorFramework client;
    private final Set<String> persistentExistNodePath = new ConcurrentHashSet<>();
    private Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    public ICuratorZookeeperClient(String address, int timeout, int sessionExpire) {
        try {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(address)
                    .retryPolicy(new RetryNTimes(1, 1000))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(sessionExpire);
            client = builder.build();
            client.getConnectionStateListenable().addListener(new CuratorConnectionStateListener(address, timeout, sessionExpire));
            client.start();
            boolean connected = client.blockUntilConnected(timeout, TimeUnit.MILLISECONDS);
            if (!connected) {
                throw new IllegalStateException("zookeeper not connected");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private class CuratorConnectionStateListener implements ConnectionStateListener {
        private String address;
        private int timeout;
        private int sessionExpire;
        private long lastSessionId;

        public CuratorConnectionStateListener(String address, int timeout, int sessionExpire) {
            this.address = address;
            this.timeout = timeout;
            this.sessionExpire = sessionExpire;
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState state) {
            long sessionId = -1L; // unknown session id
            try {
                sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
            } catch (Exception e) {
                System.out.println("Curator client state changed, but failed to get the related zk session instance.");
            }
            if (state == ConnectionState.LOST) {
                System.out.println("Curator zookeeper session " + Long.toHexString(lastSessionId) + " expired.");
            } else if (state == ConnectionState.SUSPENDED) {
                System.out.println("Curator zookeeper connection of session " + Long.toHexString(sessionId) + " timed out. " +
                        "connection timeout value is " + timeout + ", session expire timeout value is " + sessionExpire);
            } else if (state == ConnectionState.CONNECTED) {
                lastSessionId = sessionId;
                System.out.println("Curator zookeeper client instance initiated successfully, session id is " + Long.toHexString(sessionId));
            } else if (state == ConnectionState.RECONNECTED) {
                if (lastSessionId == sessionId && sessionId != -1L) {
                    System.out.println("Curator zookeeper connection recovered from connection lose," +
                            "reuse the old session" + Long.toHexString(sessionId));
                } else {
                    System.out.println("New session created after old session lost, " +
                            "old session " + Long.toHexString(lastSessionId) + ", new session " + Long.toHexString(sessionId));
                }
            }
        }
    }

    public CuratorWatcherImpl createChildListener(String path) {
        return new CuratorWatcherImpl(client, path);
    }

    public CuratorWatcherImpl createDataListener() {
        return new CuratorWatcherImpl(client);
    }

    private static class CuratorWatcherImpl implements CuratorWatcher, TreeCacheListener {

        private CuratorFramework client;
        private String path;
        private volatile ChildListener childListener;

        public CuratorWatcherImpl(CuratorFramework client, String path, ChildListener childListener) {
            this.client = client;
            this.path = path;
            this.childListener = childListener;
        }

        public CuratorWatcherImpl(CuratorFramework client, String path) {
            this.client = client;
            this.path = path;
        }

        public CuratorWatcherImpl(CuratorFramework client) {
            this.client = client;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (event.getType() == Watcher.Event.EventType.None) {
                return;
            }
            System.out.println("child Changed");
            if (childListener != null) {
                childListener.childChanged(path, client.getChildren().usingWatcher(this).forPath(path));
            }
        }

        @Override
        public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
            System.out.println("listen the zookeeper changed. The changed data:" + event.getData());
            TreeCacheEvent.Type type = event.getType();
            String content = null;
            String path = null;
            switch (type) {
                case NODE_ADDED:
                    System.out.println("NODE_ADDED");
                    path = event.getData().getPath();
                    content = event.getData().getData() == null ? "" : new String(event.getData().getData(), CHARSET);
                    break;
                case NODE_UPDATED:
                    System.out.println("NODE_UPDATED");
                    path = event.getData().getPath();
                    content = event.getData().getData() == null ? "" : new String(event.getData().getData(), CHARSET);
                    break;
                case NODE_REMOVED:
                    System.out.println("NODE_REMOVED");
                    path = event.getData().getPath();
                    break;
                case INITIALIZED:
                    System.out.println("INITIALIZED");
                    break;
                case CONNECTION_LOST:
                    System.out.println("CONNECTION_LOST");
                    break;
                case CONNECTION_RECONNECTED:
                    System.out.println("CONNECTION_RECONNECTED");
                    break;
                case CONNECTION_SUSPENDED:
                    System.out.println("CONNECTION_SUSPENDED");
                    break;
            }
            System.out.println(path + ": " + content);
        }
    }

    @Override
    public void create(String path, boolean ephemeral) {
        if (!ephemeral) {
            if (persistentExistNodePath.contains(path)) {
                return;
            }
            if (checkExists(path)) {
                persistentExistNodePath.add(path);
                return;
            }
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path);
        } else {
            createPersistent(path);
            persistentExistNodePath.add(path);
        }
    }

    @Override
    public void create(String path, String content, boolean ephemeral) {
        if (checkExists(path)) {
            persistentExistNodePath.remove(path);
            deletePath(path);
        }
        int i = path.lastIndexOf('/');
        if (i > 0) {
            create(path.substring(0, i), false);
        }
        if (ephemeral) {
            createEphemeral(path, content);
        } else {
            createPersistent(path, content);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        try {
            return client.getChildren().usingWatcher(createTargetChildListener(path, listener)).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private CuratorWatcher createTargetChildListener(String path, ChildListener listener) {
        return new CuratorWatcherImpl(client, path, listener);
    }

    private void createPersistent(String path, String data) {
        byte[] bytes = data.getBytes(CHARSET);
        try {
            client.create().withMode(CreateMode.PERSISTENT).forPath(path, bytes);
        } catch (KeeperException.NodeExistsException e) {
            try {
                client.setData().forPath(path, bytes);
            } catch (Exception e1) {
                throw new IllegalStateException(e1.getMessage(), e1);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createEphemeral(String path, String data) {
        byte[] bytes = data.getBytes(CHARSET);
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path, bytes);
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("ZNode " + path + " already exists, since we will only try to recreate a node on a session expiration" +
                    ", this duplication might be caused by a delete delay from the zk server, which means the old expired session" +
                    " may still holds this ZNode and the server just hasn't got time to do the deletion. In this case, " +
                    "we can just try to delete and create again." + e);
            deletePath(path);
            createEphemeral(path, data);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createPersistent(String path) {
        try {
            client.create().withMode(CreateMode.PERSISTENT).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("ZNode " + path + " already exists." + e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void createEphemeral(String path) {
        try {
            client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
            System.out.println("ZNode " + path + " already exists, since we will only try to recreate a node on a session expiration" +
                    ", this duplication might be caused by a delete delay from the zk server, which means the old expired session" +
                    " may still holds this ZNode and the server just hasn't got time to do the deletion. In this case, " +
                    "we can just try to delete and create again." + e);
            deletePath(path);
            createEphemeral(path);
        } catch (Exception e) {

        }
    }

    private void deletePath(String path) {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private boolean checkExists(String path) {
        try {
            if (client.checkExists().forPath(path) != null) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public List<String> addTargetChildListener(String path, CuratorWatcherImpl listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void addDataListener(String path, CuratorWatcherImpl listener) {
        addDataListener(path, listener, null);
    }

    public void addDataListener(String path, CuratorWatcherImpl listener, Executor executor) {
        try {
            TreeCache treeCache = TreeCache.newBuilder(client, path).setCacheData(false).build();
            treeCacheMap.putIfAbsent(path, treeCache);
            if (executor == null) {
                treeCache.getListenable().addListener(listener);
            } else {
                treeCache.getListenable().addListener(listener, executor);
            }
            treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("Add treeCache listener for path:" + path, e);
        }
    }

    public void close() {
        client.close();
    }
}
