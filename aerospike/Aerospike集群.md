# Aerospike集群

## 集群部署

集群节点使用心跳功能相互跟踪。使用心跳，节点可以自己协调。节点是同行，没有主节点。所有节点都跟踪集群中的其他节点。在节点管理期间，所有群集节点都使用心跳机制检测更改。

Aerospike使用以下方法定义集群：

- 组播（UDP）使用IP：PORT来广播心跳消息。

- Mesh（TCP）使用一个Aerospike服务器的地址加入集群。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/as/%E5%BF%83%E8%B7%B3.png)

### 组播（UDP）

在心跳子节中：

1. 设置`mode`为`multicast`。

2. 设置`multicast-group`为有效的多播地址（239.0.0.0-239.255.255.255）。

3. （可选）设置`address`为用于集群内通信的接口的IP。此设置还控制将使用的界面**结构**。将群集内流量隔离到特定网络接口时需要。

4. 设置`interval`和`timeout`

   - `interval` （建议：150）控制发送心跳包的频率。
   - `timeout` （建议：10）控制间隔的数量，如果节点没有从丢失的节点收到心跳，则认为节点被集群中的其余节点丢失。
   - 使用默认设置，节点将在1.5秒内知道另一个节点离开群集。

   ```
    heartbeat {
       mode multicast                  # Send heartbeats using Multicast
       multicast-group 239.1.99.2              # multicast address
       port 9918                       # multicast port
       address 192.168.1.100 # (Optional) (Default any) IP of the NIC to
                                       # use to send out heartbeat and bind
                                       # fabric ports
       interval 150                    # Number of milliseconds between heartbeats
       timeout 10                      # Number of heartbeat intervals to wait
                                       # before timing out a node
     }
   ```

### Mesh（TCP）

在心跳子节中：

1. 设置`mode`为`mesh`。
2. （可选）设置`address`为用于集群内通信的本地接口的IP。此设置还控制将使用的界面**结构**。将群集内流量隔离到特定网络接口时需要。
3. 设置[`mesh-seed-address-port`](https://www.aerospike.com/docs/reference/configuration/#mesh-seed-address-port)为群集中节点的IP地址（或3.10版本的合格DNS名称）和心跳端口。
4. 设置`interval`和`timeout`
   - [`interval`](https://www.aerospike.com/docs/reference/configuration/#interval) （建议：150）控制发送心跳包的频率。
   - [`timeout`](https://www.aerospike.com/docs/reference/configuration/#timeout) （建议：10）控制间隔的数量，如果节点没有从丢失的节点接收到心跳，则认为节点被集群中的其余节点丢失。
   - 使用推荐的设置，节点将在1.5秒内知道另一个节点离开群集。

```
  heartbeat {
    mode mesh                   # Send heartbeats using Mesh (Unicast) protocol
    address 192.168.1.100       # (Optional) (Default: any) IP of the NIC on
                                # which this node is listening to heartbeat
    port 3002                   # port on which this node is listening to
                                # heartbeat
    mesh-seed-address-port 192.168.1.100 3002 # IP address for seed node in the cluster
                                              # This IP happens to be the local node
    mesh-seed-address-port 192.168.1.101 3002 # IP address for seed node in the cluster
    mesh-seed-address-port 192.168.1.102 3002 # IP address for seed node in the cluster
    mesh-seed-address-port 192.168.1.103 3002 # IP address for seed node in the cluster

    interval 150                # Number of milliseconds between heartbeats
    timeout 10                  # Number of heartbeat intervals to wait before
                                # timing out a node
  }
```

## 集群管理

Aerospike数据库使用**Shared-Nothing**架构，其中：

- AS集群中的每个节点都是相同的。
- 所有节点都是对等节点
- 没有单一的失败点

Aerospike 使用 **Smart Partitions™**算法使数据均匀分布在群集中的所有节点上。插入数据使会先使用`RIPEMD160`将记录的键（任何大小）散列为20字节的固定长度字符串。前12位形成分区ID，用于确定哪个分区包含此记录。分区在群集节点之间平均分配。如果群集中有*n个*节点，则每个节点存储大约1 / *n*的数据。

> RIPEMD160是一个经过现场测试的极随机散列函数，可确保记录在逐个分区的基础上非常均匀地分布。分区遵循不同节点和集群的正态分布（与均值相差3个标准差）。



集群管理子系统处理节点成员身份，并确保当前成员和所有集群中的节点保持一致。集群管理子系统的具体目标是：

- 在集群中的所有节点上达到当前集群成员的单一一致视图。
- 自动检测新节点到达/离开的无缝集群重新配置。
- 检测网络故障，并适应这种网络故障。
- 最小化检测和适应集群成员资格更改的时间。

### 集群视图

每个Aerospike节点都会自动分配唯一的节点标识符，是由MAC地址和监听端口组成的一个函数，集群视图由元组定义：`<cluster_key,succession_list>`其中：

- **cluster_key**是一个随机生成的8字节值，用于标识集群视图的实例
- **succession_list**是作为集群一部分的唯一节点标识符集合，它使Aerospike节点能够使用相同的成员节点集来区分两个集群视图。

对集群视图的每次更改，对运行延迟和整个系统的性能都有显著的影响。这意味着需要快速检测节点到达/离开事件，随后需要有效的协调机制来处理集群视图的任何更改

### 集群节点的发现

通过节点之间周期性的交换心跳消息来检测节点的到达和离开。集群节点的每个节点维护一个邻接表，它是最近向该节点发送心跳消息的其他节点的列表。离开集群的节点由于在可配置的超时时间间隔内没有检测到心跳消息，之后，它将从邻接列表中删除。

检测机制的主要目标：

1. 为避免由于零星和瞬间的网络故障，而将节点声明为已离开
2. 防止异常节点频繁地加入和离开集群。由于CPU，网络，磁盘等系统级资源在使用上的瓶颈，节点可能出现异常。

如何实现以上目标：

1. 替代心跳机制，在异常或阻塞的网络中，丢失某些数据包的可能性很大。因此，除了常规的心跳消息外，还使用节点间定期交换的其他消息作为备用的二次心跳机制。例如，副本的写入被用作替代心跳消息。这保障心跳机制的健壮性。
2. 节点健康评分，集群中的每个节点通过计算平均消息丢失来评估每个相邻节点的运行状态评分。这是对该节点丢失多少个传入消息的估计。
3. 集群视图改变，AS数据库集群使用Paxos算法来维护集群视图，一旦邻接表发生改变，就会触发运行一个Paxos共识算法来确定一个新的集群视图。

## 可靠性

Aerospike在一个或多个节点上复制分区。一个节点成为分区读写的数据主节点，其他节点存储副本。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/as/%E6%95%B0%E6%8D%AE%E9%9B%86%E7%BE%A4%E5%88%86%E5%B8%83.png)

此示出了4节点的集群，其中每个节点是用于数据的大致1/4的数据主*AND*每个节点是用于数据的1/4的复制品。一个节点是数据主节点。数据作为副本在所有其他节点上分发。对于此示例，如果节点1变得不可用，则来自节点＃1的副本分布在其他节点上。 

> 复制因子是可配置的; 但是，它不能超过群集中的节点数。更多副本等于更好的可靠性，但由于写请求必须到达所有副本，因此会产生更高的群集需求。大多数部署使用复制因子2（一个主副本和一个副本）。

在没有网络故障的情况下，同步复制可提供更高级别的正确性。写入事务在提交数据并将结果返回给客户端之前传播到所有副本。在极少数情况下，当集团重新配置时，Aerospike智能客户端可能已将请求发送到错误的节点，因为它已暂时过时，Aerospike **Smart Cluster™**透明地将请求代理到正确的节点。当群集从分区中恢复时，可能存在与不同分区冲突应用的写入。在这种情况下，Aerospike应用启发式选择最可能的版本，即它解决了不同数据副本之间发生的任何冲突。默认情况下，会选择具有最大更改次数（最高生成计数）的版本，但可以选择具有最近修改时间的版本。正确的选择将由数据模型确定。

## 分区

在Aerospike数据库中，每个命名空间被分为4096个分区，这些分区在集群节点之间平均分配。

Aerospike使用随机散列方法确保分区均匀分布。无需手动分片。集群节点协调以均匀划分。Aerospike客户端检测集群更改并将请求发送到正确的节点。添加或删除节点时，群集会自动重新平衡。集群中的所有节点都是对等节点 - 没有单个数据库主节点可以发生故障并将整个数据库关闭。

Aerospike使用散列记录键将新记录分配给分区。此确定性哈希进程始终将记录映射到同一分区。数据记录在其整个生命周期中保持在同一分区中。分区可以在服务器之间移动，但不应拆分。不应将记录重新分配给另一个分区。

> 每个群集节点都有一个配置文件。每个节点上的命名空间配置参数必须相同。

## 数据分布

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/as/%E6%95%B0%E6%8D%AE%E5%88%86%E5%B8%83.png)

当数据插入AS数据库时，Aerospike会使用RipeMD160（RACE原始完整性消息摘要）算法将记录的主键散列成160字节的摘要。Digest Space被分为4096个不重叠的“分区”（partitions），记录根据主键摘要进行分区。Aerospike协调索引和数据，以便在运行读取操作或查询是避免任何跨节点流量。写入可能需要基于复制因子的多个节点之间的通信。索引和数据的分配结合数据分布散列函数，最终导致节点间数据分布一致性。