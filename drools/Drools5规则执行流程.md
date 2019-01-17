## 规则文件加载编译流程

```java
KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
knowledgeBuilder.add(ResourceFactory.newClassPathResource("rule/Nodeshared.drl",this.getClass()), ResourceType.DRL);
Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
```

- **KnowledgeBuilderFactory：**该工厂用于构建KnowledgePackages中集体持有的知识库资源。 KnowledgePackage还提供“命名空间”的角色。 可以提供可选的KnowlegeBuilderConfiguration。 KnowledgeBuilderConfiguration本身是从这个工厂创建的。 KnowledgeBuilderConfiguration允许您设置ClassLoader以及其他设置（如默认方言和编译器）以及许多其他选项。
- **KnowledgeBuilder：**知识库生成器负责获取文件，如.drl文件、.bpmn2文件或.xls文件；并将其转换为知识库可以使用的规则和流程定义的知识包（KnowledgePackage）
- **KnowledgePackage：**提供给知识库的知识定义集合。该名称用于提供这些定义的“命名空间”分隔。

### add操作做了些什么事

#### 注册资源

调用`knowledgeBuilder.add`之后会先调用`PackageBuilder.registerBuildResource( resource, type );`将`Resource` push到` Stack<List<Resource>> buildResources`栈中

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/pushresource.png)

#### 处理资源

在push到`buildResources`栈之后调用`PackageBuilder.public void addKnowledgeResource( Resourceresource,ResourceType type,ResourceConfiguration configuration )`方法，在改方法中会根据传入的`ResourceType`来调用相应的方法处理resource。这里只用到了DRL文件，所以只看DRL文件的处理。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/addKnowledgeResource.png)

PackageBuilder.addPackageFromDrl方法

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/addpackfromdrl.png)

在上面方法可以看到在添加规则包的之前会先调用`drlToPackageDescr(Resource resource)`将`resource`解析为包描述`PackageDescr`。

#### 规则文件解析

在`drlToPackageDescr`中会先判断`resource`类型是否是`DescrResource`，如果是则直接获取`PackageDescr`。否则先`new DrlParser()`，然后调用`parse( resource )`方法解析规则文件。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/drltopackageDescr.png)

DrlParser.parse方法中是先调用该类的`getParser`方法获取`DRLParser`，然后传入该类的`compile`方法：

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/parse.png)

#### 使用getParse进行获取DRLParser

DRLParser是Drools中org.drools.lang包下的一个类，有点类似工具类的作用。该类中的有四个属性TokenStream、RecognizerSharedState、ParserHelper和DRLExpressions。其中TokenStream是antlr中的类，耳DRLExpressions是drools继承了antlr的Parser类以实现自己的语法分析功能。

```java
private DRLParser getParser( final InputStream is, final String encoding ) {
    try {
        //该类是antlr中的类
        ANTLRInputStream antlrInputStream;
        if (encoding != null) {
            antlrInputStream = new ANTLRInputStream(is, encoding);
        } else {
            antlrInputStream = new ANTLRInputStream(is);
        }
        //获取词法分析器
        lexer = new DRLLexer(antlrInputStream);
        //先将DRLLexer转换为CommonTokenStream，在new DRLParser的时候会初始化它的四个属性
        DRLParser parser = new DRLParser( new CommonTokenStream( lexer ) );
        //返回parser
        return parser;
    } catch ( final Exception e ) {
        throw new RuntimeException( "Unable to parser Reader",e );
    }
}
```

- **ANTLRInputStream：**一种从InputStream中提取的ReaderStream。 用于从stdin读取并指定文件编码等

- **DRLLexer：**该类继承了antlr中Lexer类，也就是Drools实现了自己词法分析器。
- **CommonTokenStream：**最常见的令牌流，其中每个令牌被缓冲并且令牌被过滤以用于特定通道（解析器将仅看到这些令牌）。

#### 获取PackageDescr

在`DrlParser.compile(boolean isEditor,final DRLParser parser )`中实际是调用`DRLParserPackageDescr compilationUnit(Resource resource) `方法来处理分析结果，最后返回`PackageDescr`。（获取到`PackageDescr`对象之后会将antlr异常转换为drools分析器异常）

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/complie.png)

而在`compilationUnit`方法会先将`resource`传入`PackageDescrBuilderImpl`中，最后返回`PackageDescrBuilder`传入最终的编译方法中。

在compilationUnit方法中会循环遍历input（该input为TokenStream类型，是词法分析器之后的结果）的内容，

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/compliationUnit.png)

最终解析完成的到`PackageDescr`，其中包含在规则文件中设置的一些全局属性、function等。其中rules为规则信息。规则中的`consequence`为规则的RHS

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/packageDescr.png)

#### AddPackage方法

经过上面的词法分析和语法分析之后获取到PackageDescr对象。然后调用addPackage方法出里PackageDescr对象。在该方法中会对包进行一个合并。然后调养编译方法

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/addPackage.png)

#### 编译规则

在compileAllRules方法中会先判断之前的分析是否有错误，只有在没有错误的时候才会调用`private void compileRules(PackageDescr packageDescr, PackageRegistry pkgRegistry) `方法对规则进行编译。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/complieAllRules.png)

在`compileRules(PackageDescr packageDescr, PackageRegistry pkgRegistry)`方法中分两步，先对function进行处理，然后处理RuleDescr。

下面是循环遍历处理规则的操作：

```java
// 确保规则按依赖关系排序，以便以后生成依赖规则
sortRulesByDependency(packageDescr);

// 迭代和编译
for (RuleDescr ruleDescr : packageDescr.getRules()) {
    if (isEmpty(ruleDescr.getNamespace())) {
        // 确保设置了命名空间
        ruleDescr.setNamespace(packageDescr.getNamespace());
    }
    //将包的全局属性设置给rule
    Map<String, AttributeDescr> pkgAttributes = packageAttributes.get(packageDescr.getNamespace());
    inheritPackageAttributes(pkgAttributes, ruleDescr);
	//这是方言
    if (isEmpty(ruleDescr.getDialect())) {
        ruleDescr.addAttribute(new AttributeDescr("dialect",pkgRegistry.getDialect()));
    }
    //编译处理
    addRule(ruleDescr);
}


private void addRule( final RuleDescr ruleDescr ) {
    if ( ruleDescr.getResource() == null ) {
        ruleDescr.setResource( resource );
    }
    PackageRegistry pkgRegistry = this.pkgRegistryMap.get( ruleDescr.getNamespace() );
	//获取当前规则包
    Package pkg = pkgRegistry.getPackage();
    //获取方言的编译注册器
    DialectCompiletimeRegistry ctr = pkgRegistry.getDialectCompiletimeRegistry();
    //初始化规则构建的上下文对象，该方法最后一个对象是根据方法获取相应的方言注册器
    RuleBuildContext context = new RuleBuildContext( this, ruleDescr,ctr, pkg, ctr.getDialect( pkgRegistry.getDialect()) );
    
    //将上下文对象传入RuleBuilder（规则构建器）中中构建规则
    ruleBuilder.build( context );
	//以下是设置rule的一些信息
    this.results.addAll( context.getErrors() );
    this.results.addAll( context.getWarnings() );

    context.getRule().setResource( ruleDescr.getResource() );

    context.getDialect().addRule( context );

    if (this.ruleBase != null) {
        if (pkg.getRule( ruleDescr.getName() ) != null) {
            this.ruleBase.lock();
            try {
                // XXX: this one notifies listeners
                this.ruleBase.removeRule( pkg,
                                         pkg.getRule( ruleDescr.getName() ) );
            } finally {
                this.ruleBase.unlock();
            }
        }
    }
	//将rule添加到pkg
    pkg.addRule( context.getRule() );
}
```

规则构建上下文对象：主要包括当前规则包、规则包描述、规则和规则描述信息

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/ruleBuilderContext.png)

在`RuleBuilder.build(final RuleBuildContext context) `中处理流程：

- 先判断ruleDescr是否有父类，有这将父类设置给上下文的rule

- 然后处理注解

- 根据方言和规则描述的Lhs的Class获取相应的规则条件生成器（RuleConditionBuilder）

  - Lhs为AndDescr类型

  ![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/lhs0.png)

- 调用RuleConditionBuilder的build方法构建条件，返回GroupElement设置给上下文rule属性

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/ce.png)

- 处理规则的属性

- 处理RHS，最终会使用mvel编译

```
System.out.println( $cheddar.getName() + " likes cheddar" );
```

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/exp.png)

```java
 public void build(final RuleBuildContext context) {
     RuleDescr ruleDescr = context.getRuleDescr();
     //查询并获取对象，而不是使用字符串
     if ( null != ruleDescr.getParentName() && null != context.getPkg().getRule( ruleDescr.getParentName() ) ) {
         context.getRule().setParent( context.getPkg().getRule( ruleDescr.getParentName() ) );
     }
     //添加规则的所有元属性
     buildMetaAttributes( context );
	 //初始化添加生成器
     final RuleConditionBuilder builder = (RuleConditionBuilder) context.getDialect().getBuilder( ruleDescr.getLhs().getClass() );
     if ( builder != null ) {
         Pattern prefixPattern = null;
         //判断该规则藐视类型是否是查询（querry）
         if ( context.getRuleDescr() instanceof QueryDescr ) {
             prefixPattern = context.getDialect().getQueryBuilder().build( context,
                                                                          (QueryDescr) context.getRuleDescr() );
         }
         //调用build方法构建
         final GroupElement ce = (GroupElement) builder.build( context,
                                                              ruleDescr.getLhs(),
                                                              prefixPattern );
		 //setLhs
         context.getRule().setLhs( ce );
     } else {
         throw new RuntimeDroolsException( "BUG: builder not found for descriptor class " + ruleDescr.getLhs().getClass() );
     }

     // 构建规则的所有属性
     // 必须在生成lhs之后，因为某些属性需要来自lhs的绑定
     buildAttributes( context );

     // 构建结果并生成它的调用程序
     if ( !(ruleDescr instanceof QueryDescr) ) {
         // 如果我们有查询，不要构建结果
         //根据方言获取相应结果构建起
         ConsequenceBuilder consequenceBuilder = context.getDialect().getConsequenceBuilder();
         consequenceBuilder.build( context, Rule.DEFAULT_CONSEQUENCE_NAME );

         for ( String name : ruleDescr.getNamedConsequences().keySet() ) {
             consequenceBuilder.build( context, name );
         }
     }
 }
```

### 总结

从上面的流程可以看出，在调用drools提供的`knowledgeBuilder.add`方法之后，drools引擎会对资源文件进行相应的词法分析、语法分析以判断文件是否存在错误，并返回DRLParser；然后处理分析结果，生成规则包描述（PackageDescr）；最后调用编译方法对规则包描述中的规则进行编译处理，将编译好的规则信息添加到package。

## 知识库的创建

```java
KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
knowledgeBase.addKnowledgePackages(knowledgePackages);
```

- **KnowledgeBase：**收集应用当中知识（knowledge）定义的知识库对象，可以指定一个KnowledgeBaseConfiguration对象：

以上代码是使用工厂类初始化一个知识库，然后将前面生成的规则包添加到知识库

工厂类初始化知识库时会初始化一个`RuleBase`对象

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/rulebase.png)

### 向知识库中添加规则包

`knowledgeBase.addKnowledgePackages(knowledgePackages);`操作会调用`((ReteooRuleBase)ruleBase).addPackages( list);`方法

```java
public void addKnowledgePackages(Collection<KnowledgePackage> knowledgePackages) {
    List<Package> list = new ArrayList<Package>();
    for ( KnowledgePackage knowledgePackage : knowledgePackages ) {
        list.add( ((KnowledgePackageImp) knowledgePackage).pkg  );
    }
    ((ReteooRuleBase)ruleBase).addPackages( list);
}
```

`ReteooRuleBase.addPackages`会调用父类`AbstractRuleBase`的`addPackages`方法对添加规则包，在该方法中会对规则包进行多次迭代操作：（以下为源码中每次迭代的注释）

1. `we need to merge all byte[] first, so that the root classloader can resolve classes`（我们需要首先合并所有字节[]，以便根类加载器可以解析类）
2. `dd all Type Declarations, this has to be done first incase packages cross reference each other during build process.` (添加所有类型声明，这必须首先在构建过程中相互交叉引用。)
3. `now iterate again, this time onBeforeExecute will handle any wiring or cloader re-creating that needs to be done as part of the merge`(现在再次迭代，这次onBeforeExecute将处理任何需要在合并过程中完成的布线或cloader重新创建)
4. 最后一次遍历是处理规则

```java
 for (Package newPkg : newPkgs) {
     Package pkg = this.pkgs.get( newPkg.getName() );
     // 合并规则包
     mergePackage( pkg,
                  newPkg );

     // 将窗口声明（window declarations）添加到Kbase 
     for( WindowDeclaration window : newPkg.getWindowDeclarations().values() ) {
         addWindowDeclaration( newPkg,
                              window );
     }
     // 向Kbase添加入口点
     for (String id : newPkg.getEntryPointIds()) {
         addEntryPoint( id );
     }
     // 将规则添加到规则库
     for ( Rule rule : newPkg.getRules() ) {
         //调用addRule方法添加规则到规则包
         addRule( newPkg, rule );
     }
     // 将流添加到规则库
     if ( newPkg.getRuleFlows() != null ) {
         final Map<String, org.drools.definition.process.Process> flows = newPkg.getRuleFlows();
         for ( org.drools.definition.process.Process process : flows.values() ) {
             // XXX: we could take the lock inside addProcess() out, but OTOH: this is what the VM is supposed to do ...
             addProcess( process );
         }
     }
     this.eventSupport.fireAfterPackageAdded( newPkg );
 }

 public void addRule( final Package pkg,final Rule rule ) throws InvalidPatternException {
     lock();
     try {
         this.eventSupport.fireBeforeRuleAdded( pkg, rule );
         //实际会去调用实现类ReteooRuleBase的addRule方法
         addRule( rule );
         this.eventSupport.fireAfterRuleAdded( pkg,rule );
     } finally {
         unlock();
     }
 }
//ReteooRuleBase类
protected void addRule(final Rule rule) throws InvalidPatternException {
    // 增加规则。retebuilder引用了工作内存，并将传播任何现有事实。
    this.reteooBuilder.addRule( rule );
}

//ReteooBuilder类
synchronized void addRule(final Rule rule) throws InvalidPatternException {
    final List<TerminalNode> terminals = this.ruleBuilder.addRule( rule,
                                                                  this.ruleBase,
                                                                  this.idGenerator );
    this.rules.put( rule,terminals.toArray( new BaseNode[terminals.size()] ) );
}
```

经过一系列`addRule`方法的调用，最后调用的是`ReteooRuleBuilder`的addRule方法

#### Rete网络添加规则ReteooRuleBuilder.addRule

```java

public List<TerminalNode> addRule( final Rule rule,
                                  final InternalRuleBase rulebase,
                                  final ReteooBuilder.IdGenerator idGenerator ) throws InvalidPatternException {
    // 终端节点
    final List<TerminalNode> nodes = new ArrayList<TerminalNode>();

    // 转换规则并获取子规则数组
    final GroupElement[] subrules = rule.getTransformedLhs( rulebase.getConfiguration().getComponentFactory().getLogicTransformerFactory().getLogicTransformer() );

    for (int i = 0; i < subrules.length; i++) {

        // 创建上下文对象
        final BuildContext context = new BuildContext( rulebase,idGenerator );
        context.setRule( rule );

        // 如果以流模式运行，则计算事件的时间距离
        if (EventProcessingOption.STREAM.equals( rulebase.getConfiguration().getEventProcessingMode() )) {
            TemporalDependencyMatrix temporal = this.utils.calculateTemporalDistance( subrules[i] );
            context.setTemporalDistance( temporal );
        }

        if (rulebase.getConfiguration().isSequential()) {
            context.setTupleMemoryEnabled( false );
            context.setObjectTypeNodeMemoryEnabled( false );
            context.setAlphaNodeMemoryAllowed( false );
        } else {
            context.setTupleMemoryEnabled( true );
            context.setObjectTypeNodeMemoryEnabled( true );
            context.setAlphaNodeMemoryAllowed( true );
        }

        // 组装规则的节点信息
        final TerminalNode node = this.addSubRule( context,subrules[i],i,rule );
        // 将终端节点添加到终端节点列表中
        nodes.add( node );

    }
    return nodes;
}


private TerminalNode addSubRule( final BuildContext context,
                                     final GroupElement subrule,
                                     final int subruleIndex,
                                     final Rule rule ) throws InvalidPatternException {
    // 根据连接条件获取适当的生成器
    final ReteooComponentBuilder builder = this.utils.getBuilderFor( subrule );

    // 检查是否需要初始事实
    if (builder.requiresLeftActivation( this.utils,
                                       subrule )) {
        this.addInitialFactPattern( subrule );
    }

    // builds and attach
    builder.build( context,this.utils,subrule );

    ActivationListenerFactory factory = context.getRuleBase().getConfiguration().getActivationListenerFactory( rule.getActivationListener() );
   
    TerminalNode terminal = factory.createActivationListener( context.getNextId(),
                                                             context.getTupleSource(),
                                                             rule,
                                                             subrule,
                                                             subruleIndex,
                                                             context );

    BaseNode baseTerminalNode = (BaseNode) terminal;
    baseTerminalNode.attach( context );
    context.addNode( baseTerminalNode );
    updatePropagations( baseTerminalNode, context );
    //返回终端节点
    return terminal;
}
```

在addSubRule方法中最终我这里调用的`GroupElementBuilder`类的`AndBuilder`内部类，该内部类实现了`ReteooComponentBuilder`接口，在build方法中会遍历规则的模式并生成相应的节点信息。

#### AndBuilder.build方法

```java
public void build(final BuildContext context,
                          final BuildUtils utils,
                          final RuleConditionElement rce) {

    final GroupElement ge = (GroupElement) rce;

    // 遍历child（规则中的模式）并构建节点
    for (final RuleConditionElement child : ge.getChildren()) {

        final ReteooComponentBuilder builder = utils.getBuilderFor(child);
		//构建节点改模式的节点信息
        builder.build( context,utils,child );
        // 判断是否需要添加LeftInputAdapterNodes
        if (context.getObjectSource() != null && context.getTupleSource() == null) {
            // we know this is the root OTN, so record it
            ObjectSource source = context.getObjectSource();
            while ( !(source instanceof ObjectTypeNode) ) {
                source = source.getParentObjectSource();
            }
            context.setRootObjectTypeNode( (ObjectTypeNode) source );


            // 构建LeftInputAdapterNodes节点，并附加节点          
            context.setTupleSource( (LeftTupleSource) utils.attachNode( context,context.getComponentFactory().getNodeFactoryService().buildLeftInputAdapterNode( context.getNextId(),context.getObjectSource(),context ) ) );
			//将对象源设置为空
            context.setObjectSource( null );
        }

        // 判断是否需要构建JoinNode
        if (context.getObjectSource() != null && context.getTupleSource() != null) {
            // so, create the tuple source and clean up the constraints and object source
            final BetaConstraints betaConstraints = utils.createBetaNodeConstraint(context,context.getBetaconstraints(),false );

            context.setTupleSource( (LeftTupleSource) utils.attachNode(context,context.getComponentFactory().getNodeFactoryService().buildJoinNode( context.getNextId(),context.getTupleSource(),context.getObjectSource(),betaConstraints,context) ) );
            context.setBetaconstraints( null );
            context.setObjectSource( null );
        }
    }
}
```



#### 附加节点

在附加节点的方法中会判断节点是否是共享，如果节点是共享的则会直接从上下文对象中找到共享节点。

```java
/**
* 将节点附加到网络中。 如果已存在可替换的节点，则使用它。
*
* @param context  当前生成的上下文
*            
* @param candidate 分析的节点
*            
* @return 实际的附加节点，可以是作为参数给出的节点，或者最终如果启用了共享则已经在缓存中的节点
*/
public BaseNode attachNode(final BuildContext context,
                           final BaseNode candidate) {
    BaseNode node = null;
    RuleBasePartitionId partition = null;
    if ( candidate instanceof EntryPointNode ) {
        //入口节点共享
        node = context.getRuleBase().getRete().getEntryPointNode( ((EntryPointNode) candidate).getEntryPoint() );
        //所有入口点节点都属于主分区
        partition = RuleBasePartitionId.MAIN_PARTITION;
    } else if ( candidate instanceof ObjectTypeNode ) {
        // ObejctTypeNode始终共享，从ReteOO网络中获取当前ObjectTypeNode
        Map<ObjectType, ObjectTypeNode> map = context.getRuleBase().getRete().getObjectTypeNodes( context.getCurrentEntryPoint() );
        if ( map != null ) {
            ObjectTypeNode otn = map.get( ((ObjectTypeNode) candidate).getObjectType() );
            //判断是否获取到该ObjectTypeNode
            if ( otn != null ) {
                // adjusting expiration offset  调整到期偏移量
                otn.setExpirationOffset( Math.max( otn.getExpirationOffset(),
                                                  ((ObjectTypeNode) candidate).getExpirationOffset() ) );
                node = otn;
            }
        }
        // 所有ObjectTypeNodes属于住分区
        partition = RuleBasePartitionId.MAIN_PARTITION;
    } else if ( isSharingEnabledForNode( context,candidate ) ) {
        //判断节点是否共享
        if ((context.getTupleSource() != null) && (candidate instanceof LeftTupleSink) ) {
            node = context.getTupleSource().getSinkPropagator().getMatchingNode( candidate );
        } else if ( (context.getObjectSource() != null) && (candidate instanceof ObjectSink) ) {
            node = context.getObjectSource().getSinkPropagator().getMatchingNode( candidate );
        } else {
            throw new RuntimeDroolsException( "This is a bug on node sharing verification. Please report to development team." );
        }
    }

    if ( node == null ) {
        //当做新的节点加入
        node = candidate;
        if ( partition == null ) {
            if ( context.getPartitionId() == null ) {
                context.setPartitionId( context.getRuleBase().createNewPartitionId() );
            }
            partition = context.getPartitionId();
        }
        // set node whit the actual partition label
        node.setPartitionId( partition );
        node.attach(context);
        // 将节点添加到上下文列表以跟踪所有添加的节点
        context.addNode( node );
    } else {
        // 找到共享节点，撤销上一个ID
        context.releaseId( candidate.getId() );
    }
    node.addAssociation( context.getRule(), context.peekRuleComponent() );
    //返回节点信息
    return node;
}
```

### 总结

向KnowledgeBase添加规则包列表，drools会向会遍历该列表中的规则包；然后遍历规则包规则；在处理规则的时候会获得规则的模式列表，再根据连接条件获取相应的构建器；最后生成节点信息，并根据节点类型已经设置判断该节点是否属于共享节点。

## 插入事实

```java
//获取会话
StatefulKnowledgeSession kSession = knowledgeBase.newStatefulKnowledgeSession();
//插入事实
FactHandle insert1 = kSession.insert(c);
```

- **StatefulKnowledgeSession：**与引擎交互的最常用方式。 StatefulKnowledgeSession允许应用程序与引擎建立迭代对话，其中会话的状态保持在调用之间。 对于同一组数据，可以多次触发推理过程。 但是，在应用程序完成使用会话之后，它必须调用dispose（）方法以释放资源和使用的内存。

### 判断句柄是否在内存中

使用insert方法向drools内存插入对象时，引擎会先从内存中获取该对象的句柄判断该对象是否已经在内存中

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/gethandle1.bmp)

从上面代码可以看出实际是根据obj的hashCode来获取对象；如果没有获取到句柄，则调用`private InternalFactHandle createHandle(final Object object,ObjectTypeConf typeConf)`新建一个事实句柄，否则直接返回。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/facthandlenull.png)

### 创建句柄

句柄创建成功之后引擎会将该句柄保存在内存中

```java
  private InternalFactHandle createHandle(final Object object,
                                            ObjectTypeConf typeConf) {
      InternalFactHandle handle;
      handle = this.handleFactory.newFactHandle( object,
                                                typeConf,
                                                this.wm,
                                                this );
      this.objectStore.addHandle( handle,object );
      return handle;
  }
```

最终返回的句柄：

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/handle.png)

### 插入句柄到当前内存

5. 句柄创建成功之后会调用`insert`方法插入到rete网络中并会传播

```java
//org.drools.common.NamedEntryPoint.insert
public void insert(final InternalFactHandle handle,
                   final Object object,
                   final Rule rule,
                   final Activation activation,
                   ObjectTypeConf typeConf) {
    this.ruleBase.executeQueuedActions();

    this.wm.executeQueuedActions();

    if ( activation != null ) {
        // 释放资源，以便可以对其进行GC'ED
        activation.getPropagationContext().releaseResources();
    }
    //传播上下文
    final PropagationContext propagationContext = new PropagationContextImpl( this.wm.getNextPropagationIdCounter(),PropagationContext.ASSERTION,rule,(activation == null) ? null : activation.getTuple(),handle,this.wm.agenda.getActiveActivations(),this.wm.agenda.getDormantActivations(),entryPoint );

    //  通过Rete网络传播FactHandleimpl。 应该在节点内存中记住所有FactHandleImpl，以便以后的运行时规则附件可以将匹配的事实传播给它们。
    this.entryPointNode.assertObject( handle,
                                     propagationContext,
                                     typeConf,
                                     this.wm );

    propagationContext.evaluateActionQueue( this.wm );

    this.wm.workingMemoryEventSupport.fireObjectInserted( propagationContext,
                                                         handle,
                                                         object,
                                                         this.wm );

    this.wm.executeQueuedActions();        

    if ( rule == null ) {
        // This is not needed for internal WM actions as the firing rule will unstage
        this.wm.getAgenda().unstageActivations();
    }        
}

```

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/nodememory.png)

### 总结

调用`session`的`insert`方法后，引擎会去内存中获取该对象的句柄，如果已经存在则直接返回句柄，不存在则创建一个新的句柄，这里获取句柄主要是根据对象的`hashCode`；创建句柄成功后会调用`NamedEntryPoint.insert`方法向rete网络中插入该句柄，并且会向下传播。

插入的Fact对象会判断对象符合知识库的那条规则，并将规则保存在内存的agenda对象中；

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/vmagend.png)

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/element.png)

## 执行规则

```java
kSession.fireAllRules();
//释放内存资源
kSession.dispose();
```

规则执行实际是取出上面`agenda`中的规则信息，最后将已经编译后的RHS传入mvel中执行。