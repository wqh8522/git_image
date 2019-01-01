# Rete算法

## Rete概述

> Rete算法最初是由卡内基梅隆大学的查尔斯·福尔热博士发明，并于1978-1979年在他的博士论文中记录，该论文的简化版本于1982年出版（http://citeseer.ist.psu.edu/context/505087/0）

- Rete算法是一种高效的模式匹配算法来实现产生式规则系统；
- 一种高效的算法，通过缓存来避免相同条件多次评估的情况，但是带来了大量的内存使用（以空间换时间）；
- Rete 在拉丁语中译为”net”，即网络。Rete算法通过规则条件生成一个网络，每个规则条件是一个节点
- Rete 匹配算法是一种进行大量模式集合和大量对象集合间比较的高效方法，通过网络筛选的方法找出所有匹配各个模式的对象和规则。
- 其核心思想是将分离的匹配项根据内容动态构造匹配树，以达到显著降低计算量的效果。Rete 算法可以被分为两个部分：规则编译和规则执行 。当 Rete 算法进行事实的断言时，包含三个阶段：匹配、选择和执行，称做 match-select-act cycle。
- Rete算法通过形成一个rete网络进行模式匹配，利用基于规则的系统的两个特征，即时间冗余性（Temporal redundancy）和结构相似性（structural similarity），提高系统模式匹配效率。

## 相关概念

**事实（fact）**：对象之间及对象属性之间的多元关系。为简单起见，事实用一个三元组来表示：`（identifier ^attribute  value）`，例如如下事实：

```
w1:(B1  ^ on B2)           w6:(B2  ^color blue)
w2:(B1  ^ on B3)           w7:(B3  ^left-of B4)
w3:(B1  ^ color red)       w8:(B3  ^on table)
w4:(B2  ^on table)         w9:(B3  ^color red)
w5:(B2  ^left-of B3)
```

**规则（rule）**：包含条件和行为两部分，条件部分又叫左手元（LHS），行为部分又叫右手元（RHS）。条件部分可以有多条条件，可以用 and 或 or 连接，一般形式如下：

```
(name-of-this-production
LHS /*one or more conditions*/
-->
RHS /*one or more actions*/
)
例如，下面的例子：
 (find-stack-of-two-blocks-to-the-left-of-a-red-block 
 (^on) 
 (^left-of) 
 (^color red) 
 --> 
 ...RHS... 
 ) 
```

 **模式（patten）**：也就是规则的条件部分，是已知事实的泛化形式，是未实例化的多元关系 。比如，前面的那条规则的条件部分：
 (^on) 
 (^left-of) 
 (^color red)  

## 模式匹配的一般算法

规则主要由两部分组成：条件和结论，条件部分也称为左端（记为LHS, left-hand side），结论部分也称为右端（记为RHS, right-hand side）。为分析方便，假设系统中有N条规则，每个规则的条件部分平均有P个模式，工作内存中有M个事实，事实可以理解为需要处理的数据对象。
规则匹配，就是对每一个规则r, 判断当前的事实o是否使LHS(r)=True，如果是，就把规则r的实例r(o)加到冲突集当中。所谓规则r的实例就是用数据对象o的值代替规则r的相应参数，即绑定了数据对象o的规则r。
规则匹配的一般算法：

1. 从N条规则中取出一条r；
2. 从M个事实中取出P个事实的一个组合c；
3.  用c测试LHS(r)，如果LHS(r（c）)=True，将RHS(r（c）)加入冲突集中；
4.  取出下一个组合c，goto 3；
5.  取出下一条规则r，goto 2；

## Rete网络

Rete算法的编译结果是规则集对应的Rete网络，Rete网络是一个事实（fact）可以在其中流动的图。

Rete网络的节点可以分四类：根节点（root）、类型节点（typenode）、alpha节点、beta节点，其中根节点是一个虚拟节点，是构建Rete网络的入口；类型节点存储事实的各种类型，各个事实从对应的类型节点进入rete网络。

​	