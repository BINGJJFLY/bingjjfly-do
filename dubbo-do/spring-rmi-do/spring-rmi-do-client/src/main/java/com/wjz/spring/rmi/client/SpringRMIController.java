package com.wjz.spring.rmi.client;

import com.wjz.spring.rmi.SpringRMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpringRMIController {

    @Autowired
    @Qualifier("springRMIService")
    SpringRMIService springRMIService;

    @RequestMapping("/index")
    public String index(String name) {
        return springRMIService.getData(name);
    }
}
