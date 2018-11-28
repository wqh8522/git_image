package com.wqh.demo.drools.service;

import org.junit.Test;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;

public class Drools7TestOne {

    @Test
    public void testOne()throws Exception{
//        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        kbuilder.add(ResourceFactory.newClassPathResource("rules/produceRule.drl",this.getClass()),ResourceType.DRL);
//        if (kbuilder.hasErrors()){
//            System.out.println(kbuilder.getErrors().toString());
//        }
//        assert (kbuilder.hasErrors());
//
//        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//
////        kbase.addPackage(kbuilder.getKnowledgePackages())

//        KieServices ks = KieServices.Factory.get();
//        KieContainer kieContainer = ks.getKieClasspathContainer();
//
//        kieContainer.newKieSession()
//        KnowledgeBuilder： 在业务代码当中收集已经编译的规则代码，然后对这些规则文件进行编译，最终产生一批编译好的规则包（KnowledgePackage）给其他应用程序使用
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    }
}
