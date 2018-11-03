---
title: WebService知识点
date: 2017-08-21 14:47:38
tags: webservice
---
## 三要素 ##
### SOAP ###
SOAP:简单对象访问协议（Simple Object Access Protocal），是一种简单的基于 XML 的协议，它使应用程序通过 HTTP 来交换信息，简单理解为soap=http+xml。Soap协议版本主要使用soap1.1、soap1.2<!--more-->
#### SOAP是什么 ####
- SOAP 是一种网络通信协议
- SOAP即Simple Object Access Protocol简易对象访问协议
- SOAP 用于跨平台应用程序之间的通信
- SOAP 被设计用来通过因特网(http)进行通信
- SOAP ＝ HTTP+XML，其实就是通过HTTP发xml数据
- SOAP 很简单并可扩展支持面向对象
- SOAP 允许您跨越防火墙
- SOAP 将被作为 W3C 标准来发展

#### SOAP语法格式 ####
##### 构建模块 #####
- 必需的Envelope 元素，此元素将整个 XML 文档标识为一条 SOAP 消息
- 可选的Header 元素，包含头部信息
- 必需的Body 元素，包含所有的调用和响应信息 
- 可选的 Fault 元素，提供有关在处理此消息所发生错误的信息

##### 语法规则 #####
- SOAP 消息必须用 XML 来编码
- SOAP 消息必须使用 SOAP Envelope 命名空间
- SOAP 消息必须使用 SOAP Encoding 命名空间
- SOAP 消息不能包含 DTD 引用
- SOAP 消息不能包含 XML 处理指令

##### 基本结构 #####
```java
<?xml version="1.0"?>
<soap:Envelope
xmlns:soap="http://www.w3.org/2001/12/soap-envelope"
soap:encodingStyle="http://www.w3.org/2001/12/soap-encoding">

<soap:Header>
...
</soap:Header>

<soap:Body>
...
  <soap:Fault>
  ...
  </soap:Fault>
</soap:Body>

</soap:Envelope> 
```
### WSDL ###
WSDL 是基于 XML 的用于描述Web Service及其函数、参数和返回值。通俗理解Wsdl是webservice的使用说明书。

#### wsdl是什么 ####
- WSDL 指网络服务描述语言(Web Services Description Language)。
- WSDL是一种使用 XML 编写的文档。这种文档可描述某个 Web service。它可规定服务的位置，以及此服务提供的操作（或方法）。
- WSDL 是一种 XML 文档
- WSDL 用于描述网络服务
- WSDL 也可用于定位网络服务

#### wsdl结构 ####


1. `<service>`    服务视图，webservice的服务结点，它包括了服务端点
2. `<binding>`     为每个服务端点定义消息格式和协议细节
3. `<portType>`   服务端点，描述 web service可被执行的操作方法，以及相关的消息，通过binding指向portType
4. `<message>`  定义一个操作（方法）的数据参数(可有多个参数)
5. `<types>`       定义 web service 使用的全部数据类型

#### wsdl说明书阅读 ####
从下往上读，先找到服务视图，通过binging找到protType，找到了protType就找到了我们要调用的webservice方法。
![](https://raw.githubusercontent.com/wqh8522/my_note/pic/redit/Oy0c8Ll.png)

### UDDI ###
UDDI 是一种目录服务，企业可以使用它对 Web services 进行注册和搜索。
UDDI，英文为 "Universal Description, Discovery and Integration"，可译为"通用描述、发现与集成服务"。

UDDI 是一个独立于平台的框架，用于通过使用 Internet 来描述服务，发现企业，并对企业服务进行集成。

- UDDI 指的是通用描述、发现与集成服务
- UDDI 是一种用于存储有关 web services 的信息的目录。
- UDDI 是一种由 WSDL 描述的 web services 界面的目录。
- UDDI 经由 SOAP 进行通信
- UDDI 被构建入了微软的 .NET 平台

## 常用注解 ##
### @WebService ###
@WebService-定义服务，在public class上边，可选参数：
- targetNamespace：指定命名空间
- name：portType的名称
- portName：port的名称
- serviceName：服务名称
- endpointInterface：SEI接口地址，如果一个服务类实现了多个接口，只需要发布一个接口的方法，可通过此注解指定要发布服务的接口。

	##	@WebMethod ###
@WebMethod-定义方法，在公开方法上边，可选参数：
- operationName：方法名
- exclude：设置为true表示此方法不是webservice方法，反之则表示webservice方法
- action：此操作的动作。 对于 SOAP 绑定，此方法可确定 soap 动作的值。 

### @WebResult ###
@WebResult-定义返回值，在方法返回值前边，可选参数:
- name：返回结果值的名称
- partName：表示此返回值的 wsdl:part 的名称。此名称只在操作是 rpc 样式，或者操作是文档样式且参数样式为 BARE 时使用。 
- targetNamespace：返回值的 XML 名称空间。只在操作是文档样式或者返回值映射到某一个头时使用。如果目标名称空间被设置为 ""，则此名称空间表示空名称空间。
- header：如果为 true，则结果是从消息头而不是消息正文获取的。 

### @WebParam ###
@WebParam-定义参数，在方法参数前边，可选参数：
- name：指定参数的名称
- partName：表示此返回值的 wsdl:part 的名称。此名称只在操作是 rpc 样式，或者操作是文档样式且参数样式为 BARE 时使用。 
- targetNamespace：返回值的 XML 名称空间。只在操作是文档样式或者返回值映射到某一个头时使用。如果目标名称空间被设置为 ""，则此名称空间表示空名称空间。
- header：如果为 true，则结果是从消息头而不是消息正文获取的。 
- mode：参数的流向（IN、OUT 或 INOUT 之一）。OUT 和 INOUT 模式只是为那些符合 Holder 类型定义（JAX-WS 2.0 [5] 的第 2.3.3 节）的参数类型所指定的。Holder 类型的参数必须为 OUT 或 INOUT。 



