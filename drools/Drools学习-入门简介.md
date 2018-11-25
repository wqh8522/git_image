# Drools5学习-入门

Drools目前已经更新到**7.14.0.Final**，由于公司使用的是Drools5，所以先从5开始了解学习。

> **drools**是一个业务规则管理系统，具有基于前向链接和后向链接推理的规则引擎，允许快速可靠地评估业务规则和复杂的事件处理。规则引擎也是创建专家系统的基本构建块，在人工智能中，该专家系统是模拟人类专家的决策能力的计算机系统。

## 什么是规则引擎

规则引擎是由推理引擎发展而来，是一种嵌入在应用程序中的组件，实现了将业务决策从应用程序代码中分离出来，并使用预定义的语义模块编写业务决策。接受数据输入，解释业务规则，并根据业务规则做出业务决策。
大多数规则引擎都支持规则的次序和规则冲突检验，支持简单脚本语言的规则实现，支持通用开发语言的嵌入开发。目前业内有多个规则引擎可供使用，其中包括商业和开放源码选择。开源的代表是 Drools，商业的代表是 Visual Rules ,I Log。

## Drools规则引擎

Drools（JBoss Rules ）具有一个易于访问企业策略、易于调整以及易于管理的开源业务规则引擎，符合业内标准，速度快、效率高。业务分析师或审核人员可以利用它轻松查看业务规则，从而检验是否已编码的规则执行了所需的业务规则。
JBoss Rules 的前身是 Codehaus 的一个开源项目叫 Drools。现在被纳入 JBoss 门下，更名为 JBoss Rules，成为了 JBoss 应用服务器的规则引擎。
Drools 是为 Java 量身定制的基于 Charles Forgy 的 RETE 算法的规则引擎的实现。具有了 OO 接口的 RETE,使得商业规则有了更自然的表达。

## Drools 使用概览

Drools 是 Java 编写的一款开源规则引擎，实现了 Rete 算法对所编写的规则求值，支持声明方式表达业务逻辑。使用 DSL(Domain Specific Language)语言来编写业务规则，使得规则通俗易懂，便于学习理解。支持 Java 代码直接嵌入到规则文件中。
Drools 主要分为两个部分：一是 Drools 规则，二是 Drools 规则的解释执行。规则的编译与运行要通过 Drools 提供的相关 API 来实现。而这些 API 总体上游可分为三类：规则编译、规则收集和规则的执行。Drools 是业务规则管理系统（BRMS）解决方案，涉及以下项目：

- ![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools/20181125182941.png) Drools Workbench：业务规则管理系统
- ![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools/20181125182953.png) Drools Expert：业务规则引擎
- ![1543141809757](C:\Users\wanqh\AppData\Local\Temp\1543141809757.png) Drools Fusion：事件处理
- ![1543141817932](C:\Users\wanqh\AppData\Local\Temp\1543141817932.png) JBPM：工作流引擎
- ![1543141839611](C:\Users\wanqh\AppData\Local\Temp\1543141839611.png) OptaPlanner：规划引擎

## Drools5



![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools/JBossDrools.png)



![1543137207844](C:\Users\wanqh\AppData\Roaming\Typora\typora-user-images\1543137207844.png)

http://download.jboss.org/drools/release/5.6.0.Final/



https://blog.csdn.net/wo541075754/article/details/74456890