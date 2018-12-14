package com.wqh.demo.drools.flow;

import org.junit.Test;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import com.wqh.demo.drools.pojo.Score;

public class TestHelper {
	
	@Test
	public void test(){
		Resource bpmnRe = ResourceFactory.newClassPathResource("process/userFlowTest.bpmn",TestHelper.class);
		Resource drlRe1 = ResourceFactory.newClassPathResource("process/usesFlowTest.drl",TestHelper.class);
//		Resource drlRe2 = ResourceFactory.newClassPathResource("process/namRule.drl",TestHelper.class);
		KieHelper kieHelper = new KieHelper();
		kieHelper.addResource(bpmnRe, ResourceType.BPMN2);
		kieHelper.addResource(drlRe1, ResourceType.DRL);
//		kieHelper.addResource(drlRe2, ResourceType.DRL);
		KieSession kieSession = kieHelper.build().newKieSession();
		Score score = new Score();
		score.setName("wqh");
		score.setScore(100);
		kieSession.insert(score);
		kieSession.startProcess("drools_flow_no1");
		kieSession.fireAllRules();
		kieSession.dispose();
	}

}
