---
title: CentOS7下RabbitMQ的安装介绍
date: 2018-01-26 09:13:48
tags: [RabbitMQ,CentOS]

---

# 介绍

RabbitMQ是一个在AMQP基础上完成的，可复用的企业消息系统。它是由Erlang语言开发。

AMQP：Advanced Message Queue，高级消息队列协议。他是应用层协议的一个开放标准，为面向消息的中间件设计，基于此协议的客户端与消息中间件可传递消息，并不受产品、开发语言等条件的限制。

> RabbitMQ 最初起源于金融系统，用于在分布式系统中存储转发消息，在易用性、扩展性、高可用性等方面表现不俗。具体特点包括：<!--more-->
>
> 1. 可靠性（Reliability）：RabbitMQ使用一些机制来保证可靠性，如持久化、传输确认、发布确认。
> 2. 灵活的路由（Flexible Routing）：在消息进入队列之前，通过 Exchange 来路由消息的。
> 3. 消息集群（Clustering）：多个 RabbitMQ 服务器可以组成一个集群，形成一个逻辑 Broker 。
> 4. 高可用（Highly Available Queues）：队列可以在集群中的机器上进行镜像，使得在部分节点出问题的情况下队列仍然可用。
> 5. 多种协议（Multi-protocol）：RabbitMQ支持多种消息队列协议，比如STOMP、MQTT等。
> 6. 多语言客户端（Many Clients）：RabbitMQ 几乎支持所有常用语言，比如 Java、.NET、Ruby 等。
> 7. 管理界面（Management UI）：RabbitMQ 提供了一个易用的用户界面，使得用户可以监控和管理消息 Broker 的许多方面。
> 8. 跟踪机制（Tracing）：如果消息异常，RabbitMQ 提供了消息跟踪机制，使用者可以找出发生了什么。
> 9. 插件机制（Plugin System）：RabbitMQ 提供了许多插件，来从多方面进行扩展，也可以编写自己的插件。

# 基本概念

项目结构图：（直接引用百度百科的）

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/27555791.jpg)

概念说明：

- Broker：消息队列服务器的实体，是一个中间件应用，负责接收消息生产者的消息，然后将消息发送至消息接收者或其他的Braker
- Exchange：消息交换机，是消息第一个到达的地方，消息通过它指定的路由规则，分发到不同的消息队列中去。
- Queue：消息队列，消息通过发送和路由之后最终达到的地方，到达Queue的消息即进入逻辑上等待消费的状态。每个消息都会被发送到一个或多个队列。
- Binding：绑定，它的作用就是把Exchange和Queue按照路由规则绑定起来，也就是Exchange和Queue之间的虚拟链接。
- Routing Key：路由关键字，Exchange根据这个关键字进行消息投递。
- Virtual host：虚拟主机，是对Broker的虚拟划分，将消费者、生产者和它们依赖的AMQP相关结构进行隔离，一般都是为了安全考虑。比如：我们可以在一个Broker中设置多个虚拟主机，对不同用户进行权限的分离。
- Connection：连接。代表生产者、消费者、Broker之间进行通信的物理网络。
- Channel：消息通道，用于连接生产者和消费者的逻辑结构。在客户端每个连接里，可建立多个Channel，每个Channel代表一个会话任务，通过Channel可以隔离同一个连接中的不同交互内容。
- Producer：消息生产者。
- Consumer：消息消费者。

消息队列的使用过程：

1. 客户端连接到消息队列服务器，打开一个channel。
2. 客户端声明一个exchange，并设置相关属性。
3. 客户端声明一个queue，并设置相关属性。
4. 客户端使用routing key，在exchange和queue之间建立好绑定关系。
5. 客户端投递消息到exchange。

exchange接收到消息后，就根据消息的key和已经设置的binding，进行消息路由，将消息投递到一个或多个队列里。

exchange也有几个类型，完全根据key进行投递的叫做Direct交换机，例如，绑定时设置了routing key为”abc”，那么客户端提交的消息，只有设置了key为”abc”的才会投递到队列。对key进行模式匹配后进行投递的叫做Topic交换机，符号”#”匹配一个或多个词，符号”*”匹配正好一个词。例如”abc.#”匹配”abc.def.ghi”，”abc.*”只匹配”abc.def”。还有一种不需要key的，叫做Fanout交换机，它采取广播模式，一个消息进来时，投递到与该交换机绑定的所有队列。

RabbitMQ支持消息的持久化，也就是数据写在磁盘上，为了数据安全考虑，我想大多数用户都会选择持久化。消息队列持久化包括3个部分：

1. exchange持久化，在声明时指定durable => 1
2. queue持久化，在声明时指定durable => 1
3. 消息持久化，在投递时指定delivery_mode => 2（1是非持久化）

如果exchange和queue都是持久化的，那么它们之间的binding也是持久化的。如果exchange和queue两者之间有一个持久化，一个非持久化，就不允许建立绑定。

[^摘自百度百科]: https://baike.baidu.com/item/rabbitmq/9372144?fr=aladdin

# 安装介绍

## Erlang安装配置

前面说到RabbitMQ是由Erlang语言开发，所以需要先安装Erlang环境

- 下载安装，地址：[http://www.erlang.org/downloads](http://www.erlang.org/downloads) ，选择版本下载。

```bash
wget http://erlang.org/download/otp_src_20.1.tar.gz
```

- 解压编译安装

```bash
#解压
tar -zvxf otp_src_20.1.tar.gz 
#配置安装路径编译代码
cd otp_src_20.1/
./configure --prefix=/opt/erlang --without-javac
make && make install
```

如下安装完成

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/48515723.jpg-blog)

- 查看安装结果

```bash
cd /opt/erlang
bin/erl
```

安装成功

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/62188249.jpg-blog)

- 配置环境变量

```bash
vim /etc/profile
#添加下面的配置
#set erlang environment
export PATH=$PATH:/opt/erlang/bin
#使配置文件生效
source  /etc/profile
```

## RabbitMQ的安装配置

- 下载安装，从官网下载，[点击下载](http://www.rabbitmq.com/releases/rabbitmq-server/) ，进入选择版本。在linux环境下需要下带有unix的，如下：

```bash
wget http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.12/rabbitmq-server-generic-unix-3.6.12.tar.xz
```

- 解压，这里将其解压到opt目录中。解压之后进入opt目录，修改rabbitmq的文件夹

```bash
xz -d rabbitmq-server-generic-unix-3.6.12.tar.xz 
tar -vxf  rabbitmq-server-generic-unix-3.6.12.tar -C /opt/
cd /opt/
mv rabbitmq-server-3.6.12/ rabbitmq
```

- 配置环境变量

```bash
vim /etc/profile
#添加以下配置
#set rabbitmq environment
export PATH=$PATH:/opt/rabbitmq/sbin
#使得文件生效
source  /etc/profile
```

## RabbitMQ服务操作

- 启动服务

```bash
 rabbitmq-server -detached
```

- 查看服务状态

```bash
./sbin/rabbitmqctl status

#显示一下信息，说明已经启动
Status of node rabbit@localhost
[{pid,1452},
 {running_applications,
     [{rabbit,"RabbitMQ","3.6.12"},
      {ranch,"Socket acceptor pool for TCP protocols.","1.3.0"},
      {ssl,"Erlang/OTP SSL application","8.2.1"},
      {public_key,"Public key infrastructure","1.5"},
      {asn1,"The Erlang ASN1 compiler version 5.0.3","5.0.3"},
      {crypto,"CRYPTO","4.1"},
      {mnesia,"MNESIA  CXC 138 12","4.15.1"},
      {os_mon,"CPO  CXC 138 46","2.4.3"},
      {rabbit_common,
          "Modules shared by rabbitmq-server and rabbitmq-erlang-client",
          "3.6.12"},
      {compiler,"ERTS  CXC 138 10","7.1.2"},
      {xmerl,"XML parser","1.3.15"},
      {syntax_tools,"Syntax tools","2.1.3"},
      {sasl,"SASL  CXC 138 11","3.1"},
      {stdlib,"ERTS  CXC 138 10","3.4.2"},
      {kernel,"ERTS  CXC 138 10","5.4"}]},
 {os,{unix,linux}},
 {erlang_version,
     "Erlang/OTP 20 [erts-9.1] [source] [64-bit] [smp:1:1] [ds:1:1:10] [async-threads:64] [kernel-poll:true]\n"},
 {memory,
     [{connection_readers,0},
      {connection_writers,0},
      {connection_channels,0},
      {connection_other,0},
      {queue_procs,2744},
      {queue_slave_procs,0},
      {plugins,0},
      {other_proc,19513536},
      {metrics,184272},
      {mgmt_db,0},
      {mnesia,61136},
      {other_ets,1523640},
      {binary,211896},
      {msg_index,43568},
      {code,21408137},
      {atom,891849},
      {other_system,17779446},
      {total,61620224}]},
 {alarms,[]},
 {listeners,[{clustering,25672,"::"},{amqp,5672,"::"}]},
 {vm_memory_calculation_strategy,rss},
 {vm_memory_high_watermark,0.4},
 {vm_memory_limit,771637248},
 {disk_free_limit,50000000},
 {disk_free,36134227968},
 {file_descriptors,
     [{total_limit,924},{total_used,2},{sockets_limit,829},{sockets_used,0}]},
 {processes,[{limit,1048576},{used,153}]},
 {run_queue,0},
 {uptime,21},
 {kernel,{net_ticktime,60}}]
```

- 停止RabbitMQ

```bash
rabbitmqctl stop
```

## 配置网页插件

RabbitMQ网页管理的端口是15672，但是现在还不能访问，需要添加网页插件才能访问

```bash
rabbitmq-plugins enable rabbitmq_management
```

在浏览器中输入ip:15672

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/57199153.jpg-blog)

在网上看到有人说默认的账号密码是guest，但是该账号只能通过localhost登录。所以需要配置一个用户并设置权限。

1. 添加用户

```bash
rabbitmqctl add_user [username] [password]
```

2. 添加权限

```bash
rabbitmqctl set_permissions -p "/" [username] ".*" ".*" ".*"
```

3. 修改用户角色

```bash
rabbitmqctl set_user_tags [username] administrator
```

下面就可以使用刚添加的用户登录了。可以在该页面进行RabbitMQ的管理，包括用户的设置。

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/36041742.jpg-blog)

## 安装报错

- error: No curses library functions found configure

  需要先安装ncurses-devel

  ```bash
  yum install ncurses-devel
  ```

- odbc : ODBC library - link check failed

  ![](http://oy09glbzm.bkt.clouddn.com/18-1-26/99491906.jpg-blog)

  类似这样的错误，我们只需要关注APPLICATIONS DISABLED部分的提示信息，如这里就是缺少odbc，使用命令安装即可

  ```bash
  yum install unixODBC unixODBC-devel
  ```

# 集群的配置

## 单机多节点

RabbitMQ启动之后，默认的名称是Rabbit，监听的端口是5672，如果想在同一台机器上启动多个节点，那么其他的节点就会因为节点名称和端口与默认的冲突而导致启动失败，可以通过设置环境变量来实现，具体方法如下：

1. 先关闭上面的网页管理插件

```bash
rabbitmq-plugins disable rabbitmq_management
```

2. 首先在机器上设置设置两个节点**rabbit和rabbit_01**

```bash
rabbitmqctl stop //先停止运行节点，再进行集群部署
#启动第一个节点
RABBITMQ_NODE_PORT=5672 RABBITMQ_NODENAME=rabbit rabbitmq-server -detached
#启动第二个节点
RABBITMQ_NODE_PORT=5673 RABBITMQ_NODENAME=rabbit_1 rabbitmq-server -detached 
```

3. 将第二个节点rabbit-01加入到第一个集群节点rabbit中

```bash
#停止rabbit_1节点的应用
rabbitmqctl -n rabbit_1@localhost stop_app
#将rabbit_1添加到集群节点rabbit中去
rabbitmqctl -n rabbit_1@localhost join_cluster rabbit@localhost
#查看集群节点状态
rabbitmqctl cluster_status
#启动rabbit_1节点应用
rabbitmqctl -n rabbit_1@localhost start_app
#查看rabbit_1节点的状态
rabbitmqctl -n rabbit_1 status
```

如下集群配置完毕

![](http://oy09glbzm.bkt.clouddn.com/18-1-26/62559007.jpg-blog)

## 多机多节点配置

不同于单机多节点的情况，在多机环境，如果要在cluster集群内部署多个节点，需要注意两个方面：

- **保证需要部署的这几个节点在同一个局域网内**
- **需要有相同的Erlang Cookie，否则不能进行通信，为保证cookie的完全一致，采用从一个节点copy的方式**

环境介绍

| RabbitMQ节点        | IP地址          | 工作模式 | 操作系统             |
| ----------------- | ------------- | ---- | ---------------- |
| rabbitmqCluster   | 186.16.195.24 | DISK | CentOS 7.0 - 64位 |
| rabbitmqCluster01 | 186.16.195.25 | DISK | CentOS 7.0 - 64位 |
| rabbitmqCluster02 | 186.16.195.26 | DISK | CentOS 7.0 - 64位 |

cluster部署过程：

- 局域网配置

分别在三个节点的/etc/hosts下设置相同的配置信息

```bash
  186.16.195.24 rabbitmqCluster
  186.16.195.25 rabbitmqCluster01
  186.16.195.26 rabbitmqCluster02
```

- **设置不同节点间同一认证的Erlang Cookie** 
  采用从主节点copy的方式保持Cookie的一致性

```bash
[root@rabbitmqCluster01]# scp /var/lib/rabbitmq/.erlang.cookie 186.16.195.25:/var/lib/rabbitmq
[root@rabbitmqCluster02]# scp /var/lib/rabbitmq/.erlang.cookie 186.16.195.26:/var/lib/rabbitmq12
```

- **使用 -detached运行各节点**

```
rabbitmqctl stop
rabbitmq-server -detached 12
```

- **查看各节点的状态**

```bash
[root@rabbitmqCluster]#rabbitmqctl cluster_status
[root@rabbitmqCluster01]#rabbitmqctl cluster_status
[root@rabbitmqCluster02]#rabbitmqctl cluster_status
```

- **创建并部署集群**，以rabbitmqCluster01节点为例：

```bash
[root@rabbitmqCluster01]#rabbitmqctl stop_app
[root@rabbitmqCluster01]#rabbitmqctl join_cluster rabbit@rabbitmqCluster
[root@rabbitmqCluster01]#rabbitmqctl start_app123
```

- **查看集群状态**

```bash
[root@rabbitmqCluster]#rabbitmqctl cluster_status
```

# 参考

1. [CentOS7下RabbitMQ服务安装配置](http://www.linuxidc.com/Linux/2016-03/129557.htm)
2. [消息队列之 RabbitMQ](https://www.jianshu.com/p/79ca08116d57)
3. [RabbitMQ分布式集群架构和高可用性（HA）](http://blog.csdn.net/woogeyu/article/details/51119101)
4. [https://geewu.gitbooks.io/rabbitmq-quick/content/](https://geewu.gitbooks.io/rabbitmq-quick/content/)