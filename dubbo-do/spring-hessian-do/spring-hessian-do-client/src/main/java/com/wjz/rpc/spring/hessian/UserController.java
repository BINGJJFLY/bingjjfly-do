package com.wjz.rpc.spring.hessian;

import com.wjz.rpc.spring.hessian.domain.User;
import com.wjz.rpc.spring.hessian.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/index")
    public int index() {
        return userService.insert(new User("spring-hessian", 18));
    }
}
