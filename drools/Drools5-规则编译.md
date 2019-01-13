# Drools5-规则执行流程

## 执行规则

- 规则文件

```java
//created on: 2019-1-3
package node

dialect "mvel"

import com.wqh.demo.pojo.Cheese
import com.wqh.demo.pojo.Person

rule cheddar
when
    $cheddar : Cheese( $cheddarName : name == "cheddar" )
    $person : Person( favouriteCheese == $cheddar )
    //eval( $person.getName() == $cheddar )
then
    System.out.println( $person.getName() + " likes cheddar" );
end

rule cheddar1
when
    $cheddar : Cheese( $cheddarNaame : name == "cheddar" )
    $person : Person( favouriteCheese != $cheddar )
then
    System.out.println( $cheddar + " does not like cheddar1" );
    System.out.println( $person.getName() + " does not like cheddar1" );
end

rule cheddar2
when
    $cheddar : Cheese( $cheddarName : name != "cheddar" )
    $person : Person( favouriteCheese == $cheddar )
then
    System.out.println( $person.getName() + " does not like cheddar2" );
end

rule cheddar3
when
    $cheddar : Cheese( $cheddarName : name != "cheddar" )
    $person : Person( favouriteCheese != $cheddar )
then
    System.out.println( $person.getName() + " does not like cheddar2" );
end
```

- 规则执行

```java
@Test
public void executeDrools(){
    Cheese c = new Cheese();
    c.setName("cheddar1");

    Person p = new Person();
    p.setFavouriteCheese(c);
    p.setName("person");
    //KnowledgeBuilder：知识库生成器负责获取文件，如.drl文件、.bpmn2文件或.xls文件；并将其转换为知识库可以使用的规则和流程定义的知识包（KnowledgePackage）
    KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    //编译规则
  knowledgeBuilder.add(ResourceFactory.newClassPathResource("rule/Nodeshared.drl",this.getClass()), ResourceType.DRL);
    //hasErrors：得到编译规则过程中发现规则是否有错误，可以通过getErrors获取错误
    if (knowledgeBuilder.hasErrors()){
        logger.error("规则配置有问题，{}",knowledgeBuilder.getErrors().toString());
    }
    //产生规则包集合
    Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
    //创建KnowledgeBaseConfiguration对象，用来存放规则引擎运行时环境参数的配置对象
    KnowledgeBaseConfiguration kbConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
    //KnowledgeBase：收集应用当中知识（knowledge）定义的知识库对象，可以指定一个KnowledgeBaseConfiguration对象：
    KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(kbConfig);
    //将 KnowledgePackage 集合添加到 KnowledgeBase 当中
    knowledgeBase.addKnowledgePackages(knowledgePackages);
    //编译完成将规则包文件在引擎当中运行，
    //StatefulKnowledgeSession：一种常见的与规则引擎进行交互的方式，他可以与规则引擎建立一个持续的交互通道，在推理计算的过程当中可能会多次触发统一数据集
    StatefulKnowledgeSession kSession = knowledgeBase.newStatefulKnowledgeSession();
    kSession.insert(c);
    kSession.insert(p);
    //释放内存资源
    kSession.fireAllRules();
    kSession.dispose();
    //StatelessKnowledgeSession 对StatefulKnowledgeSession做了一定的封装，使用execute执行规则
    //StatelessKnowledgeSession statelessKnowledgeSession = knowledgeBase.newStatelessKnowledgeSession();
    //statelessKnowledgeSession.execute(product);
}
```

## 规则编译流程

在Drools5中规则执行的流程：规则编写——规则加载——规则编译——规则执行

### 规则文件加载

在Drools5中可以使用如下代码将规则文件.drl加载到`KnowledgeBuilder`(知识库)

```java

```



