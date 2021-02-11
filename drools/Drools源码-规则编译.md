# Drools源码-规则编译

## Drools7执行规则方式

```java
Cheese c = new Cheese();
c.setName("cheddar1");
Person p = new Person();
p.setFavouriteCheese(c);
p.setName("pppp");
//Resource是用于为底层IO资源提供Reader或InputStream的通用接口。
Resource drlRe1 = ResourceFactory.newClassPathResource("com/wqh/demo/drools/rule/Nodeshared.drl", "UTF-8",TestHelper.class);
//KieServices是一个线程安全的单例，充当集线器，可以访问Kie提供的其他服务。作为一般规则，getX（）方法只返回对另一个单例的引用，而newX（）方法创建一个新实例。
KieServices kieServices = KieServices.Factory.get();
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
```

## Drools5执行规则

```java
 Cheese c = new Cheese();
        c.setName("cheddar1");

        Person p = new Person();
        p.setFavouriteCheese(c);
        p.setName("person");
//      KnowledgeBuilder：知识库生成器负责获取文件，如.drl文件、.bpmn2文件或.xls文件；并将其转换为知识库可以使用的规则和流程定义的知识包（KnowledgePackage）
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
```

## 规则执行

Drools