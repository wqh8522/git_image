---
title: SpringAOP的使用
date: 2017-08-28 10:10:56
categories: spring
tags: [spring,AOP]
---
## 什么是AOP ##
AOP（Aspect Oriented Programming 面向切面编程），通过预编译方式和运行期动态代理实现程序功能的统一维护的一种技术。AOP是OOP的延续，是软件开发中的一个热点，也是Spring框架中的一个重要内容，是函数式编程的一种衍生范型。利用AOP可以对业务逻辑的各个部分进行隔离，从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性，同时提高了开发的效率。

常用于日志记录，性能统计，安全控制，事务处理，异常处理等等。

## 定义AOP术语 ##

<!--more-->
切面（Aspect）：切面是一个关注点的模块化，这个关注点可能是横切多个对象；

连接点（Join Point）：连接点是指在程序执行过程中某个特定的点，比如某方法调用的时候或者处理异常的时候；

通知（Advice）：指在切面的某个特定的连接点上执行的动作。Spring切面可以应用5中通知：

 - 前置通知（Before）:在目标方法或者说连接点被调用前执行的通知；
 - 后置通知（After）：指在某个连接点完成后执行的通知；
 - 返回通知（After-returning）：指在某个连接点成功执行之后执行的通知；
 - 异常通知（After-throwing）：指在方法抛出异常后执行的通知；
 - 环绕通知（Around）：指包围一个连接点通知，在被通知的方法调用之前和之后执行自定义的方法。

切点（Pointcut）：指匹配连接点的断言。通知与一个切入点表达式关联，并在满足这个切入的连接点上运行，例如：当执行某个特定的名称的方法。

引入（Introduction）：引入也被称为内部类型声明，声明额外的方法或者某个类型的字段。

目标对象（Target Object）：目标对象是被一个或者多个切面所通知的对象。

AOP代理（AOP Proxy）：AOP代理是指AOP框架创建的对对象，用来实现切面契约（包括通知方法等功能）

织入（Wearving）：指把切面连接到其他应用出程序类型或者对象上，并创建一个被通知的对象。或者说形成代理对象的方法的过程。

![](http://i.imgur.com/NNwG4Yz.png)

## Spring对AOP的支持 ##

 1. 基于代理的经典SpringAOP；
 2. 纯POJO切面；
 3. @AspectJ注解驱动的切面；
 4. 注入式AspectJ切面（适用于Spring各版本）；

前三种都是SpringAOP实现的变体，SpringAOP构建在动态代理基础之上，因此，Spring对AOP的支持局限于方法的拦截。


## 切入点表达式 ##
![](http://i.imgur.com/z8MbDIP.png)


## 使用SpringAOP ##
SpringAOP的支持必须呀导入spring-aspects的jar包
```java
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-aspects</artifactId>
    <version>4.3.5.RELEASE</version>
</dependency>
```

### 使用注解定义切面 ###
采用注解的方式定义切面以及通知


	

```java
@Aspect
public class Audience {
    //使用@Pointcut注解声明频繁使用的切入点表达式
    @Pointcut("execution(* com.wqh.concert.Performance.perform(..))")
    public void performance(){}
    
    @Before("performance()")
    public void silenceCellPhones(){
        System.out.println("Sillencing cell phones");
    }
    @Before("performance()")
    public void takeSeats(){
        System.out.println("Task Seat");
    }
    @AfterReturning("performance()")
    public void applause(){
        System.out.println("CLAP CLAP CLAP");
    }
    @AfterThrowing("performance()")
    public void demandRefund(){
        System.out.println("Demand a Refund");
    }
}
```

另外需要在applicationContext.xml也就是spring的配置文件中添加配置：
	
	

```java
<!--启用AspectJ的自动代理-->
<aop:aspectj-autoproxy/>
<!--声明bean-->
<bean class="com.wqh.concert.Audience"/>
```

### 在XML中声明切面 ###
定义pojo类，这里只是把上面定义的注解全public class AudienceXML {
```java
    public void silenceCellPhones() {
        System.out.println("Sillencing cell phones");
    }
    public void takeSeats() {
        System.out.println("Task Seat");
    }
    public void applause() {
        System.out.println("CLAP CLAP CLAP");
    }
    public void demandRefund() {
        System.out.println("Demand a Refund");
    }
```
applicationContext.xml配置
```java
<!--声明bean-->
<bean name="audienceXML" class="com.wqh.concert.AudienceXML"/>
<aop:config>
    <!--引入bean-->
    <aop:aspect ref="audienceXML">
        <!--定义切点-->
        <aop:pointcut id="perform"
                      expression="execution(* com.wqh.concert.Performance.perform(..))"/>
        <!--定义通知
            method：通知，也就是具体的方法
            pointcut-ref：引用的切点
            pointcut：切点-->
        <aop:before method="silenceCellPhones"
                    pointcut-ref="perform"/>

        <aop:before method="takeSeats" pointcut-ref="perform"/>
        <aop:after-returning method="applause" pointcut-ref="perform"/>

        <aop:after-throwing method="demandRefund"
                            pointcut="execution(* com.wqh.concert.Performance.perform(..))"/>
    </aop:aspect>
</aop:config>
```

![](http://i.imgur.com/uGg9KUl.png)

## 环绕通知 ##
在springAOP中有五种通知，环绕通知是最为强大的通知。它能够让你编写的逻辑将被通知的目标方法完全包装起来。实际上就像在一个通知方法中同时编写前置通知和后置通知。
本片文章具体讲解环绕通知的使用。
### 使用注解 ###
使用环绕通知定义切面：

	

```java
@Aspect
public class AudienceAround {
    //使用@Pointcut注解声明频繁使用的切入点表达式
    @Pointcut("execution(* com.wqh.concert.Performance.perform(..))")
    public void performance(){}

    @Around("performance()")
    public void watchPerformance(ProceedingJoinPoint joinPoint){

        try {
            System.out.println("Silencing cell phones");
            System.out.println("Taking seats");
            joinPoint.proceed();
            System.out.println("Demanding a refund");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
```

可以看到在上面的代码中，定义通知的时候在通知方法中添加了入参：ProceedingJoinPoint。在创建环绕通知的时候，这个参数是必须写的。因为在需要在通知中使用ProceedingJoinPoint.proceed()方法调用被通知的方法。

另外，如果忘记调用proceed()方法，那么通知实际上会阻塞对被通知方法的调用。

### 在XML中定义 ###
首先去掉上面类的所有注解：这里为了区别就重新创建一个类

```java
public class AudienceAroundXML {
    public void watchPerformance(ProceedingJoinPoint joinPoint){
        try {
            System.out.println("Silencing cell phones");
            System.out.println("Taking seats");
            joinPoint.proceed();
            System.out.println("Demanding a refund");

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
```

配置：
	
```java
<!--声明bean-->
<bean name="audienceAroundXML" class="com.wqh.concert.AudienceAroundXML"/>
	<!--配置切面及通知-->
<aop:config>
    <aop:aspect ref="audienceAroundXML">
        <aop:pointcut id="performance"
                      expression="execution(* com.wqh.concert.Performance.perform(..))"/>
        <aop:around method="watchPerformance" pointcut-ref="performance"/>
    </aop:aspect>
</aop:config>
```

## 处理通知中的参数 ##
### Spring借助AspectJ的切点表达式语言来定义Spring切面 ###

![](http://i.imgur.com/fXL2qhm.png)

在spring中尝试使用其他指示器时，会抛出IllegalArgument-Exception异常。

如上的这些指示器，只有exception指示器是实际执行匹配的，而其他都是用来限制匹配的。

### 切面表达式分析 ###
带参数的切点表达式分解

![](http://i.imgur.com/1QRixjd.png)

在该切点表达式中使用了args(trackNumber)限定符。表示传递给playTrack()方法的int类型参数也会传递到通知中去。参数名trackNumber也与切点方法签名中的参数相匹配。

### 创建切面 ###
	

```java
@Aspect
public class TrackCounter {

    @Pointcut("execution(* com.wqh.aop.CompactDisc.playTrack(int))&&args(trackNumber)")
    public void trackPlayder(int trackNumber){}

    @Before("trackPlayder(trackNumber)")
    public void countTrack(int trackNumber) {
        System.out.println("前置通知:targetNumber=" + trackNumber);
    }
}
	
```
	
### 连接点类 ###

```java
@Service
public class CompactDisc {
    public void playTrack(int trackNumber){
        System.out.println("trackNumber =" + trackNumber);
    }
}
```
	
### XML配置 ###

 	

```java
<!--启用AspectJ的自动代理-->
<aop:aspectj-autoproxy/>

<!--声明bean-->
	<bean class="com.wqh.aop.TrackCounter"/>
	<!--自动扫描包下的类-->
<context:component-scan base-package="com.wqh.aop"/>
```

### 测试 ###
  

```java
@Test
public void testT(){
    ApplicationContext applicationContext =
            new ClassPathXmlApplicationContext(
                    new String[]{"classpath:/spring/applicationContext.xml"});
    CompactDisc compactDisc = (CompactDisc) applicationContext.getBean("compactDisc");
    compactDisc.playTrack(12);
}
   	
```

上面给指定方法传入的参数是12，在通知中获取到了该参数

![](http://i.imgur.com/Zrr0oSB.png)

另外：在xml中配置切面来处理通知中的参数，其实也差不多，只是把切点表达式放到了XML配置文件中。
## 给类添加新的功能 ##
### 引入Spring实战中的知识 ###
在SpringAOP中，我们可以为Bean引入新的方法。代理拦截器调用并委托给实现该方法的其他对象。
![](http://i.imgur.com/0vLHPrw.png)

当引入接口的方法被调用时，代理会把此调用委托给实现了新接口的某给其他对象。

### 使用注解方式引入 ###
#### 代码 ####
首先是连接点的接口及其实现类

```java
public interface Person {
    void say();
}
```

```java
public class ChinesePerson implements Person {
    @Override
    public void say() {
        System.out.println("说中文");
    }
}
```
	
创建需要添加的功能，这里个人类扩展一个吃的功能

	

```java
public interface Food {
    void eat();
}
```

```java
public class ChineseFood implements Food {
    @Override
    public void eat() {
        System.out.println("吃中餐");
    }
}
	
```

#### 编写切面 ####

	

```java
@Aspect
public class addFuction {
    @DeclareParents(value = "com.wqh.addfunction.Person+",defaultImpl = ChineseFood.class)
    public static Food food;
}
```

注意这里的表达式使用的式@DeclareParents注解；该注解所标注的静态属性指明了要引入的接口。
注解中使用的value属性指定哪种类型的bean要引入该接口，这里Person后后面的“+”号表示所有子类型，而不是该类的本身。defaultImpl，指定了为引入功能提供实现的类。

使用XML配置bean：
```java
<!--启用AspectJ的自动代理-->
<aop:aspectj-autoproxy/>

<!--声明bean-->
<bean class="com.wqh.addfunction.addFuction"/>
<bean name="chinesePerson" class="com.wqh.addfunction.ChinesePerson"/>
```
###  测试 ###
```java
@Test
public void testAdd(){
    ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "classpath:spring/applicationContext.xml");
    Person person = (Person) applicationContext.getBean("chinesePerson");
    person.say();
    //这里可以将chinesePerson bean转换为Food类，所以添加成功
    Food food = (Food) applicationContext.getBean("chinesePerson");
    food.eat();
}
```

![](http://i.imgur.com/znG9krq.png)

### 在XML中引入 ###
首先将上面的addFuction注解全部删除，其他不变；然后在xml中添加相应的配置：
```java
<!--启用AspectJ的自动代理-->
<aop:aspectj-autoproxy/>
<!--声明bean-->
<bean name="chinesePerson" class="com.wqh.addfunction.ChinesePerson"/>

 <aop:config>
     <aop:aspect>
         <aop:declare-parents types-matching="com.wqh.addfunction.Person+"
                              implement-interface="com.wqh.addfunction.Food"
         default-impl="com.wqh.addfunction.ChineseFood"/>
     </aop:aspect>
 </aop:config>
```
     
这里的types-matching与上面的vale作用一样;</br>
default-impl与defaultImpl作用一样，这也可以使用delegate-ref；当然如果使用delegate-ref则是要引用SpringBean；</br>
implement-interface则是要引入的接口
