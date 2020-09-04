package com.wjz.remoting.transport.netty4;

public class CodeC {

    // 根据消息内容和请求id，拼接消息帧
    public static String generatorFrame(String msg, String reqId) {
        return msg + ":" + reqId + "|";
    }
}
