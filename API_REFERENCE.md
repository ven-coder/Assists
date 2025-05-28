# Assists API 参考文档

本文档提供了Assists框架中所有可用API的详细参考信息。

## AssistsCore 核心API

AssistsCore是Assists框架的核心类，提供了所有主要的无障碍服务功能。

### 初始化和服务管理

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `init(application: Application)` | 初始化AssistsCore | 无 |
| `openAccessibilitySetting()` | 打开系统无障碍服务设置页面 | 无 |
| `isAccessibilityServiceEnabled()` | 检查无障碍服务是否已开启 | Boolean |
| `getPackageName()` | 获取当前窗口所属的应用包名 | String |

### 元素查找

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `findById(id, filterText?, filterDes?, filterClass?)` | 通过id查找所有符合条件的元素 | List<AccessibilityNodeInfo> |
| `AccessibilityNodeInfo.findById(id, filterText?, filterDes?, filterClass?)` | 在指定元素范围内通过id查找元素 | List<AccessibilityNodeInfo> |
| `findByText(text, filterViewId?, filterDes?, filterClass?)` | 通过文本内容查找所有符合条件的元素 | List<AccessibilityNodeInfo> |
| `findByTextAllMatch(text, filterViewId?, filterDes?, filterClass?)` | 查找所有文本完全匹配的元素 | List<AccessibilityNodeInfo> |
| `AccessibilityNodeInfo.findByText(text, filterViewId?, filterDes?, filterClass?)` | 在指定元素范围内通过文本查找元素 | List<AccessibilityNodeInfo> |
| `findByTags(className, viewId?, text?, des?)` | 根据多个条件查找元素 | List<AccessibilityNodeInfo> |
| `AccessibilityNodeInfo.findByTags(className, viewId?, text?, des?)` | 在指定元素范围内根据多个条件查找元素 | List<AccessibilityNodeInfo> |
| `getAllNodes(filterViewId?, filterDes?, filterClass?, filterText?)` | 获取当前窗口中的所有元素 | List<AccessibilityNodeInfo> |

### 元素类型判断

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `AccessibilityNodeInfo.isFrameLayout()` | 判断元素是否是FrameLayout | Boolean |
| `AccessibilityNodeInfo.isViewGroup()` | 判断元素是否是ViewGroup | Boolean |
| `AccessibilityNodeInfo.isView()` | 判断元素是否是View | Boolean |
| `AccessibilityNodeInfo.isImageView()` | 判断元素是否是ImageView | Boolean |
| `AccessibilityNodeInfo.isTextView()` | 判断元素是否是TextView | Boolean |
| `AccessibilityNodeInfo.isLinearLayout()` | 判断元素是否是LinearLayout | Boolean |
| `AccessibilityNodeInfo.isRelativeLayout()` | 判断元素是否是RelativeLayout | Boolean |
| `AccessibilityNodeInfo.isButton()` | 判断元素是否是Button | Boolean |
| `AccessibilityNodeInfo.isImageButton()` | 判断元素是否是ImageButton | Boolean |
| `AccessibilityNodeInfo.isEditText()` | 判断元素是否是EditText | Boolean |

### 元素信息获取

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `AccessibilityNodeInfo.txt()` | 获取元素的文本内容 | String |
| `AccessibilityNodeInfo.des()` | 获取元素的描述内容 | String |
| `AccessibilityNodeInfo.getAllText()` | 获取元素的所有文本内容（包括text和contentDescription） | ArrayList<String> |
| `AccessibilityNodeInfo.containsText(text)` | 判断元素是否包含指定文本 | Boolean |
| `AccessibilityNodeInfo.getBoundsInScreen()` | 获取元素在屏幕中的位置信息 | Rect |
| `AccessibilityNodeInfo.getBoundsInParent()` | 获取元素在父容器中的位置信息 | Rect |
| `AccessibilityNodeInfo.isVisible(compareNode?, isFullyByCompareNode?)` | 判断元素是否可见 | Boolean |

### 元素层级操作

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `AccessibilityNodeInfo.getNodes()` | 获取指定元素下的所有子元素 | ArrayList<AccessibilityNodeInfo> |
| `AccessibilityNodeInfo.getChildren()` | 获取元素的直接子元素 | ArrayList<AccessibilityNodeInfo> |
| `AccessibilityNodeInfo.findFirstParentByTags(className)` | 查找第一个符合指定类型的父元素 | AccessibilityNodeInfo? |
| `AccessibilityNodeInfo.findFirstParentClickable()` | 查找元素的第一个可点击的父元素 | AccessibilityNodeInfo? |

### 元素操作

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `AccessibilityNodeInfo.click()` | 点击元素 | Boolean |
| `AccessibilityNodeInfo.longClick()` | 长按元素 | Boolean |
| `AccessibilityNodeInfo.paste(text)` | 向元素粘贴文本 | Boolean |
| `AccessibilityNodeInfo.setNodeText(text)` | 设置元素的文本内容 | Boolean |
| `AccessibilityNodeInfo.selectionText(selectionStart, selectionEnd)` | 选择元素中的文本 | Boolean |
| `AccessibilityNodeInfo.scrollForward()` | 向前滚动可滚动元素 | Boolean |
| `AccessibilityNodeInfo.scrollBackward()` | 向后滚动可滚动元素 | Boolean |

### 手势操作

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `gestureClick(x, y, duration?)` | 在指定坐标位置执行点击手势 | Boolean (suspend) |
| `AccessibilityNodeInfo.nodeGestureClick(offsetX?, offsetY?, switchWindowIntervalDelay?, duration?)` | 在元素位置执行点击手势 | Boolean (suspend) |
| `AccessibilityNodeInfo.nodeGestureClickByDouble(offsetX?, offsetY?, switchWindowIntervalDelay?, clickDuration?, clickInterval?)` | 在元素位置执行双击手势 | Boolean (suspend) |
| `gesture(startLocation, endLocation, startTime, duration)` | 执行点击或滑动手势 | Boolean (suspend) |
| `gesture(path, startTime, duration)` | 执行自定义路径的手势 | Boolean (suspend) |
| `dispatchGesture(gesture, nonTouchableWindowDelay?)` | 执行手势操作 | Boolean (suspend) |

### 系统操作

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `back()` | 执行返回操作 | Boolean |
| `home()` | 返回主屏幕 | Boolean |
| `notifications()` | 打开通知栏 | Boolean |
| `recentApps()` | 显示最近任务 | Boolean |

### 应用启动

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `launchApp(intent)` | 通过Intent启动应用 | Boolean (suspend) |
| `launchApp(packageName)` | 通过包名启动应用 | Boolean (suspend) |

### 屏幕截图 (Android R+)

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `takeScreenshot()` | 截取整个屏幕 | Bitmap? (suspend) |
| `takeScreenshotSave(file?, format?)` | 截取整个屏幕并保存到文件 | File? (suspend) |
| `AccessibilityNodeInfo.takeScreenshot()` | 截取指定元素的屏幕截图 | Bitmap? (suspend) |
| `AccessibilityNodeInfo.takeScreenshotSave(file?, format?)` | 截取指定元素的屏幕截图并保存到文件 | File? (suspend) |

### 坐标计算

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `getX(baseWidth, x)` | 根据基准宽度计算实际X坐标 | Int |
| `getY(baseHeight, y)` | 根据基准高度计算实际Y坐标 | Int |
| `getAppBoundsInScreen()` | 获取当前应用在屏幕中的位置 | Rect? |
| `initAppBoundsInScreen()` | 初始化并缓存当前应用在屏幕中的位置 | Rect? |
| `getAppWidthInScreen()` | 获取当前应用在屏幕中的宽度 | Int |
| `getAppHeightInScreen()` | 获取当前应用在屏幕中的高度 | Int |

### 调试工具

| 方法名 | 说明 | 返回值 |
|--------|------|--------|
| `AccessibilityNodeInfo.logNode(tag?)` | 在日志中输出元素的详细信息 | 无 |

## 使用说明

### 重要注意事项

- **可选参数**：带 `?` 的参数表示可选参数
- **协程方法**：标记为 `suspend` 的方法需要在协程中调用
- **版本要求**：标记为 `Android R+` 的方法需要Android 11及以上版本
- **扩展函数**：`AccessibilityNodeInfo.xxx()` 表示扩展函数，需要在AccessibilityNodeInfo实例上调用

## 其他API

更多API正在整理中，包括：
- StepManager（步骤管理器）
- AssistsWindowManager（浮窗管理器）
- AssistsService（无障碍服务）
- 其他工具类API


## 💝 支持开源

开源不易，您的支持是我坚持的动力！

如果Assists框架对您的项目有帮助，可以通过以下方式支持我喔：

### ⭐ Star支持
- 给项目点个Star，让更多开发者发现这个框架
- 分享给身边的朋友和同事

### 💰 赞助支持
- [爱发电支持](https://afdian.com/a/vencoder) - 您的每一份支持都是我们前进的动力
- 加入付费社群获得更多技术支持和源码资源
- 一杯Coffee的微信赞赏
<img width="200" alt="image" src="https://github.com/user-attachments/assets/3862a40c-631c-4ab0-b1e7-00ec3e3e00ad" />


### 📞 联系我
- 个人微信：x39598

**感谢所有的支持者，得到你们的支持我将会更加完善开源库的能力！** 🚀

---

> 如有疑问或发现文档错误，欢迎提交Issue或联系开发者。 
