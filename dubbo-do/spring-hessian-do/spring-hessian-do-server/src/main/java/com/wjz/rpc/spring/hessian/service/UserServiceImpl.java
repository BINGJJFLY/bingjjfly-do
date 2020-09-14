package com.wjz.rpc.spring.hessian.service;

import com.wjz.rpc.spring.hessian.domain.User;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Override
    public int insert(User user) {
        System.out.println(user);
        return 1;
    }
}
