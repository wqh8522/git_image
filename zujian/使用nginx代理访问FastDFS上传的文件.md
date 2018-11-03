---
title: 使用nginx代理访问FastDFS上传的文件
date: 2017-12-29 09:12:24
categories: FastDFS
tags: [FastDFS,Nginx]
---

上一次介绍了关于FastDFS系统单机版的安装配置以及使用官方测试上传图片：
[Linux下FastDFS系统的搭建](http://www.wanqhblog.top/2017/12/27/FastDFS%E7%B3%BB%E7%BB%9F%E7%9A%84%E6%90%AD%E5%BB%BA/)
上传成功返回的链接还无法访问，因为FastDFS不支持http协议，所以要想直接访问，还需要使用nginx访问传的图片，文件。

![](http://oy09glbzm.bkt.clouddn.com/17-12-27/58813378.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/70/gravity/SouthEast/dx/5/dy/5)
<!--more-->
## nginx的搭建 ##
这里就不介绍了，传送门：[CentOS下Nginx的配置安装](http://www.wanqhblog.top/2017/11/25/CentOS%E4%B8%8BNginx%E7%9A%84%E9%85%8D%E7%BD%AE%E5%AE%89%E8%A3%85/)

## fastdfs-nginx-module ##
下载地址：[https://github.com/happyfish100/fastdfs-nginx-module](https://github.com/happyfish100/fastdfs-nginx-module)
这里我从新安装nginx服务器，命令如下：如果是在现有的nginx上添加，可能会比较麻烦
```
[root@localhost nginx]# wget https://github.com/happyfish100/fastdfs-nginx-module/archive/master.zip
[root@localhost nginx]# unzip master.zip
[root@localhost nginx]# cd nginx-1.9.0/
[root@localhost nginx-1.9.0]#  ./configure --prefix=/usr/local/webserver/nginx --with-http_stub_status_module --with-pcre=/usr/local/nginx/pcre-8.41 --add-module=/usr/local/nginx/fastdfs-nginx-module-master/src
[root@localhost nginx-1.9.0]# make
[root@localhost nginx-1.9.0]# make install
```
安装完成之后要三个配置文件拷贝到fastDFS的配置文件目录下，我这里是`/etc/fdfs`
```
[root@localhost nginx-1.9.0]# cp /usr/local/nginx/fastdfs-nginx-module-master/src/mod_fastdfs.conf /etc/fdfs

[root@localhost src]# cp /usr/local/fastdfs/fastdfs-5.11/conf/http.conf /etc/fdfs/

[root@localhost src]# cp /usr/local//fastdfs/fastdfs-5.11/conf/mime.types /etc/fdfs/

```
修改mod_fastdfs.conf 配置信息：
```java
[root@localhost bin]# vim /etc/fdfs/mod_fastdfs.conf 
```
```
base_path=/data/fastdfs/storage
tracker_server=192.168.18.130:22122
store_path0=/data/fastdfs/storage
```
然后建立软连接：指向文件保存路径
```
[root@localhost nginx-1.9.0]# ln -s /data/fastdfs/storage/data/ /data/fastdfs/storage/data/M00
```
最后修改nginx的配置文件：添加location

![](http://oy09glbzm.bkt.clouddn.com/17-12-29/82209240.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)


## 测试 ##
启动nginx：
`[root@localhost src]# /usr/local/webserver/nginx/sbin/nginx -s reload`
再次使用客户端测试类上传一张图片或者文件：
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/76287184.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
在浏览器访问返回的链接：
![](http://oy09glbzm.bkt.clouddn.com/17-12-29/17508526.jpg?imageView2/0/q/75|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/300/fill/IzJDMUJFRQ==/dissolve/100/gravity/SouthEast/dx/10/dy/10)


----------
关于nginx+FastDFS的简单配置介绍完成！！！