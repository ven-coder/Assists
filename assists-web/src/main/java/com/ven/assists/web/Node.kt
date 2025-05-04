package com.ven.assists.web

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.web.Node.Bounds
import com.ven.assists.web.utils.UniqueIdGenerator
import java.util.UUID

class Node(
    val nodeId: String,
    val text: String,
    val des: String,
    val viewId: String,
    val className: String,
    val isScrollable: Boolean,
    val isClickable: Boolean,
    val isEnabled: Boolean,
    val boundsInScreen: Bounds,
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
        nodeId = NodeCacheManager.add(this),
        text = this.text?.toString() ?: "",
        des = this.contentDescription?.toString() ?: "",
        viewId = this.viewIdResourceName ?: "",
        className = this.className?.toString() ?: "",
        isScrollable = this.isScrollable,
        isClickable = this.isClickable,
        isEnabled = this.isEnabled,
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