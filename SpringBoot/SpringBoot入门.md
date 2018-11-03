---
title: SpringBoot入门
date: 2017-10-05 09:40:20
categories: Spring
tags: SpringBooot
---
## Spring中常用注解 ##
### 声明Bean的注解 ###
- **@Component：**声明类为组件，没有明确的角色；
- **@Service：**声明该类在业务逻辑层（service层）使用；
- **@Reponsitory：**在数据访问层（dao层）使用；
- **@Controller：**表现层（MVC->SpringMVC）使用；
四个注解效果几乎等效，主要是区别类的作用，是层次更清晰。
- **@Scope：**描述Spring容器如何新建Bean实例，
	- **Singleton：**单例，一个Spring容器中只有一个Bean实例，Spring默认为该配置
	- **Prototype：**每次调用新建一个Bean实例
	- **Request：**web项目中，给每一个http request新建一个Bean实例
	- **Session：**web项目中，给每一个http session新建一个Bean实例
	- **GlobalSession：**这个只在portal应用中有用，给每一个global http session新建一个bean实例 
### 注入Bean的注解 ###
- **@Autowired:**自动装配，Spring提供的注解；<!--more-->默认按类型装配，默认情况下必须要求依赖对象必须存在，如果要允许null 值，可以设置它的required属性为false，如：@Autowired(required=false) ，如果我们想使用名称装配可以结合**@Qualifier**注解进行使用
- **@Resource：**JSR-250提供的注解；默认安照名称进行装配，名称可以通过name属性进行指定，如果没有指定name属性，当注解写在字段上时，默认取字段名进行按照名称查找，如果注解写在setter方法上默认取属性名进行装配。 当找不到与名称匹配的bean时才按照类型进行装配。但是需要注意的是，如果name属性一旦指定，就只会按照名称进行装配。
- **@Inject：**JSR-330提供的注解。与**@Autowired**基本一样，按类型装配，可以通过**@Qualifier**显式指定装配类型 
这三个注解可在set方法或者属性上，个人觉得注解在属性上更好，优点是代码少，层次更清晰。
### Java配置注解 ###
- **@Configuration：**声明当前类是一个配置类，该类相当于Spring一个xml配置文件。这个类中可能有0个或多个@Bean注解。也可以不使用包扫描，因为所有的Bean都在此类中定义。
- **@Bean：**注解在方法上，声明当前方法返回的是值为一个Bean；如果
- **@ComponentScan：**自动扫描指定包名下所有使用**@Servcie**、**@Reponsitory**、**@Controller**和**@Component**的类，并注册为Bean。
使用java配置方式的Spring程序，可以使用**AnnotationConfigApplicationContext**作为Spring容器，接受输入一个配置类作为参数，然后使用**genBean()**获取配置的Bean。
### AOP注解 ###
AOP：面向切面编程，相对于OOP面向对象编程；Spring的AOP的存在是为了解耦,AOP可以让一组类共享相同行为。在OOP中只能通过继承类和实现接口来使代码的耦合度增强，且继承是能为单继承，阻碍更多行为添加到一组类上，AOP弥补了OOP的不足。
Spring支持AspectJ的注解式切面编程。

- **@Aspect:**声明是一个切面；
- **@Before：**定义切点，定义为前置通知
- **@After：**定义为后置通知
- **@Around：**定义为环绕通知，其中参数为切点
- **@PointCut：**定义统一的拦截规则，上面的通知可以引用

如果是使用java配置方式的Spring程序，需要在配置类上使用**@EnableAspectJAutoProxy**开启对AspectJ代理的支持。
### Spring EL和资源调用注解 ###
Spring EL表达式支持在xml和注解中使用，类似于jsp的EL表达式。
- **@PropertySource:**指定资源文件地址，
- **@Value：**其中的参数为读取资源信息，也可以使用该注解注入，这样就需要配置一个PropertySourcePalceholderConfigurer的Bean。
### Bean的初始化和销毁 ###
使用JSR250形式的Bean
- **@PostConstruct：**在构造函数执行完之后执行
- **@PerDestory：**在Bean销毁之前执行
在**@Bean**注解中可以指定属性：initMethod和destoryMethod指定类的init和destory方法在构造之后、bean销毁之前执行。
### 异步注解 ###
- **@EnableAsync：**注解开启异步任务支持，在配置类中使用
- **@Async：**表明该方法是个异步方法，如果该注解用在类上，则表明该类所有的方法都是异步方法。
### 计划任务 ###
- **@EnableScheduling：**在配置类来开启对计划任务的支持，
- **@Schedule：**声明这是一个计划任务，使用fixedRate属性每个固定时间执行，cron可按照指定时间执行，cron是UNIX和类UNIX系统下的定时任务。
### 测试注解 ###
- **@ContextConfiguration：**加载配置ApplicationContext，其中class属性用来加载配置类
- **@ActiveProfile：**用来声明活动的profile
### Spring MVC注解 ###
- **@Controller：**声明该类是一个控制器
- **@RequestMapping：**配置URL和方法之间的映射
- **@ResponseBody：**支持将返回值放在response体内，而不是返回一个页面；此注解可放在返回值钱或者方法前。返回一般为json数据
- **@RequestBody：**允许request的参数放在request体内，而不是直接在地址后面，此注解放在参数钱
- **@PathVariable：**用来接收路径参数
- **@RestController:**组合注解，组合了@Controller和@ResponseBody
- **@ControllerAdvice：**可以将对于控制器的全局配置放置在同一个位置
- **@ExceptionHandler:**用于全局处理控制里的异常，
- **@InitBinder：**用来设置WebDataBinder，**WebDataBinder**用来自动绑定前台参数到Model中
- **@ModelAttribute：**绑定键值对到Model里，**@ControllerAdvice**注解标注的类中是让全局的@RequestMapping都能获得到在此处设置的键值对。
### 其他注解 ###
- **@Profile：**为在不同环境下使用不同的配置提供支持
- **@ConfigurationProperties：**绑定指定的properties中的值，并且支持层级关系。
- **@Primary：** 标志这个 Bean 如果在多个同类 Bean 候选时，该 Bean 优先被考虑。
- **@Conditional：**根据满足某一特定条件创建一个特定的Bean
- **@EnableWebMvc：**开启Web MVC的配置支持
- **@EnableConfigurationProperties：**开启对**@ConfigurationProperties**注解配置Bean的支持
- **@EnableTransactionManagement:**开启对注解式事务的支持
- **@EnableCaching：**开启注解式的缓存支持
## SpringBoot简介 ##
> Spring Boot是由Pivotal团队提供的全新框架，其设计目的是用来简化新Spring应用的初始搭建以及开发过程。该框架使用了特定的方式来进行配置，从而使开发人员不再需要定义样板化的配置。通过这种方式，Spring Boot致力于在蓬勃发展的快速应用开发领域(rapid application development)成为领导者。

### 优点 ###
1. 快速构建项目；
2. 对主流开发框架的无配置集成；
3. 项目可独立运行，无须外部依赖Servlet容器；
4. 提供运行时的应用监控；
5. 极大地提高了开发、部署效率；
6. 与云计算的天然集成。

## 创建第一个SpringBoot应用 ##
可以使用Spring网站在线创建：[https://start.spring.io/](https://start.spring.io/)
![](https://i.imgur.com/09JwCTm.png)
下载完解压后就能得到一个标准的maven项目，这里也可以选择gradle方式
![](https://i.imgur.com/XURuy7w.png)
使用IDE创建，这里使用IDEA创建：
选择Spring Initializr，在右边选择jdk版本
![](https://i.imgur.com/fsAi30r.png)
修改填写maven的一些属性，这里的Java Version要与前面的版本对应
![](https://i.imgur.com/8mnotun.png)
选择SpringBoot的版本以及需要添加的依赖
![](https://i.imgur.com/nfymcBB.png)
最后就是项目名称
![](https://i.imgur.com/YCAnc3f.png)
创建完成的项目结构：
![](https://i.imgur.com/4F2kGUw.png)
添加一个Controller并启动项目测试：
```java
@RestController
public class Hello {

    @RequestMapping("/hello")
    public String hello(){
        return  "Hello SpringBoot";
    }
}
```
```java
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
```
访问：http://localhost:8080/hello
![](https://i.imgur.com/QtGfbag.png)
## 表单验证 ##
创建一个学生类，添加学生信息验证学生年龄最小为20岁，学生姓名不能为空：
```java
public class Students {

    private String id;

    @NotNull(message = "姓名必传")
    private String name;

    //@Min 该注解限制字段的最小值
    @Min(value = 20 ,message = "未满20岁的学生禁止注册！！")
    private Integer age;

    public Students() {
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Students{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```
```java
 /** 日志打印 */
    private final static Logger logger = LoggerFactory.getLogger(Hello.class);

    /**
     * @Valid 表示该对象需要验证
     * @param student 
     * @param bindingResult 验证返回的信息
     * @return
     */
    @PostMapping("/saveStudent")
    public Object saveStudent(@Valid Students student,BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            //如果失败，打印并返回验证结果
            String error = bindingResult.getFieldError().getDefaultMessage();//获取错误信息
            logger.info(String.format("errorMsg={}"),error+"age:"+student.getAge());
            return error;
        }
        return student;
    }
```
![](https://i.imgur.com/2xKpn9f.png)
![](https://i.imgur.com/d5jrdmg.png)
## 统一异常处理 ##
@ControllerAdvice:使用该注解标注的类，表明该类是Controller的全局配置类；
@ExceptionHandler：全局处理控制器的异常
自定义异常类
```java
**
 * Created By wqh
 * 2017/9/24   15:13
 * Description: 自定义异常，只有继承RuntimeException才能被Spring的hadler捕获
 */
public class StudentExceprion extends RuntimeException{
    private Integer code;

    public StudentExceprion(ResultEnum resultEnum) {
        super(resultEnum.getMessage());
        this.code = resultEnum.getCode();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
```
```java
/**
 * Created By wqh
 * 2017/9/24 14:50
 * Description:返回结果的封装的工具类
 */
public class ResultUtil {
    public static ResponseResult success(Object object){
        ResponseResult responseResult = new ResponseResult();
        responseResult.setCode(1);
        responseResult.setMessage("成功");
        responseResult.setData(object);
        return responseResult;
    }

    public static  ResponseResult success(){
        return success(null);
    }

    public static ResponseResult error(Integer code,String content){
        ResponseResult<Students> responseResult = new ResponseResult<>();
        responseResult.setCode(code);
        responseResult.setMessage(content);
        return responseResult;
    }
}
```
控制层全局配置类
```java
@ControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseResult handle(Exception e){
        if(e instanceof StudentExceprion){
            StudentExceprion studentExceprion = (StudentExceprion) e;
            return ResultUtil.error(studentExceprion.getCode(),studentExceprion.getMessage());
        }else {

            return ResultUtil.error(ResultEnum.UNKNOW_ERROR.getCode(),ResultEnum.UNKNOW_ERROR.getMessage());
        }
    }
}
```
```java
/**
 * Created By wqh
 * 2017/9/24   15:26
 * Description: 返回结果的枚举类
 */
public enum  ResultEnum {
    UNKNOW_ERROR (-1,"未知错误"),
    SUCCESS(1,"成功"),
    PRIMARY_ERROR(100,"你可能在上小学"),
    MIDDLE_ERROR(101,"你可能在上初中"),
    WORK_ERROR(102,"你可能在打工");

    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
```
```java
/**
 * Created By wqh
 * 2017/9/24 14:43
 * Description: 相应结果的封装
 */
public class ResponseResult<T> {

   /** 响应码 */
    private Integer code;

    /** 响应信息 */
    private String message;

    /** 数据 */
    private T data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
```
```java
public void getAge(String id) throws Exception {
    Students stu = studentDao.findOne(id);
    if(stu.getAge()<10){
        //抛出自定义异常
        throw new StudentExceprion(ResultEnum.PRIMARY_ERROR);
    }else if (stu.getAge()>10 && stu.getAge()<15){
        throw new StudentExceprion(ResultEnum.MIDDLE_ERROR);
    }else{
        throw new StudentExceprion(ResultEnum.WORK_ERROR);
    }
}
```
在控制层直接调用Service层既可，但是异常需要往外抛，交给统一的处理
## 单元测试 ##
使用Junit进行单元测试时很重要的，这里分别介绍测试service和dao层
添加测试依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```
### 测试Service层 ###
Service层的测试比较简单，只要将Serice的Bean注入到测试类中。但是在SpringBoot中的测试类，需要在类上添加两个注解**@RunWith(SpringRunner.class)、@SpringBootTest**
```java
 	@Autowired
    private StudentService studentService;

    @Test
    public void saveStus() throws Exception {

        Students student = new Students();
        student.setAge(155);
        student.setName("wqeqwasd");
        student.setMoney(13d);
        studentService.saveStus(student);

    }
```
### 测试Controller层 ###
在开发Rest风格API时，对于Controller层的测试也是相当重要的；
```java
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StudentActionTest {

    @Autowired
    private MockMvc mockMvc;
    @Test
    public void save() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/studentAction/save")
                                                .param("name","wqh")
                                                .param("age","50"))//添加参数
                .andExpect(MockMvcResultMatchers.status().isOk())//返回状态码为200
                .andDo(print())//打印出请求和相应的内容
                .andReturn().getResponse().getContentAsString();//将返回信息转化为String类型
//                .andExpect(MockMvcResultMatchers.content().string("ww"));//返回结果必须是ww
    }

}
```
## SpringBoot实现热部署 ##
SpringBoot热部署是使用了spring-boot-devtools工具：
添加依赖
```xml
<!--热部署功能-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```
Pom文件build中添加
```xml
<plugins>
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <fork>true</fork>
        </configuration>
    </plugin>
</plugins>
```
IDEA配置
当我们修改了Java类后，IDEA默认是不自动编译的，而spring-boot-devtools又是监测classpath下的文件发生变化才会重启应用，所以需要设置IDEA的自动编译：
![](https://i.imgur.com/S8SPCWm.png)
## 学习资料汇总 ##
### 推荐博客 ###
- [程序猿DD](http://blog.didispace.com/categories/Spring-Boot/)
- [liaokailin的专栏](http://blog.csdn.net/liaokailin/article/category/5765237)
- [Spring Boot 揭秘与实战 系列](http://blog.720ui.com/columns/springboot_all/)
- [catoop的专栏](http://blog.csdn.net/column/details/spring-boot.html)
- [简书Spring Boot专题](http://www.jianshu.com/c/f0cf6eae1754)
- [方志朋Spring Boot 专栏](http://blog.csdn.net/column/details/15397.html)
- [Spring-boot集成](http://lihao312.iteye.com/)
- [纯洁的微笑](http://www.ityouknow.com/spring-boot)
### 推荐网站 ###
- [Spring boot 官网](http://projects.spring.io/spring-boot/)
- [Spring Boot参考指南-中文版](https://qbgbook.gitbooks.io/spring-boot-reference-guide-zh/content/)
- [Gradle 中文参考指南](https://dongchuan.gitbooks.io/gradle-user-guide-/content/tutorials/)
- [慕课网视频](http://www.imooc.com/learn/767)
- [spring-boot-tutorials](http://www.mkyong.com/tutorials/spring-boot-tutorials/)
### 开源代码 ###
- [SpringBoot官方例子](https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples "SpringBoot官方例子")
- [spring-boot-examples](https://github.com/ityouknow/spring-boot-examples)
- [SpringBoot-Learning](https://github.com/dyc87112/SpringBoot-Learning)
- [favorites-web](https://github.com/cloudfavorites/favorites-web)
- [springboot-learning-example](https://github.com/JeffLi1993/springboot-learning-example)
- [spring-boot-all](https://github.com/leelance/spring-boot-all)
