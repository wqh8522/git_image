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

Rete算法的编译结果是规则集对应的Rete网络，Rete网络是一个事实（fact）可以在其中流动的图。

Rete网络的节点可以分四类：根节点（root）、类型节点（typenode）、alpha节点、beta节点，其中根节点是一个虚拟节点，是构建Rete网络的入口；类型节点存储事实的各种类型，各个事实从对应的类型节点进入rete网络。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/node.png)

### RootNode

Rete网络的根节点，所有对象通过根节点进入Rete网络。根节点会流转到ObjectTypeNode。

### ObejctTypeNode

对象类型节点，保证所传入的对象只会进入自己类型所在的网络，提高工作效率。ObjectTypeNode向播到`AlphaNodes`、`LeftInputAdapterNodes`和`BetaNodes`。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/ObjectTypeNode.png)

<span style="color:red">在Drools中，当一个对象insert时，会从一个Map中查找，如果Map中不存在，则扫描所有ObectTypeNode，查找它在列表中缓存有效的匹配项。</span>

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

   > a. 取出规则中的一个模式(模式就是规则中的最小一个匹配项，如：age>10,age<20；那么age>10就是一个模式，age<20是另一个模式。)；检查模式中的参数类型，如果是新类型，则加入一个类型节点。b. 检查模式对应的Alpha节点是否已存在，如果存在则记录节点位置，如果没有则将模式作为一个Alpha节点加入到网络中，同时根据Alpha节点的模式建立Alpha内存表；
   >
   > c. 重复b，直到所有模式处理完毕；
   >
   > d. 组合Beta节点，按照如下方式：Beta左输入节点为Alpha(1)，右输入为Alpha(2)。Beta(i) 左输入节点为 Beta(i-1)，右输入节点为 Alpha(i) i>2 并将两个父节点的内存表内联成为自己的内存表；
   >
   > e. 重复 d 直到所有的 Beta 节点处理完毕;
   >
   > f. 将动作（Then部分）封装成叶节点（Action节点）作为Beta(n)的输出节点

3. 重复2直到所有规则处理完毕；

执行忘上述步骤，建立的Rete网络：

​       ![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools%20flow/rete-network.png)

## Rete算法匹配过程