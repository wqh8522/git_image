---
title: Oeacle常见的日期处理
date: 2017-09-27 19:51:00
categories: 
tags: oracle
---
## 日期处理常用函数 ##
1. sysdate：当前日期和时间；
2. to_char()：将数值型或日期型转换为字符型；
3. to_date()：日期转换函数，例：to_date('2017-09-27 10:00:00', 'yyyy-mm-dd hh24:mi:ss')；
4. last_day(sysdate)：返回日期所在月的最后一天；
5. trunc(number[,decimals])：指定元素格式截去一部分日期值；
6. add_months(D,N)：返回日期D加N月后对应的日期时间，N为正时则表示D之后；N为负时则表示为D之前；N为小数则会自动先删除小数部分，而用整数部分;<!--more-->
7.  decode(条件,值1,翻译值1,值2,翻译值2,...值n,翻译值n,缺省值)：根据条件返回相应的值；
8. ceil(n)：取大于等于数值n的最小整数；
9. floor(n)：取小于等于数值n的最大整数；
10.	dbtimezone()：返回时区；
11. ......
## 常见操作 ##
```sql
--得到当前的日期
select sysdate from dual;

--取得当前日期是本月的第几周
select to_char(sysdate,'YYYYMMDD HH24:MI:SS W') from dual;

select to_char(sysdate,'w') from dual;

--取得当前日期是一个星期中的第几天,注意星期日是第一天 返回的是number 1开始星期天
select to_char(sysdate,'d') from dual;
select to_char(sysdate,'yyyy') from dual;--取得年
select to_char(sysdate,'q') from dual;--取得季度
select to_char(sysdate,'mm') from dual;--取得月份
select to_char(sysdate,'dd') from dual;--取得日期
select to_char(sysdate,'ddd') from dual;-- 一年中的第几天
select to_char(sysdate,'ww') from dual; -- 年中的第几个星期
select to_char(sysdate,'w') from dual;  -- 该月的第几个信息
select to_char(sysdate,'day') from dual; --星期几
select to_char(sysdate,'hh') from dual; --小时 12小时制
select to_char(sysdate,'hh24') from dual; --小时 24小时制
select to_char(sysdate,'MI') from dual; --分
select to_char(sysdate,'ss') from dual; --秒
  
--得到当天凌晨0点0分0秒的日期
select trunc(sysdate) from dual;
  
--得到这天的最后一秒
select trunc(sysdate)+0.99999 from dual;

--得到小时的具体数值
select trunc(sysdate)+1/24 from dual;
select trunc(sysdate)+7/24 from dual;

--得到明天凌晨0点0分0秒的日期
select trunc(sysdate+1) from dual;
select trunc(sysdate)+1 from dual;

--本月一日的日期
select trunc(sysdate,'mm') from dual;

--下月一日的日期
select trunc(add_months(sysdate, 1),'mm') from dual;

--返回当前月的最后一天
select last_day(sysdate) from dual;
select last_day(trunc(sysdate)) from dual;
select trunc(last_day(sysdate)) from dual;
select trunc(add_months(sysdate,1),'mm') - 1 from dual;

--得到一年的每一天
select trunc(sysdate,'yyyy')+ rn -1 date0 from
(select rownum rn from all_objects
where rownum<366);

--给现有的日期加上2年
select add_months(sysdate,24) from dual;

--判断某一日子所在年分是否为润年
select decode(to_char(last_day(trunc(sysdate,'y')+31),'dd'),'29','闰年','平年') from dual;

--判断两年后是否为润年
select decode(to_char(last_day(trunc(add_months(sysdate,24),'y')+31),'dd'),'29','闰年','平年') from dual;

--查询当前日期的季度
select ceil(to_number(to_char(sysdate,'mm'))/3) from dual;

```
## 案例 ##

根据时间以及用户id计算该用户本周、本日、本月记录的数据
```sql
-- 本日 
select y.create_by,count(y.create_date),u.name from test y,sys_user u
 where to_date(y.create_date)=to_date(sysdate) and y.create_by=u.id
group by y.create_by,u.name;

--本周
select create_by,count(create_date)
from test
where to_date(create_date)>=to_date(sysdate-to_number(to_char(sysdate-1,'D')+1)) and slzt='1'
group by create_by;

--本月
select to_char(create_date,'yyyy-mm'),create_by,count(create_date) from test
where to_date(create_date)>=to_date(trunc(sysdate,'mm')) and to_date(create_date)<=to_date(last_day(sysdate))
group by to_char(create_date,'yyyy-mm'),create_by;

```

