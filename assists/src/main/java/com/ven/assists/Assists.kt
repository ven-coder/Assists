package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Assists {

    //日志TAG
    var LOG_TAG = "assists_log"


    private var job = Job()

    //协程域
    var coroutine: CoroutineScope = CoroutineScope(job + Dispatchers.IO)
        private set
        get() {
            if (job.isCancelled || !job.isActive) {
                job = Job()
                field = CoroutineScope(job + Dispatchers.IO)
            }
            return field
        }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return coroutine.launch(block = block)
    }


    /**
     * 无障碍服务，未开启前为null，使用注意判空
     */
    @JvmStatic
    var service: AssistsService? = null

    val serviceListeners: ArrayList<AssistsServiceListener> = arrayListOf()

    private var appRectInScreen: Rect? = null

    var mediaProjectionService: MediaProjectionService? = null
        set(value) {
            field = value
            if (value != null) {
                serviceListeners.forEach { it.screenCaptureEnable() }
            }
        }

    /**
     * 以下用于快捷判断元素是否是指定类型
     */
    fun AccessibilityNodeInfo.isFrameLayout(): Boolean {
        return className == NodeClassValue.FrameLayout
    }

    fun AccessibilityNodeInfo.isViewGroup(): Boolean {
        return className == NodeClassValue.ViewGroup
    }

    fun AccessibilityNodeInfo.isView(): Boolean {
        return className == NodeClassValue.View
    }

    fun AccessibilityNodeInfo.isImageView(): Boolean {
        return className == NodeClassValue.ImageView
    }

    fun AccessibilityNodeInfo.isTextView(): Boolean {
        return className == NodeClassValue.TextView
    }

    fun AccessibilityNodeInfo.isLinearLayout(): Boolean {
        return className == NodeClassValue.LinearLayout
    }

    fun AccessibilityNodeInfo.isRelativeLayout(): Boolean {
        return className == NodeClassValue.RelativeLayout
    }

    fun AccessibilityNodeInfo.isButton(): Boolean {
        return className == NodeClassValue.Button
    }

    fun AccessibilityNodeInfo.isImageButton(): Boolean {
        return className == NodeClassValue.ImageButton
    }

    fun AccessibilityNodeInfo.isEditText(): Boolean {
        return className == NodeClassValue.EditText
    }


    fun AccessibilityNodeInfo.txt(): String {
        return text?.toString() ?: ""
    }

    fun AccessibilityNodeInfo.des(): String {
        return contentDescription?.toString() ?: ""
    }


    /**
     * 请求录屏权限
     * @param isAutoEnable 是否自动通过，如果设置自动通过前提需要先开启无障碍服务（当前仅测试小米系统通过，其他机型系统未测试）
     */
    suspend fun requestScreenCapture(isAutoEnable: Boolean): Boolean {
        mediaProjectionService ?: let {
            delay(1000)
            if (isAutoEnable && service != null) {
                findByText("立即开始").firstOrNull()?.let {
                    val result = it.click()
                    return result
                }
            }
        }
        return true
    }

    /**
     * 是否拥有录屏权限
     */
    fun isEnableScreenCapture(): Boolean {
        return mediaProjectionService != null
    }

    fun init(application: Application) {
        LogUtils.getConfig().globalTag = LOG_TAG
    }

    /**
     * 打开无障碍服务设置
     */
    @JvmStatic
    fun openAccessibilitySetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ActivityUtils.startActivity(intent)
    }

    /**
     *检查无障碍服务是否开启
     */
    fun isAccessibilityServiceEnabled(): Boolean {
        return service != null
    }

    /**
     * 获取当前窗口所属包名
     */
    fun getPackageName(): String {
        return service?.rootInActiveWindow?.packageName?.toString() ?: ""
    }

    /**
     * 通过id查找所有符合条件元素
     * @param text 筛选返回符合指定文本的元素
     */
    fun findById(id: String, text: String? = null): List<AccessibilityNodeInfo> {
        var nodeInfos = service?.rootInActiveWindow?.findById(id) ?: arrayListOf()

        nodeInfos = text?.let {
            nodeInfos.filter {

                if (it.txt() == text) {
                    return@filter true
                }

                return@filter false
            }

        } ?: let { nodeInfos }

        return nodeInfos
    }

    /**
     * 在当前元素范围下，通过id查找所有符合条件元素
     */
    fun AccessibilityNodeInfo?.findById(id: String): List<AccessibilityNodeInfo> {
        if (this == null) return arrayListOf()
        findAccessibilityNodeInfosByViewId(id)?.let {
            return it
        }
        return arrayListOf()
    }

    /**
     * 通过文本查找所有符合条件元素
     */
    fun findByText(text: String): List<AccessibilityNodeInfo> {
        return service?.rootInActiveWindow?.findByText(text) ?: arrayListOf()
    }

    /**
     * 根据文本查找所有文本相同的元素
     */
    fun findByTextAllMatch(text: String): List<AccessibilityNodeInfo> {
        val listResult = arrayListOf<AccessibilityNodeInfo>()
        val list = service?.rootInActiveWindow?.findByText(text)
        list?.let {
            it.forEach {
                if (TextUtils.equals(it.text, text)) {
                    listResult.add(it)
                }
            }
        }
        return listResult
    }

    /**
     * 在当前元素范围下，通过文本查找所有符合条件元素
     */
    fun AccessibilityNodeInfo?.findByText(text: String): List<AccessibilityNodeInfo> {
        if (this == null) return arrayListOf()
        findAccessibilityNodeInfosByText(text)?.let {
            return it
        }
        return arrayListOf()
    }

    /**
     * 判断元素是否包含指定的文本
     */
    fun AccessibilityNodeInfo?.containsText(text: String): Boolean {
        if (this == null) return false
        getText()?.let {
            if (it.contains(text)) return true
        }
        contentDescription?.let {
            if (it.contains(text)) return true
        }
        return false
    }

    /**
     * 获取元素文本列表，包括text，content-desc
     */
    fun AccessibilityNodeInfo?.getAllText(text: String): ArrayList<String> {
        if (this == null) return arrayListOf()
        val texts = arrayListOf<String>()
        getText()?.let {
            texts.add(it.toString())
        }
        contentDescription?.let {
            texts.add(it.toString())
        }
        return texts
    }


    /**
     * 根据类型查找元素
     * @param className 完整类名，如[androidx.recyclerview.widget.RecyclerView]
     * @param viewId 筛选返回符合指定viewId的元素
     * @param text 筛选返回符合指定文本的元素
     * @param des 筛选返回符合指定描述文本的元素
     * @return 所有符合条件的元素
     */
    fun findByTags(className: String, viewId: String? = null, text: String? = null, des: String? = null): List<AccessibilityNodeInfo> {
        var nodeList = arrayListOf<AccessibilityNodeInfo>()
        getAllNodes().forEach {
            if (TextUtils.equals(className, it.className)) {
                nodeList.add(it)
            }
        }
        nodeList = viewId?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.viewIdResourceName == viewId
                })
            }
        } ?: let {
            return@let nodeList
        }

        nodeList = text?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.txt() == text
                })
            }
        } ?: let { return@let nodeList }
        nodeList = des?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.des() == des
                })
            }
        } ?: let { return@let nodeList }
        return nodeList
    }

    /**
     * 在当前元素范围下，根据类型查找元素
     * @param className 完整类名，如[androidx.recyclerview.widget.RecyclerView]
     * @param viewId 筛选返回符合指定viewId的元素
     * @param text 筛选返回符合指定文本的元素
     * @param des 筛选返回符合指定描述文本的元素
     * @return 所有符合条件的元素
     */
    fun AccessibilityNodeInfo.findByTags(
        className: String,
        viewId: String? = null,
        text: String? = null,
        des: String? = null
    ): List<AccessibilityNodeInfo> {
        var nodeList = arrayListOf<AccessibilityNodeInfo>()
        getNodes().forEach {
            if (TextUtils.equals(className, it.className)) {
                nodeList.add(it)
            }
        }
        nodeList = viewId?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.viewIdResourceName == viewId
                })
            }
        } ?: let {
            return@let nodeList
        }

        nodeList = text?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.txt() == text
                })
            }
        } ?: let { return@let nodeList }
        nodeList = des?.let {
            return@let arrayListOf<AccessibilityNodeInfo>().apply {
                addAll(nodeList.filter {
                    return@filter it.des() == des
                })
            }
        } ?: let { return@let nodeList }

        return nodeList
    }

    /**
     * 根据类型查找首个符合条件的父元素
     * @param className 完整类名，如[androidx.recyclerview.widget.RecyclerView]
     */
    fun AccessibilityNodeInfo.findFirstParentByTags(className: String): AccessibilityNodeInfo? {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        findFirstParentByTags(className, nodeList)
        return nodeList.firstOrNull()
    }


    /**
     * 递归根据类型查找首个符合条件的父元素
     * @param className 完整类名，如[androidx.recyclerview.widget.RecyclerView]
     * @param container 存放结果容器
     */
    fun AccessibilityNodeInfo.findFirstParentByTags(className: String, container: ArrayList<AccessibilityNodeInfo>) {
        getParent()?.let {
            if (TextUtils.equals(className, it.className)) {
                container.add(it)
            } else {
                it.findFirstParentByTags(className, container)
            }
        }
    }

    /**
     * 获取所有元素
     */
    fun getAllNodes(): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        service?.rootInActiveWindow?.getNodes(nodeList)
        return nodeList
    }

    /**
     * 获取当前元素下所有子元素
     */
    fun AccessibilityNodeInfo.getNodes(): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        this.getNodes(nodeList)
        return nodeList
    }

    /**
     * 递归获取所有元素
     */
    private fun AccessibilityNodeInfo.getNodes(nodeList: ArrayList<AccessibilityNodeInfo>) {
        nodeList.add(this)
        if (nodeList.size > 10000) return
        for (index in 0 until this.childCount) {
            getChild(index)?.getNodes(nodeList)
        }
    }

    /**
     * 查找第一个可点击的父元素
     */
    fun AccessibilityNodeInfo.findFirstParentClickable(): AccessibilityNodeInfo? {
        arrayOfNulls<AccessibilityNodeInfo>(1).apply {
            findFirstParentClickable(this)
            return this[0]
        }
    }

    /**
     * 查找可点击的父元素
     */
    private fun AccessibilityNodeInfo.findFirstParentClickable(nodeInfo: Array<AccessibilityNodeInfo?>) {
        if (parent?.isClickable == true) {
            nodeInfo[0] = parent
            return
        } else {
            parent?.findFirstParentClickable(nodeInfo)
        }
    }

    /**
     * 获取当前元素下的子元素（不包括子元素中的子元素）
     */
    fun AccessibilityNodeInfo.getChildren(): ArrayList<AccessibilityNodeInfo> {
        val nodes = arrayListOf<AccessibilityNodeInfo>()
        for (i in 0 until this.childCount) {
            val child = getChild(i)
            nodes.add(child)
        }
        return nodes
    }

    /**
     * 手势模拟，点或直线
     *
     * @param startLocation 开始位置，长度为2的数组，下标 0为 x坐标，下标 1为 y坐标
     * @param endLocation 结束位置
     * @param startTime 开始间隔时间
     * @param duration 持续时间
     * @return true 执行成功，false 执行失败
     */
    @JvmStatic
    suspend fun gesture(
        startLocation: FloatArray,
        endLocation: FloatArray,
        startTime: Long,
        duration: Long,
    ): Boolean {
        val path = Path()
        path.moveTo(startLocation[0], startLocation[1])
        path.lineTo(endLocation[0], endLocation[1])
        return gesture(path, startTime, duration)
    }

    /**
     * 手势模拟
     * @param path 手势路径
     * @param startTime 开始间隔毫秒
     * @param duration 持续毫秒
     * @return true 执行成功，false 执行失败
     */
    @JvmStatic
    suspend fun gesture(
        path: Path,
        startTime: Long,
        duration: Long,
    ): Boolean {
        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, startTime, duration)
        val gestureDescription = builder.addStroke(strokeDescription).build()
        val deferred = CompletableDeferred<Int>()
        service?.dispatchGesture(gestureDescription, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                deferred.complete(1)
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                deferred.complete(0)
            }
        }, null) ?: let {
            deferred.complete(0)
        }
        val result = deferred.await()
        return result == 1
    }

    /**
     * 获取元素在屏幕中的范围
     */
    fun AccessibilityNodeInfo.getBoundsInScreen(): Rect {
        val boundsInScreen = Rect()
        getBoundsInScreen(boundsInScreen)
        return boundsInScreen
    }

    /**
     * 手势点击元素所处的位置
     */
    suspend fun AccessibilityNodeInfo.gestureClick() {
        val rect = getBoundsInScreen()
        gestureClick(rect.left + 15F, rect.top + 15F, 10)
    }

    /**
     * 点击元素
     * @return 执行结果，true成功，false失败
     */
    fun AccessibilityNodeInfo.click(): Boolean {
        return performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * 手势长按元素所处的位置
     */
    suspend fun AccessibilityNodeInfo.gestureLongClick() {
        val rect = getBoundsInScreen()
        gestureClick(rect.left + 15F, rect.top + 15F, 1000)
    }

    /**
     * 点击屏幕指定位置
     * @param x 坐标
     * @param y 坐标
     */
    suspend fun gestureClick(
        x: Float,
        y: Float,
        duration: Long = 10
    ): Boolean {
        return gesture(
            floatArrayOf(x, y), floatArrayOf(x, y),
            0,
            duration,
        )
    }

    /**
     * 手势点击、长按当前元素
     * @param offsetX x轴偏移量
     * @param offsetY y轴偏移量
     * @param switchWindowIntervalDelay 浮窗隐藏显示间隔时长
     * @param duration 手势执行时长，需要长按就设置时间长点，默认点击时长
     */
    suspend fun AccessibilityNodeInfo.nodeGestureClick(
        offsetX: Float = ScreenUtils.getScreenWidth() * 0.01953f,
        offsetY: Float = ScreenUtils.getScreenWidth() * 0.01953f,
        switchWindowIntervalDelay: Long = 100,
        duration: Long = 25
    ): Boolean {
        AssistsWindowManager.switchNotTouchableAll()
        delay(switchWindowIntervalDelay)
        val rect = getBoundsInScreen()
        val result = gesture(
            floatArrayOf(rect.left.toFloat() + offsetX, rect.top.toFloat() + offsetY),
            floatArrayOf(rect.left.toFloat() + offsetX, rect.top.toFloat() + offsetY),
            0,
            duration,
        )
        delay(switchWindowIntervalDelay)
        AssistsWindowManager.switchTouchableAll()
        return result
    }

    /**
     * 返回
     * @return 执行结果，true成功，false失败
     */
    fun back(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) ?: false
    }


    /**
     * 回到主页
     * @return 执行结果，true成功，false失败
     */
    fun home(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME) ?: false
    }

    /**
     * 显示通知栏
     * @return 执行结果，true成功，false失败
     */
    fun notifications(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
    }

    /**
     * 最近任务
     * @return 执行结果，true成功，false失败
     */
    fun tasks(): Boolean {
        return service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS) ?: false
    }

    /**
     * 粘贴文本到当前元素
     * @return 执行结果，true成功，false失败
     */
    fun AccessibilityNodeInfo.paste(text: String?): Boolean {
        performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        service?.let {
            val clip = ClipData.newPlainText("${System.currentTimeMillis()}", text)
            val clipboardManager = (it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            clipboardManager.setPrimaryClip(clip)
            clipboardManager.primaryClip
            return performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }
        return false
    }

    /**
     * 选择输入框文本
     * @param selectionStart 文本起始下标
     * @param selectionEnd 文本结束下标
     * @return 执行结果，true成功，false失败
     */
    fun AccessibilityNodeInfo.selectionText(selectionStart: Int, selectionEnd: Int): Boolean {
        val selectionArgs = Bundle()
        selectionArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, selectionStart)
        selectionArgs.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, selectionEnd)
        return performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, selectionArgs)
    }

    /**
     * 修改输入框文本内容
     * @return 执行结果，true成功，false失败
     */
    fun AccessibilityNodeInfo.setNodeText(text: String?): Boolean {
        text ?: return false
        return performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundleOf().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        })
    }

    /**
     * 根据基准分辨率宽度获取对应当前分辨率的x坐标
     */
    fun getX(baseWidth: Int, x: Int): Int {
        val screenWidth = ScreenUtils.getScreenWidth()
        return (x / baseWidth.toFloat() * screenWidth).toInt()
    }

    /**
     * 根据基准分辨率高度获取对应当前分辨率的y坐标
     */
    fun getY(baseHeight: Int, y: Int): Int {
        var screenHeight = ScreenUtils.getScreenHeight()
        if (screenHeight > baseHeight) {
            screenHeight = baseHeight
        }
        return (y.toFloat() / baseHeight * screenHeight).toInt()
    }


    /**
     * 获取当前app在屏幕中的位置，如果找不到android:id/content节点则为空
     */
    fun getAppBoundsInScreen(): Rect? {
        return service?.let {
            return@let findById("android:id/content").firstOrNull()?.getBoundsInScreen()
        }
    }


    /**
     * 初始化当前app在屏幕中的位置
     */
    fun initAppBoundsInScreen(): Rect? {
        return getAppBoundsInScreen().apply {
            appRectInScreen = this
        }
    }

    /**
     * 获取当前app在屏幕中的宽度，获取前需要先执行initAppBoundsInScreen，避免getAppBoundsInScreen每次获取新的会耗时
     */
    fun getAppWidthInScreen(): Int {
        return appRectInScreen?.let {
            return@let it.right - it.left
        } ?: ScreenUtils.getScreenWidth()
    }


    /**
     * 获取当前app在屏幕中的高度，获取前需要先执行initAppBoundsInScreen，避免getAppBoundsInScreen每次获取新的会耗时
     */
    fun getAppHeightInScreen(): Int {
        return appRectInScreen?.let {
            return@let it.bottom - it.top
        } ?: ScreenUtils.getScreenHeight()
    }

    /**
     * 向前滚动（需元素是可滚动的）
     * @return 执行结果，true成功，false失败。false可作为滚动到底部或顶部依据
     */
    fun AccessibilityNodeInfo.scrollForward(): Boolean {
        return performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /**
     * 向后滚动（需元素是可滚动的）
     * @return 执行结果，true成功，false失败。false可作为滚动到底部或顶部依据
     */
    fun AccessibilityNodeInfo.scrollBackward(): Boolean {
        return performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
    }

    /**
     * 控制台输出元素信息
     */
    fun AccessibilityNodeInfo.logNode(tag: String = LOG_TAG) {
        StringBuilder().apply {
            val rect = getBoundsInScreen()
            append("-------------------------------------\n")
            append("位置:left=${rect.left}, top=${rect.top}, right=${rect.right}, bottom=${rect.bottom} \n")
            append("文本:$text \n")
            append("内容描述:$contentDescription \n")
            append("id:$viewIdResourceName \n")
            append("类型:${className} \n")
            append("是否已经获取到到焦点:$isFocused \n")
            append("是否可滚动:$isScrollable \n")
            append("是否可点击:$isClickable \n")
            append("是否可用:$isEnabled \n")
            Log.d(tag, toString())
        }
    }

}