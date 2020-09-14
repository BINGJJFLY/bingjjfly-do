package com.wjz.spring.rmi.server;

import com.wjz.spring.rmi.SpringRMIService;
import org.springframework.stereotype.Service;

@Service("springRMIService")
public class SpringRMIServiceImpl implements SpringRMIService {

    @Override
    public String getData(String name) {
        return "your name is "+name;
    }
}
