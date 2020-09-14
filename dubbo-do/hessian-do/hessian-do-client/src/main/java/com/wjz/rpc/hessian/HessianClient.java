package com.wjz.rpc.hessian;

import com.caucho.hessian.client.HessianProxyFactory;
import com.wjz.rpc.hessian.domain.User;
import com.wjz.rpc.hessian.service.UserService;

import java.net.MalformedURLException;

public class HessianClient {

    public static void main(String[] args) throws MalformedURLException {
        String url = "http://localhost:8080/hessian-do-server/user/service";
        HessianProxyFactory hessianProxyFactory = new HessianProxyFactory();
        UserService userService = (UserService) hessianProxyFactory.create(UserService.class, url);
        System.out.println(userService.insert(new User("hessian", 18)));
    }
}
