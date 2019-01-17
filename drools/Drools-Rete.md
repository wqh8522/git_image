# Drools-Rete算法

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

![](https://img-blog.csdn.net/20161225123805084?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMjM3MzgxNQ==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

Rete算法的编译结果是规则集对应的Rete网络，Rete网络是一个事实（fact）可以在其中流动的图。

Rete网络的节点可以分四类：根节点（root）、类型节点（typenode）、alpha节点、beta节点，其中根节点是一个虚拟节点，是构建Rete网络的入口；类型节点存储事实的各种类型，各个事实从对应的类型节点进入rete网络。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/node.png)

### RootNode

Rete网络的根节点，所有对象通过根节点进入Rete网络。根节点会流转到ObjectTypeNode。

### ObejctTypeNode

对象类型节点，保证所传入的对象只会进入自己类型所在的网络，提高工作效率。ObjectTypeNode向下传播到`AlphaNodes`、`LeftInputAdapterNodes`和`BetaNodes`。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/ObjectTypeNode.png)

<span style="color:red">在Drools中，当一个对象声明时，会从一个Map中查找，如果Map中不存在，则扫描所有ObectTypeNode，查找它在列表中缓存有效的匹配项。</span>

```java
//ObjectTypeConfigurationRegistry类
public ObjectTypeConf getObjectTypeConf(EntryPointId entrypoint,Object object) {    
        // first see if it's a ClassObjectTypeConf        
        Object key;
        if (object instanceof Activation) {
            key = ClassObjectType.Match_ObjectType.getClassType();
        } else if (object instanceof Fact) {
            key = ((Fact) object).getFactTemplate().getName();
        } else {
            key = object.getClass();
        }
        ObjectTypeConf objectTypeConf = this.typeConfMap.get( key );
        // it doesn't exist, so create it.
        if ( objectTypeConf == null ) {
            if ( object instanceof Fact ) {
                objectTypeConf = new FactTemplateTypeConf( entrypoint,((Fact)object).getFactTemplate(),this.kBase );
            } else {
                objectTypeConf = new ClassObjectTypeConf( entrypoint,
                                                          (Class<?>) key,
                                                          this.kBase );
            }
            ObjectTypeConf existing = this.typeConfMap.putIfAbsent( key, objectTypeConf );
            if ( existing != null ) {
                // Raced, take the (now) existing.
                objectTypeConf = existing;
            }
        }
        return objectTypeConf;
    }

```

### AlphaNode

Alpha节点是规则的条件部分的一个模式，一个对象只有和本节点匹配成功之后，才能继续向下传播。Alpha节点用于评估文字条件。当规则对单个对象类型具有多个文字条件时，它们将链接在一起。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/AlphaNode.png)

<span style="color:red">Drools通过使用散列优化从ObjectTypeNode到AlphaNode的传播来扩展Rete。每次将AlphaNode添加到ObjectTypeNode时，它都会将字面值`(literal value)`作为键添加到HashMap中，并将AlphaNode作为值。当新实例进入ObjecType节点时，不会传播到每个AlphaNode，它可以从HashMap中检索正确的AlphaNode，从而避免不必要的文字检查。</span>

### BetaNode

BetaNode包含JoinNode和NotNode（完成exists检查）。BetaNode节点用于比较两个对象和它们的字段，两个对象可能是相同类型或者不同的类型。将两个输入称为左和右。左输入通常是对象列表；在Drools中是一个元组`（Tuple）`，Bate节点也有内存，左边输入被称为`Beta Memory`，会保存左右达到过得语义；右边输称为`Alpha Memory`，会保存所有到达过的对象。

<span style="color:red">Drools通过在BetaNodes上执行索引来扩展Rete，例如：如果我们知道BetaNode正在对String字段执行检查，则每个对象进入时对该String值进行哈希查找。这意味着当事实从相反方向进入时，不会迭代所有事实以找到有效的连接，而是返回可能有效的候选者。在任何时候都会发现一个有效的连接，即元组与Objec连接在一起；这被称为部分匹配，然后传播到下一个节点。</span>

#### JoinNode

用作连接（Join）操作的节点，相当于and，相当于数据库表连接操作，属于BetaNode类型的节点。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/JoinNode.png)

#### NotNode

根据右边输入对左边输入的对象数组进行过滤。

### LeftInputAdapterNodes

将单个对象转化为对象数组

### Terminal Nodes

终端节点用于指示匹配其所有条件的单个规则；一个规则可以具有多个终端节点

## Drools节点共享

Drools通过节点共享来提高规则引擎的性能。因为规则可能存在部分相同的模式，节点的共享运行我们对内存那种的节点数量进行压缩。

```java
package node

dialect "mvel"
    
import com.wqh.demo.drools.pojo.Cheese
import com.wqh.demo.drools.pojo.Person

rule "cheddar"
when
    Cheese( $cheddar : name == "cheddar" )
    $person : Person( favouriteCheese == $cheddar )
then
    System.out.println( $person.getName() + " likes cheddar" );
end

rule "cheddar1"
when
    Cheese( $cheddar : name == "cheddar" )
    $person : Person( favouriteCheese != $cheddar )
then
    System.out.println( $person.getName() + " does not like cheddar" );
end
```

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/DroolsNodeShare.png)

## 创建Rete网络

1. 创建根节点，

2. 加入一条规则

   > a. 取出规则中的一个模式(模式就是规则中的最小一个匹配项，如：age>10,age<20；那么age>10就是一个模式，age<20是另一个模式。)；检查模式中的参数类型，如果是新类型，则加入一个类型节点。
   >
   > b. 检查模式对应的Alpha节点是否已存在，如果存在则记录节点位置，如果没有则将模式作为一个Alpha节点加入到网络中，同时根据Alpha节点的模式建立Alpha内存表；
   >
   > c. 重复b，直到所有模式处理完毕；
   >
   > d. 组合Beta节点，按照如下方式：Beta左输入节点为Alpha(1)，右输入为Alpha(2)。Beta(i) 左输入节点为 Beta(i-1)，右输入节点为 Alpha(i) i>2 并将两个父节点的内存表内联成为自己的内存表；
   >
   > e. 重复 d 直到所有的 Beta 节点处理完毕;
   >
   > f. 将动作（Then部分）封装成叶节点（Action节点）作为Beta(n)的输出节点

3. 重复2直到所有规则处理完毕；

执行完上述步骤，建立的Rete网络：

​       ![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/rete-network.png)

上图（图a和图b），他们的左边都是beta-network，右边都是 alpha-network, 圆圈是 join-node。右边的 alpha-network 是根据事实库和规则条件构建的，其中除 alpha-network 节点的节点都是根据每一条规则条件的模式 , 从事实库中 match 过来的，即在编译构建网络的过程中静态建立的。只要事实库是稳定的，RETE 算法的执行效率应该是非常高的，其原因就是已经通过静态的编译，构建了 alpha-network。左边的 beta-network 表现出了 rules 的内容，其中 p1,p2,p3 共享了许多 BetaMemory 和 join-node, 这样能加快匹配速度。

## Rete算法匹配过程

1. 对于每个事实，通过select操作进行过滤，使事实沿着rete网络达到合适的Alpha节点
2. 对于收到的每一个Alpha节点，用Project（投影操作）将那些适当的变量绑定分离出来，使各个新的变量绑定集沿 rete 网到达适当的 bete 节点。
3. 对于收到新的变量绑定的beta节点，使用Project操作产生新的绑定集，是这些新的变量绑定沿rete网络至下一个beta节点以至最后一个Project。
4. 对于每条规则，用Project操作将结论实例化所需的绑定分离出来。

如果把 rete 算法类比到关系型数据库操作，则事实集合就是一个关系，每条规则就是一个查询，再将每个事实绑定到每个模式上的操作看作一个 Select 操作，记一条规则为 P，规则中的模式为 c1,c2,…,ci, Select 操作的结果记为 r(ci), 则规则 P 的匹配即为 `r(c1)◇r(c2)◇…◇(rci)`。其中◇表示关系的连接（Join）操作。

Rete 网络的连接（Join）和投影 (Project) 和对数据库的操作形象:

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/joinandproject.png)



##  运行时执行

WME ：存储区储存的最小单位是工作存储区元素（Working Memory Element，简称WME），WME是为事实建立的元素，是用于和非根结点代表的模式进行匹配的元素。

Token：是WME的列表，包含有多个WME，（在Forgy的论文中，把Token看成是WME的列表或者单个WME，为了阐述方便，本文将把Token只看成WME的列表）

1. 如果WME的类型和根节点的后继节点TypeNode（Alpha节点的一种）所指定的类型相同，则会将该事实保存在TypeNode节点对应的Alpha存储区，该WME被传到后继节点继续匹配，否则会放弃该WME的后续匹配；

> TypeNode存储： 每次一个AlphaNode被加到一个 ObjectTypeNode的时候，就以字面值（literal value）也就是file 作为key，以AlphaNode作为value加入HashMap。当一个新的实例进入ObjectTypeNode的时候，不用传递到每 一个AlphaNode，它可以直接从HashMap中获得正确的AlphaNode，避免了不必要的字面检查。

2. 如果WME被传递到alpha节点，则会检查WME是否和该节点对应的模式相匹配，若匹配，则会将该事实保存在该alpha节点对应的存储区中，改WME被传递到后继节点继续匹配，否则会放弃该WME的后续匹配；

>alpha 存储：检测WME是否和该结点对应的模式相匹配，若匹配，则会将该事实保存在该alpha结点对应的存储区中，该WME被传递到后继结点继续匹配

3. 如果WME被传递到beta结点的右端，则会加入到该beta结点的right存储区，并和left存储区中的Token进行匹配（匹配动作根据beta结点的类型进行，例如：join，projection，selection），匹配成功，则会将该WME加入到Token中，然后将Token传递到下一个结点，否则会放弃该WME的后续匹配；

>bate存储区：
>
>每个非根结点都有一个存储区。其中1-input（alpha）结点有alpha存储区和一个输入口；
>
>2-input（bate）结点有left存储区和right存储区和左右两个输入口，其中left存储区是beta存储区，right存储区是alpha存储区。存储区储存的最小单位是工作存储区元素（Working Memory Element，简称WME），WME是为事实建立的元素，是用于和非根结点代表的模式进行匹配的元素。

4. 如果Token被传递到beta结点的左端，则会加入到该beta结点的left存储区，并和right存储区中的WME进行匹配（匹配动作根据beta结点的类型进行，例如：join，projection，selection），匹配成功，则该Token会封装匹配到的WME形成新的Token，传递到下一个结点，否则会放弃该Token的后续匹配；

5. 如果WME被传递到beta结点的左端，将WME封装成仅有一个WME元素的WME列表做为Token，然后按照（4）所示的方法进行匹配；

6. 如果Token传递到终结点，则和该根结点对应的规则被激活，建立相应的Activation，并存储到Agenda当中，等待激发。

7. 如果WME被传递到终结点，将WME封装成仅有一个WME元素的WME列表做为Token，然后按照（6）所示的方法进行匹配；

以上是RETE算法对于不同的结点，来进行WME或者token和结点对应模式的匹配的过程。

## Rete算法优缺点

### Rete特点

- Rete 算法是一种启发式算法，不同规则之间往往含有相同的模式，因此在 beta-network 中可以共享 BetaMemory 和 betanode。如果某个 betanode 被 N 条规则共享，则算法在此节点上效率会提高 N 倍。
-  Rete 算法由于采用 AlphaMemory 和 BetaMemory 来存储事实，当事实集合变化不大时，保存在 alpha 和 beta 节点中的状态不需要太多变化，避免了大量的重复计算，提高了匹配效率。
- 从 Rete 网络可以看出，Rete 匹配速度与规则数目无关，这是因为事实只有满足本节点才会继续向下沿网络传递。

### Rete缺点

- 事实的删除与事实的添加顺序相同, 除了要执行与事实添加相同的计算外, 还需要执行查找, 开销很高
- RETE 算法使用了Beta存储区存储已计算的中间结果, 以牺牲空间换取时间, 从而加快系统的速度。然而β存储区根据规则的条件与事实的数目而成指数级增长, 所以当规则与事实很多时, 会耗尽系统资源 

### 建议

- 容易变化的规则尽量置后匹配，可以减少规则的变化带来规则库的变化。
- 约束性较为通用或较强的模式尽量置前匹配，可以避免不必要的匹配。
- 针对 Rete 算法内存开销大和事实增加删除影响效率的问题，技术上应该在 alpha 内存和 beata 内存中，只存储指向内存的指针，并对指针建里索引（可用 hash 表或者非平衡二叉树）。
- Rete 算法 JoinNode 可以扩展为 AndJoinNode 和 OrJoinNode，两种节点可以再进行组合

## Drools中的Rete

### ReteOO

ReteOO是Drools在Rete算法基础上对Rete算法的功能增强：

- 节点共享：共享了Alpha和Bate节点
- Alpha索引：具有许多子节点的Alpha节点使用散列查找机制来避免测试每个结果
- Beta索引：Join、Not和Exit节点使用散列索引其内存，以减少相等检查的连接尝试。
- 基于树的图：加入

