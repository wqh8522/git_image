---
title: Linux下FastDFS系统的搭建
date: 2017-12-27 11:41:58
categories: FastDFS
tags: 分布式文件系统
---
FastDFS是由淘宝的余庆先生所开发，是一个轻量级、高性能的开源分布式文件系统，用纯C语言开发，包括文件存储、文件同步、文件访问（上传、下载）、存取负载均衡、在线扩容、相同内容只存储一份等功能，适合有大容量存储需求的应用或系统。做分布式系统开发时，其中要解决的一个问题就是图片、音视频、文件共享的问题，分布式文件系统正好可以解决这个需求。同类的分布式文件系统有谷歌的GFS、HDFS（Hadoop）、TFS（淘宝）等。 <!--more-->

源码开放下载地址：[https://github.com/happyfish100 ](https://github.com/happyfish100 )
早期源码开放下载地址：[https://sourceforge.net/projects/fastdfs/files/ ](https://sourceforge.net/projects/fastdfs/files/ )
官网论坛：[http://bbs.chinaunix.net/forum-240-1.html](http://bbs.chinaunix.net/forum-240-1.html)
## 术语 ##
- <font color="red">Tracker Server：</font>跟踪服务器，主要做调度工作，在访问上起负载均衡的作用。记录storage server的状态，是连接Client和Storage server的枢纽。
- <font color="red">Storage Server：</font>存储服务器，文件和meta data都保存到存储服务器上
- <font color="red"> group：</font>组，也可称为卷。同组内服务器上的文件是完全相同的
 文件标识：包括两部分：组名和文件名（包含路径）
-  <font color="red">meta data：</font>文件相关属性，键值对（Key Value Pair）方式，如：width=1024,heigth=768
## FastDFS结构 ##
### 系统架构图 ###
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/68974025.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
部署结构
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/61576190.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
### 文件上传下载 ###
**上传流程**
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/23619298.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
**下载流程**
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/82524766.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
## 系统的部署 ##
### 环境介绍 ###
操作系统：centerOS 7.3 需要有gcc,安装命令
`yum -y install gcc gcc+ gcc-c++ openssl openssl-devel pcre pcre-devel`
FastDFS ：5.11
### 安装libfastcommon（公共代码） ###
下载zip包
```linux
 wget https://github.com/happyfish100/libfastcommon/archive/master.zip
```
解压，如果解压出现`-bash: unzip: command not found`，说明系统没有安装unzip和zip，使用`yum install -y unzip zip`安装即可
```linux
unzip master.zip 
```
编译安装
```linux
 ./make.sh

 ./make.sh install
```
查看此库文件是否存在，存在则表示安装成功
`ls -l /usr/lib64/libfastcommon.so`

### 安装FastDFS ###
同样需要下载解压和编译安装
```Bash
wget https://github.com/happyfish100/fastdfs/archive/V5.11.zip

unzip V5.11.zip

./make.sh 

./make.sh install

```
查看fdfs的相关命令,fastDFS命令目录：/usr/bin
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/4190816.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
## 配置文件介绍 ##
FastDFS的配置文件在/etc/fdfs下面，默认都是以.sample为后缀的，这个后缀要去掉，最好就是先复制一份出来。配置文件大部分是保持默认的，要修改的地方不多。
### tracker.conf配置介绍 ###
```Bash
# ===========================基本配置====================================
	disabled=false
	#配置tracker.conf这个配置文件是否生效，因为在启动fastdfs服务端进程时需要指定配置文件，所以需要使次配置文件生效。false是生效，true是让此文件不生效。
	bind_addr=
	#程序的监听地址，如果不设定则监听所有地址
	port=22122
	#tracker监听的端口
	connect_timeout=30
	#连接超时时间，针对socket套接字函数connect，默认为30秒
	network_timeout=60
	#网络通讯超时（秒）
	base_path=/home/yuqing/fastdfs
	#Tracker数据/日志目录地址，这里一般要修改的，如：/opt/fdfs
	max_connections=256
	#允许的最大连接数，这里要根据需求调整.如设置成2048.
	accept_threads=1
	#接收数据的线程数
	work_threads=4
	#工作线程数，一般为cpu个数，当然CPU核数太多的话可以稍小一点。如我们是12CCPU，这里设置为8.
	min_buff_size = 8KB
	#接收/发送数据的buff大小，必须大于8KB
	max_buff_size = 128KB
	#接收/发送数据的buff大小必须小于128KB
	store_lookup=2
	#在存储文件时选择group的策略也就是文件上传选取group的规则，0:轮训策略 1:指定某一个组 2:负载均衡，选择空闲空间最大的group。
	store_group=group2
	#如果上面store_lookup=1，这里就起作用了，这里就要指定某一个组来存储上传的文件了。（一般store_lookup=1是由于旧的组已经没有空间了，因为下面有规则设置，所以就要新添加的机器就要放到新组里面，所以再上传就要传到新组里面。）
	store_server=0
	# 选择哪个storage server 进行上传操作一个文件被上传后，这个storage server就相当于这个文件的storage server源，会对同组的storage server推送这个文件达到同步效果
	# 0: 轮询方式（默认）
	# 1: 根据ip 地址进行排序选择第一个服务器（IP地址最小者）
	# 2: 根据优先级进行排序（上传优先级由storage server来设置，参数名为upload_priority），优先级值越小优先级越高。
	store_path=0
	#选择文件上传到storage中的哪个(目录/挂载点),storage可以有多个存放文件的base path 0:轮训策略 2:负载均衡，选择空闲空间最大的。我们线上选择的是2，让其选择空闲空间最大的去存放。
	download_server=0
	# 选择哪个 storage server 作为下载服务器。
	# 0: 轮询方式，可以下载当前文件的任一storage server
	# 1: 哪个为源storage server就用哪一个,就是之前上传到哪个storage server服务器就是哪个了
	reserved_storage_space = 10%
	#系统预留空间，当一个group中的任何storage的剩余空间小于定义的值，整个group就不能上传文件了（V4开始支持百分比方式），如：10G（字节数可以是GB,MB,KB,B）或者10%。
	log_level=info
	#日志信息级别
	run_by_group=
	#进程以那个用户组运行，不指定默认是当前用户组
	run_by_user=
	#进程以那个用户运行，不指定默认是当前用户
	allow_hosts=*
	# 可以连接到此 tracker server 的ip范围，默认是允许所有（对所有类型的连接都有影响，包括客户端，storage server）
	# for example:
	# allow_hosts=10.0.1.[1-15,20]
	# allow_hosts=host[01-08,20-25].domain.com
	# allow_hosts=192.168.5.64/26
	sync_log_buff_interval = 10
	#同步或刷新日志信息到硬盘的时间间隔，单位为秒。注意：tracker server的日志不是时时写硬盘的，而是先写内存。
	check_active_interval = 120
	#检测storage服务器的间隔时间，storage定期主动向tracker发送心跳，如果在指定的时间没收到信号，tracker认为storage故障，默认120s
	thread_stack_size = 64KB
	#线程栈的大小。FastDFS server端采用了线程方式。线程栈越大，一个线程占用的系统资源就越多。如果要启动更多的线程可以适当降低本参数值。
	storage_ip_changed_auto_adjust = true
	#这个参数控制当storage server IP地址改变时，集群是否自动调整。注：只有在storage server进程重启时才完成自动调整。
	# ===========================同步======================================
	storage_sync_file_max_delay = 86400
	# V2.0引入的参数。存储服务器之间同步文件的最大延迟时间，缺省为1天。根据实际情况进行调整。
	storage_sync_file_max_time = 300
	# V2.0引入的参数。存储服务器同步一个文件需要消耗的最大时间，缺省为300s，即5分钟。
	# 注：本参数并不影响文件同步过程。本参数仅在下载文件时，作为判断当前文件是否被同步完成的一个阀值。
	# ===========================trunk 和 slot============================
	use_trunk_file = false 
	# V3.0引入的参数。是否使用小文件合并存储特性，缺省是关闭的。
	slot_min_size = 256
	#V3.0引入的参数。trunk file分配的最小字节数。比如文件只有16个字节，系统也会分配slot_min_size个字节。
	slot_max_size = 16MB
	#V3.0引入的参数。只有文件大小<=这个参数值的文件，才会合并存储。如果一个文件的大小大于这个参数值，将直接保存到一个文件中（即不采用合并存储方式）。
	trunk_file_size = 64MB
	#V3.0引入的参数。合并存储的trunk file大小，至少4MB，缺省值是64MB。不建议设置得过大。
	trunk_create_file_advance = false
	#是否开启创建trunk file。只有当这个参数为true，下面3个以trunk_create_file_打头的参数才有效。默认我们开启trunk合并。
	trunk_create_file_time_base = 02:00
	# 提前创建trunk file的起始时间点（基准时间），02:00表示第一次创建的时间点是凌晨2点  
	trunk_create_file_interval = 86400
	#创建trunk file的时间间隔，单位为秒。如果每天只提前创建一次，则设置为86400  
	trunk_create_file_space_threshold = 20G
	# 提前创建trunk file时，需要达到的空闲trunk大小，比如本参数为20G，而当前空闲trunk为4GB，那么只需要创建16GB的trunk file即可。  
	trunk_init_check_occupying = false
	#trunk初始化时，是否检查可用空间是否被占用
	trunk_init_reload_from_binlog = false
	#是否无条件从trunk binlog中加载trunk可用空间信息，FastDFS缺省是从快照文件storage_trunk.dat中加载trunk可用空间，该文件的第一行记录的是trunk binlog的offset，然后从binlog的offset开始加载
	trunk_compress_binlog_min_interval = 0
	#压缩trunk binlog 的最小时间间隔，单位：秒。默认值为0，0代表不压缩。FastDFS会在trunk初始化或者被销毁的时候压缩trunk binlog文件，如果设置的话建议设置成86400，一天设置一次。
	# ===========================其他设置============================
	use_storage_id = false
	#是否使用storage id替换ip作为storage server标识，默认为false
	storage_ids_filename = storage_ids.conf
	#在文件中设置组名、server ID和对应的IP地址，参见源码目录下的配置示例：storage_ids.conf
	id_type_in_filename = ip
	#存储服务器的文件名中的id类型，取值如下
	# IP：存储服务器的IP地址
	# id：被存储服务器的服务器标识
	# 只有当use_storage_id设置为true时此参数是有效的，默认值是IP
	store_slave_file_use_link = false
	#存储从文件是否采用symbol link（符号链接）方式。如果设置为true，一个从文件将占用两个文件：原始文件及指向它的符号链接。
	rotate_error_log = false
	#是否定期轮转error log，目前仅支持一天轮转一次
	error_log_rotate_time=00:00
	#error log定期轮转的时间点，只有当rotate_error_log设置为true时有效
	rotate_error_log_size = 0
	#error log按大小轮转，设置为0表示不按文件大小轮转，否则当error log达到该大小，就会轮转到新文件中
	log_file_keep_days = 0
	#保留日志文件0表示不删除旧日志文件，默认值为0
	use_connection_pool = false
	#是否使用连接池
	connection_pool_max_idle_time = 3600
	#连接的空闲时间超过这个时间将被关闭，单位：秒
	# ===========================HTTP 相关=================================
	http.server_port=8080
	#tracker server上的HTTP服务器端口号
	http.check_alive_interval=30
	# 检查storage http server存活的间隔时间，单位为秒
	http.check_alive_type=tcp
	#检查存储HTTP服务器的活动类型，值为：tcp：仅连接到具有HTTP端口的存储服务器，不要求和获取响应http：storage check alive url必须返回http状态200默认值为tcp
	http.check_alive_uri=/status.html
	#检查storage http server是否alive的uri/url
```
>上面有很大一部分提到了trunk合并，一般我们是默认不合并的，小文件就是存储到目录里面。下面两篇链接讲的比较详细和专业：
>[http://blog.csdn.net/hfty290/article/details/42026215](http://blog.csdn.net/hfty290/article/details/42026215)
>[http://blog.csdn.net/liuaigui/article/details/9981135](http://blog.csdn.net/liuaigui/article/details/9981135)
### storage.conf配置文件介绍 ###
```Bash
	disabled=false
	#是否启用禁用配置文件，false是不启用禁用，true是启动禁用。
	group_name=group1
	#本storage server所属组名
	bind_addr=
	#绑定IP，默认就是监听在0.0.0.0上面
	client_bind=true
	#bind_addr通常是针对server的。当指定bind_addr时，本参数才有效。本storage server作为client连接其他服务器（如tracker server、其他storage server），是否绑定bind_addr。true：绑定bind_addr所指定的IP。false：绑定本机的任意IP
	port=23000
	#storage server服务端口
	connect_timeout=30
	#连接超时(秒)，默认值 30s
	network_timeout=60
	#网络超时(秒)，默认值 60s
	heart_beat_interval=30
	#心跳间隔时间，单位为秒 (这里是指主动向tracker server 发送心跳)
	stat_report_interval=60
	#storage server向tracker server报告磁盘剩余空间的时间间隔，单位为秒。
	base_path=/home/yuqing/fastdfs
	# base_path目录地址,根目录必须存在子目录会自动生成。注 :这里不是上传的文件存放的地址,之前是的,在某个版本后更改了
	max_connections=256
	#最大连接数，根据需求可以调大，如设置成2048.
	buff_size = 256KB
	#接收/发送数据的buff大小，必须大于8KB，如这里可以设置成2048KB，设置的大一点。
	accept_threads=1
	#接收数据的线程数
	work_threads=4
	#接收数据的线程数，可以是CPU的核数或者比CPU核数小。
	disk_rw_separated = true
	#磁盘IO读写是否分离，缺省是分离的。磁盘读/写分离为false则为混合读写，如果为true则为分离读写的。默认值为V2.00以后为true。
	disk_reader_threads = 1
	# 针对单个存储路径的读线程数，缺省值为1。我们这里设置为4.
	# 读写分离时，系统中的读线程数 = disk_reader_threads * store_path_count
	# 读写混合时，系统中的读写线程数 = (disk_reader_threads + disk_writer_threads) * store_path_count
	disk_writer_threads = 1
	# 针对单个存储路径的写线程数，缺省值为1。我们这里设置为4.
	# 读写分离时，系统中的写线程数 = disk_writer_threads * store_path_count
	# 读写混合时，系统中的读写线程数 = (disk_reader_threads + disk_writer_threads) * store_path_count
	sync_wait_msec=50
	# 同步文件时，如果从binlog中没有读到要同步的文件，休眠N毫秒后重新读取。0表示不休眠，立即再次尝试读取。如果没特别需求，默认值50毫秒就可以了。
	# 出于CPU消耗考虑，不建议设置为0。如何希望同步尽可能快一些，可以将本参数设置得小一些，比如设置为10ms
	sync_interval=0
	#同步完一个文件后间隔多少毫秒同步下一个文件，0表示不休息直接同步
	sync_start_time=00:00
	sync_end_time=23:59
	#上面两段表示这段时间内同步文件，也就是说全天都在同步数据。第一条是开始时间段，第二条是结束时间段。
	write_mark_file_freq=500
	#同步完多少文件后写mark标记，什么是mak标记等下面搭建的时候会文件举例。
	store_path_count=1
	#storage在存储文件时支持多路径，默认只设置一个，通俗的将也就是你要挂载几个存储盘，你这里就设置对应的磁盘数，比如12块盘当store，这里就设置为12.
	store_path0=/home/yuqing/fastdfs
	#逐一配置store_path个路径，索引号基于0。注意配置方法后面有0,1,2 ......，需要配置0到store_path - 1。如果不配置base_path0，那边它就和base_path对应的路径一样。
	#如下面的配置（这里是两块盘，所以store_path_count=2）：
	#store_path0=/data01/fastdfs 
	#store_path1=/data02/fastdfs 
	subdir_count_per_path=256
	# FastDFS存储文件时，采用了两级目录。如果本参数只为N（如：256），那么storage server在初次运行时，会自动创建 N * N 个存放文件的子目录。
	tracker_server=192.168.209.121:22122
	#tracker_server 的列表要写端口(是主动连接tracker_server)有多个tracker server时，每个tracker server写一行，如下面：
	#192.168.1.103:22122
	#192.168.1.104:22122
	log_level=info
	#日志级别,我们这里设置为了warn。
	run_by_group=
	#指定运行该程序的用户组，不设置就默认为当前启动程序的用户组。
	run_by_user=
	#指定运行该程序的用户，不设置就默认为当前启动程序的用户。
	allow_hosts=*
	#默认是允许所有主机连接
	file_distribute_path_mode=0
	# 0: 轮流存放，在一个目录下存储设置的文件数后（参数file_distribute_rotate_count中设置文件数），使用下一个目录进行存储。
	# 1: 随机存储，根据文件名对应的hash code来分散存储。
	file_distribute_rotate_count=100
	# 当上面的参数file_distribute_path_mode配置为0（轮流存放方式）时，本参数有效。
	# 当一个目录下的文件存放的文件数达到本参数值时，后续上传的文件存储到下一个目录中。
	fsync_after_written_bytes=0
	#写大文件时调用fsync到磁盘，0：永远不要调用fsync。other：写多少字节后开始同步，当写入字节> =这个字节时调用fsync。默认值为0（从不调用fsync）
	sync_log_buff_interval=10
	# 同步或刷新日志信息到硬盘的时间间隔，单位为秒。注意：storage server 的日志信息不是时时写硬盘的，而是先写内存。
	sync_binlog_buff_interval=10
	#同步binglog（更新操作日志）到硬盘的时间间隔，单位为秒。本参数会影响新上传文件同步延迟时间
	sync_stat_file_interval=300
	# 把storage的stat文件同步到磁盘的时间间隔，单位为秒。注：如果stat文件内容没有变化，不会进行同步
	thread_stack_size=512KB
	# 线程栈的大小。FastDFS server端采用了线程方式。应该大于等于512KB。
	upload_priority=10
	#本storage server作为源服务器，上传文件的优先级，可以为负数。值越小，优先级越高。里就和 tracker.conf 中store_server= 2时的配置相对应了。
	if_alias_prefix=
	#网卡别名，用ifconfig -a可以看到很多本机的网卡别名，类似eth0,eth0:0等等。多个网卡别名使用逗号分割，默认为空，让系统自动选择。
	check_file_duplicate=0
	# 是否检测上传文件已经存在。如果已经存在，则不存文件内容，建立一个符号链接以节省磁盘空间。 这个应用要配合FastDHT 使用，所以打开前要先安装FastDHT。1或yes 是检测，0或no 是不检测
	file_signature_method=hash
	#文件签名方法用于检查文件重复，hash：四个32位哈希码。md5：MD5签名。默认值是V4.01之后的哈希
	key_namespace=FastDFS
	#当参数check_file_duplicate设定为1 或 yes时(true/on也是可以的)，在FastDHT中的命名空间。
	keep_alive=0
	# 与FastDHT servers 的连接方式 (是否为持久连接) ，默认是0（短连接方式）。可以考虑使用长连接，这要看FastDHT server的连接数是否够用。
	use_access_log = false
	#是否记录访问日志
	rotate_access_log = false
	# 是否定期轮转access log，目前仅支持一天轮转一次
	access_log_rotate_time=00:00
	# access log定期轮转的时间点，只有当rotate_access_log设置为true时有效
	rotate_error_log = false
	#是否定期轮转error log，目前仅支持一天轮转一次
	error_log_rotate_time=00:00
	# error log定期轮转的时间点，只有当rotate_error_log设置为true时有效
	rotate_access_log_size = 0
	# access log按文件大小轮转，设置为0表示不按文件大小轮转，否则当access log达到该大小，就会轮转到新文件中
	rotate_error_log_size = 0
	# error log按文件大小轮转，设置为0表示不按文件大小轮转，否则当error log达到该大小，就会轮转到新文件中
	log_file_keep_days = 0
	#保留日志文件的日期0表示不删除旧的日志文件
	file_sync_skip_invalid_record=false
	#文件同步的时候，是否忽略无效的binlog记录
	use_connection_pool = false
	# 是否使用连接池
	connection_pool_max_idle_time = 3600
	# 连接的空闲时间超过这个时间将被关闭，单位：秒
	http.domain_name=
	#如果domain_name为空，请使用此存储服务器的IP地址，否则此域名将在由跟踪服务器重定向的URL中出现
	http.server_port=8888
	#该存储服务器上的Web服务器的端口
```
### client.conf配置文件介绍 ###
```Bash
	connect_timeout=30
	#连接的超时时间
	network_timeout=60
	#网络超时(秒)，默认值 60s
	base_path=/home/yuqing/fastdfs
	#存储日志文件的基本路径，如/opt/fastdfs
	tracker_server=192.168.0.197:22122
	#tracker server的列表，多个的话就是多行，如：
	#192.168.1.103:22122
	#192.168.1.104:22122
	log_level=info
	#日志级别
	use_connection_pool = false
	#是否使用连接池
	connection_pool_max_idle_time = 3600
	#连接的空闲时间超过这个时间将被关闭，单位：秒
	load_fdfs_parameters_from_tracker=false
	#是否加载来自跟踪服务器的FastDFS参数，默认值为false。这里可以设置为true。
	use_storage_id = false
	#是否使用storage id替换ip作为storage server标识，默认为false
	storage_ids_filename = storage_ids.conf
	#在文件中设置组名、server ID和对应的IP地址，参见源码目录下的配置示例：storage_ids.conf
	http.tracker_server_port=80
	#HTTP设置，tracker server上的HTTP服务器端口号
```
### 配置启动Tracker ###
首先创建数据目录，并修改tracker.conf配置文件
```Bash
mkdir  /data/fastdfs/tracker
```
修改配置文件
`vim /etc/fdfs/tracker.conf`
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/80552480.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
启动tracker
```java

cd /usr/bin/

fdfs_trackerd /etc/fdfs/tracker.conf restart

```
查看启动结果，tracker的默认端口为 22122
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/15145051.jpg)

### 配置启动storage ###
按照前面的介绍，storage应该安装在其他服务器节点上，并且可以添加多个groups。这里为了方便，直接跟Tracker在同一台服务器，并且就不安装多个了。
修storage.conf配置文件
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/63365541.jpg)
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/12855148.jpg)
启动stroge
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/58576437.jpg)

## 测试 ##
直接下载官方的测试demo：[https://github.com/happyfish100/fastdfs-client-java](https://github.com/happyfish100/fastdfs-client-java)
这里使用idea运行项目，新建fastDFS.conf
```java
connect_timeout = 2
network_timeout = 20
#tracker服务器地址，如果有多个tracker可以配置多个tracker_server
tracker_server=192.168.18.129:22122
```
需要运行的类，先添加两个参数：一个是配置文件，一个是要上传文件。
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/33260434.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

![](http://oy09glbzm.bkt.clouddn.com/17-12-27/87962066.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)

![](http://oy09glbzm.bkt.clouddn.com/17-12-27/22721002.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
点击运行即可，运行结果
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/58813378.jpg?imageView2/0/q/100|watermark/2/text/d2FucWhibG9nLnRvcA==/font/5a6L5L2T/fontsize/500/fill/I0VGRUZFRg==/dissolve/100/gravity/SouthEast/dx/10/dy/10)
----------

参考:
- [FastDFS之基础知识](http://www.51niux.com/?id=137)
- [FASTDFS](https://www.jianshu.com/p/1c71ae024e5e)

