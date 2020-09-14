package com.wjz.spring.rmi.server;

import com.wjz.spring.rmi.SpringRMIService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.remoting.rmi.RmiServiceExporter;

import java.rmi.RemoteException;

@SpringBootApplication
public class SpringRMIServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRMIServerApplication.class, args);
    }

    @Bean
    public RmiServiceExporter exporter(SpringRMIService springRMIService) {
        RmiServiceExporter exporter = new RmiServiceExporter();
        exporter.setServiceInterface(SpringRMIService.class);
        exporter.setServiceName("SpringRMIService");
        exporter.setService(springRMIService);
        exporter.setRegistryPort(1234);
        exporter.setServicePort(1099);
        try {
            exporter.afterPropertiesSet();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return exporter;
    }
}
