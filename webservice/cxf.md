---
title: WebService之CXF
date: 2017-08-23 11:52:00
tags: webservice
---
## 什么是CXF ##
Apache CXF = Celtix + XFire，开始叫 Apache CeltiXfire，后来更名为 Apache CXF 了，以下简称为 CXF。CXF 继承了 Celtix 和 XFire 两大开源项目的精华，提供了对 JAX-WS 全面的支持，并且提供了多种 Binding 、DataBinding、Transport 以及各种 Format 的支持，并且可以根据实际项目的需要，采用代码优先（Code First）或者 WSDL 优先（WSDL First）来轻松地实现 Web Services 的发布和使用。Apache CXF已经是一个正式的Apache顶级项目。

<!--more-->
## CXF与Spring整合 ##
### 需求 ###
CXF与spring整合，实现手机号码归属地查询。所以这里我们需要调用公网的号码归属地查询。
### 导入公网的接口 ###
公网地址：[http://www.webxml.com.cn/zh_cn/index.aspx](http://www.webxml.com.cn/zh_cn/index.aspx)
手机号码归属地查询WSDL：[http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?wsdl](http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?wsdl)

在CMD中输入命令：wsimport -s . http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx?wsdl生成公网的调用代码，将代码复制到工程中

### 添加依赖 ###
```xml
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxws</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-frontend-jaxrs</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-rt-transports-http</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf</artifactId>
    <version>${cxf.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>${spring.version}</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-web</artifactId>
    <version>${spring.version}</version>
</dependency>
```
### 服务接口 ###
这里与jaxws不同的是只要在服务接口类上添加@WebService注解既可
```java
/**
 * 基于jaxws开发的SEI
 * 天气查询接口
 */
@WebService(targetNamespace = "http://service.wqh.com",
name = "PhoneQueryInterface",
portName = "PhoneQueryInterfacePort",
serviceName = "PhoneQueryInterfaceService")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)//发布soap1.2
public interface PhoneQueryInterface {
    String getMobileCodeInfo (String mobileCode);
}

```

### 接口实现类 ###

```java
public class PhoneQueryInterfaceImpl implements PhoneQueryInterface {

    /*公网的号码归属地查询对象*/
    private MobileCodeWSSoap mobileCodeWSSoap;

    public String getMobileCodeInfo(String mobileCode) {
        String codeInfo = mobileCodeWSSoap.getMobileCodeInfo(mobileCode, null);
        return codeInfo;
    }

    public MobileCodeWSSoap getMobileCodeWSSoap() {
        return mobileCodeWSSoap;
    }

    public void setMobileCodeWSSoap(MobileCodeWSSoap mobileCodeWSSoap) {
        this.mobileCodeWSSoap = mobileCodeWSSoap;
    }
}

```
### 添加配置信息 ###
applicationContext.xml：

```java

<!-- 配置发布webservice服务 -->
<jaxws:server address="/mobil" serviceClass="com.wqh.cxf.service.PhoneQueryInterface">
    <jaxws:serviceBean>
        <ref bean="phoneQuery"></ref>
    </jaxws:serviceBean>
</jaxws:server>
<!--
配置公网号码归属地查询的客户端
address：公网归属地的地址
-->
<jaxws:client id="mobileCodeWSSoap" serviceClass="cn.com.webxml.MobileCodeWSSoap"
              address="http://ws.webxml.com.cn/WebServices/MobileCodeWS.asmx"/>
<!--归属地查询的bean-->
<bean id="phoneQuery" class="com.wqh.cxf.service.PhoneQueryInterfaceImpl">
    <property name="mobileCodeWSSoap" ref="mobileCodeWSSoap">
    </property>
</bean>

```
web.xml

```java
<!-- 加载spring容器 -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
</context-param>
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
<servlet>
    <description>Apache CXF Endpoint</description>
    <display-name>cxf</display-name>
    <servlet-name>cxf</servlet-name>
    <servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>cxf</servlet-name>
    <url-pattern>/ws/*</url-pattern>
</servlet-mapping>

```
启动服务器

### 查看wsdl ###
在浏览器地址栏输入：
http://127.0.0.1:8080/｛项目名｝/ws/mobil?wsdl

### 编写客户端 ###
这里客户端可以使用前面介绍的wsimiport命令，也可以使用wsdl2java命令。如果使用wsdl2java命令需要在系统中添加cxf的环境变量。


下载CXF [http://cxf.apache.org/download.html](http://cxf.apache.org/download.html)

解压下载的文件，将bin目录添加到环境变量中。
然后使用命令：wsdl2java –d . –frontend jaxws21 http://127.0.0.1:8080/｛项目名｝/ws/mobil?wsdl

注意：这里加上了–frontend jaxws21。原因是因为：cxf需要JAX-WS API 2.2而jdk6的jax-ws是2.1 版本，需要wsdl2java 使用“-frontend jaxws21”

代码与jaxws方式一样

```java
//创建服务视图
PhoneQueryInterfaceService phoneQueryInterfaceService = new PhoneQueryInterfaceService();
//通过服务视图得到服务端点
PhoneQueryInterface phoneQueryInterfaceServicePort = phoneQueryInterfaceService.getPort(PhoneQueryInterface.class);
//调用webservice服务方法
String result1 = phoneQueryInterfaceServicePort.getMobileCodeInfo("号码");
System.out.println(result1);
```
## CXF实现rest服务 ##
### 什么是rest服务 ###
REST 是一种软件架构模式，只是一种风格，rest服务采用HTTP 做传输协议，REST 对于HTTP 的利用分为以下两种：资源定位和资源操作。
- 资源定位：
>Rest要求对资源定位更加准确，如下：
非rest方式：http://ip:port/queryUser.action?userType=student&id=001
Rest方式：http://ip:port/user/student/001
Rest方式表示互联网上的资源更加准确，但是也有缺点，可能目录的层级较多不容易理解。

- 资源操作：
>利用HTTP 的GET、POST、PUT、DELETE 四种操作来表示数据库操作的SELECT、UPDATE、INSERT、DELETE 操作。
比如：
查询学生方法：
设置Http的请求方法为GET，url如下：http://ip:port/user/student/001
添加学生方法：
设置http的请求方法为PUT，url如下：http://ip:port/user/student/001/张三/......

Rest常用于资源定位，资源操作方式较少使用。

REST 是一种软件架构理念，现在被移植到Web 服务上，那么在开发Web 服务上，偏于面向资源的服务适用于REST，REST 简单易用，效率高，SOAP 成熟度较高，安全性较好。
注意：REST 不等于WebService，JAX-RS 只是将REST 设计风格应用到Web 服务开发上。
参考：[理解RESTful架构](http://www.ruanyifeng.com/blog/2011/09/restful "理解RESTful架构")

### rest服务发布 ###
#### 需求 ####
发布查询学生信息的服务，返回的数据格式为json和xml
#### 服务接口 ####
pojo：在pojo类上使用@XmlRootElement注解，是该类对象可以装换成xml
```java
@XmlRootElement(name = "student")
public class Student {

	private long id;
	private String name;
	private Date birthday;
	//getset
}
```
服务类接口：
```java
@WebService
@Path("/student")
@BindingType(javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING)//发布soap1.2
public interface StudentService {
	
	//查询学生信息
	@GET //http的get方法
	@Path("/query/{id}")//id参数通过url传递
	@Produces(MediaType.APPLICATION_XML)//设置媒体类型xml格式
    Student queryStudent(@PathParam("id")long id);
	
	
	//查询学生列表
	@GET //http的get方法
	@Path("/querylist/{type}")
	@Produces({"application/json;charset=utf-8",MediaType.APPLICATION_XML})//设置媒体类型xml格式和json格式
	//如果想让rest返回xml需要在rest的url后边添加?_type=xml，默认为xml
	//如果想让rest返回json需要在rest的url后边添加?_type=json
	List<Student> queryStudentList(@PathParam("type") String type);

}
```
接口实现类：

```java
public class StudentServiceImpl implements StudentService {

	@Override
	public Student queryStudent(long id) {
		// 使用静态数据
		Student student = new Student();
		student.setId(id);
		student.setName("张三");
		student.setBirthday(new Date());
		return student;
	}

	@Override
	public List<Student> queryStudentList(String type) {
		List<Student> list = new ArrayList<Student>();
		Student student1 = new Student();
		student1.setId(1234);
		student1.setName("张三");
		student1.setBirthday(new Date());
        
		Student student2 = new Student();
        student2.setId(2345);
        student2.setName("李四");
        student2.setBirthday(new Date());


        Student student3 = new Student();
        student3.setId(3456);
        student3.setName("王五");
        student3.setBirthday(new Date());

        list.add(student1);
		list.add(student2);
		list.add(student3);
		return list;
	}

}
```
#### 发布服务 ####
1、使用java代码发布，如果直接使用程序发布，需要导入cxf.jaxrsde 
```java
//使用jaxrsServerFactoryBean发布rest服务
JAXRSServerFactoryBean jaxrsServerFactoryBean = new JAXRSServerFactoryBean();
//设置rest的服务地址
jaxrsServerFactoryBean.setAddress("http://127.0.0.1:12345/rest");
//设置服务对象
jaxrsServerFactoryBean.setServiceBean(new StudentServiceImpl());
//设置资源 对象，如果有多个pojo资源 对象中间以半角逗号隔开
jaxrsServerFactoryBean.setResourceClasses(StudentServiceImpl.class);
//发布rest服务
jaxrsServerFactoryBean.create();
```
2、整合spring发布，在applicationContext.xml中配置
```java
<!-- service -->
<bean id="studentService" class="com.wqh.ws.cxf.rest.service.StudentServiceImpl"/>

<!-- 发布rest服务
使用jaxws:server和jaxws:endpoint可以发布服务
webservice地址=tomcat地址+cxf servlet的路径+/rest

这里与之前不同的是，不需要添加serviceClass
 -->
<jaxrs:server address="/rest">
	<jaxrs:serviceBeans>
	   <ref bean="studentService"/>
	</jaxrs:serviceBeans>
</jaxrs:server>

```
这里rest服务是生成wadl说明文档，查看wadl在地址栏输入：http://localhost:8080/webcxf/ws/rest?_wadl
![](http://i.imgur.com/JOt5ubZ.png)
这里能看到一些发布的信息，然后在地址栏中请求发布的服务：
http://localhost:8080/webcxf/ws/rest/student/query/1323

![](http://i.imgur.com/tLQzzPQ.png)

查看json格式数据：_type指定要返回的数据格式
http://localhost:8080/webcxf/ws/rest/student/querylist/1323?_type=json

![](http://i.imgur.com/HGBkyCK.png)

## 总结 ##
WebService是一个平台独立的，低耦合的，自包含的、基于可编程的web的应用程序。WebService的发也有很多种方式，比如常见的jax-ws和CXF。对于jax-ws开发，感觉比较简单与方便，不需要添加太多的jar依赖；但是有一定的局限性。而对于CXF，jar包依赖太过复杂，使用maven添加需要更加注意。