**利用Android无障碍服务（AccessibilityService）能做什么**

可以开发各种各样的自动化脚本程序以及协助脚本，比如：
1. 微信自动抢红包
2. 微信自动接听电话
3. 支付宝蚂蚁森林自动浇水
4. 支付宝芭芭农场自动施肥、自动收集能量...
5. 各种平台的拓客、引流、营销系统
6. 远程控制

# Assists作用
基于Android无障碍服务（AccessibilityService）封装的框架
1. 简化自动化脚本开发
2. 为自动化脚本提供各种增强能力
3. 提高脚本易维护性

# 主要能力
1. 易于使用的无障碍服务API
2. 浮窗管理器：易于实现及管理浮窗
3. 步骤器：为快速实现、可复用、易维护的自动化步骤提供框架及管理
4. 配套屏幕管理：快速生成输出屏幕截图、元素截图
5. 屏幕管理结合opencv：便于屏幕内容识别为自动化提供服务

# 一些示例

| 基础示例 | 进阶示例 | 高级示例 |
| - | - |-|
| <img src="https://github.com/user-attachments/assets/b537bab4-cc55-41c2-8f81-9e8b965e939a" width=200/> | <img src="https://github.com/user-attachments/assets/9b50628c-603e-47d1-a6ae-5600358575fc" width=200/> |<img src="https://github.com/user-attachments/assets/262b9028-5926-478b-93bd-3e20110db391" width=200/>|

| 自动收能量 |自动发朋友圈|自动滑动朋友圈| 无障碍服务开启引导 |
|:-:|:-:|:-:|:-:|
| <img src="https://github.com/ven-coder/Assists/assets/27257149/8d1d09b2-e4b3-44dc-b5df-68fcdcac7a62" width=180 /> |<img src="https://github.com/ven-coder/Assists/assets/27257149/4713656b-a8ff-4c99-9814-a0b883ebbe64" width=180 />|<img src="https://github.com/ven-coder/Assists/assets/27257149/056ef46b-8076-4f90-ab5a-263ff308f8e8" width=180 />| <img src="https://github.com/user-attachments/assets/9e20a757-8d8f-47e6-999b-8532b4e6827a" width=180 /> |
防止下拉通知栏|通知/Toast监听|自动接听微信电话|窗口缩放&拖动|
|<img src="https://github.com/user-attachments/assets/76613db4-c0a9-4ad8-abde-ec0ef8f7ed09" width=180 />|<img src="https://github.com/user-attachments/assets/cc6a861a-3512-43c0-9c1d-4e61229dc527" width=180 />|<img src="https://github.com/user-attachments/assets/25472235-8d6d-4327-9bc5-db47253b7f0e" width=180 />|<img src="https://github.com/user-attachments/assets/184fb248-66e0-4bb4-aaae-c1b8c4cef70a" width=180 />|

##### 更多示例可以直接下载demo查看
<img src="https://github.com/ven-coder/Assists/assets/27257149/c4ce8c21-ac8b-4d3f-bfe4-257a525fb3c5" width=200/>

[&#128229;直接下载](https://www.pgyer.com/1zaijG)

# 🚀 快速开始
### 1. 导入依赖
#### 1.1 项目根目录build.gradle添加
```
allprojects {
    repositories {
	//添加jitpack仓库
        maven { url 'https://jitpack.io' }
    }
}
```


#### 1.2 主模块build.gradle添加
最新版本：[![](https://jitpack.io/v/ven-coder/Assists.svg)](https://jitpack.io/#ven-coder/Assists)
```
dependencies {
    //按需添加
    //基础库（必须）
    implementation "com.github.ven-coder.Assists:assists-base:最新版本"
    //屏幕录制相关（可选）
    implementation "com.github.ven-coder.Assists:assists-mp:最新版本"
    //opencv相关（可选）
    implementation "com.github.ven-coder.Assists:assists-opcv:最新版本"
    
}
```

### 2. 注册&开启服务
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
            android:name="com.ven.assists.service.AssistsService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <!--android:priority="10000" 可提高服务在设置中的权重，排在前面-->
            <intent-filter android:priority="10000">
                <action android:name="android.accessibilityservice.AccessibilityService" />
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/assists_service" />
        </service>
        
        <!-- 或者使用下面的服务可以解决一些应用混淆节点的问题，比如微信8.0.51以上版本获取的节点元素错乱问题 -->
        <!-- ⚠️ 选其一 -->
        <service
            android:name="com.google.android.accessibility.selecttospeak.SelectToSpeakService"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
            <!--android:priority="10000" 可提高服务在设置中的权重，排在前面     -->
            <intent-filter android:priority="10000">
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
#### 1.2 开启服务
调用```AssistsCore.openAccessibilitySetting()```跳转到无障碍服务设置页面，找到对应的应用开启服务。
服务开启后执行以下API测试是否成功集成：
```
AssistsCore.getAllNodes().forEach { it.logNode() }
```
这段代码是获取当前页面所有节点元素的基本信息在Logcat（tag：assists_log）打印出来，如下图：

<img src="https://github.com/user-attachments/assets/81725dc3-d924-44f4-89fe-75938ae659e9" width=350/>

至此，已成功集成Assists。如果没有任何输出请检查集成步骤是否正确。

# 步骤器-快速实现复杂自动化脚本
步骤器可以帮助快速实现复杂的业务场景，比如自动发朋友圈、获取微信所有好友昵称、自动删除好友...等等都是一些逻辑较多的业务场景，步骤器可帮助快速实现。
### 1.继承```StepImpl```
直接在接口`onImpl(collector: StepCollector)`写步骤逻辑，每个步骤自定义步骤的序号，用于区分执行的步骤。如果重复则会以最后一个步骤为准

```kotlin
class MyStepImpl:StepImpl() {
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
//从MyStepImpl步骤1开始执行，isBegin是否作为起始步骤，默认false
StepManager.execute(MyStepImpl::class.java, 1, isBegin = true)
```

### 3. 停止执行
```kotlin
// 设置停止标志，将取消所有正在执行的步骤
StepManager.isStop = true
```

## 其他教程博客
### 获取节点信息
- [使用weditor获取节点信息](https://juejin.cn/post/7484188555735613492)

- [使用Appium获取节点信息](https://juejin.cn/post/7483409317564907530)

- [使用uiautomatorviewer获取节点信息](https://blog.csdn.net/weixin_37496178/article/details/138328871?fromshare=blogdetail&sharetype=blogdetail&sharerId=138328871&sharerefer=PC&sharesource=weixin_37496178&sharefrom=from_link)

### 示例教程
- [Appium结合AccessibilityService实现自动化微信登录](https://juejin.cn/post/7483409317564907530)

## 更新日志
### v3.2.12
更新时间：2025-03-25
1. assists-mp去掉对于opencv减少不必要的包体积
2. 兼容微信8.0.51以上版本获取的节点元素被混淆问题

### [版本历史](https://github.com/ven-coder/Assists/releases)

## 有问题欢迎反馈交流（微信群二维码失效可以加我拉进群）

| 交流群| 个人微信 |
|:---------:|:-----------:|
| <img src="https://github.com/user-attachments/assets/f42960bd-a005-4df3-bf53-310912e4e486" width=200/> | <img src="https://github.com/user-attachments/assets/49378ec3-71a2-4a5e-8510-bec4ec8d915e" width=200/>
1群已满200人，要进1群可加我拉进1群

### ❤️ 已入驻爱发电，感谢[支持](https://afdian.com/a/vencoder) 

## 我的付费社群
付费社群提供的服务：
1. 完整易于阅读的开发文档
2. Assists开发指导
3. 开发疑难解答
4. 群友互助资源对接
5. 基于Assists开发的抖音养号，小红书养号，支付宝能量收集，支付宝农场，无线远程控制等源码（补充中...）
<img width="500" alt="image" src="https://github.com/user-attachments/assets/7607a4e6-4845-474e-a9c6-e685cc306523" />

##### 微信扫扫即可加入
<img src="https://github.com/ven-coder/Assists/assets/27257149/7ae8e825-f489-46e3-96f0-ed03d12db9e8" width=200/>

##### 定制开发可直接联系个人微信：x39598

# License
[GNU General Public License v3.0](https://github.com/ven-coder/Assists/blob/master/LICENSE)
