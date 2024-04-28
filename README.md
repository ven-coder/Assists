# Assists （无线远程控制系统开发中...2024.4.24）
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

## v2.0.0更新日志（文档后续补充...）
1. 整理api
2. 增加
- 浮窗支持拖动修改大小
- 
  <img src="https://github.com/ven-coder/Assists/blob/master/graphics/scale.gif" width=150>
- 根据类型查找元素
- 在当前元素范围下，根据类型查找元素
- 获取当前页面所有元素
- 获取指定元素下所有子元素
- 查找第一个可点击的父元素
- 拓展-获取元素在屏幕中的范围
- 拓展-手势点击元素所处的位置
- 拓展-点击元素
- 拓展-手势长按元素所处的位置
3. 修复
- 基准分辨率获取对应当前分辨率的坐标部分机型不一致问题

## 功能计划
- 录屏
- 图文识别
- js支持
- vue.js支持
- uniapp插件
- flutter插件

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
    implementation 'com.github.ven-coder:assists:2.0.0'
}
```

### 2. 注册服务
#### 1.1 主模块AndroidManifest.xml中注册服务
一定要在主模块中注册服务，不然进程被杀服务也会自动被关闭需要再次开启（小米可保持杀进程保持开启，其他vivo、oppo、鸿蒙机型似乎不行）
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
至此，开启无障碍服务后即可使用包装的API了

## 步骤器-快速实现复杂业务
步骤器可以帮助快速实现复杂的业务场景，比如自动发朋友圈、获取微信所有好友昵称、自动删除好友...等等都是一些逻辑较多的业务场景，步骤器可帮助快速实现。
前提: 已完成前面的[配置](https://github.com/ven-coder/Assists?tab=readme-ov-file#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
### 1.继承```StepImpl```
直接在接口`onImpl(collector: StepCollector)`写逻辑

```kotlin
//OpenWechat为该业务场景的分类
class OpenWechat:StepImpl {
    override fun onImpl(collector: StepCollector) {
	//步骤1逻辑
        collector.next(1) {//1为该步骤的标识
            //步骤1逻辑
	    ...
            //执行步骤2，this::class.java为当前StepImpl实现类的步骤逻辑，如果传其他的StepImpl就会执行指定的StepImpl逻辑
            StepManager.execute(this::class.java, 2)
        }.next(2) {
            //步骤2逻辑
	    ...
	    //下一步
	    StepManager.execute(this::class.java, 3)
        }.next(2) {
            //步骤3逻辑
	    ...
	    //下一步
	    StepManager.execute(this::class.java, 4)
        }
	其他步骤
	...
    }
}

```

### 2. 开始执行
执行前请确保无障碍服务已开启

```kotlin
//从OpenWechat的步骤1开始执行，isBegin需要为true，默认false
StepManager.execute(OpenWechat::class.java, 1, isBegin = true)
```
具体的使用可以下载查看demo源码

### 更多开发文档请查看：[https://github.com/ven-coder/Assists/wiki](https://github.com/ven-coder/Assists/wiki)

## 示例&下载
[&#9654;示例视频](https://www.youtube.com/embed/kNuw9sUsDKo)

扫码下载

<img src="https://github.com/ven-coder/Assists/blob/master/graphics/1714263085511.jpg" width=200/>

[&#128229;直接下载](https://www.pgyer.com/1zaijG)

## 有问题欢迎反馈交流（微信群二维码失效可以加我拉进群）

| 交流群 | 个人微信 |
|:---------:|:-----------:|
| <img src="https://github.com/ven-coder/Assists/blob/master/graphics/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20240425232301.jpg" width=200/>    | <img src="https://github.com/ven-coder/Assists/blob/master/graphics/me.jpg" width=200/>

# License
[GNU General Public License v3.0](https://github.com/ven-coder/Assists/blob/master/LICENSE)
