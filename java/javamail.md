---
title: 使用JavaMail发送邮件（带附件）
date: 2017-08-20 17:36:06
tags: javamail
---
在使用javaMail发邮件之前先简单说一下邮件的三个协议：

1. POP3：POP的全称是 Post Office Protocol ，即邮局协议，用于电子邮件的接收，它使用TCP的110端口，现在常用的是第三版，所以简称为 POP3。 
<!--more-->
2. SMTP：SMTP（Simple Mail Transfer Protocal）称为简单邮件传输协议，目标是向用户提供高效、可靠的邮件传输。
3. IMAP：
IMAP是Internet Message Access Protocol的缩写，顾名思义，主要提供的是通过Internet获取信息的一种协议。

## 前期准备 ##
如果是使用像QQ邮箱、163邮箱、126邮箱等这种普通的邮箱，需要先开启POP3/SMTP/IMAP服务，并且获取授权。如果是企业邮箱，可以直接使用。这里以163邮箱为例。

![](http://i.imgur.com/5fMcgQh.png)
先查看是否开启服务
![](http://i.imgur.com/YU0igMq.png)
开启并设置客户端授权码，这个很重要。如果是qq邮箱，需要发短信配置

## jar包 ##
javaMail需要两个jar，直接使用maven导入依赖


```java
		<dependency>
            <groupId>javax.activation</groupId>
            <artifactId>activation</artifactId>
            <version>1.1.1</version>
        </dependency>
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.5.2</version>
        </dependency>
```
## 代码 ##
```java
	//主机名,上面在开启POP3/SMTP/IMAP服务的时候有服务器地址
    public static String HOST_NAME = "smtp.126.com";
    //用户名，邮箱的登陆名
    public static String USER_NAME = "xxxxx";
    //密码，这里是开始设置的授权码
    public static String USER_PASSWD = "xxxxx";
    //发件人
    public static String FROM_NAME = "xxxxx";
    //抄送人
    public static String CC_NAME = "xxxxx";

    /**
     * @param toEmail 发送给谁
     * @param code   发送的内容，验证码
     */
    public static void sendMail(String toEmail, String code) {
        //1：创建Properties
        Properties properties = new Properties();
        properties.put("mail.host", HOST_NAME);
        properties.put("mail.smtp.auth", "true");
        //2：获取用户名和密码进行认证
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USER_NAME, USER_PASSWD);
            }
        };
        //3：获取session对象
        Session session = Session.getInstance(properties, authenticator);
        //4：设置邮件发送信息
        Message message = new MimeMessage(session);
        try {
            //4.1：设置发件人
            message.setFrom(new InternetAddress(FROM_NAME));
            //4.2：设置收件人
			//Message.RecipientType.TO 收件人
			//Message.RecipientType.CC 抄送
			//Message.RecipientType.BCC 暗送
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            //4.3：设置邮件主题
            message.setSubject("主题！！");
            //4.4：设置邮件正文
            message.setContent(code,"text/html;charset=utf-8");
            //5：发送邮件
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
```