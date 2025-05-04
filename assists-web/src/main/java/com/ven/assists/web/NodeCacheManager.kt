package com.ven.assists.web

import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.web.utils.UniqueIdGenerator
import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import java.time.Instant
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object NodeCacheManager {
    val cache = ConcurrentHashMap<String, SoftReference<AccessibilityNodeInfo>>()

    fun get(nodeId: String): AccessibilityNodeInfo? {
        val node = cache[nodeId]?.get()
        LogUtils.d("is node null: ${node == null}")
        return node
    }

    fun add(node: AccessibilityNodeInfo): String {
        val generateUUID = UniqueIdGenerator.generateUUID()
        cache[generateUUID] = SoftReference(node)
        return generateUUID
    }
}

class NodeInfoWrapper(val node: AccessibilityNodeInfo, val cacheTime: Long) {
}