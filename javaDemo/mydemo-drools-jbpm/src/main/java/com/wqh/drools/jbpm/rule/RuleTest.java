package com.wqh.drools.jbpm.rule;

import com.wqh.drools.jbpm.pojo.Credit;
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

import com.wqh.drools.jbpm.pojo.Users;

public class RuleTest {

//	@Test
	public void testRule() {
		
		Resource userDrl = ResourceFactory.newClassPathResource("com/rule/credit/creditRuleTest.drl",RuleTest.class);
		Resource scoreDrl = ResourceFactory.newClassPathResource("com/rule/name/nameRule.drl",RuleTest.class);
		Resource creditDrl = ResourceFactory.newClassPathResource("com/rule/score/scoreRuleTest.drl",RuleTest.class);

		KieHelper kieHelper = new KieHelper();
		kieHelper.addResource(userDrl,ResourceType.DRL);
		kieHelper.addResource(scoreDrl,ResourceType.DRL);
		kieHelper.addResource(creditDrl,ResourceType.DRL);
		KieBase kieBase = kieHelper.build() ;

		Users users = new Users();
		users.setName("wqh111");

		KieSession kieSession = kieBase.newKieSession();
		kieSession.insert(users);
		int i = kieSession.fireAllRules();
		System.out.println(i);

		QueryResults results = kieSession.getQueryResults("list all credits from working memory");
		for(QueryResultsRow row : results){
			Credit credit = (Credit) row.get("credit");
			System.out.println(credit.toString());
		}
		kieSession.dispose();
	}
}
