package com.wjz.remoting.transport.netty4;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FutureMapUtil {

    public static ConcurrentMap<String, CompletableFuture> futureMap = new ConcurrentHashMap<String, CompletableFuture>();

    public static void put(String k, CompletableFuture v) {
        futureMap.putIfAbsent(k, v);
    }

    public static CompletableFuture remove(String k) {
        return futureMap.remove(k);
    }
}
