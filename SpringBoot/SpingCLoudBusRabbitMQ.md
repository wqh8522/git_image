---
title: SpingCloudBus整合RabbitMQ
date: 2018-01-25 14:22:55
categories: SpringCloud
tags: [SpringCloud,RabbitMQ]

---

# SpringCloudBus介绍

> SpringCloudBus：消息总线，可以将分布式系统的节点与轻量级消息代理连接，然后实现广播状态更改（如配置更改）或广播其他管理指令。总线就像一个分布式执行器，用于扩展SpringBoot应用程序，但可以用作应用程序之间的通信通道。
>
> 消息代理是一种消息验证、传输、路由的架构模式。它是一个中间产品，核心是一个消息的路由程序，用来实现接收和分发消息，并根据设定好的消息处理流来转发给正确的应用。通常一下场景需要使用消息代理<!--more-->
>
> - 将消息路由到一个或多个目的地
> - 消息转化为其他的表达方式
> - 执行消息的聚集、消息的分解，并将结果发送到它们的目的地，然后重新组合响应返回给信息用户
> - 调用Web服务来检索数据
> - 响应事件或错误
> - 使用发布-订阅模式提供内容或基于主题的消息内容。

# RabbitMQ实现消息总线

RibbitMQ是实现了高级消息队列协议（AMQP）的开源消息代理软件，也称为面向消息的中间件。这里假设你已经了解RabbitMQ，具体的安装就不介绍了，可自行Google。

## SpringBoot整合RabbitMQ

新建一个SpringBoot项目，这里命名hello-rabbitmq，然后添加`amqp`依赖。

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-amqp</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
```

在配置文件`application.properties`中添加`RabbitMQ`的相关配置

```java
spring.application.name=rabbitmq-hello

#配置rabbitmq的主机
spring.rabbitmq.host=192.168.18.133
#访问端口
spring.rabbitmq.port=5672
#安装RabbitMQ时配置的用户名
spring.rabbitmq.username=wqh
#安装RabbitMQ时配置的密码
spring.rabbitmq.password=wqh
```

然后创建消息生产者Sender，这里发送一串字符串到hello的消息对列中

```java
/*
 *消息生产者Sender使用AmqpTemplate接口的实例来实现消息的发送
 */
@Component
public class Sender {
    private final Logger logger = LoggerFactory.getLogger(Sender.class);
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sender(){
        String context = "wqh say hello " + new Date();
        logger.info("发送消息=========》》》》{}",context);
        this.amqpTemplate.convertAndSend("hello",context);
    }
}
```

消息接收者Receiver，实现了对hello消息队列的消费

```java
/**
 * 消息消费者Receiver 使用@RabbitListener注解定义该类对hello队列的监听, 并用@RabbitHandler 注解来指定对消息的处理方法
 *
 */
@Component
@RabbitListener(queues = "hello")
public class Receiver {
    private final Logger logger = LoggerFactory.getLogger(Receiver.class);

    @RabbitHandler
    public void receiver(String hello){
        logger.info("接收消息=====》》》》》{}",hello);
    }
}
```

创建RabbitMQ的配置类

```java
/**
 *  RabbitMQ的配置类，用来配队列、交换器、路由等高级信息
 */
public class RabbitConfig {
    @Bean
    public Queue helloConfig(){
        return new Queue("hello");
    }
}
```

创建测试类

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqApplicationTests {
	@Autowired
	private Sender sender;
	@Test
	public void hello(){
		sender.sender();
	}
}
```

上面说到一个hello队列，所以这里需要在RabbitMQ中添加一个hello队列，进入RabbitMQ的管理页面。

![](http://oy09glbzm.bkt.clouddn.com/18-1-24/84351839.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

启动项目，运行测试方法：

![](http://oy09glbzm.bkt.clouddn.com/18-1-24/73497814.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

## SpringCloudBus整合RabbitMQ

前面使用SpringCloudConfig构建了一个配置中心，[传送门](http://www.wanqhblog.top/2018/01/19/SpringCloudConfig/)  。前面的结构中如果远程仓库配置发生改变，我们需要调用每个服务的`/refresh`接口或者重启服务才能获取到最新的配置信息，这种方法在微服务架构中几乎是完全不可行的。使用SpringCloudBus和RabbitMQ整合可以优雅的实现应用配置的动态刷新。直接改造之前的config-client项目

- 添加`spring-cloud-starter-bus-amqp`依赖

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-starter-bus-amqp</artifactId>
</dependency>
```

- 在配置文件中添加RabbitMQ的相关配置，这里需要注意的要忽略权限，不然访问`/bus/refresh`接口的时候回返回`Full authentication is required to access this resource` 。

```java
#忽略权限拦截
management.security.enabled=false

spring.rabbitmq.host=192.168.18.133
spring.rabbitmq.port=5672
spring.rabbitmq.username=wqh
spring.rabbitmq.password=wqh
```

- 将配置文件复制一份，修改端口，然后打包。分别启动eureka-server服务发现、config-server配置中心、两个config-client实例。项目结构

![](http://oy09glbzm.bkt.clouddn.com/18-1-24/99612148.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

- 启动项目之后访问`http://localhost:60000/get_name`和`http://localhost:60001/get_name`获取到form属性。然后修改仓库中的中属性的值，会发现获取的到值并没有改变
- 然后发出一个post请求到一个config-client，访问接口`/bus/refresh` ，发现这时候两个服务中都可以获取打最新的值

在`/bus/refresh` 接口中有一个参数destination，该参数可以指定具体的实例刷新配置，还可指定具体的服务刷新配置。

- 指定实例，只会触发端口号为60000的

  ```
  /bus/refresh?destination=config-client:60000
  ```

- 指定服务，这里会触发config-client服务的所有实例进行刷新

  ```
  /bus/refresh?destination=config-client:**
  ```

在《SpringCloud微服务实战》中提到了一个系统架构的优化，就是将消息总线加入到配置中心，然后通过destination参数来指定更新配置的服务或实例。

![](http://oy09glbzm.bkt.clouddn.com/18-1-24/87259611.jpg-blog)

------

参考：《SpringCloud微服务实战》

示例地址：[https://gitee.com/wqh3520/](https://gitee.com/wqh3520/spring-cloud-1-9)

原文地址： [SpingCloudBus整合RabbitMQ](http://www.wanqhblog.top/2018/01/24/SpingCLoudBusRabbitMQ/)