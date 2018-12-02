package com.wqh.springboot.drools.config;


import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.spring.KModuleBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

@Configuration
public class DroolsConfig {


    private static final String RULE_PATH = "rules/";
    /**
     * 获取KieServices
     * @return
     */
    public KieServices getKieServices(){
        return KieServices.Factory.get();
    }

    /**
     * 获取指定文件路径下的所有规则文件
     * @return
     * @throws IOException
     */
    public Resource[] getRuleFiles() throws IOException {
        //Spring资源解析器
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        //获取RULE_PATH路径下的所有资源
        return resourcePatternResolver.getResources("classpath*:" + RULE_PATH + "**/*.*");
    }


    /**
     * KieFileSystem是一个内存文件系统，用于以编程方式定义组成KieModule的资源,而不是在kmodule.xml文件中
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(KieFileSystem.class)
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = getKieServices().newKieFileSystem();
        for(Resource file : getRuleFiles()){
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULE_PATH + file.getFilename(),"UTF-8"));
        }
        return kieFileSystem;
    }

    /**
     * KieContainer：指定KieModule的KieBase容器
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean(KieContainer.class)
    public KieContainer kieContainer () throws IOException {
        //KieRepository 单例，一个存储所有KieModule的仓库。
        final KieRepository kieRepository = getKieServices().getRepository();
        //向KieRepository中添加新的KieModule
        kieRepository.addKieModule(new KieModule() {
            @Override
            public ReleaseId getReleaseId() {
                //如果用户未明确提供KieModule，则返回用于标识此KieRepository中的KieModule的默认ReleaseId
                return kieRepository.getDefaultReleaseId();
            }
        });
        //KieBuilder：KieModule的资源构建者，
        KieBuilder kieBuilder = getKieServices().newKieBuilder(kieFileSystem());
        kieBuilder.buildAll();
        ReleaseId defaultReleaseId = kieRepository.getDefaultReleaseId();

        return getKieServices().newKieContainer(defaultReleaseId);
    }


    /**
     * KieBase是所有应用程序知识定义的存储库。 它将包含规则，流程，功能，类型模型。 KieBase本身不包含运行时数据，而是从KieBase创建会话，其中可以插入数据并启动流程实例。
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean(KieBase.class)
    public KieBase kieBase() throws IOException {
        return kieContainer().getKieBase();
    }

    /**
     * KieSession提供了与引擎交互的方法
     * @return
     * @throws IOException
     */
    @Bean
    @ConditionalOnMissingBean(KieSession.class)
    public KieSession kieSession() throws IOException {
        return kieBase().newKieSession();
    }


    @Bean
    @ConditionalOnMissingBean(KModuleBeanFactoryPostProcessor.class)
    public KModuleBeanFactoryPostProcessor kiePostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }
}
