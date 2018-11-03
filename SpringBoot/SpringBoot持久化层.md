---
title: SpringBoot持久化层操作
date: 2017-10-05 22:26:18
categories: Spring
tags: [SpringBoot,Data-Jpa,mybatis,hibernate]
---
## 使用Spring Data Jpa操作数据库 ##
要使用Spring Data Jpa必须先添加相应的依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
数据库配置信息，使用yml的配置文件，个人感觉这种配置结构更清晰<!--more-->
```prperties
spring:
  #数据库的配置
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/springbootjpa
    username: root
    password: 1234
  #配置jpa
  jpa:
    hibernate:
      ddl-auto: update #设置数据库的行为
    show-sql: true
```
添加实体类：
```java
@Entity//数据库表的映射
public class User {

    @Id
    @GeneratedValue//设置为自增主键
    private Integer id;

    private String name;

    private String age;
	
	public User(){
    }	

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }
}
```
添加完实体类之后就可以启动项目，启动项目会自动创建User表。
### CRUD操作 ###
在Spring-Data-Jpa 中提供了一种类似于声明式编程的方式，开发者只需要编写数据访问接口（称为Repository），Spring Data JPA就能基于接口中的方法命名自动地生成实现。
定义 UserRepository 接口，继承JpaRepository，此接口是 Spring-Data-Jpa 内部定义好的泛型接口，第一个参数实体类，第二个参数是ID。已经帮我们实现了基本的增删改查的功能，现在只要持有 UserRepository 就能操作数据表
```java
@Repository
public interface UserRepository  extends JpaRepository {
}

```
### 分析 ###
进入源码查看接口的继承结构发现 JpaRepository继承自PagingAndSortingRepository继承自CrudRepository继承自Repository。
#### Repository ####
 泛型接口，第一个参数是实体类，第二个参数是实体类ID，最顶层接口，不包含任何方法，目的是为了统一所有的 Repository 的类型，且能让组件扫描的时候自动识别
```java
public interface Repository<T, ID extends Serializable> {

}
```
#### CrudRepository ####
Repository的子接口，封装数据表的 CRUD 方法。
```java
public interface CrudRepository<T, ID extends Serializable> extends Repository<T, ID> {
  <S extends T> S save(S var1);//存储一条数据实体

  <S extends T> Iterable<S> save(Iterable<S> var1);//批量存储数据

  T findOne(ID var1);//根据id查询一条数据实体

  boolean exists(ID var1);//判断指定id是否存在

  Iterable<T> findAll();//查询所有的数据

  Iterable<T> findAll(Iterable<ID> var1);//根据一组id批量查询实体

  long count();//返回数据的条数

  void delete(ID var1);//根据id删除数据

  void delete(T var1);//删除数据

  void delete(Iterable<? extends T> var1);//批量删除数据

  void deleteAll();//删除所有数据
}
```
#### PagingAndSortingRepository ####
CrudRepository的子接口，扩展了分页和排序功能。
```java
public interface PagingAndSortingRepository<T, ID extends Serializable> extends CrudRepository<T, ID> {
  Iterable<T> findAll(Sort var1);//根据某个排序获取所有数据
  Page<T> findAll(Pageable var1);//根据分页信息获取某一页的数据
}
```
#### JpaRepository ####
PagingAndSortingRepository的子接口，增加一些实用的功能, 如批量操作
```java
public interface JpaRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
  List<T> findAll(); //获取所有数据，以List的方式返回

  List<T> findAll(Sort var1); //根据某个排序获取所有数据，以List的方式返回

  List<T> findAll(Iterable<ID> var1); //根据一组id返回对应的对象，以List的方式返回

  <S extends T> List<S> save(Iterable<S> var1); //将一组对象持久化到数据库中，以List的方式返回

  void flush(); //将修改更新到数据库

  <S extends T> S saveAndFlush(S var1); //保存数据并将修改更新到数据库

  void deleteInBatch(Iterable<T> var1); //批量删除数据

  void deleteAllInBatch(); //批量删除所有数据

  T getOne(ID var1); //根据id查找并返回一个对象
}
```
### 自定义查询 ###
Spring Data 会识别出find...By, read...By和get...By这样的前缀，从后面的命名中解析出查询的条件。方法命名的的第一个By表示查询条件的开始，多个条件可以通过And和Or来连接。

## 事务处理 ##
在SpringData中事务的处理只需要在Service所需要加事务的方法上添加注解**@Transactional**，前提是使用基于组件扫描。如果使用基于注注解的配置包扫描，则需要在配置类上使用**@EnableTransactionManagement**来开启对事务的支持。
## SpringBoot整合Mybatis ##
首先需要添加mybatis-Spring的依赖：
```xml
<dependency>
	<groupId>org.mybatis.spring.boot</groupId>
	<artifactId>mybatis-spring-boot-starter</artifactId>
	<version>1.3.0</version>
</dependency>
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>5.1.43</version>
</dependency>
```
配置文件：
```prpertice
#配置数据源
spring:
  datasource:
      driver-class-name: com.mysql.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/springbootjpa?useUnicode=true&characterEncoding=utf-8
      username: root
      password: 1234
#mybatis的配置
mybatis:
  type-aliases-package: com.wqh.springbootmybatis.domain
```
Maper
```java
@Repository
public interface UserMapper{
    @Select("select * from user")
    public List<UserEntity> findAll();

    @Insert("insert into user(name,age) values(#{name},#{age})")
    public void saveUser(UserEntity user);
}
```
这里使用的注解形式，如果需要使用xml配置方式，可以在配置文件中加入相应配置
```
  config-location:   //配置文件的地址
  mapper-locations:  //Mapper配置文件的地址
```
配置类：这里如果不使用第三方数据源（如：druid）可以不用该类；
```java
@Configuration
//指明了扫描dao层，并且给dao层注入指定的SqlSessionTemplate
@MapperScan(basePackages = "com.wqh.dao", sqlSessionTemplateRef = "sqlSessionTemplate")
public class DataSourceConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String uasername;
    @Value("${spring.datasource.password}")
    private String password;

    @Bean(name = "dataSource")
    @Primary//多数据源时，指定该Bean为主
    public DataSource dataSource() {
    	//使用Druid数据源
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName(driverClass);
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(uasername);
        druidDataSource.setPassword(password);
        return druidDataSource;
    }

    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "dataSourceTransactionManager")
    @Primary
    public DataSourceTransactionManager dataSourceTransactionManager(@Qualifier("dataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}

```
测试
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;
    @Test
    public void saveUser() throws Exception {
        UserEntity u = new UserEntity();
        u.setName("wqh");
        u.setAge("23");
        userService.saveUser(u);

    }
}
```
参考：

- [http://www.jianshu.com/p/ff4839931c54](http://www.jianshu.com/p/ff4839931c54)
- [springboot(六)：如何优雅的使用mybatis](http://www.ityouknow.com/springboot)