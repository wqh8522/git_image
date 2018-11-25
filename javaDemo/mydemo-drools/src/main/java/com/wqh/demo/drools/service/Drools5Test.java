package com.wqh.demo.drools.service;


import com.wqh.demo.drools.pojo.Product;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Drools5Test {

    private static final Logger logger = LoggerFactory.getLogger(Drools5Test.class);
    public void executeDrools(){
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        knowledgeBuilder.add(ResourceFactory.newClassPathResource("rules/productRule.drl",this.getClass()), ResourceType.DRL);
        if (knowledgeBuilder.hasErrors()){
            logger.error("规则配置有问题",knowledgeBuilder.getErrors().toString());
        }
        Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();

        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
        //将 KnowledgePackage 集合添加到 KnowledgeBase 当中
        knowledgeBase.addKnowledgePackages(knowledgePackages);

        StatefulKnowledgeSession kSession = knowledgeBase.newStatefulKnowledgeSession();
        Product product = new Product();
        product.setType(Product.DIAMOND);

        kSession.insert(product);
        kSession.fireAllRules();
        kSession.dispose();
        logger.info("{}的折扣为{}%",product.getType(),product.getDiscount());
    }

    public static void main(String[] args) {
        Drools5Test drools5Test = new Drools5Test();
        drools5Test.executeDrools();
    }
}
