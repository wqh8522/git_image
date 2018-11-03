---
title: CentOS7下Kafka的安装介绍
date: 2018-01-25 14:29:14
tags: [Kafka]
---

# 介绍

> **Kafka**是分布式发布-订阅消息系统，最初由LinkedIn公司开发，之后成为之后成为Apache基金会的一部分，由[Scala](https://baike.baidu.com/item/Scala)和[Java](https://baike.baidu.com/item/Java/85979)编写。Kafka是一种快速、可扩展的、设计内在就是分布式的，分区的和可复制的提交日志服务。
>
> 它与传统系统相比，有以下不同：
>
> - 它被设计为一个分布式系统，易于向外扩展；
> - 它同时为发布和订阅提供高吞吐量；
> - 它支持多订阅者，当失败时能自动平衡消费者；
> - 它将消息持久化到磁盘，因此可用于批量消费，例如[ETL](http://en.wikipedia.org/wiki/Extract,_transform,_load)，以及实时应用程序。 <!--more-->

# 基础概念

- Broker：Kafka集群包含一个或多个服务器，这些服务器就是Broker
- Topic：每条发布到Kafka集群的消息都必须有一个Topic
- Partition：是物理概念上的分区，为了提供系统吞吐率，在物理上每个Topic会分成一个或多个Partition，每个Partition对应一个文件夹
- Producer：消息产生者，负责生产消息并发送到Kafka Broker
- Consumer：消息消费者，向kafka broker读取消息并处理的客户端。
- Consumer Group：每个Consumer属于一个特定的组，组可以用来实现一条消息被组内多个成员消费等功能。

# 安装kakfka

从[官网下载](https://kafka.apache.org/downloads)Kafka安装包，解压安装，或直接使用命令下载。

```bash
wget http://mirror.bit.edu.cn/apache/kafka/1.0.0/kafka_2.11-1.0.0.tgz
```

解压安装

```bash
tar -zvxf kafka_2.11-1.0.0.tgz -C /usr/local/
d /usr/local/kafka_2.11-1.0.0/
```

修改配置文件

```bash
 vim config/server.properties 
```

修改其中

```bash
broker.id=1
log.dirs=data/kafka-logs
```

# 功能验证

## 启动zookeeper

使用安装包中的脚本启动单节点Zookeeper实例：

```bash
bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
```

## 启动Kafka服务

使用kafka-server-start.sh启动kafka服务：

```bash
bin/kafka-server-start.sh config/server.properties
```

## 创建Topic

使用kafka-topics.sh 创建但分区单副本的topic test 

```bash
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

## 查看Topic

```bash
bin/kafka-topics.sh --list --zookeeper localhost:2181
```

![](http://oy09glbzm.bkt.clouddn.com/18-1-25/43812418.jpg-blog)

## 产生消息

使用kafka-console-producer.sh 发送消息

```bash
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test 
```

![](http://oy09glbzm.bkt.clouddn.com/18-1-25/84256783.jpg-blog)

## 消费消息

使用kafka-console-consumer.sh 接收消息并在终端打印

```bash
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
```

## 删除Topic

```bash
bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic test
```

## 查看描述 Topic 信息

```bash
bin/kafka-topics.sh --describe --zookeeper localhost:2181 --topic test
```

![](http://oy09glbzm.bkt.clouddn.com/18-1-25/44126022.jpg-blog)

第一行给出了所有分区的摘要，每个附加行给出了关于一个分区的信息。 由于我们只有一个分区，所以只有一行。

“Leader”: 是负责给定分区的所有读取和写入的节点。 每个节点将成为分区随机选择部分的领导者。

“Replicas”: 是复制此分区日志的节点列表，无论它们是否是领导者，或者即使他们当前处于活动状态。

“Isr”: 是一组“同步”副本。这是复制品列表的子集，当前活着并被引导到领导者。

# 集群配置

Kafka支持两种模式的集群搭建：

1. 单机多broker集群配置；
2. 多机多broker集群配置。

## 单机多breoker

利用单节点部署多个broker。不同的broker不同的id，监听端口以及日志目录，如：

- 将配置文件复制两份

```bash
cp config/server.properties config/server-1.properties
cp config/server.properties config/server-2.properties 
```

- 修改配置文件信息，这里为了对应，将上面的broker.id改为了0

```bash
vim config/server-1.properties
#修改内容
broker.id=1
listeners=PLAINTEXT://your.host.name:9093
log.dirs=/data/kafka-logs-1


vim config/server-2.properties
#修改内容
broker.id=2
listeners=PLAINTEXT://your.host.name:9094
log.dirs=/data/kafka-logs-2
```

- 启动多个kafka服务

```bash
in/kafka-server-start.sh config/server-1.properties 

bin/kafka-server-start.sh config/server-2.properties 
```

- 最后按照上面方法产生和消费信息。

## 多机多broker

分别在多个节点按上述方式安装Kafka，配置启动多个Zookeeper 实例。如：192.168.18.130、192.168.18.131、192.168.18.132三台机器

分别配置多个机器上的Kafka服务 设置不同的broke id，zookeeper.connect设置如下:

```bash
zookeeper.connect=192.168.18.130:2181,192.168.18.131:2181,192.168.18.132:2181
```



-----------

# 参考资料

1. [在CentOS 7上安装Kafka](https://www.mtyun.com/library/how-to-install-kafka-on-centos7)
2. [Apache Kafka：下一代分布式消息系统](http://www.infoq.com/cn/articles/apache-kafka/)
3. [Kafka 安装及快速入门](http://www.54tianzhisheng.cn/2018/01/04/Kafka/#%E4%BD%BF%E7%94%A8-Kafka-Connect-%E6%9D%A5%E5%AF%BC%E5%85%A5-%E5%AF%BC%E5%87%BA%E6%95%B0%E6%8D%AE)



