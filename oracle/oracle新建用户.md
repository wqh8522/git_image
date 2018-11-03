---
title: Oracle新建用户
date: 2017-10-01 09:22:39
categories: 
tags: oracle
---
1.首先我们可以用scott用户以sysdba的身份登录oracle. 
```sql
conn scott/tiger as sysdba
```
2.然后我就可以来创建用户了. 
```sql
create user name identified by name;
```
3.修改用户的密码. <!--more-->
```sql
alter user name identified by password; 
```
4.创建一个表空间.
```sql
create tablespace tablespace_name datafile 'c:\tablespace_name.dbf' size 200M;
```
5.创建好表空间,还需要将表空间分配给用户. 
```sql
alter user name default tablespace tablespacename;
```
6.给用户分配了表空间,用户还不能登陆（没有登录权限）,因此还需要为用户分配权限 
```sql
grant create session,create table,create view,create sequence,unlimited tablespace to tablespace_name;
```
7.删除user
```sql
drop user ×× cascade
```
8.说明： 删除了user，只是删除了该user下的schema objects，是不会删除相应的tablespace的。
删除tablespace
```sql
DROP TABLESPACE tablespace_name INCLUDING CONTENTS AND DATAFILES;
```