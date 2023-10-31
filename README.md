# Assists
Android无障碍服务（AccessibilityService）开发框架，快速开发复杂自动化任务、远程协助、监听等
***
## Android无障碍服务能做什么
利用Android无障碍服务可以开发一些Android系统内的自动化任务，比如经典的微信自动抢红包、支付宝蚂蚁森林自动浇水、芭芭农场自动施肥等
 
还可以开发远程协助功能，市面上向日葵等一些远程协助功能就是利用无障碍服务和投屏权限开发的

还能开发一些拓客、引流、营销系统，抖音自动点赞评论、微博自动转发评论关注等

总之，利用Android的无障碍服务可以开发各种自动化的任务或者界面信息监听、远程协助等

## Assists开发框架能做什么

按照Google官方文档继承实现的无障碍服务，对于复杂的自动化任务，不仅代码逻辑实现不清晰，后期的修改维护也会很头疼，所以在实践过程中实现了这个框架

在这个框架下开发Android无障碍服务业务可以让你的业务开发更加快速、逻辑更加健壮且容易维护。

## 快速开始
### 1. 添加依赖
#### 1.1 将JitPack仓库添加到根目录build.gradle文件中

```groovy
allprojects {
    repositories {
    	//添加JitPack仓库
        maven { url 'https://jitpack.io' }
    }
}
```

#### 1.2 添加依赖到主模块的build.gradle中，
```groovy
dependencies {
	//添加依赖
    implementation 'com.github.ven-coder:assists:1.0.3'
}
```
### 2. 注册服务
#### 1.1 主模块AndroidManifest.xml中注册服务
一定要在主模块中注册服务，不然进程被杀服务也会自动被关闭需要再次开启
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.ven.assists.simple">

    <application
        android:name="com.ven.assists.simple.App"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:requestLegacyExternalStorage="true"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme"
        android:usesCleartextTraffic="true">
        <!-- 添加以下代码 -->
        <service
            android:name="com.ven.assist.AssistsService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/assists_service" />
        </service>
    </application>

</manifest>
```
### 3. 实现业务逻辑
#### 3.1 继承```StepImpl```
实现`onImpl(collector: StepCollector)`接口，通过```collector.next()```实现步骤逻辑

```kotlin
class OpenWechat:StepImpl {
    override fun onImpl(collector: StepCollector) {
        collector.next(1) {
        	//步骤1逻辑
        	//打开微信
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                Assists.service?.startActivity(this)
            }
            //步骤1执行完，紧接执行步骤2
            StepManager.execute(this::class.java, 2)
        }.next(2) {
        	//步骤2逻辑
        	//查找通讯录按钮
        	UIOperate.findByText("通讯录").forEach {
                //获取到按钮后执行其他逻辑
            }
        }
    }
}
```

#### 3.2 开始执行
执行前请确保无障碍服务已开启，开始执行请使用`beginExecute()`，后续的步骤执行请使用`execute()`方法

```kotlin
//从步骤1开始执行
StepManager.beginExecute(OpenWechat::class.java, 1)
```

## API
### UIOperate

* findById
>根据id查找元素控件，例：
  
```
//查找当前页面id为"com.tencent.mm:id/gkp"的界面元素
UIOperate.findById("com.tencent.mm:id/gkp")
```


* findByTags
>根据类型查找元素控件，例：
  
```
//查找元素child下类型为"android.widget.TextView"的元素
//如child为空则查找当前整个页面元素
UIOperate.findByTags("android.widget.TextView", child)
```

* findByText
>根据文本查找元素控件，例：
  
```
//查找当前页面文本为"返回"或包含"返回"的界面元素
UIOperate.findByText("返回")
```
* getAllNodes
>获取当前页面所有元素
* getNodes
>获取指定元素下所有子元素
* findParentClickable
>查找父类可点击的元素
* clickScreen
>点击屏幕指定位置
* back
>返回
* paste
>粘贴文本到指定元素

文档持续更新中...

## 示例&下载
[&#9654;示例视频](https://www.youtube.com/embed/kNuw9sUsDKo)

[&#128229;直接下载](https://www.pgyer.com/1zaijG)或扫码下载
<div align="left">
<img src="https://raw.githubusercontent.com/ven-coder/Assists/master/graphics/169176584627.jpg" width=200/>
</div>

## 有问题欢迎反馈交流（微信群二维码失效可以加我拉进群）

| 交流群 | 个人微信 |
|:---------:|:-----------:|
| <img src="https://github.com/ven-coder/Assists/blob/master/graphics/1698759668953.png" width=200/>    | <img src="https://raw.githubusercontent.com/ven-coder/assists/master/graphics/wechat1.jpg" width=200/>
