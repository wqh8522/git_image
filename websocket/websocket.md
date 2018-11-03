---
title: SpringMVC使用websocket做消息推送
date: 2017-11-22 20:13:57
categories: websocket
tags: [springmvc,websocket]
---
## WebSocket ##
> WebSocket协议支持（在受控环境中运行不受信任的代码的）客户端与（选择加入该代码的通信的）远程主机之间进行全双工通信。用于此的安全模型是Web浏览器常用的基于原始的安全模式。 协议包括一个开放的握手以及随后的TCP层上的消息帧。 该技术的目标是为基于浏览器的、需要和服务器进行双向通信的（服务器不能依赖于打开多个HTTP连接（例如，使用XMLHttpRequest或`<`iframe>和长轮询））应用程序提供一种通信机制。

## socket消息推送流程  ##
1. 后台创建socket服务<!--more-->；
2. 用户登录后与后台建立socket连接，默认使用websocket，如果浏览器不支持则使用scokjs连接；
3. 建立连接后，服务端可以向用户推送信息；

javaweb中，socket的实现方式有多种，这里使用Spring-webscoket的方式实现。
## demo ##
### 搭建环境 ###
在SpringMVC的项目基础上，导入websocket的相关jar包。
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-websocket</artifactId>
    <version>4.1.9.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-messaging</artifactId>
    <version>4.1.9.RELEASE</version>
</dependency>
```
### websocket服务端实现类 ###
```java

@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer{

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        System.out.println("==========================注册socket");
        //注册websocket server实现类，"/webSocketServer"访问websocket的地址
        registry.addHandler(msgSocketHandle(),
                "/webSocketServer").
                addInterceptors(new WebSocketHandshakeInterceptor());
        //使用socketjs的注册方法
        registry.addHandler(msgSocketHandle(),
                "/sockjs/webSocketServer").
                addInterceptors(new WebSocketHandshakeInterceptor())
                .withSockJS();
    }

	 /**
     *
     * @return 消息发送的Bean
     */
    @Bean(name = "msgSocketHandle")
    public WebSocketHandler msgSocketHandle(){
        return new MsgScoketHandle();
    }
}
```
这里使用的config配置的形式注册bean和配置，所以需要在SpringMVC的配置文件中添加对类的自动扫描
```xml
	<mvc:annotation-driven />
	<context:component-scan base-package="com.wqh.websocket"/>
```
### 拦截器类 ###
主要是获取到当前连接的用户，并把用户保存到WebSocketSession中
```java
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
   private static final Logger logger = LoggerFactory.getLogger(WebSocketHandshakeInterceptor.class);

    /**
     * 握手前
     * @param request
     * @param response
     * @param webSocketHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Map<String, Object> attributes) throws Exception {
        logger.info("握手操作");
        if (request instanceof ServletServerHttpRequest){
           ServletServerHttpRequest servletServerHttpRequest = (ServletServerHttpRequest) request;
           HttpSession session = servletServerHttpRequest.getServletRequest().getSession(false);
           if(session != null){
           		//从session中获取当前用户
               User user = (User) session.getAttribute("user");
               attributes.put("user",user);
           }
       }

        return true;
    }

    /**
     * 握手后
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @param webSocketHandler
     * @param e
     */
    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
```
### socket处理消息类 ###
```java
@Component
public class MsgScoketHandle implements WebSocketHandler {

    /**已经连接的用户*/
    private static final ArrayList<WebSocketSession> users;

    static {
    	//保存当前连接用户
        users = Lists.newArrayList();
    }

    /**
     * 建立链接
     * @param webSocketSession
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        //将用户信息添加到list中
        users.add(webSocketSession);
        System.out.println("=====================建立连接成功==========================");
        User user  = (User) webSocketSession.getAttributes().get("user");
        if(user != null){
            System.out.println("当前连接用户======"+user.getName());
        }
        System.out.println("webSocket连接数量====="+users.size());
    }

    /**
     * 接收消息
     * @param webSocketSession
     * @param webSocketMessage
     * @throws Exception
     */
    @Override
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        User user = (User) webSocketSession.getAttributes().get("user");
        System.out.println("收到用户:"+user.getName()+"的消息");
        System.out.println(webSocketMessage.getPayload().toString());
        System.out.println("===========================================");

    }

    /**
     * 异常处理
     * @param webSocketSession
     * @param throwable
     * @throws Exception
     */
    @Override
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable){
        if (webSocketSession.isOpen()){
            //关闭session
            try {
                webSocketSession.close();
            } catch (IOException e) {
            }
        }
        //移除用户
        users.remove(webSocketSession);
    }

    /**
     * 断开链接
     * @param webSocketSession
     * @param closeStatus
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        users.remove(webSocketSession);
        User user = (User) webSocketSession.getAttributes().get("user");
        System.out.println(user.getName()+"断开连接");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 发送消息给指定的用户
     * @param user
     * @param messageInfo
     */
    public void sendMessageToUser(User user, TextMessage messageInfo){
        for (WebSocketSession session : users) {
            User sessionUser = (User) session.getAttributes().get("user");
            //根据用户名去判断用户接收消息的用户
            if(user.getName().equals(sessionUser.getName())){
                try {
                    if (session.isOpen()){
                        session.sendMessage(messageInfo);
                        System.out.println("发送消息给："+user.getName()+"内容："+messageInfo);
                    }
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```
### controller及页面 ###
这里简单的模拟登录，前台传入登录参数，直接将参数保存到session中。
```java
@RequestMapping("websocket")
@Controller
public class UserController {

    @Autowired
    private MsgScoketHandle msgScoketHandle;

    @RequestMapping("login")
    public String login(User user, HttpServletRequest request){
        user.setId(UUID.randomUUID().toString().replace("-",""));
        request.getSession().setAttribute("user",user);
        return "/index";
    }

    @ResponseBody
    @RequestMapping("sendMsg")
    public String sendMag(String content,String toUserName){
        User user = new User();
        user.setName(toUserName);
        TextMessage textMessage = new TextMessage(content);
        msgScoketHandle.sendMessageToUser(user,textMessage);
        return "200";
    }
}
```
登录页面省略，直接socket连接页面，这里使用`sockjs`来创建连接，所以需要先添加js文件
[sockjs.min.js](https://raw.githubusercontent.com/sockjs/sockjs-client/master/dist/sockjs.min.js)
```html
<script>
    $(document).ready(function() {
        var ws;
        if ('WebSocket' in window) {
            ws = new WebSocket("ws://"+window.location.host+"/webSocketServer");
        } else if ('MozWebSocket' in window) {
            ws = new MozWebSocket("ws://"+window.location.host+"/webSocketServer");
        } else {
            //如果是低版本的浏览器，则用SockJS这个对象，对应了后台“sockjs/webSocketServer”这个注册器，
            //它就是用来兼容低版本浏览器的
            ws = new SockJS("http://"+window.location.host+"/sockjs/webSocketServer");
        }
        ws.onopen = function (evnt) {
        };
        //接收到消息
        ws.onmessage = function (evnt) {
            alert(evnt.data);
            $("#msg").html(evnt.data);
        };
        ws.onerror = function (evnt) {
            console.log(evnt)
        };
        ws.onclose = function (evnt) {
        }

        $("#btn1").click(function () {

            ws.send($("#text").val());
        });
        $("#btn2").bind("click",function () {
            var url = "${pageContext.request.contextPath}/websocket/sendMsg";
            var content =  $("#text").val();
            var toUserName = "admin"
            $.ajax({
                data: "content=" + content + "&toUserName=" + toUserName,
                type: "get",
                dataType: 'text',
                async: false,
                contentType: "application/x-www-form-urlencoded;charset=UTF-8",
                encoding: "UTF-8",
                url: url,
                success: function (data) {
                    alert(data.toString());
                },
                error: function (msg) {
                    alert(msg);
                },

            });

        })
    });

</script>
<body>
当前登录用户：${pageContext.session.getAttribute("user").name}<br>
    <input type="text" id="text">
    <button id="btn1" value="发送给后台">发送给后台</button>
    <button id="btn2" value="发送给其他用户">发送给其他用户</button>
    <div id="msg"></div>
</body>
</html>

```
## 启动项目 ##
在控制可以看到socket注册成功
![](http://oy09glbzm.bkt.clouddn.com/17-11-22/12127517.jpg)
访问页面，第一个用户使用admin登录，第二个使用1234登录
![](http://oy09glbzm.bkt.clouddn.com/17-11-22/50674851.jpg)![](http://oy09glbzm.bkt.clouddn.com/17-11-22/16242008.jpg)
首先将消息发送给后台，后台打印消息
![](http://oy09glbzm.bkt.clouddn.com/17-11-22/37329041.jpg)
使用1234用户发送消息给admin
![](http://oy09glbzm.bkt.clouddn.com/17-11-22/51622418.jpg)
## 爬坑 ##
在Springmvc项目中都会指定连接访问的后缀，比如.do、.action，但是这里会导致按照以上配置会导致前端连接socket服务时404。我的解决办法是修改web.xml，将`DispatcherServlet`的` <url-pattern>`改为`/`。。。但是新的问题又出现了，页面无法加载资源文件，所以还需要在SpringMVC.xml中添加对静态资源的配置，这里具体的`mapping`和`location`看自己的具体项目。
```xml
	<mvc:resources mapping="/css/**" location="/css/" />
	<mvc:resources mapping="/images/**" location="/images/" />
	<mvc:resources mapping="/js/**" location="/js/" />
```
