package com.ven.assist.ext

import android.graphics.Rect
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assist.Assists
import com.ven.assist.ui.UIOperate

private val boundsInScreen = Rect()

/**
 * 获取控件在屏幕中的范围
 */
fun AccessibilityNodeInfo.getBoundsInScreen(): Rect {
    getBoundsInScreen(boundsInScreen)
    return boundsInScreen
}

/**
 * 手势点击控件所处的位置
 */
fun AccessibilityNodeInfo.clickScreen(): Long {
    val rect = getBoundsInScreen()
    return UIOperate.clickScreen(rect.left + 15F, rect.top + 15F, 10)
}

/**
 * 点击控件
 */
fun AccessibilityNodeInfo.click() {
    performAction(AccessibilityNodeInfo.ACTION_CLICK)
}

/**
 * 手势长按控件所处的位置
 */
fun AccessibilityNodeInfo.clickScreenLong(): Long {
    val rect = getBoundsInScreen()
    return UIOperate.clickScreen(rect.left + 15F, rect.top + 15F, 1000)
}

/**
 * 粘贴文本至控件
 */
fun AccessibilityNodeInfo.pasteText(text: String) {
    UIOperate.paste(this, text)
}

/**
 * 获取控件下所有子控件
 */
fun AccessibilityNodeInfo.getNodes(): ArrayList<AccessibilityNodeInfo> {
    return UIOperate.getNodes(this)
}

/**
 * 在当前控件范围内根据文本或id查找符合其中一个条件的控件
 */
fun AccessibilityNodeInfo.findByTextAndId(text: String, viewIdResourceName: String): AccessibilityNodeInfo? {
    getNodes().forEach {
        if ((TextUtils.equals(text, it.text) || TextUtils.equals(text, it.contentDescription)) &&
            TextUtils.equals(viewIdResourceName, it.viewIdResourceName)
        ) {
            return it
        }
    }
    return null
}

/**
 * 根据文本查找控件范围内的子控件
 */
fun AccessibilityNodeInfo.findByText(text: String): AccessibilityNodeInfo? {
    getNodes().forEach {
        if (TextUtils.equals(text, it.text) || TextUtils.equals(text, it.contentDescription)) {
            return it
        }
    }
    return null
}

/**
 * 输出控件信息
 */
fun AccessibilityNodeInfo.logToText() {
    Log.d(
        Assists.Config.logTag, "text:$text / " +
                "contentDescription:$contentDescription / " +
                "viewIdResourceName:$viewIdResourceName / " +
                "isFocused:$isFocused / " +
                "isScrollable:$isScrollable / " +
                "isEnabled:$isEnabled"
    )
}