package com.wjz.rpc.spring.hessian;

import com.wjz.rpc.spring.hessian.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;
import org.springframework.remoting.caucho.HessianServiceExporter;

@SpringBootApplication
public class SpringHessianClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHessianClientApplication.class, args);
    }

    @Bean
    public HessianProxyFactoryBean userService() {
        HessianProxyFactoryBean proxyFactoryBean = new HessianProxyFactoryBean();
        proxyFactoryBean.setServiceUrl("http://127.0.0.1:8002/UserService");
        proxyFactoryBean.setServiceInterface(UserService.class);
        return proxyFactoryBean;
    }
}
