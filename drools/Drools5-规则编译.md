# Drools5-规则执行流程

## 执行规则

- 规则文件

```java
//created on: 2019-1-3
package node

dialect "mvel"

import com.wqh.demo.pojo.Cheese
import com.wqh.demo.pojo.Person

rule cheddar
when
    $cheddar : Cheese( $cheddarName : name == "cheddar" )
    $person : Person( favouriteCheese == $cheddar )
    //eval( $person.getName() == $cheddar )
then
    System.out.println( $person.getName() + " likes cheddar" );
end

rule cheddar1
when
    $cheddar : Cheese( $cheddarNaame : name == "cheddar" )
    $person : Person( favouriteCheese != $cheddar )
then
    System.out.println( $cheddar + " does not like cheddar1" );
    System.out.println( $person.getName() + " does not like cheddar1" );
end

rule cheddar2
when
    $cheddar : Cheese( $cheddarName : name != "cheddar" )
    $person : Person( favouriteCheese == $cheddar )
then
    System.out.println( $person.getName() + " does not like cheddar2" );
end

rule cheddar3
when
    $cheddar : Cheese( $cheddarName : name != "cheddar" )
    $person : Person( favouriteCheese != $cheddar )
then
    System.out.println( $person.getName() + " does not like cheddar2" );
end
```

- 规则执行

```java
@Test
public void executeDrools(){
    Cheese c = new Cheese();
    c.setName("cheddar1");

    Person p = new Person();
    p.setFavouriteCheese(c);
    p.setName("person");
    //KnowledgeBuilder：知识库生成器负责获取文件，如.drl文件、.bpmn2文件或.xls文件；并将其转换为知识库可以使用的规则和流程定义的知识包（KnowledgePackage）
    KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
    //编译规则
  knowledgeBuilder.add(ResourceFactory.newClassPathResource("rule/Nodeshared.drl",this.getClass()), ResourceType.DRL);
    //hasErrors：得到编译规则过程中发现规则是否有错误，可以通过getErrors获取错误
    if (knowledgeBuilder.hasErrors()){
        logger.error("规则配置有问题，{}",knowledgeBuilder.getErrors().toString());
    }
    //产生规则包集合
    Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
    //创建KnowledgeBaseConfiguration对象，用来存放规则引擎运行时环境参数的配置对象
    KnowledgeBaseConfiguration kbConfig = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
    //KnowledgeBase：收集应用当中知识（knowledge）定义的知识库对象，可以指定一个KnowledgeBaseConfiguration对象：
    KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase(kbConfig);
    //将 KnowledgePackage 集合添加到 KnowledgeBase 当中
    knowledgeBase.addKnowledgePackages(knowledgePackages);
    //编译完成将规则包文件在引擎当中运行，
    //StatefulKnowledgeSession：一种常见的与规则引擎进行交互的方式，他可以与规则引擎建立一个持续的交互通道，在推理计算的过程当中可能会多次触发统一数据集
    StatefulKnowledgeSession kSession = knowledgeBase.newStatefulKnowledgeSession();
    kSession.insert(c);
    kSession.insert(p);
    //释放内存资源
    kSession.fireAllRules();
    kSession.dispose();
    //StatelessKnowledgeSession 对StatefulKnowledgeSession做了一定的封装，使用execute执行规则
    //StatelessKnowledgeSession statelessKnowledgeSession = knowledgeBase.newStatelessKnowledgeSession();
    //statelessKnowledgeSession.execute(product);
}
```

## 规则编译执行流程

在Drools5中规则执行的流程：规则编写——规则编译——规则执行

### 规则文件编译

在Drools5中可以使用如下代码将规则文件.drl加载到`KnowledgeBuilder`(知识库)，生成规则包

```java
KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
knowledgeBuilder.add(ResourceFactory.newClassPathResource("rule/Nodeshared.drl",this.getClass()), ResourceType.DRL);
 Collection<KnowledgePackage> knowledgePackages = knowledgeBuilder.getKnowledgePackages();
```

#### 使用add方法所做的事

调用add方法后，drools会使用词法分析器和语法分析器分析规则是否有错误，然后

1. 调用`PackageBuilder.registerBuildResource`方法将`resource`对象`push`到`PackageBuilder.buildResources`栈中；

```java
public void registerBuildResource( final Resource resource, ResourceType type ) {
        InternalResource ires = (InternalResource) resource;
        if ( ires.getResourceType() == null ) {
            ires.setResourceType( type );
        } else if ( ires.getResourceType() != type ) {
            this.results.add( new ResourceTypeDeclarationWarning( resource, ires.getResourceType(), type ) );
        }
        if ( ResourceType.CHANGE_SET == type ) {
            try {
                ChangeSet changeSet = parseChangeSet( resource );
                List<Resource> resources = new ArrayList<Resource>(  );
                resources.add( resource );
                for ( Resource addedRes : changeSet.getResourcesAdded() ) {
                    resources.add( addedRes );
                }
                for ( Resource modifiedRes : changeSet.getResourcesModified() ) {
                    resources.add( modifiedRes );
                }
                for ( Resource removedRes : changeSet.getResourcesRemoved() ) {
                    resources.add( removedRes );
                }
                buildResources.push( resources );
            } catch ( Exception e ) {
                results.add( new DroolsError() {
                    public String getMessage() {
                        return "Unable to register changeset resource " + resource;
                    }
                    public int[] getLines() { return new int[ 0 ]; }
                } );
            }
        } else {
            buildResources.push( Arrays.asList( resource ) );
        }
    }
```

>PackageBuilder：是用于解析和编译规则以及将它们组合或合并到二进制包实例中的主要编译器类。这可以通过合并到现有的二进制包中，或完全从源中完成。
>
>如果您使用的是Java方言，JavaDialectConfiguration将尝试使用ClassLoader.loasClass（String）验证指定的编译器是否在类路径中。如果您只想在编译器中使用Janino sa，则必须在实例化此类或PackageBuilder之前重载编译器属性，或者确保Eclipse在类路径中，因为Eclipse是默认值。
>
>通常，使用适用的addPackageFromXXX方法之一构建完整的包。然而，可以通过添加单独的组成部分来递增地构造包。在逐步构建包时，将缓存包级别属性并将其应用于随后添加的规则。在使用相同的PackageBuilder从多个源构造包时应该谨慎，因为即使添加到PackageBuilder的资源没有明确包含包级属性，缓存的包级别属性仍将适用。

2. 然后调用`PackageBuilder.addKnowledgeResource`，在`addKnowledgeResource`方法中根据`ResourceType`调用相应的`addPackageFromXXX`方法；这里用的DRL，所以只关注了`addPackageFromDrl`方法的具体实现。

```java
public void addKnowledgeResource( Resource resource,
            ResourceType type,
            ResourceConfiguration configuration ) {
        try {
            ( (InternalResource) resource ).setResourceType( type );
            if (ResourceType.DRL.equals( type )) {
                addPackageFromDrl( resource );
            } else if (ResourceType.DESCR.equals( type )) {
                addPackageFromDrl( resource );
            } else if (ResourceType.DSLR.equals( type )) {
                addPackageFromDslr( resource );
            } else if (ResourceType.DSL.equals( type )) {
                addDsl( resource );
            } else if (ResourceType.XDRL.equals( type )) {
                addPackageFromXml( resource );
            } else if (ResourceType.BRL.equals( type )) {
                addPackageFromBrl( resource );
            } else if (ResourceType.DRF.equals( type )) {
                addProcessFromXml( resource );
            } else if (ResourceType.BPMN2.equals( type )) {
                BPMN2ProcessFactory.configurePackageBuilder( this );
                addProcessFromXml( resource );
            } else if (ResourceType.DTABLE.equals( type )) {
                addPackageFromDecisionTable( resource, configuration );
            } else if (ResourceType.PKG.equals( type )) {
                addPackageFromInputStream(resource);
            } else if (ResourceType.CHANGE_SET.equals( type )) {
                addPackageFromChangeSet(resource);
            } else if (ResourceType.XSD.equals( type )) {
                addPackageFromXSD(resource, (JaxbConfigurationImpl) configuration);
            } else if (ResourceType.PMML.equals( type )) {
                addPackageFromPMML(resource, type, configuration);
            } else {
                addPackageForExternalType(resource, type, configuration);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
```

3. 在`addPackageFromDrl`方法中会先调用`drlToPackageDescr`方法获取`PackageDescr`，然后方法返回的参数传入`addPackage`方法中编译规则。在`drlToPackageDescr`方法中调用`DrlParser.parse`方法处理`resource`

```java
//该方法会先判断传入的resource是否是DescrResource
PackageDescr drlToPackageDescr(Resource resource) throws DroolsParserException, IOException {
        PackageDescr pkg;
        boolean hasErrors = false;
        if (resource instanceof DescrResource) {
            pkg = (PackageDescr) ( (DescrResource) resource ).getDescr();
        } else {
            //创建一个DrlParser（Drl文件的解析器）对象
            final DrlParser parser = new DrlParser();
           	//调用paser方法
            pkg = parser.parse( resource );
            this.results.addAll( parser.getErrors() );
            if (pkg == null) {
                this.results.add( new ParserError( resource, "Parser returned a null Package", 0, 0 ) );
            }
            hasErrors = parser.hasErrors();
        }
        return hasErrors ? null : pkg;
    }
```

4. `DrlParser`类中的`parse`方法实际是先调用`getParser`获取到`DRLParser`，然后调用`compile`方分析规则，返回`PackageDescr`，

```java
//DrlParser类中的parse方法
 public PackageDescr parse(final boolean isEditor,
                              final Resource resource) throws DroolsParserException, IOException {
     this.resource = resource;
     //获取resource的输入流
     InputStream is = resource.getInputStream();
     String encoding = null;
     if (resource instanceof ClassPathResource) {
         encoding = ((ClassPathResource) resource).getEncoding();
     }
     if (resource instanceof ReaderResource) {
         encoding = ((ReaderResource) resource).getEncoding();
     }
     if (resource instanceof InputStreamResource) {
         encoding = ((InputStreamResource) resource).getEncoding();
     }
     final DRLParser parser = getParser( is, encoding );
     return compile( isEditor, parser );
 }

//获取DRLParser
private DRLParser getParser( final InputStream is, final String encoding ) {
    try {
       //antlr工具中的类：一种从InputStream中提取的ReaderStream。用于从stdin读取并指定文件编码等
        ANTLRInputStream antlrInputStream;
        if (encoding != null) {
            antlrInputStream = new ANTLRInputStream(is, encoding);
        } else {
            antlrInputStream = new ANTLRInputStream(is);
        }
        //创建DRLLexer词法解析器。DRLLexer类继承了antlr中的Lexer
        lexer = new DRLLexer(antlrInputStream);
        //根据DRLLexer获取DRLParser，在DRLParser构造方法中会初始化RecognizerSharedState对象、ParserHelper和DRLExpressions对象。
        DRLParser parser = new DRLParser( new CommonTokenStream( lexer ) );
        return parser;
    } catch ( final Exception e ) {
        throw new RuntimeException( "Unable to parser Reader",e );
    }
}
```

> CommonTokenStream：最常见的令牌流，其中每个令牌被缓冲并且令牌被过滤以用于特定通道（解析器将仅看到这些令牌）。
>
> 即使它缓冲了所有令牌，该令牌流也会根据需要从令牌源中提取令牌。 换句话说，在您使用consume（），LT（）等请求令牌之前，流不会从词法分析器中提取。
>
>   此流和BufferedTokenStream超类之间的唯一区别是此流知道如何忽略关闭通道令牌。 如果你没有在隐藏的通道上将空格和注释等传递给解析器，那么使用超类可能会有性能优势（即，你在lexer规则中设置$ channel而不是调用skip（）。）
>
> ParserHelper：是一个包含DRL解析器使用的所有辅助函数/方法的类
>
> DRLExpressions：继承了antlr中的Parser(语法分析器)类，并实现Drools自己的分析功能

5. `DrlParser.compile`方法会调用传入的`DRLParser(语法分析器)`类中的`compilationUnit`方法分析该`esource`中规则是否有错误

```java
private PackageDescr compile(boolean isEditor,
                                  final DRLParser parser ) throws DroolsParserException {
        PackageDescr pkgDescr = null;
        try {
            if ( isEditor ) {
                parser.enableEditorInterface();
            }
            //传入的resource会使用DescrFactory.newPackage(resource)转换成PackageDescrBuilder对象
            pkgDescr = parser.compilationUnit(resource);
            editorSentences = parser.getEditorInterface();
            //得到分析时的错误
            makeErrorList( parser );
            if ( isEditor || !this.hasErrors() ) {
                return pkgDescr;
            } else {
                return null;
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            final ParserError err = new ParserError( resource,GENERIC_ERROR_MESSAGE + e.toString()+"\n"+ Arrays.toString( e.getStackTrace() ),-1, 0 );
            this.results.add( err );
            if ( isEditor ) {
                return pkgDescr;
            } else {
                throw new DroolsParserException(GENERIC_ERROR_MESSAGE+.getMessage(),e );
            }
        }
    }
```

6. 最后`drlToPackageDescr`方法`PackageDescr`传到`PackageBuilder.addPackage`方法中合并当前规则包和编译规则

```java
public void addPackage( final PackageDescr packageDescr ) {
    //初始化PackageRegistry
    PackageRegistry pkgRegistry = initPackageRegistry(packageDescr);
    if (pkgRegistry == null) {
        return;
    }

    currentRulePackage = pkgRegistryMap.size() -1;

    // 合并规则包
    mergePackage(pkgRegistry, packageDescr);

    compileAllRules(packageDescr, pkgRegistry);
}
```

#### 规则编译的方法

在PackageBuilder.addPackage方法中会对规则进行编译，一下为规则编译的方法

```java
private void compileRules(PackageDescr packageDescr, PackageRegistry pkgRegistry) {
    //获取function
    List<FunctionDescr> functions = packageDescr.getFunctions();
    if (!functions.isEmpty()) {
        for (FunctionDescr functionDescr : functions) {
            if (isEmpty(functionDescr.getNamespace())) {
                //确保在组件上设置了命名空间
                functionDescr.setNamespace(packageDescr.getNamespace());
            }
            //确保函数是用Java语言编译的
            functionDescr.setDialect("java");
            preCompileAddFunction(functionDescr);
        }
        //编译
        for (FunctionDescr functionDescr : functions) {
            //从包继承方言
            addFunction(functionDescr);
        }
        //我们现在需要编译所有函数，因此像mvel这样的脚本语言可以找到它们
        compileAll();
        for (FunctionDescr functionDescr : functions) {
            postCompileAddFunction( functionDescr );
        }
    }

    // 确保规则按依赖关系排序，以便以后生成依赖规则
    sortRulesByDependency(packageDescr);

    //遍历编译
    for (RuleDescr ruleDescr : packageDescr.getRules()) {
        if (isEmpty(ruleDescr.getNamespace())) {
            //设置命名空间
            ruleDescr.setNamespace(packageDescr.getNamespace());
        }
        Map<String, AttributeDescr> pkgAttributes = packageAttributes.get(packageDescr.getNamespace());
        //实体规则继承包属性
        inheritPackageAttributes(pkgAttributes, ruleDescr);
        if (isEmpty(ruleDescr.getDialect())) {
            ruleDescr.addAttribute(new AttributeDescr("dialect", pkgRegistry.getDialect()));
        }
        addRule(ruleDescr);
    }
}

private void addRule( final RuleDescr ruleDescr ) {
    if ( ruleDescr.getResource() == null ) {
        ruleDescr.setResource( resource );
    }

    PackageRegistry pkgRegistry = this.pkgRegistryMap.get( ruleDescr.getNamespace() );

    Package pkg = pkgRegistry.getPackage();
    //方言配置注册表。 它还负责向所有已注册的方言发布行动。
    DialectCompiletimeRegistry ctr = pkgRegistry.getDialectCompiletimeRegistry();
    //当前规则的上下文对象
    RuleBuildContext context = new RuleBuildContext( this,ruleDescr,ctr,pkg,ctr.getDialect( pkgRegistry.getDialect() ) );
    //调用RuleBuilder的build方法构建规则
    ruleBuilder.build( context );

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

    pkg.addRule( context.getRule() );
}
```

RuleBuilder.build(final RuleBuildContext context)方法

```java
public void build(final RuleBuildContext context) {
        RuleDescr ruleDescr = context.getRuleDescr();
        //Query and get object instead of using String
        if ( null != ruleDescr.getParentName() && null != context.getPkg().getRule( ruleDescr.getParentName() ) ) {
            context.getRule().setParent( context.getPkg().getRule( ruleDescr.getParentName() ) );
        }
        //添加规则的所有元属性
        buildMetaAttributes( context );
    	//RuleConditionBuilder用于定义能够构建特定条件元素的类的接口。
        final RuleConditionBuilder builder = (RuleConditionBuilder) context.getDialect().getBuilder( ruleDescr.getLhs().getClass() );
        if ( builder != null ) {
            Pattern prefixPattern = null;
            if ( context.getRuleDescr() instanceof QueryDescr ) {
                prefixPattern = context.getDialect().getQueryBuilder().build( context,(QueryDescr) context.getRuleDescr() );
            }
            //这一步会去处理规则的模式，调用的是GroupElementBuilder.build，在该方法中会从context中获取方言（getDialect()）然后获取对应的Builder，这里同样会使用词法分析器和语法分析器对Parttern进行分析
            final GroupElement ce = (GroupElement) builder.build( context,
                                                                  ruleDescr.getLhs(),
                                                                  prefixPattern );
			//设置Lhs
            context.getRule().setLhs( ce );
        } else {
            throw new RuntimeDroolsException( "BUG: builder not found for descriptor class " + ruleDescr.getLhs().getClass() );
        }

        // 构建规则的所有属性
        // 必须在生成lhs之后，因为某些属性需要来自lhs的绑定
        buildAttributes( context );

        // Build the consequence and generate it's invoker/s
        // generate the main rule from the previously generated s.
        if ( !(ruleDescr instanceof QueryDescr) ) {
            // do not build the consequence if we have a query

            ConsequenceBuilder consequenceBuilder = context.getDialect().getConsequenceBuilder();
            consequenceBuilder.build( context, Rule.DEFAULT_CONSEQUENCE_NAME );
            
            for ( String name : ruleDescr.getNamedConsequences().keySet() ) {
                consequenceBuilder.build( context, name );
            }
        }
    }
```

编译规则是把.drl的文件解析为knowledgePackages

### KnowledgeBase收集规则

```java
KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
knowledgeBase.addKnowledgePackages(knowledgePackages);
```

调用newKnowledgeBase()方法之后drools会创建一个新的知识库，然后调用` addKnowledgePackages(Collection<KnowledgePackage> kpackages);`方法将规则包添加到知识库。这个过程中drools会对规则包中的规则进行解析，将其解析成ReteOO网络。

### 节点共享代码

在Drools中是存在节点共享的，drools在分析规则的每个节点的时候会调用BuildUtils.attachNode方法判断该类型的节点是否是共享的。

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

### 事实句柄（FactHandle）

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/drools-rete/20190116103535.png)

