---
title: SpringBoot使用Redis做缓存
date: 2017-12-26 23:26:40
categories: SpringBoot
tags: [SpringBoot,Redis]
---
> 随着时间的积累，应用的使用用户不断增加，数据规模也越来越大，往往数据库查询操作会成为影响用户使用体验的瓶颈，此时使用缓存往往是解决这一问题非常好的手段之一。Spring 3开始提供了强大的基于注解的缓存支持，可以通过注解配置方式低侵入的给原有Spring应用增加缓存功能，提高数据访问性能。
> 在Spring Boot中对于缓存的支持，提供了一系列的自动化配置，使我们可以非常方便的使用缓存。
<!--more-->

## 缓存工具简单比较 ## 
实现缓存有多种方式：EhCache、MemCached、Redis等
1. EhCache：纯Java进程内缓存框架，配置简单、结构清晰、功能强大。开始接触javaweb的时候，不管是使用hibernate还是mybatis，这个应该是最常见的缓存的工具；经常会用作二级缓存。百度百科上的特点：

![](http://oy09glbzm.bkt.clouddn.com/17-12-26/79766332.jpg)
2. MemCached：一个自由开源的，高性能的分布式对象缓存；使用key-value存储系统。特点：	
	- 协议简单
	- 基于libevent的事件处理
	- 内置内存存储方式
	- memcached不互相通信的分布式
3. Redis：NoSql数据库，不仅仅支持k/v存储，同时还提供list、set、hash的数据结构的存储。具体学习材料可以进官网，传送门：[http://www.redis.cn/](http://www.redis.cn/)
## Redis缓存介绍 ##
本文的项目主要是SpringBoot+mybatis+redis+mysql。使用redis做缓存可以有几种实现方式：
1. 直接将redis作为mybatis的二级缓存；
2. 通过注解方式给项目添加缓存；
3. 手动调用jedis。这种方式不推荐。。。
## 环境的搭建 ##
在SpringBoot中已经有对于缓存的支持，只需要做简单的配置添加即可。
pom.xml文件
```xml
  <!--redis-->
	<dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-data-redis</artifactId>
	</dependency>
	<!--缓存-->
	<dependency>
	    <groupId>org.springframework.boot</groupId>
	    <artifactId>spring-boot-starter-cache</artifactId>
	</dependency>
```
添加redis的配置,这列使用的是yml的配置
```java
# redis配置
  redis:
    #数据库索引，默认为0
    database: 0
    #服务器地址
    host: localhost
    #端口
    port: 6379
    #密码（默认为空）
    password:
    pool:
      #连接池最大连接数（使用负值表示没有限制）
      max-active: 8
      #连接池最大阻塞等待时间（使用负值表示没有限制）
      max-wait: -1
      # 连接池中的最大空闲连接
      max-idle: 8
      # 连接池中的最小空闲连接
      min-idle: 0
    # 连接超时时间（毫秒）
    timeout: 5000
```
## 使用 ##
### 使用注解配置缓存 ###
这种方式比较简单，在相应的service中添加Cache注解就可以，项目代码，mybatis的配置文件以及mapper文件省略，
test实体类：这里使用了lombok的`@Data`注解
```java
@Data
public class Test implements Serializable{

    private String id;

    private String name;

    private String remake;
}
```
service类：这里的注解使用就是在service类中使用
```java
@Service
@CacheConfig(cacheNames = "test")
public class TestService {

    @Autowired
    private TestMapper testMapper;

    @Cacheable(key = "#p0")
    public Test get(String id) {
        return testMapper.get(id);
    }

    @CachePut(key = "#p0.id")
    public Test insert(Test test) {
        testMapper.insert(test);
        return test;
    }

    @CachePut(key = "#p0.id")
    public Test update(Test test) {
        testMapper.update(test);
        return test;
    }

}
```
测试类：
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TestServiceTest.class);
    @Autowired
    private TestService testService;

    @Test
    public void get() {
        com.wqh.blog.domain.Test test = testService.get("d8e875c8-9425-485b-9665-f5dda1e788bf");
        logger.info("======"+test+"=============");
    }

    @Test
    public void insert() {
        com.wqh.blog.domain.Test test = new com.wqh.blog.domain.Test();
        test.setId(UUID.randomUUID().toString());
        test.setName("redis");
        test.setRemake("hhhhhhhhhhhhhhhhhhhhhhh");
        testService.insert(test);
    }

    @Test
    public void update() {
    }
}
```
首先运行，插入方法，然后使用RedisClient查看，可以发现已经有数据。然后调用get方法，控制台并没有打印sql语句，配置成功
![](http://oy09glbzm.bkt.clouddn.com/17-12-26/36759880.jpg)
#### Cache注解 ####
这里直接引用程序员DD大神博客的内容：
- <font color="red">@CacheConfig：</font>主要用于配置该类中会用到的一些共用的缓存配置。在这里@CacheConfig(cacheNames = "test")：配置了该数据访问对象中返回的内容将存储于名为test的缓存对象中，我们也可以不使用该注解，直接通过@Cacheable自己配置缓存集的名字来定义。
- <font color="red">@Cacheable：</font>配置了get函数的返回值将被加入缓存。同时在查询时，会先从缓存中获取，若不存在才再发起对数据库的访问。该注解主要有下面几个参数：		
	-  <font color="red">value、cacheNames：</font>两个等同的参数（cacheNames为Spring 4新增，作为value的别名），用于指定缓存存储的集合名。由于Spring 4中新增了@CacheConfig，因此在Spring 3中原本必须有的value属性，也成为非必需项了
	-  <font color="red">key：</font>缓存对象存储在Map集合中的key值，非必需，缺省按照函数的所有参数组合作为key值，若自己配置需使用SpEL表达式，比如：@Cacheable(key = "#p0")：使用函数第一个参数作为缓存的key值，更多关于SpEL表达式的详细内容可参考官方文档
	-  <font color="red">condition：</font>缓存对象的条件，非必需，也需使用SpEL表达式，只有满足表达式条件的内容才会被缓存，比如：@Cacheable(key = "#p0", condition = "#p0.length() < 3")，表示只有当第一个参数的长度小于3的时候才会被缓存，若做此配置上面的AAA用户就不会被缓存，读者可自行实验尝试。
	-  <font color="red">unless：</font>另外一个缓存条件参数，非必需，需使用SpEL表达式。它不同于condition参数的地方在于它的判断时机，该条件是在函数被调用之后才做判断的，所以它可以通过对result进行判断。
	-  <font color="red">keyGenerator：</font>用于指定key生成器，非必需。若需要指定一个自定义的key生成器，我们需要去实现org.springframework.cache.interceptor.KeyGenerator接口，并使用该参数来指定。需要注意的是：该参数与key是互斥的
	-  <font color="red">cacheManager：</font>用于指定使用哪个缓存管理器，非必需。只有当有多个时才需要使用
	-  <font color="red">cacheResolver：</font>用于指定使用那个缓存解析器，非必需。需通过org.springframework.cache.interceptor.CacheResolver接口来实现自己的缓存解析器，并用该参数指定。
- <font color="red">@CachePut：</font>配置于函数上，能够根据参数定义条件来进行缓存，它与@Cacheable不同的是，它每次都会真是调用函数，所以主要用于数据新增和修改操作上。它的参数与@Cacheable类似，具体功能可参考上面对@Cacheable参数的解析
- <font color="red">@CacheEvict：</font>配置于函数上，通常用在删除方法上，用来从缓存中移除相应数据。除了同@Cacheable一样的参数之外，它还有下面两个参数：
	- <font color="red">allEntries：</font>非必需，默认为false。当为true时，会移除所有数据
	- <font color="red">beforeInvocation：</font>非必需，默认为false，会在调用方法之后移除数据。当为true时，会在调用方法之前移除数据。

### 使用redis做mybatis的二级缓存 ###
在mybatis中有一级缓存、二级缓存
1. 一级缓存：该缓存是基于SqlSession的，mybatis默认开启一级缓存。
2. 二级缓存：该缓存是Mapper级别的，默认没有开启二级缓存，需要在配置文件中开启：
```java
<!-- 开启二级缓存，默认是false -->
<setting name="cacheEnabled" value="true"/>
```
依赖跟前面一样，使用redis做二级缓存，主要是要实现`org.apache.ibatis.cache`包下的`Cache`接口
```java
public class RedisCache implements Cache {

    private static final Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final String id;

    private RedisTemplate redisTemplate;
    /**
     *    redis过期时间
     */
    private static final long EXPIRE_TIME_IN_MINUTES = 300;

    public RedisCache(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Put query result to redis
     * TimeUnit.HOURS 设置时间的类型：时、分、秒、毫秒
     * @param key
     * @param value
     */
    @Override
    public void putObject(Object key, Object value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, value, EXPIRE_TIME_IN_MINUTES, TimeUnit.HOURS);
        logger.debug("Put query result to redis");
    }


    /**
     * Get cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    public Object getObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        logger.debug("Get cached query result from redis");
        Object o = opsForValue.get(key);
        return o;
    }

    /**
     * Remove cached query result from redis
     *
     * @param key
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object removeObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
        logger.debug("Remove cached query result from redis");
        return null;
    }

    /**
     * Clears this cache instance
     */
    @Override
    public void clear() {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.execute((RedisCallback) connection -> {
            connection.flushDb();
            return null;
        });
        logger.debug("Clear all the cached query result from redis");
    }

    /**
     * This method is not used
     *
     * @return
     */
    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    private RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = SpringContextHolder.getBean("cacheRedisTemplate");
        }
        return redisTemplate;
    }
}
```
自定义一个序列化接口
```java
public class RedisObjectSerializer implements RedisSerializer<Object> {

    private Converter<Object, byte[]> serializer = new SerializingConverter();
    private Converter<byte[], Object> deserializer = new DeserializingConverter();

    static final byte[] EMPTY_ARRAY = new byte[0];
    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return EMPTY_ARRAY;
        }

        try {
            return serializer.convert(object);
        } catch (Exception ex) {
            return EMPTY_ARRAY;
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (isEmpty(bytes)) {
            return null;
        }
        try {
            return deserializer.convert(bytes);
        } catch (Exception ex) {
            throw new SerializationException("Cannot deserialize", ex);
        }
    }

    private boolean isEmpty(byte[] data) {
        return (data == null || data.length == 0);
    }
}

```
redis的配置类
```java
@Configuration
public class RedisConfig {

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        return new JedisConnectionFactory();
    }


    @Bean
    public RedisTemplate<String,Object> cacheRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);


        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 设置值（value）的序列化采用自定义的RedisObjectSerializer
        redisTemplate.setValueSerializer(new RedisObjectSerializer());
        // 设置键（key）的序列化采用jackson2JsonRedisSerializer
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```
前面已经开启了二级缓存，现在需要修改Mapper.xml文件
```java
  <!--表示开启基于redis的二级缓存-->
    <cache type="com.wqh.blog.config.cache.RedisCache">
        <property name="eviction" value="LRU" />
        <property name="flushInterval" value="6000000" />
        <property name="size" value="1024" />
        <property name="readOnly" value="false" />
    </cache>
```
另外在插入、修改和删除时需要添加`flushCache="true"`。
缓存添加成功的话，使用RedisClient可以查看：
![](http://oy09glbzm.bkt.clouddn.com/17-12-27/38131953.jpg)
### 手动将数据添加到redis ###
在上面的RedisConfig类中添加方法，比如我这里要将Test对象保存到redis:
```java
  @Bean
    public RedisTemplate<String,Test> testRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String,Test> template = new RedisTemplate<String,Test>();
        template.setConnectionFactory(redisConnectionFactory);
        //直接使用Jedis提供的StringRedisSerializer
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Test> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Test>(Test.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        //使用jackson2JsonRedisSerializer序列化value
        template.setValueSerializer(jackson2JsonRedisSerializer);
        return template;
    }
```
测试类
```java
	@Autowired
	private RedisTemplate<String, com.wqh.blog.domain.Test> testRedisTemplate;

	@Test
	public void testRedis() throws Exception{
		com.wqh.blog.domain.Test test = new com.wqh.blog.domain.Test();
		test.setId(UUID.randomUUID().toString());
		test.setName("redis");
		test.setRemake("hhhhhhhhhhhhhhhhhhhhhhh");
		testRedisTemplate.opsForValue().set(test.getId(),test);
		Assert.assertEquals("redis",testRedisTemplate.opsForValue().get(test.getId()).getName());
	}
```
## 问题 ##
前面使用注解方式添加缓存的功能，如果使用RedisClient查看数据的话，会发现数出现乱码现象，这主要是序列化问题。解决办法只要自定义一个redisTemplate的bean
```java
    @Bean
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        //key序列化方式,但是如果方法上有Long等非String类型的话，会报类型转换错误
        //Long类型不可以会出现异常信息;
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(redisSerializer);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
```

----------
参考：

- [Spring Boot中的缓存支持（一）注解配置与EhCache使用](http://blog.didispace.com/springbootcache1/)

- [Springboot中使用redis，配置redis的key value生成策略](http://blog.csdn.net/tianyaleixiaowu/article/details/70595073)

