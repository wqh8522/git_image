package com.wqh.drools.jbpm.flow;

import com.wqh.drools.jbpm.pojo.Credit;
import com.wqh.drools.jbpm.pojo.Users;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;


public class ProcessTest1 {

	
	@Test
	public void testProcess() {
		Users users = new Users();
		users.setName("wqh111");
		Resource bpmnRe = ResourceFactory.newClassPathResource("com/process/TestProcess.bpmn",ProcessTest1.class);
//		Resource drlRe1 = ResourceFactory.newClassPathResource("com/process/testProcess.drl",ProcessTest1.class);
		Resource drlRe1 = ResourceFactory.newClassPathResource("com/process/test/test1.drl",ProcessTest1.class);
		Resource drlRe2 = ResourceFactory.newClassPathResource("com/process/test/test2.drl",ProcessTest1.class);
		Resource drlRe3 = ResourceFactory.newClassPathResource("com/process/test/test3.drl",ProcessTest1.class);
		
		KieHelper kieHelper = new KieHelper();
		
		kieHelper.addResource(bpmnRe,ResourceType.BPMN2);
		kieHelper.addResource(drlRe1,ResourceType.DRL);
		kieHelper.addResource(drlRe2,ResourceType.DRL);
		kieHelper.addResource(drlRe3,ResourceType.DRL);
		KieBase kieBase = kieHelper.build() ;

        KieSession kieSession = kieBase.newKieSession();
        FactHandle factHandle = kieSession.insert(users);

        kieSession.startProcess("com.process.TestProcess");
//        kieSession.fireAllRules();
        QueryResults results = kieSession.getQueryResults("list all credits from working memory");
        for(QueryResultsRow row : results){
            Credit credit = (Credit) row.get("credit");
            System.out.println(credit.toString());
        }
        kieSession.dispose();
	}
}
