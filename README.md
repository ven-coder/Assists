# Assists v3.1.0
Android无障碍服务（AccessibilityService）开发框架，快速开发复杂自动化任务、远程协助、监听等
***
## Android无障碍服务能做什么
利用Android无障碍服务可以开发一些Android系统内的自动化任务，比如经典的微信自动抢红包、支付宝蚂蚁森林自动浇水、芭芭农场自动施肥等
 
还可以开发远程协助功能，市面上向日葵等一些远程协助功能就是利用无障碍服务和投屏权限开发的

还能开发一些拓客、引流、营销系统，抖音自动点赞评论、微博自动转发评论关注等

总之，利用Android的无障碍服务可以开发各种自动化的任务或者界面信息监听、远程协助等

## Assists开发框架能做什么

按照Google官方文档继承实现的无障碍服务，对于复杂的自动化任务，不仅代码逻辑实现不清晰，后期的修改维护也会很头疼，所以在实践过程中实现了这个框架

使用这个框架下开发Android无障碍服务业务可以让你的业务开发更加快速、逻辑更加健壮且容易维护。

## 框架项目亮点
1. 包装无障碍服务设置简单配置即可使用
2. 利用kotlin拓展AccessibilityNodeInfo，使元素的操作更加简洁
3. 结合kotlin协程封装步骤器，可使复杂自动化业务逻辑低耦合又不容易交叉出错且易于维护
4. 利用框架实现各种示例供参考

## v3.1.0更新日志-2024-8-25
1. 示例新增无障碍服务开启引导、防止下拉通知栏、通知/Toast监听、微信电话自动接听
2. 新增文本选择```AccessibilityNodeInfo.selectionText```
3. 新增修改输入框文本内容```AccessibilityNodeInfo.setNodeText```
4. 新增修改输入框文本内容```AccessibilityNodeInfo.setNodeText```
5. 新增获取当前app在屏幕中的范围```AccessibilityNodeInfo.getAppBoundsInScreen```
6. 新增获取当前app在屏幕中的宽度```AccessibilityNodeInfo.getAppWidthInScreen```
7. 新增获取当前app在屏幕中的高度```AccessibilityNodeInfo.getAppHeightInScreen```
8. 新增根据文本查找所有与文本相同的元素```AccessibilityNodeInfo.findByTextAllMatch```
9. 新增使列表向前滚动```AccessibilityNodeInfo.scrollForward```
10. 新增使列表向后滚动```AccessibilityNodeInfo.scrollBackward```

### [版本历史](https://github.com/ven-coder/Assists/wiki/%E5%8E%86%E5%8F%B2%E7%89%88%E6%9C%AC)

## 功能计划
- js支持（进行中...2024.8.25）
- 图文识别
- 录屏
- vue.js支持
- uniapp插件
- flutter插件
- 图像识别 （✅完成）

## Demo部分演示
| 图像识别支付宝自动收能量 |自动发朋友圈|自动滑动朋友圈| 无障碍服务开启引导 |
|:-:|:-:|:-:|:-:|
| <img src="https://github.com/ven-coder/Assists/assets/27257149/8d1d09b2-e4b3-44dc-b5df-68fcdcac7a62" width=180 /> |<img src="https://github.com/ven-coder/Assists/assets/27257149/4713656b-a8ff-4c99-9814-a0b883ebbe64" width=180 />|<img src="https://github.com/ven-coder/Assists/assets/27257149/056ef46b-8076-4f90-ab5a-263ff308f8e8" width=180 />| <img src="https://github.com/user-attachments/assets/9e20a757-8d8f-47e6-999b-8532b4e6827a" width=180 /> |
防止下拉通知栏|通知/Toast监听|自动接听微信电话|
|<img src="https://github.com/user-attachments/assets/76613db4-c0a9-4ad8-abde-ec0ef8f7ed09" width=180 />|<img src="https://github.com/user-attachments/assets/cc6a861a-3512-43c0-9c1d-4e61229dc527" width=180 />|<img src="https://github.com/user-attachments/assets/25472235-8d6d-4327-9bc5-db47253b7f0e" width=180 />


## 使用
### 1. 添加库
#### 1.1 Clone或下载源码
版本v3.1.0：[https://github.com/ven-coder/Assists/releases/tag/3.1.0](https://github.com/ven-coder/Assists/releases/tag/3.1.0)


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

#### 以上具体使用最好直接查看Simple源码

#### 这里有些不是很完整的文档：[https://github.com/ven-coder/Assists/wiki](https://github.com/ven-coder/Assists/wiki)

## Demo下载
扫码下载（由于加入opencv且未对abi进行过滤所以安装包有134M）
<br/>
<img src="https://github.com/ven-coder/Assists/assets/27257149/c4ce8c21-ac8b-4d3f-bfe4-257a525fb3c5" width=200/>

[&#128229;直接下载](https://www.pgyer.com/1zaijG)

## 有问题欢迎反馈交流（微信群二维码失效可以加我拉进群）

| 交流群 | 个人微信 |
|:---------:|:-----------:|
| <img src="https://github.com/user-attachments/assets/7eb48337-7f8c-4885-9fd0-93a97de88963" width=200/>    | <img src="https://github.com/ven-coder/Assists/assets/27257149/542e51d1-9f22-4381-b011-618fc0cef959" width=200/>

## 我的付费社群
付费社群提供的服务：
1. 完整易于阅读的开发文档（补充中...）
2. Assists开发指导
3. 开发疑难问答
4. 基于Assists开发的抖音养号，小红书养号，支付宝能量收集，支付宝农场，无线远程控制等源码（补充中...）
5. 群友互助资源对接

> 付费社群于2024.6.19刚刚创建，部分资料服务正在补充中，但是对于开发指导、疑难问答服务我会竭尽所能服务好大家
##### 微信扫扫即可加入
<img src="https://github.com/ven-coder/Assists/assets/27257149/7ae8e825-f489-46e3-96f0-ed03d12db9e8" width=200/>

##### 定制开发可直接联系个人微信：x39598

# License
[GNU General Public License v3.0](https://github.com/ven-coder/Assists/blob/master/LICENSE)
