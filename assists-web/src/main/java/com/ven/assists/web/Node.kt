package com.ven.assists.web

import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.web.Node.Bounds
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rect对象池管理器，支持多线程安全访问
 * 使用ConcurrentLinkedQueue实现线程安全的对象池
 */
object RectPool {
    private val pool = ConcurrentLinkedQueue<Rect>()
    private val maxPoolSize = AtomicInteger(50) // 最大池大小
    private val currentSize = AtomicInteger(0)
    
    /**
     * 获取Rect对象，如果池为空则创建新对象
     */
    fun acquire(): Rect {
        val rect = pool.poll()
        return if (rect != null) {
            currentSize.decrementAndGet()
            rect.setEmpty() // 清空Rect内容
            rect
        } else {
            Rect()
        }
    }
    
    /**
     * 归还Rect对象到池中
     */
    fun release(rect: Rect) {
        if (currentSize.get() < maxPoolSize.get()) {
            rect.setEmpty() // 清空Rect内容
            pool.offer(rect)
            currentSize.incrementAndGet()
        }
    }
    
    /**
     * 设置最大池大小
     */
    fun setMaxPoolSize(size: Int) {
        maxPoolSize.set(size)
    }
    
    /**
     * 获取当前池大小
     */
    fun getCurrentSize(): Int = currentSize.get()
    
    /**
     * 清空对象池
     */
    fun clear() {
        pool.clear()
        currentSize.set(0)
    }
}

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

    data class Bounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
        val width: Int,
        val height: Int,
        val centerX: Int,
        val centerY: Int,
        val exactCenterX: Float,
        val exactCenterY: Float,
        val isEmpty: Boolean,
    )

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

fun AccessibilityNodeInfo.toNode(): Node {
    val rect = RectPool.acquire()
    try {
        getBoundsInScreen(rect)
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
                left = rect.left,
                top = rect.top,
                right = rect.right,
                bottom = rect.bottom,
                width = rect.width(),
                height = rect.height(),
                centerX = rect.centerX(),
                centerY = rect.centerY(),
                exactCenterX = rect.exactCenterX(),
                exactCenterY = rect.exactCenterY(),
                isEmpty = rect.isEmpty,
            )
        )
        return node
    } finally {
        RectPool.release(rect)
    }
}

fun AccessibilityNodeInfo.toBounds(): Bounds {
    val rect = RectPool.acquire()
    try {
        getBoundsInScreen(rect)

        return Node.Bounds(
            left = rect.left,
            top = rect.top,
            right = rect.right,
            bottom = rect.bottom,
            width = rect.width(),
            height = rect.height(),
            centerX = rect.centerX(),
            centerY = rect.centerY(),
            exactCenterX = rect.exactCenterX(),
            exactCenterY = rect.exactCenterY(),
            isEmpty = rect.isEmpty,
        )
    } finally {
        RectPool.release(rect)
    }
}


fun Rect.toBounds(): Bounds {
    return Node.Bounds(
        left = left,
        top = top,
        right = right,
        bottom = bottom,
        width = width(),
        height = height(),
        centerX = centerX(),
        centerY = centerY(),
        exactCenterX = exactCenterX(),
        exactCenterY = exactCenterY(),
        isEmpty = isEmpty,
    )
}

fun Node.get(): AccessibilityNodeInfo? {
    val node = NodeCacheManager.get(nodeId)
    return node
}