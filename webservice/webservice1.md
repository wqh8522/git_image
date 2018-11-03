---
title: WebService入门介绍
date: 2017-08-21 11:12:38
tags: webservice
---
## 什么是WebService ##
- Web service 即web服务，它是一种跨编程语言和跨操作系统平台的远程调用技术即跨平台远程调用技术。
- 采用标准SOAP(Simple Object Access Protocol)  协议传输，soap属于w3c标准。Soap协议是基于http的应用层协议，soap协议传输是xml数据。
<!--more-->
- 采用wsdl作为描述语言即webservice使用说明书，wsdl属w3c标准。
- xml是webservice的跨平台的基础，XML主要的优点在于它既与平台无关，又与厂商无关。
- XSD，W3C为webservice制定了一套传输数据类型，使用xml进行描述，即XSD(XML Schema Datatypes)，任何编程语言写的webservice接口在发送数据时都要转换成webservice标准的XSD发送。
- 当前非SOAP协议的webService以轻量为首要目标，比如rest webservice也是webservice的一种方式。

## WebService规范简单介绍 ##
在java中，共有三种webservice规范：JAX-WS（JAX-RPC）、JAXM&SAAJ、JAX-RS。
### JAX-WS（JAX-RPC） ###
>JAX-WS  的全称为 Java API for XML-Based Webservices ，JAX-WS允许开发者可以选择RPC-oriented或者message-oriented 来实现自己的web services。早期的基于SOAP 的JAVA 的Web 服务规范JAX-RPC（Java API For XML-Remote Procedure Call）目前已经被JAX-WS 规范取代。从java5开始支持JAX-WS2.0版本，Jdk1.6.0_13以后的版本支持2.1版本，jdk1.7支持2.2版本。

### JAXM&SAAJ ###
>JAXM（JAVA API For XML Message）主要定义了包含了发送和接收消息所需的API，SAAJ（SOAP With Attachment API For Java，JSR 67）是与JAXM 搭配使用的API，为构建SOAP 包和解析SOAP 包提供了重要的支持，支持附件传输等，JAXM&SAAJ 与JAX-WS 都是基于SOAP 的Web 服务，相比之下JAXM&SAAJ 暴漏了SOAP更多的底层细节，编码比较麻烦，而JAX-WS 更加抽象，隐藏了更多的细节，更加面向对象，实现起来你基本上不需要关心SOAP 的任何细节

### JAX-RS ###
>JAX-RS是JAVA EE6 引入的一个新技术。 JAX-RS即Java API for RESTful Web Services，是一个Java 编程语言的应用程序接口，支持按照表述性状态转移（REST）架构风格创建Web服务。JAX-RS使用了Java SE5引入的Java注解来简化Web服务的客户端和服务端的开发和部署。

## 使用JAX-WS开发WebService入门实例 ##
### 服务端发开发 ###
编写SEI(Service Endpoint Interface)，在WebService中称为portType,在java中为接口

```java
	/**
	 * 基于jaxws开发的SEI
	 * 天气查询接口
	 */
	public interface WeatherInterface {
	    /*根据城市查询天气*/
	    String queryWeather(String cityName);
	}

```

接口实现类，也就是WebService的服务类

```java

	/*使用注解标注为WebService的服务类*/
	@WebService
	public class WeatherInterfaceImpl implements WeatherInterface{
	    public String queryWeather(String cityName) {
	        System.out.println("客户端要查询的城市==="+cityName);
	        String result = "多云";
	        System.out.println("像客户端返回查询结果"+result);
	        return result;
	    }
	    public static void main(String[] srgs){
	        /*
	        发布天气查询服务
	        第一个参数：WebService的地址
	        第二个参数：使用了@WebService标注的类
	        */
	        Endpoint.publish("http://127.0.0.1:1234/webther",new WeatherInterfaceImpl());
	
	    }
	}

```

注：SEI的实现类中至少要有一个非静态的公开方法作为WebService的服务方法。在类上需要使用注解@WebService
### 查看wsdl ###
1. 在浏览器地址栏输入地址：http://127.0.0.1:1234/webther?wsdl
2.	Wsdl不是webService,只是获取一个用于描述WebService的说明文件
3.	wsdl- WebServiceDescriptionLanguage,是以XML文件形式来描述WebService的”说明书”,有了说明书,我们才可以知道如何使用或是调用这个服务.
### 使用Wsimport生成客户端调用代码 ###
#### Wsimport介绍 ####
>wsimport是jdk自带的webservice客户端工具,可以根据wsdl文档生成客户端调用代码(java代码).当然,无论服务器端的WebService是用什么语言写的,都可以生成调用webservice的客户端代码，服务端通过客户端代码调用webservice。 
wsimport.exe位于JAVA_HOME\bin目录下.

常用参数为:

- -d<目录>  - 将生成.class文件。默认参数。
- -s<目录> - 将生成.java文件。
- -p<生成的新包名> -将生成的类，放于指定的包下。
(wsdlurl) - http://server:port/service?wsdl，必须的参数。

示例： C:/> wsimport –s . http://127.0.0.1:1234/weather?wsdl 

注意：-s不能分开，-s后面有个小点，点表示将代码放到当前目录下。
http….是指获取wsdl说明书的地址.

#### 客户端编写 ####
将上面使用wsimport生成的代码拷贝到项目中，然后编写客户端调用的代码
1、使用服务视图调用WebService
```java
//创建服务视图
WeatherInterfaceImplService weatherInterfaceImplService = new WeatherInterfaceImplService();
//通过服务视图得到服务断点
WeatherInterfaceImpl port = weatherInterfaceImplService.getPort(WeatherInterfaceImpl.class);
String result = port.queryWeather("深圳");
System.out.println(result);

```
2、使用jdk的service类调用webservice
```java
//wsdl路径
URL wsdlUrl = new URL("http://127.0.0.1:1234/webther?wsdl");
//从wsdl中找到服务视图
//第一个参数是wsdl的命名空间
//第二个参数是服务视图的名字
QName qName = new QName("http://jaxws.wqh.com/", "WeatherInterfaceImplService");
//创建service对象获取服务视图
Service service = Service.create(wsdlUrl,qName);
//从服务视图中获取portType
WeatherInterfaceImpl weatherInterface = service.getPort(WeatherInterfaceImpl.class);
//调用portType的方法
String result = weatherInterface.queryWeather("南昌");
System.out.println(result);
```

## WebService与Socket比较 ##

![](http://i.imgur.com/6x7WBwq.png)

- Socket  是基于TCP/ip的传输层协议
- Webservice是基于http协议传输数据，http是基于tcp的应用层协议
- Webservice采用了基于http的soap协议传输数据。
- Socket接口通过流传输，不支持面向对象。
- Webservice 接口支持面向对象，最终webservice将对象进行序列化后通过流传输。

结论：Webservice采用soap协议进行通信，底层基于socket通信，webservice不需专门针对数据流的发送和接收进行处理，是一种跨平台的面向对象远程调用技术。Socket传输速率更快。

## WebService的使用介绍 ##
### 使用场景 ###
1.	应用程序集成
分布式程序之间进行集成使用webservice直接调用服务层方法，不仅缩短了开发周期，还减少了代码复杂度，并能够增强应用程序的可维护性，因为webservice支持跨平台且遵循标准协议（soap）。
2.	软件重用
将一个软件的功能以webservice方式暴露出来，达到软件重用。例如上边分析的天气预报，将天气查询功能以webservice接口方式暴露出来非常容易集成在其它系统中；再比如一个第三方物流系统将快递查询、快递登记暴露出来，从而集成在电子商务系统中。

### 建议不用webservice ###


1.	同构程序间通信
同构程序是指采用相同的编程语言的程序之间通信，比如java远程调用RMi技术就可以非常高效的实现远程调用，使用简单方便，必需保证两边应用都是java编写才可使用。

总之，只要有其它方法比webservice更高效更可行就不要用webservice，因为web跨平台远程调用方法不止webservice一种，需要择优考虑。
RMI是java语言提供的远程调用技术。Rmi速度会比webservice快的多，比socket要慢。
### 建议使用webservice ###
1.	公开接口
面向互联网公开的接口，例如：某公司产品促销介绍、股票信息查询等，因为webservice是互联网的一个标准协议，将接口发布为webservice，其它公司很容易使用。
2.	调用webservice服务端
你作为客户端要调用别人的接口，对方接口用的是webservice，这时你也用webservice开发客户端，且协议版本要和服务端保持一致。

### Webservice优缺点 ###
优点：
1. 采用xml支持跨平台远程调用。
2. 基于http的soap协议，可跨越防火墙。
3. 支持面向对象开发。
4. 有利于软件和数据重用，实现松耦合。

缺点：
由于soap是基于xml传输，本身使用xml传输会传输一些无关的东西从而效率不高，随着soap协议的完善，soap协议增加了许多内容，这样就导致了使用soap协议去完成简单的数据传输的效率不高。如果直接用http传输自定义数据内容比webservice开发更快捷，例如第三方支付公司的支持接口。


