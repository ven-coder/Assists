package com.ven.assists.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.AssistsCore
import com.ven.assists.window.AssistsWindowManager
import java.util.Collections

class AssistsService : AccessibilityService() {
    companion object {
        var instance: AssistsService? = null
            private set

        val listeners: MutableList<AssistsServiceListener> = Collections.synchronizedList(arrayListOf<AssistsServiceListener>())
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AssistsWindowManager.init(this)
        runCatching { listeners.forEach { it.onServiceConnected(this) } }
        LogUtils.d(AssistsCore.LOG_TAG, "assists service on service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        instance = this
        runCatching { listeners.forEach { it.onAccessibilityEvent(event) } }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        runCatching { listeners.forEach { it.onUnbind() } }
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        runCatching { listeners.forEach { it.onInterrupt() } }
    }
}