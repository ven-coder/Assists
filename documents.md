# Assists作用
基于Android无障碍服务（AccessibilityService）
1. 简化自动化脚本开发
2. 为自动化脚本提供各种增强能力
3. 提高脚本易维护性

# 主要能力
1. 易于使用的无障碍服务API
2. 浮窗管理器：易于实现及管理浮窗
3. 步骤器：为快速实现、可复用、易维护的自动化步骤提供框架及管理
4. 配套屏幕管理：快速生成输出屏幕截图、元素截图
5. 屏幕管理结合opencv：便于屏幕内容识别为自动化提供服务

> ***本库为Android原生库，纯kotlin语言***

# 示例截图

| 基础示例 | 进阶示例 | 高级示例 |
| - | - |-|
| <img src="https://github.com/user-attachments/assets/b537bab4-cc55-41c2-8f81-9e8b965e939a" width=200/> | <img src="https://github.com/user-attachments/assets/9b50628c-603e-47d1-a6ae-5600358575fc" width=200/> |<img src="https://github.com/user-attachments/assets/262b9028-5926-478b-93bd-3e20110db391" width=200/>|

| 图像识别支付宝自动收能量 |自动发朋友圈|自动滑动朋友圈| 无障碍服务开启引导 |
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
    implementation "com.github.ven-coder.Assists:assists-base:v3.2.0"
    //屏幕录制相关（可选）
    implementation "com.github.ven-coder.Assists:assists-mp:v3.2.0"
    //opencv相关（可选）
    implementation "com.github.ven-coder.Assists:assists-opcv:v3.2.0"
    
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
            android:name="com.ven.assist.AssistsService"
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
        <!-- 添加代码 ↑-->
    </application>

</manifest>
```
#### 1.2 开启服务
调用```Assists.openAccessibilitySetting()```跳转到无障碍服务设置页面，找到对应的应用开启服务。
服务开启后执行以下API测试是否成功集成：
```
Assists.getAllNodes().forEach { it.logNode() }
```
这段代码是获取当前页面所有节点元素的基本信息在Logcat（tag：assists_log）打印出来，如下图：

<img src="https://github.com/user-attachments/assets/81725dc3-d924-44f4-89fe-75938ae659e9" width=350/>

至此，已成功集成Assists。如果没有任何输出请检查集成步骤是否正确。

# API

## 基础
### Assists

> *通过Assists对界面进行操作和获取信息，需要先查看界面有哪些元素、元素的结构、元素信息，有了这些才能通过Assists去获取元素并操作。如何查看可参考[这里](https://blog.csdn.net/weixin_37496178/article/details/138328871?spm=1001.2014.3001.5502)*

|方法|描述|
|-|-|
|[openAccessibilitySetting](#打开无障碍服务设置页)|打开无障碍服务设置页|
|[isAccessibilityServiceEnabled](#检查无障碍服务是否开启)|检查无障碍服务是否开启|
|[getPackageName](#获取当前窗口所属包名)|获取当前窗口所属包名|
|[findById](#通过id查找所有符合条件的元素)|通过id查找所有符合条件的元素|
|[findByText](#通过文本查找所有符合条件元素)|通过文本查找所有符合条件元素|
|[findByTextAllMatch](#查找所有与指定文本相同的元素)|查找所有与指定文本相同的元素|
|[findByTags](#根据指定条件查找元素)|根据指定条件查找元素|
|[getAllNodes](#获取当前页面所有节点元素)|获取当前页面所有节点元素|
|[dispatchGesture](#执行手势)|执行手势|
|[gesture](#执行手势（点或直线）)|执行手势（点或直线）|
|[gesture](#执行手势（根据路径）)|执行手势（根据路径）|
|[gestureClick](#根据坐标执行手势)|根据坐标执行手势|
|[back](#返回)|返回|
|[home](#回到主页)|回到主页|
|[notifications](#显示通知栏)|显示通知栏|
|[recentApps](#显示最近任务列表)|显示最近任务列表|


|*拓展*方法|描述|
|-|-|
|[AccessibilityNodeInfo?.findById](#通过id查找当前元素范围下所有符合条件元素)|通过id查找当前元素范围下所有符合条件元素|
|[AccessibilityNodeInfo?.findByText](#查找当前元素范围下所有与指定文本相同的元素)|查找当前元素范围下所有与指定文本相同的元素|
|[AccessibilityNodeInfo?.containsText](#判断当前元素是否包含指定的文本)|判断当前元素是否包含指定的文本|
|[AccessibilityNodeInfo?.getAllText](#获取当前元素范围下所有文本)|获取当前元素范围下所有文本|
|[AccessibilityNodeInfo.findByTags](#在当前元素范围下根据指定条件查找元素)|在当前元素范围下根据指定条件查找元素|
|[AccessibilityNodeInfo.findFirstParentByTags](#在当前元素范围下查找首个符合指定类型的父元素)|在当前元素范围下查找首个符合指定类型的父元素|
|[AccessibilityNodeInfo.getNodes](#获取当前元素范围下所有元素)|获取当前元素范围下所有元素|
|[AccessibilityNodeInfo.findFirstParentClickable](#查找当前元素首个可点击的父元素)|查找当前元素首个可点击的父元素|
|[AccessibilityNodeInfo.getChildren](#获取当前元素下的所有子元素（不包括子元素中的子元素）)|获取当前元素下的所有子元素（不包括子元素中的子元素）|
|[AccessibilityNodeInfo.getBoundsInScreen](#获取当前元素在屏幕中的范围大小)|获取当前元素在屏幕中的范围大小|
|[AccessibilityNodeInfo.click](#点击当前元素)|点击当前元素|
|[AccessibilityNodeInfo.longClick](#长按当前元素)|长按当前元素|
|[AccessibilityNodeInfo.nodeGestureClick](#在当前元素范围下执行点击手势)|在当前元素范围下执行点击手势|
|[AccessibilityNodeInfo.paste](#粘贴文本到当前元素)|粘贴文本到当前元素|
|[AccessibilityNodeInfo.selectionText](#选择当前元素的文本)|选择当前元素的文本|
|[AccessibilityNodeInfo.setNodeText](#修改当前元素文本)|修改当前元素文本|
|[AccessibilityNodeInfo.scrollForward](#向前滚动)|向前滚动|
|[AccessibilityNodeInfo.scrollBackward](#向后滚动)|向后滚动|
|[AccessibilityNodeInfo.logNode](#在控制台输出当前元素信息)|在控制台输出当前元素信息|


#### 打开无障碍服务设置页
`openAccessibilitySetting()`

---

#### 检查无障碍服务是否开启
`isAccessibilityServiceEnabled(): Boolean`

返回值：
- `Boolean`：true-已开启，false-未开启
---

#### 获取当前窗口所属包名
`getPackageName(): String`

返回值：
- `String`：当前窗口所属包名
  
---

#### 通过id查找所有符合条件的元素
`findById(id: String, text: String? = null): List<AccessibilityNodeInfo>`

参数：
- `id`：元素id，通过uiautomatorviewer或其他工具获取到的resource-id。如何获取参考[这里](https://blog.csdn.net/weixin_37496178/article/details/138328871?spm=1001.2014.3001.5502)
- `text`：返回符合指定文本的元素。默认为空

返回值：
- `List<AccessibilityNodeInfo>`：所有符合条件的元素列表

---

#### 通过id查找当前元素范围下所有符合条件元素
`AccessibilityNodeInfo?.findById(id: String): List<AccessibilityNodeInfo>`
> *kotlin扩展函数*

参数：
- `id`：元素id，通过uiautomatorviewer或其他工具获取到的resource-id。如何获取参考[这里](https://blog.csdn.net/weixin_37496178/article/details/138328871?spm=1001.2014.3001.5502)

返回值：
- `List<AccessibilityNodeInfo>`：所有符合条件的元素列表

---

#### 查找所有符合指定文本条件的元素
`findByText(text: String): List<AccessibilityNodeInfo>`

参数：
- `text`：指定的文本

返回值：
- `List<AccessibilityNodeInfo>`：所有符合条件的元素列表

---

#### 查找所有与指定文本相同的元素
`findByTextAllMatch(text: String): List<AccessibilityNodeInfo>`

参数：
- `text`：指定的文本

返回值：
- `List<AccessibilityNodeInfo>`：所有符合条件的元素列表
---

#### 查找当前元素范围下所有与指定文本相同的元素
`AccessibilityNodeInfo?.findByText(text: String): List<AccessibilityNodeInfo>`
参数：
- `text`：指定的文本

返回值：
- `List<AccessibilityNodeInfo>`：所有符合条件的元素列表
---

#### 判断当前元素是否包含指定的文本
`AccessibilityNodeInfo?.containsText(text: String): Boolean`

参数：
- `text`：指定的文本

返回值：
- `Boolean`：true-包含，false-不包含
  
---

#### 获取当前元素范围下所有文本
`AccessibilityNodeInfo?.getAllText(): ArrayList<String>`

返回值：
- `ArrayList<String>`：当前元素范围下所有文本

---

#### 根据指定条件查找元素
`findByTags(className: String, viewId: String? = null, text: String? = null, des: String? = null): List<AccessibilityNodeInfo>`

参数：
- `className`：元素类型
- `viewId`：元素id
- `text`：元素文本
- `des`：元素描述文本

返回值：
- `List<AccessibilityNodeInfo>`：符合条件的元素列表
  
---

#### 在当前元素范围下根据指定条件查找元素
```
 AccessibilityNodeInfo.findByTags(
        className: String,
        viewId: String? = null,
        text: String? = null,
        des: String? = null
    ): List<AccessibilityNodeInfo>
```

参数：
- `className`：元素类型
- `viewId`：元素id
- `text`：元素文本
- `des`：元素描述文本

返回值：
- `List<AccessibilityNodeInfo>`：符合条件的元素列表

---

#### 在当前元素范围下查找首个符合指定类型的父元素
`AccessibilityNodeInfo.findFirstParentByTags(className: String): AccessibilityNodeInfo?`

参数：
- `className`：元素类型

返回值：
- `AccessibilityNodeInfo?`：查找到的父元素

---

#### 获取当前页面所有节点元素
`getAllNodes(): ArrayList<AccessibilityNodeInfo>`

返回值：
- `ArrayList<AccessibilityNodeInfo>`：当前页面所有节点元素

---

#### 获取当前元素范围下所有元素
`AccessibilityNodeInfo.getNodes(): ArrayList<AccessibilityNodeInfo>`

返回值：
- `ArrayList<AccessibilityNodeInfo>`：当前元素范围下所有元素

---

#### 查找当前元素首个可点击的父元素
`AccessibilityNodeInfo.findFirstParentClickable(): AccessibilityNodeInfo?`

返回值：
- `AccessibilityNodeInfo?`：可点击的元素
---

#### 获取当前元素下的所有子元素（不包括子元素中的子元素）
`AccessibilityNodeInfo.getChildren(): ArrayList<AccessibilityNodeInfo>`

返回值：
- `ArrayList<AccessibilityNodeInfo>`：当前元素下的所有子元素

---

#### 执行手势
```
    dispatchGesture(
        gesture: GestureDescription,
        nonTouchableWindowDelay: Long = 100,
    ): Boolean
```

参数：
- `gesture`：手势描述
- `nonTouchableWindowDelay`：将浮窗设置为不可触控后等待执行手势的时间

返回值：
- `Boolean`：手势是否执行成功
---

#### 执行手势（点或直线）
```
gesture(
        startLocation: FloatArray,
        endLocation: FloatArray,
        startTime: Long,
        duration: Long,
    ): Boolean
```

参数：
- `startLocation`：开始执行手势的坐标点
- `endLocation`：结束执行手势的坐标点
- `startTime`：指定手势的 起始时间。如果设为 0，表示 立即 开始手势，如果设为 SystemClock.uptimeMillis() + 1000，则表示手势将在 1 秒后 开始。
- `duration`：执行手势的持续时间

返回值：
- `Boolean`：手势是否执行成功
---

#### 执行手势（根据路径）
```
gesture(
        path: Path,
        startTime: Long,
        duration: Long,
    ): Boolean
```

参数：
- ``：

返回值：
- ``：
---

#### 获取当前元素在屏幕中的范围大小
`AccessibilityNodeInfo.getBoundsInScreen(): Rect`

参数：
- ``：

返回值：
- ``：
---

#### 点击当前元素
`AccessibilityNodeInfo.click(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 长按当前元素
`AccessibilityNodeInfo.longClick(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 根据坐标执行手势
```
gestureClick(
        x: Float,
        y: Float,
        duration: Long = 10
    ): Boolean
```

参数：
- ``：

返回值：
- ``：
---

#### 在当前元素范围下执行点击手势
```
AccessibilityNodeInfo.nodeGestureClick(
        offsetX: Float = ScreenUtils.getScreenWidth() * 0.01953f,
        offsetY: Float = ScreenUtils.getScreenWidth() * 0.01953f,
        switchWindowIntervalDelay: Long = 250,
        duration: Long = 25
    ): Boolean
```

参数：
- ``：

返回值：
- ``：
---

#### 返回
`back(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 回到主页
`home(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 显示通知栏
`notifications(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 显示最近任务列表
`recentApps(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 粘贴文本到当前元素
`AccessibilityNodeInfo.paste(text: String?): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 选择当前元素的文本
`AccessibilityNodeInfo.selectionText(selectionStart: Int, selectionEnd: Int): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 修改当前元素文本
`AccessibilityNodeInfo.setNodeText(text: String?): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 向前滚动（元素需要是可滚动的）
`AccessibilityNodeInfo.scrollForward(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 向后滚动（元素需要是可滚动的）
`AccessibilityNodeInfo.scrollBackward(): Boolean`

参数：
- ``：

返回值：
- ``：
---

#### 在控制台输出当前元素信息
`AccessibilityNodeInfo.logNode(tag: String = LOG_TAG)`

参数：
- ``：

返回值：
- ``：
---