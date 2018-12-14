package com.wqh.drools.jbpm.config;

import com.wqh.drools.jbpm.flow.ProcessTest1;
import com.wqh.drools.jbpm.rule.RuleTest;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JbpmConfig {

    @Bean
    @Qualifier("jbpmKieBase")
    public KieBase jbpmKieBase(){
        Resource bpmnRe = ResourceFactory.newClassPathResource("com/process/TestProcess.bpmn", JbpmConfig.class);
        Resource drlRe1 = ResourceFactory.newClassPathResource("com/process/testProcess.drl",JbpmConfig.class);

        KieHelper kieHelper = new KieHelper();

        kieHelper.addResource(bpmnRe,ResourceType.BPMN2);
        kieHelper.addResource(drlRe1,ResourceType.DRL);
        KieBase kieBase = kieHelper.build() ;

        return kieBase;
    }
}
