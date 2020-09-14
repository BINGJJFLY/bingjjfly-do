package com.wjz.spring.rmi.client;

import com.wjz.spring.rmi.RmiRemoteInvocation;
import com.wjz.spring.rmi.SpringRMIService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

@SpringBootApplication
public class SpringRMIClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRMIClientApplication.class);
    }

    @Bean
    public RmiProxyFactoryBean springRMIService() {
        RmiProxyFactoryBean proxyFactoryBean = new RmiProxyFactoryBean();
        proxyFactoryBean.setRemoteInvocationFactory(methodInvocation -> {
            return new RmiRemoteInvocation(methodInvocation);
        });
        proxyFactoryBean.setServiceInterface(SpringRMIService.class);
        proxyFactoryBean.setServiceUrl("rmi://127.0.0.1:1234/SpringRMIService");
        proxyFactoryBean.setCacheStub(true);
        proxyFactoryBean.setLookupStubOnStartup(true);
        proxyFactoryBean.setRefreshStubOnConnectFailure(true);
        proxyFactoryBean.afterPropertiesSet();
        return proxyFactoryBean;
    }
}
