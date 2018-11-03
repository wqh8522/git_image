---
title: Java8：Lambda表达式增强版Comparator和排序
date: 2018-03-29 16:49:32
categories: 转载
tags: ['java8','排序']

---

### **1、概述**

在这篇教程里，我们将要去了解下即将到来的**JDK 8（译注，现在JDK 8已经发布了）中的Lambda表达式——特别是怎样使用它来编写Comparator和对集合（Collection）进行排序。**

这篇文章是Baeldung上的[“Java ——回归基础”（“Java – Back to Basic”）系列](http://www.baeldung.com/java-tutorial)的一部分。 <!--more-->

首先，让我们先定义一个简单的实体类：

```java
public class Human {
    private String name;
    private int age;
 
    public Human() {
        super();
    }
 
    public Human(final String name, final int age) {
        super();
 
        this.name = name;
        this.age = age;
    }
 
    // standard getters and setters
}
```

### **2、不使用Lambda表达式的基本排序**

在Java 8之前，对集合进行排序要**为Comparator创建一个匿名内部类**用来排序：

```java
new Comparator<Human>() {
    @Override
    public int compare(Human h1, Human h2) {
    	return h1.getName().compareTo(h2.getName());
    }
}
```

简单地用它来对Human实体列表进行排序：

```java
@Test
public void givenPreLambda_whenSortingEntitiesByName_thenCorrectlySorted() {
	List<Human> humans = Lists.newArrayList(new Human("Sarah", 10), new Human("Jack", 12));
	Collections.sort(humans, new Comparator<Human>() {
        @Override
        public int compare(Human h1, Human h2) {
            return h1.getName().compareTo(h2.getName());
        }
    });
    Assert.assertThat(humans.get(0), equalTo(new Human("Jack", 12)));
}
```

### **3、使用Lambda表达式的基本排序**

根据Lambda表达式的介绍，我们现在可以不使用匿名内部类，只使用**简单实用的语义**就可以得到相同的结果。

```java
(final Human h1, final Human h2) -> h1.getName().compareTo(h2.getName());
```

类似地，我们现在可以像之前那样来测试它的行为：

```java
@Test
public void whenSortingEntitiesByName_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(new Human("Sarah", 10), new Human("Jack", 12));

    humans.sort((Human h1, Human h2) -> h1.getName().compareTo(h2.getName()));
    Assert.assertThat(humans.get(0), equalTo(new Human("Jack", 12)));
}
```

注意：我们同样使用**新的sort API，这个API在Java 8里被添加到java.util.List** ——而不是旧的Collections.sort API。

### **4、没有类型定义（ Type Definitions）的基本排序**

我们通过不指定类型定义来进一步简化表达式 ——**编译器自己可以进行类型判断**：

```java
(h1, h2) -> h1.getName().compareTo(h2.getName())
```

测试仍然很相似：

```java
@Test
public void givenLambdaShortForm_whenSortingEntitiesByName_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(new Human("Sarah", 10), new Human("Jack", 12));
    humans.sort((h1, h2) -> h1.getName().compareTo(h2.getName()));
    Assert.assertThat(humans.get(0), equalTo(new Human("Jack", 12)));
}
```

### **5、使用静态方法的引用来排序**

下面我们将要使用带有静态方法引用的Lambda表达式去进行排序。

首先，我们要定义compareByNameThenAge方法 ——这个方法拥有与Comparator<Human>对象里的compareTo方法完全相同的签名：

```java
public static int compareByNameThenAge(Human lhs, Human rhs) {
    if (lhs.name.equals(rhs.name)) {
        return lhs.age - rhs.age;
    } else {
        return lhs.name.compareTo(rhs.name);
    }
}
```

现在，我们要使用这个引用去调用humans.sort方法：

```java
humans.sort(Human::compareByNameThenAge);
```

最终结果是一个使用静态方法作为Comparator的有效的排序集合：

```java
@Test
public void givenMethodDefinition_whenSortingEntitiesByNameThenAge_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(new Human("Sarah", 10), new Human("Jack", 12));
 
    humans.sort(Human::compareByNameThenAge);
    Assert.assertThat(humans.get(0), equalTo(new Human("Jack", 12)));
}
```

### **6、提取Comparator进行排序**

我们可以通过使用**实例方法的引用**和Comparator.comparing方法来避免定义比较逻辑——它会提取和创建一个基于那个函数的Comparable。

我们准备使用getName() getter方法去建造Lambda表达式并通过name对列表进行排序：

```java
@Test
public void givenInstanceMethod_whenSortingEntitiesByNameThenAge_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(new Human("Sarah", 10), new Human("Jack", 12));
 
    Collections.sort(humans, Comparator.comparing(Human::getName));
    Assert.assertThat(humans.get(0), equalTo(new Human("Jack", 12)));
}
```

### **7、反转排序**

JDK 8同样提供了一个有用的方法用来**反转Comparator（reverse Comparator）**——我们可以快速地利用它来反转我们的排序：

```java
@Test
public void whenSortingEntitiesByNameReversed_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(
      new Human("Sarah", 10), new Human("Jack", 12));
    Comparator<Human> comparator = (h1, h2) -> h1.getName().compareTo(h2.getName());
 
    humans.sort(comparator.reversed());
    Assert.assertThat(humans.get(0), equalTo(new Human("Sarah", 10)));
}
```

### **8、多条件排序**

比较操作的Lambda表达式不一定都是这么简单的——我们**同样可以编写更复杂的表达式，**比如先根据name后根据age来对实体进行排序：

```java
@Test
public void whenSortingEntitiesByNameThenAge_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(
      new Human("Sarah", 12), new Human("Sarah", 10), new Human("Zack", 12));
 
    humans.sort((lhs, rhs) -> {
        if (lhs.getName().equals(rhs.getName())) {
            return lhs.getAge() - rhs.getAge();
        } else {
            return lhs.getName().compareTo(rhs.getName());
        }
    });
    Assert.assertThat(humans.get(0), equalTo(new Human("Sarah", 10)));
}
```

### **9、多条件组合排序**

同样的比较逻辑——先根据name进行排序其次是age，同样可以通过Comparator新的组合支持来实现。

**从JDK 8开始，我们现在可以把多个Comparator链在一起（chain together）**去建造更复杂的比较逻辑：

```java
@Test
public void givenComposition_whenSortingEntitiesByNameThenAge_thenCorrectlySorted() {
    List<Human> humans = Lists.newArrayList(
      new Human("Sarah", 12), new Human("Sarah", 10), new Human("Zack", 12));
 
    humans.sort(Comparator.comparing(Human::getName).thenComparing(Human::getAge));
    Assert.assertThat(humans.get(0), equalTo(new Human("Sarah", 10)));
}
```

### **10、总结**

这篇文章举例说明了多种令人兴奋的方法：**使用Java 8 Lambda表达式对列表进行排序**——正确使用过去的语法糖和真正、强大实用的语义。

所有这些例子的实现和代码片段都可以在[我的github项目](https://github.com/eugenp/tutorials/tree/master/core-java-8#readme)上获取到——这是一个基于Eclipse的项目，所以它应该很容易被导入和运行。

原文链接： [baeldung](http://www.baeldung.com/java-8-sort-lambda) 翻译： [ImportNew.com ](http://www.importnew.com/)- [进林](http://www.importnew.com/author/8zjl8)
译文链接： <http://www.importnew.com/15259.html>