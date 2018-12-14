package com.wqh.drools.jbpm.config;

import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    @Bean
    @Qualifier("droolsKieBase")
    public KieBase droolsKieBase(){
        Resource userDrl = ResourceFactory.newClassPathResource("com/rule/credit/creditRuleTest.drl", DroolsConfig.class);
        Resource scoreDrl = ResourceFactory.newClassPathResource("com/rule/name/nameRule.drl",DroolsConfig.class);
        Resource creditDrl = ResourceFactory.newClassPathResource("com/rule/score/scoreRuleTest.drl",DroolsConfig.class);

        KieHelper kieHelper = new KieHelper();
        kieHelper.addResource(userDrl, ResourceType.DRL);
        kieHelper.addResource(scoreDrl,ResourceType.DRL);
        kieHelper.addResource(creditDrl,ResourceType.DRL);
        KieBase kieBase = kieHelper.build() ;
        return kieBase;
    }
}
