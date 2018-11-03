---
title: CentOS下Nginx的配置安装
date: 2017-11-25 09:15:26
categories: Nginx
tags: [Nginx,CentOS]
---
>Nginx("engine x")是一款是由俄罗斯的程序设计师Igor Sysoev所开发高性能的 Web和 反向代理 服务器，也是一个 IMAP/POP3/SMTP 代理服务器。在高连接并发的情况下，Nginx是Apache服务器不错的替代品。
>NGINX是一个免费的开源高性能的HTTP服务器和反向代理，以及一个IMAP / POP3代理服务器。NGINX以其高性能，稳定性，丰富的功能集，简单的配置和低资源消耗而闻名。
>NGINX是为解决C10K问题而编写的一些服务器之一。与传统的服务器不同，NGINX不依赖线程来处理请求。相反，它使用了一个更具可扩展性的事件驱动（异步）体系结构。这种体系结构使用很小但更重要的是，在负载下可预测的内存量。即使您不希望同时处理数千个请求，您仍然可以从NGINX的高性能和小内存占用中受益。NGINX在所有方向都可以扩展：从最小的VPS一直到大型的服务器集群。<!--more-->
## 环境 ##
nginx使用编译安装，所以需要确定系统已经安装编译工具以及库文件
```java
yum -y install make zlib zlib-devel gcc-c++ libtool  openssl openssl-devel
```
## 安装 ##
### 安装PCRE ###
PCRE的作用是使nginx支持Rewrite功能。[https://sourceforge.net/projects/pcre/files/](https://sourceforge.net/projects/pcre/files/)
1. 下载，保存目录`/usr/local/nginx`
```java
[root@localhost nginx]# wget https://sourceforge.net/projects/pcre/files/pcre/8.41/pcre-8.41.tar.gz
```
2. 解压，解压之后进入目录
```java
[root@localhost nginx]# tar -zvxf pcre-8.41.tar.gz 

[root@localhost nginx]# cd pcre-8.41/
```
3. 编译安装
```java
[root@localhost pcre2-10.30]# ./configure 
[root@localhost pcre2-10.30]# make
[root@localhost pcre2-10.30]# make install
```
4. 查看版本
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/3951804.jpg)
### 安装nginx ###
如果nginx还需支持SSL则还需要安装http_ssl_module模块，这里就不介绍了。
下载，[http://nginx.org/download/](http://nginx.org/download/)选择对应的版本，这里使用1.9.0
```jva
[root@localhost nginx]# wget http://nginx.org/download/nginx-1.9.0.tar.gz
```
下载完成后，同样是要解压编译安装.
```java
[root@localhost nginx]# tar -zvxf nginx-1.9.0.tar.gz 
[root@localhost nginx]# cd nginx-1.9.0/
[root@localhost nginx-1.9.0]# ./configure --prefix=/usr/local/webserver/nginx --with-http_stub_status_module --with-pcre=/usr/local/nginx/pcre-8.41
[root@localhost nginx-1.9.0]# make
[root@localhost nginx-1.9.0]# make install
```
查看nginx版本
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/14426767.jpg)
配置文件路径：`/usr/local/webserver/nginx/conf/nginx.conf`，使用vim编辑配置文件，添加location：

![](http://oy09glbzm.bkt.clouddn.com/17-12-29/30992423.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

### 启动测试 ###
在测试之前先运行一个tomcat服务器，使用8080端口：
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/92166265.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
然后启动nginx服务器：
```
[root@localhost apache-tomcat-8.0.48]# /usr/local/webserver/nginx/sbin/nginx 
```
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/85044529.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
上面我们配置了转发`/test`:

![](http://oy09glbzm.bkt.clouddn.com/17-12-29/95715631.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)