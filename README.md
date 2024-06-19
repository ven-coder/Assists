# Assists v3.0.0
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

## v3.0.1更新日志
1. 步骤器重构：支持协程、执行下一步方式改为直接指定
2. 获取屏幕图像
3. 集成opencv，封装图像匹配

v2.0.0版本[查看这里](https://github.com/ven-coder/Assists/tree/release-2.0.0)

## 功能计划
- 图像识别 （✅完成）
- 图文识别
- 录屏
- js支持
- vue.js支持
- uniapp插件
- flutter插件

## Demo部分演示
| 图像识别支付宝自动收能量 |自动发朋友圈|自动滑动朋友圈|
|:-:|:-:|:-:|
| <img src="https://github.com/ven-coder/Assists/assets/27257149/8d1d09b2-e4b3-44dc-b5df-68fcdcac7a62" width=250 /> |<img src="https://github.com/ven-coder/Assists/assets/27257149/4713656b-a8ff-4c99-9814-a0b883ebbe64" width=250 />|<img src="https://github.com/ven-coder/Assists/assets/27257149/056ef46b-8076-4f90-ab5a-263ff308f8e8" width=250 />

## 使用
### 1. 添加库
#### 1.1 Clone或下载源码
版本v3.0.0：https://github.com/ven-coder/Assists/releases/tag/3.0.1


#### 1.2 导入依赖库
解压后以module方式导入assists，opencv图像识别为可选库，如果需要使用到图像识别可导入其中的assists-opcv
<img src="https://github.com/ven-coder/Assists/assets/27257149/592dc0e1-8764-42e5-bede-2be2dfa9ccb8" width=400/>
<br/>
<img src="https://github.com/ven-coder/Assists/assets/27257149/9705e28c-8ff7-4678-bb57-7e475e5c131b" width=400/>

#### 1.3 引用assists
导入成功后在主模块build.gradle添加引用
```
dependencies {
    ...其他依赖
    implementation project(':assists')
    implementation project(':assists-opcv')//图像识别库，可选，按需添加
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
        <!-- 添加代码 ↓-->
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
        <!-- 添加代码 ↑-->
    </application>

</manifest>
```
至此，开启无障碍服务后即可使用包装的API，开发文档请查看：[https://github.com/ven-coder/Assists/wiki](https://github.com/ven-coder/Assists/wiki)

## 步骤器-快速实现复杂业务
步骤器可以帮助快速实现复杂的业务场景，比如自动发朋友圈、获取微信所有好友昵称、自动删除好友...等等都是一些逻辑较多的业务场景，步骤器可帮助快速实现。
前提: 已完成前面的[配置](https://github.com/ven-coder/Assists?tab=readme-ov-file#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B)
### 1.继承```StepImpl```
直接在接口`onImpl(collector: StepCollector)`写步骤逻辑，每个步骤自定义步骤的序号，用于区分执行的步骤。如果重复则会以最后一个步骤为准

```kotlin
//OpenWechat为该业务场景的分类
class OpenWechat:StepImpl() {
    override fun onImpl(collector: StepCollector) {
	//定义步骤序号为1的逻辑
        collector.next(1) {// 1为步骤的序号
            //步骤1逻辑
	    ...
            //返回下一步需要执行的序号，通过Step.get([序号])，如果需要重复该步骤可返回Step.repeat，如果返回Step.none则不执行任何步骤，相当于停止
            return@next Step.get(2) //将会执行步骤2逻辑
        }.next(2) {
            //步骤2逻辑
	    ...
	    //返回下一步需要执行的序号，通过Step.get([序号])
	    return@next Step.get(3)
        }.next(3) {
            //步骤3逻辑
	    ...
	    //返回下一步需要执行的序号，通过Step.get([序号])
	    return@next Step.get(4)
        }
	其他步骤
	...
    }
}

```

### 2. 开始执行
执行前请确保无障碍服务已开启

```kotlin
//从OpenWechat的步骤序号1开始执行，isBegin需要为true，默认false
StepManager.execute(OpenWechat::class.java, 1, isBegin = true)
```

## 图像识别模块使用
图像识别模块主要是对opencv的模板匹配api和Android的屏幕图像获取进行了一层封装，下面是所封装api的使用说明
### 检查是否拥有录屏权限
```Assists.isEnableScreenCapture()```
### 请求录屏权限
```Assists.requestScreenCapture()```
### 获取屏幕图像
```OpencvWrapper.getScreen()```
### 通过Assets获取图像
```OpencvWrapper.getTemplateFromAssets()```
### 创建掩膜
```
OpencvWrapper.createMask(
        source: Mat, //原图像
        lowerScalar: Scalar, 可操作区域最低rgb
        upperScalar: Scalar, 可操作区域最高rgb
        requisiteExtraRectList: List<Rect> = arrayListOf(), //不可操作区域
        redundantExtraRectList: List<Rect> = arrayListOf() //可操作区域，即图像匹配有效区域
    )
```
>添加掩膜进行模板匹配可大大提高匹配准确度，掩膜是一个黑白色的图像，白色为匹配的区域，黑色为不匹配区域，通过仅匹配关键区域提高匹配准确度

### 模板匹配
```OpencvWrapper.matchTemplate(image: Mat?, template: Mat?, mask: Mat? = null)```
> ```image``` 大图图像<br/>
> ```template``` 模板图像，即小图<br/>
> ```mask``` 掩膜<br/>
> 返回所有匹配结果，按需求进行筛选

### 模板匹配获取最佳结果
```OpencvWrapper.matchTemplateFromScreenToMinMaxLoc(image: Mat?, template: Mat?, mask: Mat? = null)```
> ```image``` 大图图像<br/>
> ```template``` 模板图像，即小图<br/>
> ```mask``` 掩膜<br/>
> 返回最佳匹配结果

### 筛选匹配结果获取达到阈值的匹配结果
```
OpencvWrapper.getResultWithThreshold(
        result: Mat,
        threshold: Double,
        ignoreX: Double = -1.0,
        ignoreY: Double = -1.0,
    )
```
> ```result``` 匹配结果<br/>
> ```threshold``` 匹配值，范围0-1，0最低匹配值，1最高匹配值<br/>
> ```ignoreX``` 忽略的x轴范围<br/>
> ```ignoreY``` 忽略的y轴范围<br/>
> 返回符合条件的匹配坐标列表

#### 以上具体的使用最好直接查看demo源码

#### 更多开发文档请查看：[https://github.com/ven-coder/Assists/wiki](https://github.com/ven-coder/Assists/wiki)

## Demo下载
扫码下载
<br/>
<img src="https://github.com/ven-coder/Assists/assets/27257149/c4ce8c21-ac8b-4d3f-bfe4-257a525fb3c5" width=200/>

[&#128229;直接下载](https://www.pgyer.com/1zaijG)

## 有问题欢迎反馈交流（微信群二维码失效可以加我拉进群）

| 交流群 | 个人微信 |
|:---------:|:-----------:|
| <img src="https://github.com/ven-coder/Assists/assets/27257149/841b3b8f-9aff-4d71-af62-3b743cce1112" width=200/>    | <img src="https://github.com/ven-coder/Assists/assets/27257149/542e51d1-9f22-4381-b011-618fc0cef959" width=200/>


# License
[GNU General Public License v3.0](https://github.com/ven-coder/Assists/blob/master/LICENSE)
