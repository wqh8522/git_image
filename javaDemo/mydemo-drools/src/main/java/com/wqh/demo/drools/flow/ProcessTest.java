package com.wqh.demo.drools.flow;

import com.wqh.demo.drools.pojo.Score;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

public class ProcessTest {
	
	private KieSession kieSession;

/*    @Before
    public void init() {
		KieHelper kieHelper = new KieHelper();
		KieBase kieBase = kieHelper.addResource(ResourceFactory.newClassPathResource("process/userFlowTest.bpmn", "UTF-8"),ResourceType.BPMN2).build();
		kieSession = kieBase.newKieSession();
	}


	@Test
	public void testScore(){
		Score score = new Score();
		score.setName("wqh");
		score.setScore(99);
		kieSession.insert(score);
		kieSession.fireAllRules();
		kieSession.startProcess("drools_flow_no1");
		kieSession.dispose();
	}*/


	public static void main(String[] args) throws IOException {

		//KieServices：通过该接口的方法可以访问KIE关于构建和运行的相关对象
		//Spring资源解析器
		KieServices kieServices = KieServices.Factory.get();
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		//获取RULE_PATH路径下的所有资源
		Resource[] resources = resourcePatternResolver.getResources("classpath*:process/*.*");
//		Resource[] resources = resourcePatternResolver.getResources("classpath*:process/userFlowTest.drl");

		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		for(Resource file : resources){
			kieFileSystem.write(ResourceFactory.newClassPathResource("process/" + file.getFilename(),"UTF-8"));
		}

		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		kieBuilder.buildAll();
		ReleaseId releaseId = kieBuilder.getKieModule().getReleaseId();
		KieContainer kieContainer = kieServices.newKieContainer(releaseId);
		//KieContainer：一个KieBase的容器，提供了获取KieBase的方法和创建KieSession的方法。
//        KieContainer kieContainer = kieServices.getKieClasspathContainer();
		//KieSession：一个跟Drools引擎打交道的会话，基于KieBase创建。
		//kmodule.xml :
		KieSession kieSession = kieContainer.newKieSession();

		Score score = new Score();
		score.setName("wqh");
		score.setScore(80);
		kieSession.insert(score);
		kieSession.startProcess("drools_flow_no1");
		kieSession.fireAllRules();
		System.out.println(score);
		kieSession.dispose();
	}

}
