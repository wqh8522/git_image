---
title: Oracle按年、月、日、周等统计数据
date: 2018-03-21 20:28:29
categories: Oracle
tags: 数据统计

---

在很多项目中都会有数据统计的功能，如按照年、月、周、日统计某个用户提交的数量；或者直接统计指定年、月、周或者日新增的数量。最近我接触的一个项目，客户就要求根据月和周统计每个单位提价提交的数量。<!--more-->

## 按年统计

```sql
select 
  o.id,
  o.name,
  to_char(a.create_date,'yyyy'),
  count(*)
from news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id,o.name,to_char(a.create_date,'yyyy')
```

这里就是将创建时间格式化为年形式，按照单位id分组，查询出提交的数量。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/oracle/20181103102925.png)

## 按月统计

这里直接将上面的的日期格式改为`yyyy-MM`形式即可。

```sql
select 
  o.id,
  o.name,
  to_char(a.create_date,'yyyy-MM'),
  count(*)
from news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id,o.name,to_char(a.create_date,'yyyy-MM')
```

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/oracle/20181103102940.png)

## 按周统计

按周统计可以显示两种效果，一种是显示出该日期的周一的日期，也就是自然周的日期，另外一种是按周自然周统计。

### 按自然周统计

```sql
select 
  o.id,
  o.name,
  to_char(a.create_date,'ww'),
  count(*)
from t_dzjg_news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id,o.name,to_char(a.create_date,'ww')
```

这里的`ww`是获取该日期的在年中的第几周。

![](https://raw.githubusercontent.com/wqh8522/my_note/pic/oracle/20181103103006.png)

### 按自然周的日期统计

这里处理起来可以会比较繁琐，首先要获取日期的所在周的周一的日期，然后根据周一日期分组查询：

```sql
---获取指定日期的周一的日期
select trunc(sysdate,'ww') from dual;

select to_char(trunc(sysdate,'ww'),'yyyy-MM-dd') from dual;

---将创建时间格式为周一的形式，再去查找
select 
  o.id,
  to_char(trunc(a.create_date,'ww'),'yyyy-MM-dd') AS 周一,
  count(*)
from t_dzjg_news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id,to_char(trunc(a.create_date,'ww'),'yyyy-MM-dd')

```

这里使用到一个函数`trunc()`：该函数类似截取函数，按指定的格式截取输入的数据。这里将时间截取为显示周的形式。

## 按季度统计

在oracle中`q`为时间的季度如：

```sql
select to_char(sysdate,'q') from dual;--取得当前时间的季度
```

所以上面的sql可以改为：

```sql
select 
  o.id,
  to_char(a.create_date,'q') AS 季度,
  count(*)
from t_dzjg_news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id, to_char(a.create_date,'q')
```

## 按天统计

这个就几乎没啥好说的了：

```sql
select 
  o.id,
  to_char(a.create_date,'yyyy-MM-dd') AS 日期,
  count(*)
from t_dzjg_news a
left join sys_user u on u.id = a.create_by 
left join sys_office o on o.id = u.office_id
group by o.id, to_char(a.create_date,'yyyy-MM-dd')
```

## 总结

对于数据库数据的统计，这些还只是很简单基础。这几天通过写那些统计功能，虽然写的头大，但是对数据库知识越来越深。对于写复杂的sql语句，要细分问题，一步步的查询，最后将一步步分析的简单的查询组合成想要的复杂查询，可以说是从小到大，切忌从大到小。

## 最后

关于Oracle数据库中对时间常用的操作可以查看我之前的一篇博客：

[Oeacle常见的日期处理](http://blog.csdn.net/wqh8522/article/details/78913811)