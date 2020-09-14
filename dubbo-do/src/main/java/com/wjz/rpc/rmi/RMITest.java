package com.wjz.rpc.rmi;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMITest {

    public static class RMIServer {

        public static void main(String[] args) {
            try {
                // 注册远程对象，向客户端提供远程对象服务
                // 远程对象是在远程服务上创建的，你无法确切地知道远程服务器上的对象的名称，
                // 但是,将远程对象注册到RMI Registry之后,
                // 客户端就可以通过RMI Registry请求到该远程服务对象的stub，
                // 利用stub代理就可以访问远程服务对象了。
                RemoteMath service = new RemoteMathImpl();
                /* 本地创建并启动RMI Service，被创建的Registry服务将在指定的端口上侦听到来的请求
                 * 实际上，RMI Service本身也是一个RMI应用，我们也可以从远端获取Registry:
                 *     public interface Registry extends Remote;
                 *     public static Registry getRegistry(String host, int port) throws RemoteException;
                 */
                LocateRegistry.createRegistry(1099);
                Registry registry = LocateRegistry.getRegistry();
                /* 将stub代理绑定到Registry服务的URL上 */
                registry.bind("Compute", service);
                System.out.println("Math server is ready");
                // 如果不想再让该对象被继续调用，使用下面一行
                // UnicastRemoteObject.unexportObject(remoteMath, false);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }

        }
    }

    public static class RMIClient {

        public static void main(String[] args) {
            try {
                // 从RMI Registry中请求stub
                // 如果RMI Registry就在本地机器上，URL就是:rmi://localhost:1099/Compute
                // 否则，URL就是：rmi://RMIService_IP:1099/Compute
                Registry registry = LocateRegistry.getRegistry("localhost");
                // 从Registry中检索远程对象的存根/代理
                RemoteMath remoteMath = (RemoteMath) registry.lookup("Compute");
                // 调用远程对象的方法 通过stub调用远程接口实现
                double addResult = remoteMath.add(5.0, 3.0);
                System.out.println("5.0 + 3.0 = " + addResult);
                double subResult = remoteMath.subtract(5.0, 3.0);
                System.out.println("5.0 - 3.0 = " + subResult);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }
}
