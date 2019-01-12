package com.wqh.demo.drools.flow;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.KieResources;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

import com.wqh.demo.drools.pojo.Cheese;
import com.wqh.demo.drools.pojo.Person;
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

	@Test
	public void test11(){
		Cheese c = new Cheese();
		c.setName("cheddar1");
		
		Person p = new Person();
		p.setFavouriteCheese(c);
		p.setName("pppp");

		//KieServices是一个线程安全的单例，充当集线器，可以访问Kie提供的其他服务。作为一般规则，getX（）方法只返回对另一个单例的引用，而newX（）方法创建一个新实例。
		KieServices kieServices = KieServices.Factory.get();

		//用于为底层IO资源提供Reader或InputStream的通用接口。
		Resource drlRe1 = ResourceFactory.newClassPathResource("com/wqh/demo/drools/rule/Nodeshared.drl", "UTF-8",TestHelper.class);

		//KieFileSystem是一个内存文件系统，用于以编程方式定义组成KieModule的资源
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
		//将给定的资源添加到此KieFileSystem
		kieFileSystem.write(drlRe1);
		//KieBuilder是KieModule中包含的资源的构建者
		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
		//构建KieModule中包含的所有KieBase
		kieBuilder.buildAll();
        //KieContainer:给定KieModule的所有KieBase的容器，
        //kieModule是定义一组KieBase所需的所有资源的容器，例如定义ReleaseId的pom.xml文件，声明KieBases名称和配置以及可以从它们创建的所有KieSession以及所有构建KieBases本身所需的其他文件
        //KieBase是所有应用程序知识定义的存储库。 它将包含规则，流程，功能，类型模型。 KieBase本身不包含运行时数据，而是从KieBase创建会话，其中可以插入数据并启动流程实例。
		KieContainer kieContainer = kieServices.newKieContainer(kieBuilder.getKieModule().getReleaseId());
		KieSession kieSession = kieContainer.newKieSession();

		kieSession.insert(c);
		kieSession.insert(p);
		kieSession.fireAllRules();

	}
}
