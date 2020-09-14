package com.wjz.rpc.hessian.service;

import com.wjz.rpc.hessian.domain.User;

public class UserServiceImpl implements UserService {

    @Override
    public int insert(User user) {
        System.out.println(user);
        return 1;
    }
}
