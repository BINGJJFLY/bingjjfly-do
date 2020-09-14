package com.wjz.rpc.spring.hessian;

import com.wjz.rpc.spring.hessian.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.caucho.HessianServiceExporter;

@SpringBootApplication
public class SpringHessianServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringHessianServerApplication.class, args);
    }

    @Bean("/UserService")
    public HessianServiceExporter exporter(UserService userService) {
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setServiceInterface(UserService.class);
        exporter.setService(userService);
        return exporter;
    }
}
