package com.wqh.demo.drools.service;

import com.wqh.demo.drools.filter.MyAgendaFilter;
import com.wqh.demo.drools.pojo.Product;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.ActivationGroup;
import org.kie.api.runtime.rule.AgendaGroup;

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
        System.setProperty("drools.dateformat", "yyyy-MM-dd");
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
    public void testTwo() throws Exception {
        Product product = new Product();
        product.setDiscount(100);
        //是指定的group获得焦点
//        kieSession.getAgenda().getAgendaGroup("agenda_group1").setFocus();

//        kieSession.insert(product);
        //触发规则数
        int i = kieSession.fireAllRules();
//
        System.out.println(i);
//        System.out.println(product);

        kieSession.dispose();
    }

    @Test
    public void testFilter() {
        MyAgendaFilter filter = new MyAgendaFilter("test");
        kieSession.fireAllRules(filter);
        kieSession.dispose();
    }


    @Test
    public void testDateEffective() {
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        System.setProperty("drools.dateformat", "yyyy-MM-dd");
        kieSession = kieContainer.newKieSession("ksession-rule");
        kieSession.fireAllRules();
        kieSession.dispose();
    }

    @Test
    public void testDurationOrTimer() {
        new Thread(new Runnable() {
            public void run() {
                //保持规则执行,需开启新的线程
                kieSession.fireUntilHalt();
            }
        }).start();
        try {
            Thread.sleep(5000);
            //停止规则的执行
            kieSession.halt();
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            kieSession.dispose();
        }
    }


}
