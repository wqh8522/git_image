package com.wqh.springboot.drools.controller;

import com.wqh.springboot.drools.pojo.Product;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestDroolsConfControl {

    private static final Logger logger = LoggerFactory.getLogger(TestDroolsConfControl.class);


    @Autowired
    private KieSession kieSession;

    @GetMapping("test")
    public String test() {

        Product product = new Product();
        product.setType(Product.DIAMOND);
        kieSession.insert(new String("111"));

        int i = kieSession.fireAllRules();
        logger.info("Product：{}，触发{}条规则",product,i);
        return "触发" + i + "条规则";
    }
}
