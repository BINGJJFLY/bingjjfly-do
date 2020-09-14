package com.wjz.rpc.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * <b>定义一个远程接口</b>
 *
 * 必须继承Remote接口。
 * 所有参数和返回类型必须能被序列化(因为要网络传输)。
 * 任意远程对象都必须实现此接口。
 * 只有远程接口中指定的方法可以被调用。
 * 所有方法必须抛出RemoteException。
 */
public interface RemoteMath extends Remote {

    double add(double a, double b) throws RemoteException;

    double subtract(double a, double b) throws RemoteException;
}
