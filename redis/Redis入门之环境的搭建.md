---
title: Redis入门之环境的搭建
date: 2017-08-20 13:09:50
tags: redis
---
## 一、 Ubuntu下redis的搭建 ##
这里直接使用命令下载以及安装，
<!--more-->
### 1、下载redis ###
首先进入root下

    root@ubuntu:~# wget http://download.redis.io/releases/redis-4.0.1.tar.gz

### 2、解压 ###
这里可以直接使用解压命令解压文件

    root@ubuntu:~# tar -xzvf redis-4.0.1.tar.gz 
### 3、编译安装 ###
解压完成后进入到解压后的目录中使用make命令既可编译
<figure>

    root@ubuntu:~# cd redis-4.0.1
    root@ubuntu:~/redis-4.0.1# make
</figure>
![](http://i.imgur.com/OLbC1eB.png)

如上图所示如果没有错误为编译成功

输入make test命令测试是否安装成功

    root@ubuntu:~/redis-4.0.1# make test
    
![](http://i.imgur.com/7uhWPED.png)

这里第一次可能会出现错误，如果出现错误可以再次执行该命令

### 4、启动redis ###
    root@ubuntu:~/redis-4.0.1# cd src/
    root@ubuntu:~/redis-4.0.1/src# ls

在redis的根目录下查看是否含有redis.conf文件

![](http://i.imgur.com/GDekIrp.png)

然后进入到src目录中使用ls命令查看是否包含以下文件


![](http://i.imgur.com/n5F2V47.png)

将redis添加到Path环境变量中，方便下次直接启动redis

	root@ubuntu:~/redis-4.0.1/src# cp redis-server /usr/local/bin/
	root@ubuntu:~/redis-4.0.1/src# cp redis-cli /usr/local/bin/
	
使用命令启动redis

	root@ubuntu:~/redis-4.0.1/src# redis-server 
![](http://i.imgur.com/2WyODnB.png)


如图表示服务启动成功，不要关闭该终端，使用shift+ctrl+t开启一个新的终端，输入redis-cli命令进入redis

![](http://i.imgur.com/emLpYDY.png)

配置成功

另外在Ubuntu中可以直接使用apt-get命令安装，这里直接盗用[菜鸟教程](https://www.runoob.com/redis/redis-install.html "菜鸟教程")了。
![](http://i.imgur.com/sF1LCM8.png)

## 二、在window中搭建 ##
这使用的是win7-64的系统
### 1、首先当然是下载 ###

[redis下载地址](https://github.com/MicrosoftArchive/redis/releases)

![](http://i.imgur.com/pugaQ5M.png)

### 2、安装 ###
安装版的直接点下一步就行了，安装完成之后直接进入在命令行中输入redis-cli既可
![](http://i.imgur.com/E9EFy95.png)

如果是解压版的需要解压后，进入到解压后的目录中使用redis-server命令启动服务既可使用。

## 三、连接远程redis ##

![](http://i.imgur.com/gjVNyMe.png)

-h后面为连接主机的ip地址，-p为端口号