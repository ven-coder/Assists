package com.ven.assist.ui

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.TargetApi
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils
import com.ven.assist.Assists

/**
 * UI操作工具类
 */
object UIOperate {

    /**
     * 通过id查找控件
     */
    fun findById(id: String): List<AccessibilityNodeInfo> {
        Assists.service?.rootInActiveWindow?.findAccessibilityNodeInfosByViewId(id)?.let {
            return it
        }
        return arrayListOf()
    }

    /**
     * 根据类型查找控件
     * @param className 完整类名，如[com.ven.assist.Assists]
     * @param parentNode 父节点，如果为null，则会通过根节点查找
     * @return 所有符合条件的控件
     */
    fun findByTags(className: String, parentNode: AccessibilityNodeInfo? = null): List<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        parentNode?.let {
            getNodes(parentNode).forEach {
                if (TextUtils.equals(className, it.className)) {
                    nodeList.add(it)
                }
            }
        } ?: let {
            getAllNodes().forEach {
                if (TextUtils.equals(className, it.className)) {
                    nodeList.add(it)
                }
            }
        }
        return nodeList
    }

    /**
     * 根据文本查找
     */
    fun findByText(text: String?): List<AccessibilityNodeInfo> {
        Assists.service?.rootInActiveWindow?.findAccessibilityNodeInfosByText(text)?.let {
            return it
        }
        return arrayListOf()
    }

    /**
     * 查找可点击的父控件
     */
    fun findParentClickable(nodeInfo: AccessibilityNodeInfo, callback: (nodeInfo: AccessibilityNodeInfo) -> Unit) {
        if (nodeInfo.parent.isClickable) {
            callback.invoke(nodeInfo.parent)
            return
        } else {
            findParentClickable(nodeInfo.parent, callback)
        }
    }

    /**
     * 获取所有控件
     */
    fun getAllNodes(): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        Assists.service?.rootInActiveWindow?.let {
            getNodes(it, nodeList)
        }
        return nodeList
    }

    /**
     * 获取指定控件下所有子控件
     */
    fun getNodes(parentNode: AccessibilityNodeInfo): ArrayList<AccessibilityNodeInfo> {
        val nodeList = arrayListOf<AccessibilityNodeInfo>()
        getNodes(parentNode, nodeList)
        return nodeList
    }

    /**
     * 递归获取所有控件
     */
    private fun getNodes(parentNode: AccessibilityNodeInfo, nodeList: ArrayList<AccessibilityNodeInfo>) {
        nodeList.add(parentNode)
        if (nodeList.size > 10000) return
        for (index in 0 until parentNode.childCount) {
            parentNode.getChild(index)?.let {
                getNodes(it, nodeList)
            }
        }
    }


    /**
     * 手势模拟
     *
     * @param startLocation 开始位置，长度为2的数组，下标 0为 x坐标，下标 1为 y坐标
     * @param endLocation 结束位置
     * @param startTime 开始间隔时间
     * @param duration 持续时间
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun gesture(
        startLocation: FloatArray,
        endLocation: FloatArray,
        startTime: Long,
        duration: Long,
    ): Long {
        val delay: Long = Assists.ListenerManager.gestureListener?.onGestureBegin(startLocation, endLocation) ?: 0
        val path = Path()
        path.moveTo(startLocation[0], startLocation[1])
        path.lineTo(endLocation[0], endLocation[1])
        val builder = GestureDescription.Builder()
        val strokeDescription = GestureDescription.StrokeDescription(path, startTime, duration)
        val gestureDescription = builder.addStroke(strokeDescription).build()
        object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription) {
                Assists.ListenerManager.gestureListener?.onGestureCompleted()
                Assists.ListenerManager.gestureListener?.onGestureEnd()
            }

            override fun onCancelled(gestureDescription: GestureDescription) {
                Assists.ListenerManager.gestureListener?.onGestureCancelled()
                Assists.ListenerManager.gestureListener?.onGestureEnd()
            }
        }.apply {
            if (delay == 0L) {
                Assists.service!!.dispatchGesture(gestureDescription, this, null)
            } else {
                ThreadUtils.runOnUiThreadDelayed({
                    Assists.service!!.dispatchGesture(gestureDescription, this, null)
                }, delay)
            }
        }
        return delay + startTime + duration
    }


    /**
     * 点击屏幕
     */
    fun clickScreen(
        x: Float,
        y: Float,
        duration: Long
    ): Long {
        return gesture(
            floatArrayOf(x, y), floatArrayOf(x, y),
            0,
            duration,
        )
    }

    /**
     * 根据基准分辨率宽度获取对应当前分辨率的x坐标
     */
    fun getX(baseWidth: Int, x: Int): Int {
        val screenWidth = ScreenUtils.getAppScreenWidth()
        return (x / baseWidth.toFloat() * screenWidth).toInt()
    }

    /**
     * 根据基准分辨率高度获取对应当前分辨率的y坐标
     */
    fun getY(baseHeight: Int, y: Int): Int {
        val statusBarHeight = BarUtils.getStatusBarHeight()
        var screenHeight = ScreenUtils.getAppScreenHeight() + statusBarHeight
        if (screenHeight > baseHeight) {
            screenHeight = baseHeight
        }
        return (y.toFloat() / baseHeight * screenHeight).toInt()
    }

    /**
     * 返回
     */
    fun back() {
        Assists.service?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }


    /**
     * 粘贴文本
     */
    fun paste(nodeInfo: AccessibilityNodeInfo?, text: String?) {
        Assists.service?.let {
            val clip = ClipData.newPlainText("${System.currentTimeMillis()}", text)
            val clipboardManager = (it.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            clipboardManager.setPrimaryClip(clip)
            clipboardManager.primaryClip
            nodeInfo?.performAction(AccessibilityNodeInfo.ACTION_PASTE)
        }

    }
}
