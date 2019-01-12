package com.wqh.demo.service;

import com.wqh.demo.pojo.Cheese;
import com.wqh.demo.pojo.Person;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class Drools5 {

    private static final Logger logger = LoggerFactory.getLogger(Drools5.class);

    @Test
    public void executeDrools(){
        Cheese c = new Cheese();
        c.setName("cheddar1");

        Person p = new Person();
        p.setFavouriteCheese(c);
        p.setName("person");
//      KnowledgeBuilder： 在业务代码当中收集已经编译的规则代码，然后对这些规则文件进行编译，最终产生一批编译好的规则包（KnowledgePackage）给其他应用程序使用
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        编译规则
        knowledgeBuilder.add(ResourceFactory.newClassPathResource("rule/Nodeshared.drl",this.getClass()), ResourceType.DRL);
//        hasErrors：得到编译规则过程中发现规则是否有错误，可以通过getErrors获取错误
        if (knowledgeBuilder.hasErrors()){
            logger.error("规则配置有问题，{}",knowledgeBuilder.getErrors().toString());
        }
//        产生规则包集合
        Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
//      创建KnowledgeBaseConfiguration对象，用来存放规则引擎运行时环境参数的配置对象
        KnowledgeBaseConfiguration kbConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
//      KnowledgeBase：收集应用当中知识（knowledge）定义的知识库对象，可以指定一个KnowledgeBaseConfiguration对象：
        KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(kbConfig);
        //将 KnowledgePackage 集合添加到 KnowledgeBase 当中
        knowledgeBase.addKnowledgePackages(knowledgePackages);
//      编译完成将规则包文件在引擎当中运行，
//        StatefulKnowledgeSession：一种常见的与规则引擎进行交互的方式，他可以与规则引擎建立一个持续的交互通道，在推理计算的过程当中可能会多次触发统一数据集
        StatefulKnowledgeSession kSession = knowledgeBase.newStatefulKnowledgeSession();
//
        kSession.insert(c);
        kSession.insert(p);
////        释放内存资源，必须。。。
        kSession.fireAllRules();
        kSession.dispose();
//        StatelessKnowledgeSession 对StatefulKnowledgeSession做了一定的封装，使用execute执行规则
//        StatelessKnowledgeSession statelessKnowledgeSession = knowledgeBase.newStatelessKnowledgeSession();
//        statelessKnowledgeSession.execute(product);
//        logger.info("{}的折扣为{}%",product.getType(),product.getDiscount());
    }

}
