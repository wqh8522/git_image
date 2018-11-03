---
title: 大华摄像头二次开发-web端实现实时视频监控
date: 2018-03-28 20:23:02
categories: 大华
---

最近客户提需要，需要在现有的系统中集成视频监控功能，摄像头是大华的。而大华又没有关于java的sdk，官网只能下载到c++的demo和dll文件。无奈只能自己在网上找了，最后找到了一些解决办法，把实现方法记录一下。

<!--more-->

## 使用Jna技术调用大华dll

在大华官网下载sdk，[传送门](https://www.dahuatech.com/service/downloadlists/836.html)；下载完成后解压，将里面的库文件复制到其他文件夹以方便jna的调用。具体可以查看demo：[java后端大华摄像头二次开发demo](https://download.csdn.net/download/wqh8522/10313800)，因为这种方式使用java的swing窗口开发比较方便，由于这次是做web端开发，所以没有详细的去了解，这里不作详细分析，可以查看上面的demo。

## 使用ie的activex插件

参考：[通过WEB调用大华网络摄像头](https://blog.csdn.net/whzhaochao/article/details/10026519)

这种方式实现的功能较少，只有实时视频、抓图、录制视频功能。而且只支持ie浏览器。

### 1.下载插件并注册

[点击下载](https://download.csdn.net/download/wqh8522/10313844)

### 2.使用object元素添加视频

```html
<object classid="clsid:30209FBC-57EB-4F87-BF3E-740E3D8019D2" codebase="" id="id" name="playOcx" align="center" width="350" height="300px">
</object>
```

### 3.js调用开启实时视频

```javascript
//获取object元素js对象
var SSOcx = document.getElementById(id);
//登录视频
var flag = SSOcx.SetDeviceInfo("ip", "端口", "通道", "用户名", "密码");
if (flag) {
    //开启实时视频监控
    SSOcx.StartPlay();
} else {
    return false;
}
```

### 4.实时视频全屏实现

全屏button：

```html
<button class="btn btn-white btn-sm" onclick="reqFullScreen(document.getElementById('id'))">
	<i class="fa fa-arrows-alt"></i>全屏                        </button>
```

全屏js：

```javascript
<script type="text/javascript">
    var width = "";
    var height = "";

    //进入全屏
    function reqFullScreen(element) {
        console.log(element.width)
        var de = element;
        if (de.requestFullscreen) {
            de.requestFullscreen();
            remoceWH(de);
        } else if (de.mozRequestFullScreen) {
            de.mozRequestFullScreen();
            remoceWH(de);
        } else if (de.webkitRequestFullScreen) {
            de.webkitRequestFullScreen();
            remoceWH(de);
        } else if (de.msRequestFullscreen) {
            de.msRequestFullscreen();
            doc = de;
            remoceWH(de);
        } else {
            console.log("进入全屏失败")
        }
    }

    function remoceWH(element) {
        //保存原始的宽高
        width = element.getAttribute("width");
        height = element.getAttribute("height");
        //移除object元素的宽高
        element.removeAttribute("width");
        element.removeAttribute("height");
    }

    //退出全屏
    function exitFullscreen() {
        if (document.exitFullscreen) {
            document.exitFullscreen();
        }
        else if (document.msExitFullscreen) {
            document.msExitFullscreen();
        }
        else if (document.mozCancelFullScreen) {
            document.mozCancelFullScreen();
        }
        else if (document.webkitCancelFullScreen) {
            document.webkitCancelFullScreen();
        }
        setWH();
    }
    function setWH() {
        $("object[name='playOcx']").each(function () {
            $(this).attr("width", width);
            $(this).attr("height", height);
        });
    }
    //监听esc事件
     $(document).keyup(function (event) {
         switch (event.keyCode) {
             case 27:
                 exitFullscreen();
                 break;
         }
    });
</script>
```

### 5.总结

这种方式实现起来还算方便，但是功能较少。不支持音频等。

## 使用webplugin.exe插件

这种方式也是使用别人开发的插件，但是功能几乎都有。在web开发这种方式应该也是最好的了。但是也有局限性，使用谷歌浏览器和火狐浏览器不行；对360浏览器支持，但是如果使用兼容模式还是需要下载另外下载activex插件。

### 1.下载插件

[点击下载](https://download.csdn.net/download/wqh8522/10313930)；解压后安装里面的webplugin.exe，如果是ie浏览器，另外注册上面的ocx插件。

### 2. object元素

ie模式：

```html
<object id="id" width="100%" height="87%"
        classid="clsid:7F9063B6-E081-49DB-9FEC-D72422F2727F"
        codebase="">
</object>
```

其他：

```html
<object name="playOcx" id="id" width="100%" height="87%"
        type="application/media-plugin-version-3.1.0.2"
        VideoWindTextColor="9c9c9c" VideoWindBarColor="414141">
</object>
```

### 3. 判断是否安装插件

```javascript
<script type="text/javascript">
    var Sys = {};
    var ua = navigator.userAgent.toLowerCase();
    var s;
    (s = ua.match(/(msie\s|trident.*rv:)([\d.]+)/)) ? Sys.ie = s[2] :
        (s = ua.match(/firefox\/([\d.]+)/)) ? Sys.firefox = s[1] :
            (s = ua.match(/chrome\/([\d.]+)/)) ? Sys.chrome = s[1] :
                (s = ua.match(/opera.([\d.]+)/)) ? Sys.opera = s[1] :
                    (s = ua.match(/version\/([\d.]+).*safari/)) ? Sys.safari = s[1] : 0;
    var PLUGINS_NAME = 'WebActiveEXE.Plugin.1';
    var hasPlugin = checkPlugins();

    /**
     * 检测浏览器是否存在视频插件
     * @return {Boolean}
     */
    function checkPlugins() {
        var result;
        if (Sys.ie) {
            try {
                result = new ActiveXObject(PLUGINS_NAME);
                delete result;
            } catch (e) {
                return false;
            }
            return true;
        } else {
            navigator.plugins.refresh(false);
            result = navigator.mimeTypes["application/media-plugin-version-3.1.0.2"];
            return !!(result && result.enabledPlugin);
        }
    }
</script>
```

###4. js连接摄像头开启实时监控

这个视频控件已经实现了抓图、音频和录制功能

```javascript
var g_ocx = document.getElementById(sxtList[i].id);
var bRet = g_ocx.LoginDeviceEx('' + sxtList[i].ip, sxtList[i].dkh - 1 + 1, '' + sxtList[i].yhm, '' + sxtList[i].mm, 0);
if (bRet == 0) {
    //设置窗口数量
    g_ocx.SetWinBindedChannel(1, 0, 63, 64);
    //设置截图保存地址
    g_ocx.SetConfigPath(1,"C:\\视频监控\\images");
    //设置录制视频保存地址
    g_ocx.SetConfigPath(2,"C:\\视频监控\\vidos");
    //设置画质 0 高 1 低
    // g_ocx.SetPicQuality(0);
    //设置视频实时性 0-10 实时登记依次降低
    g_ocx.SetAdjustFluency(0);
    //监视模式
    g_ocx.SetModuleMode(1); 
    //连接视频
    g_ocx.ConnectRealVideo(0, 1);
} else {
    alert("网络连接错误！");
    return false;
}
```

### 5.全屏实现

在这个控件中已经实现了全屏方法，经过测试不同的浏览器全屏的实现也有点不同：

```javascript
 function reqFullScreen(element) {
        var ocx = element;
        if (Sys.ie) {
            //ie浏览器
            ocx.SwitchToFullScreen();
        } else {
            //其他浏览器
            ocx.OnFullScreenClk();
        }
    }
```

### 6. 其他方法

- 开启音频

```javascript
var ele_id = element.getAttribute("id");
var g_ocx = element;
var soundStatus = g_ocx.PlayOpenSound();
```

- 关闭音频

```javascript
var ele_id = element.getAttribute("id");
var g_ocx = element;
var soundStatus =  g_ocx.PlayStopSound();
```

里面还有很多方法可以二次开发，具体可以参考压缩包里面的文档。

## 最后

文件打包下载：[传送门](https://download.csdn.net/download/wqh8522/10314095)