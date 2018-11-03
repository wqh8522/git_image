---
title: SpringBoot使用AOP+注解实现简单的权限验证
date: 2018-01-02 10:39:04
categories: SpringBoot
tags: [AOP,注解]
---
SpringAOP的介绍：[传送门](http://www.wanqhblog.top/2017/08/28/SpringAOP/)
## demo介绍 ##
主要通过自定义注解，使用SpringAOP的环绕通知拦截请求，判断该方法是否有自定义注解，然后判断该用户是否有该权限。这里做的比较简单，只有两个权限：一个普通用户、一个管理员。
## 项目搭建 ##
这里是基于SpringBoot的，对于SpringBoot项目的搭建就不说了。在项目中添加AOP的依赖：<!--more--->
```java
<!--AOP包-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```
## 自定义注解及解析 ##

在方法上添加该注解，说明该方法需要管理员权限才能访问。
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

      String authorities() default "ADMIN";

}
```
解析类：通过AOP的环绕通知获取方法上的注解，判断是否有Permission注解，返回注解的值。
```java
public class AnnotationParse {
    /***
     * 解析权限注解
     * @return 返回注解的authorities值
     * @throws Exception
     */
    public static String privilegeParse(Method method) throws Exception {
        //获取该方法
        if(method.isAnnotationPresent(Permission.class)){
            Permission annotation = method.getAnnotation(Permission.class);
            return annotation.authorities();
        }
        return null;
    }
}

```
## SpringAOP环绕通知 ##
```
@Aspect
@Component
public class ControllerAspect {

    private final static Logger logger = LoggerFactory.getLogger(ControllerAspect.class);

    @Autowired
    private UserService userService;
    /**
     * 定义切点
     */
    @Pointcut("execution(public * com.wqh.blog.controller.*.*(..))")
    public void privilege(){}

    /**
     * 权限环绕通知
     * @param joinPoint
     * @throws Throwable
     */
    @ResponseBody
    @Around("privilege()")
    public Object isAccessMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取访问目标方法
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        //得到方法的访问权限
        final String methodAccess = AnnotationParse.privilegeParse(targetMethod);

        //如果该方法上没有权限注解，直接调用目标方法
        if(StringUtils.isBlank(methodAccess)){
            return joinPoint.proceed();
        }else {
            //获取当前用户的权限,这里是自定义的发那个发
            User currentUser = userService.getCurrentUser();
            logger.info("访问用户，{}",currentUser.toString());
            if(currentUser == null){
                throw new LoginException(ResultEnum.LOGIN_ERROR);
            }
            if(methodAccess.equals(currentUser.getRole().toString())){
               return joinPoint.proceed();
            }else {
                throw new BusinessException(ResultEnum.ROLE_ERROR);
            }
        }
    }
}
```
## 使用 ##
只需要在需要验证的方法上添加自定义注解:` @Permission`既可
