---
title: Oracle ROW_NUMBER() OVER()函数的实际场景使用
date: 2018-03-14 11:51:34
categories: Oracle
---

##  前言

最近开发的系统中有个在线咨询功能。学生在前台提交咨询信息，教师可以登录后台回复咨询。该功能设计是直接使用一张表，使用是否开始标识该条记录是否是咨询的开始，然后使用一个会话id标识是属于一次咨询，根据创建时间排序，最后就像聊天一样。

后来遇到一个需求，就是需要查询出指定教师回复的咨询信息的第一条问和第一条答。先查询出所有开始的问，然后使用会话id内连接加子查询。<!--more-->然而遇到的问题是教师可能有多条回答，如果直接使用mybaits返回一个list，然后再去取第一条不是不可以。但是还是想直接使用sql完成，经过一番查找找到：

```sql
row_number() over(partition by  col1 ORDER BY col2 ASC ) 
```

(ps：以上问题如果有更好解决办法的欢迎评论)

##  数据库

我将项目的数据库表进行改造并插入数据

![](http://oy09glbzm.bkt.clouddn.com/18-3-14/14933320.jpg-blog)

```sql
-- ----------------------------
-- Table structure for zxzx
-- ----------------------------
CREATE TABLE "ZXZX" (
"ID" VARCHAR2(64 BYTE) NOT NULL ,
"CONSULT_ID" VARCHAR2(255 BYTE) NULL ,
"CONTENT" VARCHAR2(255 BYTE) NULL ,
"CREATE_DATE" DATE NULL ,
"IS_START" VARCHAR2(255 BYTE) NULL ,
"USER_ID" VARCHAR2(64 BYTE) NULL 
)
LOGGING
NOCOMPRESS
NOCACHE
;
-- ----------------------------
-- Records of zxzx
-- ----------------------------
INSERT INTO "ZXZX" VALUES ('12sassaasd', '12123123', '第一条问', TO_DATE('2018-03-14 21:09:26', 'YYYY-MM-DD HH24:MI:SS'), '1', '111');
INSERT INTO "ZXZX" VALUES ('213123qwewq', '12123123', '第一次回答第一条问', TO_DATE('2018-03-14 22:10:16', 'YYYY-MM-DD HH24:MI:SS'), '0', '001');
INSERT INTO "ZXZX" VALUES ('sasdass2342', '12123123', '第二次回答第一条问', TO_DATE('2018-03-14 22:11:07', 'YYYY-MM-DD HH24:MI:SS'), '0', '001');
INSERT INTO "ZXZX" VALUES ('23234wewer', '12123123', '第一条问追问', TO_DATE('2018-03-14 22:22:00', 'YYYY-MM-DD HH24:MI:SS'), '0', '111');
INSERT INTO "ZXZX" VALUES ('345', '12123123', '回答一条追问', TO_DATE('2018-03-14 23:12:55', 'YYYY-MM-DD HH24:MI:SS'), '0', '001');
INSERT INTO "ZXZX" VALUES ('234324', '334455', '第二条问', TO_DATE('2018-03-14 23:08:21', 'YYYY-MM-DD HH24:MI:SS'), '1', '111');
INSERT INTO "ZXZX" VALUES ('5623365', '334455', '回答第二条问', TO_DATE('2018-03-14 23:14:07', 'YYYY-MM-DD HH24:MI:SS'), '0', '001');
INSERT INTO "ZXZX" VALUES ('12314', '112233', '问问问', TO_DATE('2018-03-14 23:14:49', 'YYYY-MM-DD HH24:MI:SS'), '1', '111');
INSERT INTO "ZXZX" VALUES ('12342', '112233', '答答答', TO_DATE('2018-03-14 23:15:15', 'YYYY-MM-DD HH24:MI:SS'), '0', '002');

-- ----------------------------
-- Indexes structure for table zxzx
-- ----------------------------

-- ----------------------------
-- Checks structure for table zxzx
-- ----------------------------
ALTER TABLE "ZXZX" ADD CHECK ("ID" IS NOT NULL);

-- ----------------------------
-- Primary Key structure for table zxzx
-- ----------------------------
ALTER TABLE "ZXZX" ADD PRIMARY KEY ("ID");

```

![](http://oy09glbzm.bkt.clouddn.com/18-3-14/33929690.jpg-blog)

## 数据查询

这里需要将user_id=001所回答的问题查询出，也就是上图红框中的信息。

- 首先查询001的回答，这里就可能每个会话有多条，使用前面提到的row_number() over()便可以解决重复问题：

```
SELECT
       row_number()  over(partition by  consult_id  ORDER BY create_date ASC ) as rn,
       id,      
       consult_id ,      
       content,      
       user_id ,      
       is_start,      
       create_date      
FROM zxzx  
WHERE user_id = '001' AND is_start !='1'
```

![](http://oy09glbzm.bkt.clouddn.com/18-3-15/57520084.jpg-blog)

- 接着上面的查询，可以看出排序分组之后的数据都有一个rn编号，接着将rn为一取出即可：

![](http://oy09glbzm.bkt.clouddn.com/18-3-15/94742878.jpg-blog)

- 然后需要查询出所有的问的记录。将上面作为子查询，使用内连接并用consult_id字段连接。


```sql
select
   w.id,      
   w.consult_id ,      
   w.content,      
   w.user_id ,      
   w.is_start,      
   w.create_date, 
   h.id,      
   h.consult_id ,      
   h.content,      
   h.user_id ,      
   h.is_start,      
   h.create_date
from zxzx w inner join (
  select * from(
    SELECT
           row_number()  over(partition by  consult_id  ORDER BY create_date ASC ) as rn,
           id,      
           consult_id ,      
           content,      
           user_id ,      
           is_start,      
           create_date      
    FROM zxzx  
    WHERE user_id = '001' AND is_start !='1'
  )where rn = 1
) h on h.consult_id = w.consult_id 
where w.is_start = '1'
```

- 最后查询结果

![](http://oy09glbzm.bkt.clouddn.com/18-3-16/35936413.jpg-blog)

##  知识点

### 数据库结果去重

上面说到使用`row_number() over()`函数获取根据某个字段分组后的一条数据，这就有点像“去重”。在Oracle中还可以使用`distinct`来返回唯一值。

1. distinct关键字，语法如下：

```sql
SELECT DISTINCT col FROM table_name
```

​	在使用关键字 distinct 的时候，主要是要知道其作用于单个字段和多个字段的时候是有区别的：

- 作用于单个字段时，其“去重”的是表中所有该字段值重复的数据；
- 作用于多个字段的时候，其“去重”的表中所有字段（即 distinct 具体作用的多个字段）值都相同的数据。

2.  row_number() over() 函数，语法如下：

```sql
row_number() over(partition by  col1 ORDER BY col2 ASC ) 
```

​	row_number()函数用于给记录进行标号，over()函数的作用是将表中的记录进行分组和排序。语法类似上面：将表中的数据按照col1进行分组，按照col2排序。partition by：表示分组。该函数主要是根据分组进行排序之后然后取出第一条数据以实现“去重”

###  连接查询

1. 内连接`inner join  on`：只显示两张表都匹配的结果；
2. 左外连接`left join on`：以左边的表为基准，显示出左表的全部字段和右表所匹配的字段，空的用`null`表示；
3. 右外连接`right join on`：与左外连接相反，右外连接会查询出右表的所有字段，显示左表与右表匹配的字段，空用`null`表示；
4. 全连接`full join on`：显示两张表全部内容。