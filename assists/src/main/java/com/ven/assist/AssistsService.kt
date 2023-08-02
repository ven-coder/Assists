package com.ven.assist

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.LogUtils

class AssistsService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        Assists.service = this
        Assists.init()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        LogUtils.d("onServiceConnected")
        Assists.service = this
        Assists.ListenerManager.globalListeners.forEach { it.onServiceConnected(this) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Assists.service = this
        Assists.ListenerManager.globalListeners.forEach { it.onAccessibilityEvent(event) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Assists.ListenerManager.globalListeners.forEach { it.onUnbind() }
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Assists.ListenerManager.globalListeners.forEach { it.onInterrupt() }
    }
}