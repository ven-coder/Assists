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
|类|描述|
|-|-|
|[AssistsService](#assistsservice)|AccessibilityService服务类|
|[AssistsCore](#assistscore)|基础类，对AccessibilityService API进行一系列的包装便于调用|
|[AssistsWindowManager](#assistswindowmanager)|浮窗管理，管理浮窗添加、删除、触控手势切换、隐藏、浮窗toast|
|[AssistsWindowWrapper](#assistswindowwrapper)|浮窗包装，对浮窗移动、缩放做统一包装|
## 进阶
|类|描述|
|-|-|
|[MPManager](#mpmanager)|屏幕录制管理，利用屏幕录制截取屏幕内容快捷获取图片，结合OpencvWrapper可以对图片进行识别操作等|
|[OpencvWrapper](#opencvwrapper)|Opencv包装，当前仅结合[MPManager](#mpmanager)做简单的模版匹配包装，可自行结合[Assists](#assistscore)、[MPManager](#mpmanager)做更深层包装实现基于机器视觉的自动化|
## 高级
|类|描述|
|-|-|
|[StepManager](#stepmanager)|步骤管理器，对于实现自动化脚本提供一个快速实现业务、可复用、易维护的步骤框架及管理器|
|[StepImpl](#stepimpl)|步骤实现类，用于实现自动化脚本时继承此类|

### AssistsService
无障碍服务核心类，负责处理无障碍服务的生命周期和事件分发，提供全局服务实例访问和监听器管理功能。

#### 重要属性

|属性|描述|
|-|-|
|`instance`|全局服务实例。用于在应用中获取无障碍服务实例，当服务未启动或被销毁时为null|
|`listeners`|服务监听器列表。使用线程安全的集合存储所有监听器，用于分发服务生命周期和无障碍事件|

#### 生命周期方法

|方法|描述|
|-|-|
|`onCreate()`|服务创建时调用，初始化全局服务实例|
|`onServiceConnected()`|服务连接成功时调用，初始化服务实例和窗口管理器，通知所有监听器服务已连接|
|`onAccessibilityEvent(event: AccessibilityEvent)`|接收无障碍事件，更新服务实例并分发事件给所有监听器|
|`onUnbind(intent: Intent?)`|服务解绑时调用，清除服务实例并通知所有监听器|
|`onInterrupt()`|服务中断时调用，通知所有监听器服务已中断|

### AssistsCore
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
- `path`：手势路径，使用Path对象定义手势轨迹
- `startTime`：指定手势的起始时间。如果设为0，表示立即开始手势
- `duration`：执行手势的持续时间（毫秒）

返回值：
- `Boolean`：手势是否执行成功
---

#### 获取当前元素在屏幕中的范围大小
`AccessibilityNodeInfo.getBoundsInScreen(): Rect`

返回值：
- `Rect`：返回一个Rect对象，包含元素在屏幕中的位置和大小信息（left, top, right, bottom）
---

#### 点击当前元素
`AccessibilityNodeInfo.click(): Boolean`

返回值：
- `Boolean`：点击操作是否执行成功
---

#### 长按当前元素
`AccessibilityNodeInfo.longClick(): Boolean`

返回值：
- `Boolean`：长按操作是否执行成功
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
- `x`：点击位置的x坐标
- `y`：点击位置的y坐标
- `duration`：点击持续时间（毫秒），默认为10毫秒

返回值：
- `Boolean`：手势是否执行成功
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
- `offsetX`：点击位置相对于元素左边界的偏移量，默认为屏幕宽度的1.953%
- `offsetY`：点击位置相对于元素上边界的偏移量，默认为屏幕宽度的1.953%
- `switchWindowIntervalDelay`：浮窗切换显示状态的延迟时间（毫秒），默认250毫秒
- `duration`：点击持续时间（毫秒），默认25毫秒

返回值：
- `Boolean`：手势是否执行成功
---

#### 返回
`back(): Boolean`

返回值：
- `Boolean`：返回操作是否执行成功
---

#### 回到主页
`home(): Boolean`

返回值：
- `Boolean`：回到主页操作是否执行成功
---

#### 显示通知栏
`notifications(): Boolean`

返回值：
- `Boolean`：显示通知栏操作是否执行成功
---

#### 显示最近任务列表
`recentApps(): Boolean`

返回值：
- `Boolean`：显示最近任务列表操作是否执行成功
---

#### 粘贴文本到当前元素
`AccessibilityNodeInfo.paste(text: String?): Boolean`

参数：
- `text`：要粘贴的文本内容

返回值：
- `Boolean`：粘贴操作是否执行成功
---

#### 选择当前元素的文本
`AccessibilityNodeInfo.selectionText(selectionStart: Int, selectionEnd: Int): Boolean`

参数：
- `selectionStart`：选择文本的起始位置
- `selectionEnd`：选择文本的结束位置

返回值：
- `Boolean`：文本选择操作是否执行成功
---

#### 修改当前元素文本
`AccessibilityNodeInfo.setNodeText(text: String?): Boolean`

参数：
- `text`：要设置的新文本内容

返回值：
- `Boolean`：文本修改操作是否执行成功
---

#### 向前滚动（元素需要是可滚动的）
`AccessibilityNodeInfo.scrollForward(): Boolean`

返回值：
- `Boolean`：向前滚动操作是否执行成功，false可作为滚动到底部的判断依据
---

#### 向后滚动（元素需要是可滚动的）
`AccessibilityNodeInfo.scrollBackward(): Boolean`

返回值：
- `Boolean`：向后滚动操作是否执行成功，false可作为滚动到顶部的判断依据
---

#### 在控制台输出当前元素信息
`AccessibilityNodeInfo.logNode(tag: String = LOG_TAG)`

参数：
- `tag`：日志标签，默认为LOG_TAG（"assists_log"）

---

### AssistsWindowManager
浮窗管理器，提供全局浮窗的添加、删除、显示、隐藏等管理功能。

#### 重要属性

|属性|描述|
|-|-|
|`windowManager`|系统窗口管理器|
|`mDisplayMetrics`|显示度量信息|
|`viewList`|浮窗视图列表，使用线程安全的集合|

#### 核心方法

|方法|描述|
|-|-|
|`init(accessibilityService: AccessibilityService)`|初始化窗口管理器|
|`getWindowManager()`|获取系统窗口管理器实例|
|`createLayoutParams()`|创建默认的浮窗布局参数|
|`hideAll(isTouchable: Boolean = true)`|隐藏所有浮窗|
|`hideTop(isTouchable: Boolean = true)`|隐藏最顶层浮窗|
|`showTop(isTouchable: Boolean = true)`|显示最顶层浮窗|
|`showAll(isTouchable: Boolean = true)`|显示所有浮窗|
|`add(windowWrapper: AssistsWindowWrapper?, isStack: Boolean = true, isTouchable: Boolean = true)`|添加浮窗包装器|
|`add(view: View?, layoutParams: WindowManager.LayoutParams, isStack: Boolean = true, isTouchable: Boolean = true)`|添加浮窗视图|
|`push(view: View?, params: WindowManager.LayoutParams)`|添加浮窗并隐藏之前的浮窗|
|`pop(showTop: Boolean = true)`|移除最顶层浮窗并显示下一个浮窗|
|`removeView(view: View?)`|移除指定浮窗|
|`contains(view: View?)`|检查指定视图是否已添加为浮窗|
|`contains(wrapper: AssistsWindowWrapper?)`|检查指定浮窗包装器是否已添加|
|`isVisible(view: View)`|检查指定浮窗是否可见|
|`updateViewLayout(view: View, params: ViewGroup.LayoutParams)`|更新浮窗布局|
|`touchableByAll()`|设置所有浮窗为可触摸状态|
|`nonTouchableByAll()`|设置所有浮窗为不可触摸状态|

#### 扩展方法

|方法|描述|
|-|-|
|`WindowManager.LayoutParams.touchableByLayoutParams()`|设置布局参数为可触摸状态|
|`WindowManager.LayoutParams.nonTouchableByLayoutParams()`|设置布局参数为不可触摸状态|
|`ViewWrapper.touchableByWrapper()`|设置浮窗包装器为可触摸状态|
|`ViewWrapper.nonTouchableByWrapper()`|设置浮窗包装器为不可触摸状态|
|`String.overlayToast(delay: Long = 2000)`|显示一个临时的Toast样式浮窗|

#### ViewWrapper类
浮窗视图包装类，用于管理浮窗视图及其布局参数

|属性|描述|
|-|-|
|`view`|浮窗视图|
|`layoutParams`|布局参数|

### AssistsWindowWrapper
浮窗包装类，为浮窗提供统一的外观和交互行为，包括：
1. 可拖动移动位置
2. 可缩放大小
3. 可关闭
4. 支持自定义初始位置和大小限制

#### 构造参数

|参数|描述|
|-|-|
|`view`|要包装的视图|
|`wmLayoutParams`|窗口布局参数，可选|
|`onClose`|关闭回调函数，可选|

#### 属性配置

|属性|描述|默认值|
|-|-|-|
|`minHeight`|最小高度限制|-1（无限制）|
|`minWidth`|最小宽度限制|-1（无限制）|
|`maxHeight`|最大高度限制|-1（无限制）|
|`maxWidth`|最大宽度限制|-1（无限制）|
|`initialX`|初始X坐标|0|
|`initialY`|初始Y坐标|0|
|`initialXOffset`|X轴偏移量|0|
|`initialYOffset`|Y轴偏移量|0|
|`initialCenter`|是否初始居中显示|false|
|`showOption`|是否显示操作按钮（移动、缩放、关闭）|true|
|`showBackground`|是否显示背景|true|
|`wmlp`|窗口布局参数|默认布局参数|

#### 核心方法

|方法|描述|
|-|-|
|`ignoreTouch()`|设置浮窗为不可触摸状态，此状态下浮窗将忽略所有触摸事件|
|`consumeTouch()`|设置浮窗为可触摸状态，此状态下浮窗可以响应触摸事件|
|`getView()`|获取浮窗的根视图|

#### 内部实现

##### 触摸事件监听器

1. 缩放触摸事件监听器（onTouchScaleListener）
   - 处理浮窗的缩放操作
   - 记录初始触摸位置和布局尺寸
   - 根据触摸移动计算新的宽高
   - 应用尺寸限制条件

2. 移动触摸事件监听器（onTouchMoveListener）
   - 处理浮窗的拖动移动操作
   - 根据触摸位置更新浮窗坐标
   - 考虑状态栏高度的偏移

##### 视图绑定（viewBinding）
- 初始化浮窗的布局和行为
- 处理初始位置和显示
- 设置移动、缩放和关闭按钮的事件监听
- 根据配置显示或隐藏操作按钮和背景

### MPManager
屏幕录制管理器，负责处理屏幕录制相关的功能，包括权限请求、截图和图像处理。

#### 重要属性

|属性|描述|
|-|-|
|`REQUEST_CODE`|媒体投影请求码|
|`REQUEST_DATA`|媒体投影请求数据|
|`requestLaunchers`|存储Activity和其对应的结果启动器映射|
|`onEnable`|服务启用回调|
|`isEnable`|屏幕录制是否已启用|

#### 核心方法

|方法|描述|
|-|-|
|`init(application: Application)`|初始化管理器|
|`request(autoAllow: Boolean = true, timeOut: Long = 5000)`|请求屏幕录制权限|
|`takeScreenshot2Bitmap()`|获取当前屏幕截图|
|`takeScreenshot2File(file: File)`|将当前屏幕截图保存到文件|

#### *拓展*方法

|方法|描述|
|-|-|
|`AccessibilityNodeInfo.getBitmap(screenshot: Bitmap)`|获取指定元素的截图|
|`AccessibilityNodeInfo.takeScreenshot2File(screenshot: Bitmap, file: File)`|将指定元素的截图保存到文件|

#### 使用示例

1. 请求屏幕录制权限
```kotlin
// 请求权限，自动处理权限弹窗
val success = MPManager.request(autoAllow = true)
```

2. 获取屏幕截图
```kotlin
// 获取当前屏幕的Bitmap
val bitmap = MPManager.takeScreenshot2Bitmap()

// 保存截图到文件
val file = MPManager.takeScreenshot2File()
```

3. 获取元素截图
```kotlin
// 获取某个元素的Bitmap
val screenshot = MPManager.takeScreenshot2Bitmap()
val elementBitmap = element.getBitmap(screenshot)

// 保存元素截图到文件
val file = element.takeScreenshot2File(screenshot)
```

#### 注意事项

1. 在使用截图相关功能前，需要先请求并获取屏幕录制权限
2. 权限请求支持自动处理系统弹窗，也可以手动处理
3. 文件保存默认使用应用内部文件目录，文件名包含时间戳
4. 所有涉及文件操作的方法都可能返回null，需要进行空值检查

### OpencvWrapper
OpenCV包装器，提供图像处理和模板匹配等计算机视觉功能的封装，主要用于自动化过程中的图像识别和处理。

#### 核心方法

|方法|描述|
|-|-|
|`init()`|初始化OpenCV库|
|`matchTemplate(image: Mat?, template: Mat?, mask: Mat?)`|执行模板匹配操作，使用标准化相关系数匹配方法|
|`getResultWithThreshold(result: Mat, threshold: Double, ignoreX: Double, ignoreY: Double)`|从匹配结果中获取符合阈值的点位置|
|`matchTemplateFromScreenToMinMaxLoc(image: Mat?, template: Mat?, mask: Mat?)`|执行模板匹配并返回最佳匹配位置|
|`createMask(source: Mat, lowerScalar: Scalar, upperScalar: Scalar, requisiteExtraRectList: List<Rect>, redundantExtraRectList: List<Rect>)`|创建图像掩膜，基于HSV颜色空间的阈值分割|
|`getScreenMat()`|获取当前屏幕的Mat对象|
|`getTemplateFromAssets(assetPath: String)`|从Assets资源中加载模板图像|

#### 使用示例

1. 基本模板匹配
```kotlin
// 获取屏幕内容
val screenMat = OpencvWrapper.getScreenMat()
// 加载模板图像
val templateMat = OpencvWrapper.getTemplateFromAssets("template.png")
// 执行模板匹配
val result = OpencvWrapper.matchTemplate(screenMat, templateMat)
```

2. 带阈值的模板匹配
```kotlin
// 执行匹配并获取结果
val result = OpencvWrapper.matchTemplate(screenMat, templateMat)
// 获取符合阈值的匹配点
val points = OpencvWrapper.getResultWithThreshold(
    result,
    threshold = 0.9,  // 匹配阈值
    ignoreX = 50.0,   // X轴忽略距离
    ignoreY = 50.0    // Y轴忽略距离
)
```

3. 使用掩膜的模板匹配
```kotlin
// 创建掩膜
val mask = OpencvWrapper.createMask(
    source = templateMat,
    lowerScalar = Scalar(0.0, 0.0, 0.0),  // HSV下限
    upperScalar = Scalar(180.0, 255.0, 255.0),  // HSV上限
    requisiteExtraRectList = listOf(),  // 必要区域
    redundantExtraRectList = listOf()   // 冗余区域
)
// 执行带掩膜的模板匹配
val result = OpencvWrapper.matchTemplate(screenMat, templateMat, mask)
```

#### 注意事项

1. 使用前需要先初始化OpenCV库
2. 模板匹配支持掩膜操作，可以指定匹配区域
3. 获取匹配结果时可以设置忽略距离，避免相近位置重复匹配
4. 所有涉及图像操作的方法都需要注意空值处理
5. 图像掩膜创建支持HSV颜色空间的阈值分割，可以更精确地控制匹配区域

### StepManager
步骤管理器，用于管理和执行自动化步骤的核心类。提供步骤注册、执行、监听等功能，支持协程异步执行和步骤间延时控制。

#### 重要属性

|属性|描述|
|-|-|
|`DEFAULT_STEP_DELAY`|步骤默认间隔时长（毫秒），用于控制连续步骤执行之间的默认等待时间|
|`stepListeners`|步骤监听器，用于监听步骤执行的状态和进度|
|`stepCollector`|步骤收集器映射，存储所有已注册的步骤实现类及其对应的步骤收集器|
|`coroutine`|协程作用域，提供步骤异步执行的协程环境|
|`isStop`|控制停止操作的变量，当设置为true时，将取消步骤器下的所有步骤|

#### 核心方法

|方法|描述|
|-|-|
|`execute(stepImpl: Class<T>, stepTag: Int, delay: Long, data: Any?, begin: Boolean)`|执行指定步骤，支持通过Class对象指定步骤实现类|
|`execute(implClassName: String, stepTag: Int, delay: Long, data: Any?, begin: Boolean)`|执行指定步骤，支持通过类名字符串指定步骤实现类|
|`register(implClassName: String)`|注册步骤实现类，创建步骤收集器并初始化步骤实现类|

#### 使用示例

1. 注册和执行步骤
```kotlin
// 执行步骤
StepManager.execute(
    MyStepImpl::class.java,  // 步骤实现类
    stepTag = 1,             // 步骤标识
    delay = 1000,            // 延迟执行时间
    data = "some data",      // 步骤数据
    begin = true             // 作为起始步骤
)
```

2. 停止步骤执行
```kotlin
// 设置停止标志，将取消所有正在执行的步骤
StepManager.isStop = true
```

#### 注意事项

1. 执行步骤前需要先注册对应的步骤实现类
2. 步骤执行支持延迟和数据传递
3. 可以通过begin参数指定是否作为起始步骤（会重置isStop标志）
4. 步骤执行在协程中进行，支持异步操作

### StepImpl
步骤实现基类，用于实现自动化脚本的基础类，提供步骤实现的框架和协程执行环境。继承此类的子类需要实现onImpl方法来定义具体的步骤逻辑。

#### 核心方法

|方法|描述|
|-|-|
|`onImpl(collector: StepCollector)`|步骤实现方法，子类必须实现此方法来定义具体的步骤逻辑|
|`runIO(function: suspend () -> Unit)`|在IO线程执行任务，适用于执行IO操作、网络请求等耗时任务|
|`runMain(function: suspend () -> Unit)`|在主线程执行任务，适用于执行UI操作、更新界面等任务|

#### 使用示例

1. 创建步骤实现类
```kotlin
class MyStepImpl : StepImpl() {
    override fun onImpl(collector: StepCollector) {
        // 注册步骤1
        collector.collect(1) { operator ->
            // 在IO线程执行耗时操作
            runIO {
                // 执行耗时任务
            }
            
            // 在主线程更新UI
            runMain {
                // 更新界面
            }
            
            // 返回下一步
            Step.next(2)
        }
        
        // 注册步骤2
        collector.collect(2) { operator ->
            // 步骤逻辑
            Step.none
        }
    }
}
```

#### 注意事项

1. 子类必须实现onImpl方法来定义步骤逻辑
2. 使用runIO执行耗时操作，避免阻塞主线程
3. 使用runMain执行UI相关操作，确保在主线程更新界面
4. 每个步骤都需要返回一个Step对象来指示下一步操作