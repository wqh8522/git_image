---
title: 分析ClassLoader工作机制
date: 2017-08-26 11:54:25
categories: java
tags: [classloader,jvm]
---
ClassLoader：类加载器，作用：
1. 负责将Class加载到JVM中；
2. 审查每个类应该又谁来加载，是一个父优先的等级加载机制；
3. 将Class字节码重新解析成JVM统一要求的对象格式
## ClassLoader类结构分析 ##
<!--more-->
![](http://i.imgur.com/R5tC01a.png)

- defineClass：可以将byte字节流解析成JVM能够识别的Class对象，这样我们也可以通过这个字节码直接创建Class对象形式的实例化对象
	
	api中的介绍
	![](http://i.imgur.com/bFjiI1f.png)
- findClass：使用指定的二进制名称查找类，此方法在ClassLoader的实现类中应该被重写，从而取得要加载类的字节码。该方法通常与defineClass一起使用，使用该方法加载字节码之后，然后调用defineClass方法生成类的Class对象。
- loadClass：使用指定的二进制名称来加载类，如果只想在运行时能够加载自己指定的一个类，则可以使用this.getClass().getClassLoader().loadClass("classname")获取这个类的class对象。
![](http://i.imgur.com/zX6j3RL.png)
- resolveClass：链接指定的类。如果想在类被加载到JVM中时就被链接，则可以使用该方法

查看源码可以发现ClassLoader是一个抽象类。如果要实现自己的ClassLoader，一般继承URLClassLoader。另外在ClassLoader中还有一些辅助方法：
![](http://i.imgur.com/vKXgX4K.png)

## ClassLoader的等级加载机制 ##
整个JVM提供了三层	ClassLoader，可以分为两种类型。
1. BootStrap ClassLoader：启动类加载器。该类仅仅是一个类记载工具，既没有更高一级的父加载器，也没有子加载器。该类加载器并不属于JVM的类等级层次，因为BootStrap ClassLoader并没有遵守ClassLoader的加载规则，所以该加载器并没有子类。
2. ExtClassLoader：扩展类加载器。该类JVM自身的一部分，如果一个类既不是JVM内部的类，也不用户定义的类，将会由这个类来加载。加载System.getProperty("java.ext.dirs")所指定的路径或jar。打印主要加载目录

![](http://i.imgur.com/26dQrro.png)
3. AppClassLoader：系统类加载器。它是ExtClassLoader的子类。加载System.getProperty("java.class.path")所指定的路径或jar，这个目录就是我们常用到的classpath。除了System.getProperty("java.ext.dirs")目录下的类由ExtClassLoader加载，其他类都由AppClassLoader加载。

类加载器的等级结构图：

![](http://i.imgur.com/k8Ri1H5.png)

ClassLoader的类层次结构，参考深入分析JavaWeb技术内幕

![](http://i.imgur.com/N7lLSTQ.png)

## JVM加载Class文件 ##
### 加载方式 ###
JVM加载class文件到内存有两种方式：
- 显示加载：在代码中通过ClassLoader来加载需要的类。
	显示加载类的方式通常有：
	- Class.forName();
	- ClassLoader.loadClass();
	- ClassLoader.findSystemClass();
- 隐式加载：JVM自动加载需要的类到内存。
### 如何加载 ###
一般有三个阶段：
1. 将.class文件包含的字节码加载到内存中。
2. 字节码验证、Class类等数据结构的分析以及相应的内存分配和最后符号表的链接。
3. 类中静态属性和初始化赋值，以及静态块的执行。

![](http://i.imgur.com/8Q2U44B.png)

