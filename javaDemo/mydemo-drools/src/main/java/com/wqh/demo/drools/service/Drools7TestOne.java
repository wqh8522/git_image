package com.wqh.demo.drools.service;

import com.wqh.demo.drools.pojo.Product;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class Drools7TestOne {

    private KieSession kieSession;

    @Before
    public void init() {
        //KieServices：通过该接口的方法可以访问KIE关于构建和运行的相关对象
        KieServices kieServices = KieServices.Factory.get();
        //KieContainer：一个KieBase的容器，提供了获取KieBase的方法和创建KieSession的方法。
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        //KieSession：一个跟Drools引擎打交道的会话，基于KieBase创建。
        //kmodule.xml :
        kieSession = kieContainer.newKieSession("ksession-rule");
    }

    @Test
    public void testOne() throws Exception {


        Product product = new Product();
        product.setType(Product.GOLD);

        kieSession.insert(product);
        //触发规则数
        int i = kieSession.fireAllRules();

        System.out.println(i);
        System.out.println(product);
    }


    @Test
    public void testNoLoop() throws Exception {
        Product product = new Product();
        product.setDiscount(100);
        kieSession.insert(product);
        //触发规则数
        int i = kieSession.fireAllRules();
//
        System.out.println(i);
//        System.out.println(product);
    }

}
