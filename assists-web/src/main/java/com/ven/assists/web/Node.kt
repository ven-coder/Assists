package com.ven.assists.web

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.web.Node.Bounds

class Node(
    val nodeId: String,
    val packageName: String,
    val text: String,
    val des: String,
    val viewId: String,
    val className: String,
    val isScrollable: Boolean,
    val isClickable: Boolean,
    val isEnabled: Boolean,
    val boundsInScreen: Bounds,
    val hintText: String,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val isFocusable: Boolean,
    val isFocused: Boolean,
    val isLongClickable: Boolean,
    val isPassword: Boolean,
    val isSelected: Boolean,
    val isVisibleToUser: Boolean,
    val drawingOrder: Int,
) {

    data class Bounds(val left: Int, val top: Int, val right: Int, val bottom: Int)

}

fun ArrayList<AccessibilityNodeInfo>.toNodes(): ArrayList<Node> {
    val nodes = arrayListOf<Node>().apply {
        this@toNodes.forEach {
            add(
                it.toNode()
            )
        }
    }
    return nodes
}

fun List<AccessibilityNodeInfo>.toNodes(): ArrayList<Node> {
    val nodes = arrayListOf<Node>().apply {
        this@toNodes.forEach { add(it.toNode()) }
    }
    return nodes
}

private val boundsInScreenRect = Rect()

fun AccessibilityNodeInfo.toNode(): Node {

    getBoundsInScreen(boundsInScreenRect)

    val node = Node(
        packageName = packageName?.toString() ?: "",
        nodeId = NodeCacheManager.add(this),
        text = this.text?.toString() ?: "",
        des = this.contentDescription?.toString() ?: "",
        viewId = this.viewIdResourceName ?: "",
        className = this.className?.toString() ?: "",
        isScrollable = this.isScrollable,
        isClickable = this.isClickable,
        isEnabled = this.isEnabled,
        hintText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) this.hintText?.toString() ?: "" else "",
        isCheckable = this.isCheckable,
        isChecked = this.isChecked,
        isFocusable = this.isFocusable,
        isFocused = this.isFocused,
        isLongClickable = this.isLongClickable,
        isPassword = this.isPassword,
        isSelected = this.isSelected,
        isVisibleToUser = this.isVisibleToUser,
        drawingOrder = this.drawingOrder,
        boundsInScreen = Node.Bounds(
            left = boundsInScreenRect.left,
            top = boundsInScreenRect.top,
            right = boundsInScreenRect.right,
            bottom = boundsInScreenRect.bottom
        )
    )
    return node
}

fun AccessibilityNodeInfo.toBounds(): Bounds {

    getBoundsInScreen(boundsInScreenRect)

    return Node.Bounds(
        left = boundsInScreenRect.left,
        top = boundsInScreenRect.top,
        right = boundsInScreenRect.right,
        bottom = boundsInScreenRect.bottom
    )

}


fun Rect.toBounds(): Bounds {
    return Node.Bounds(
        left = left,
        top = top,
        right = right,
        bottom = bottom
    )

}

fun Node.get(): AccessibilityNodeInfo? {
    val node = NodeCacheManager.get(nodeId)
    return node
}