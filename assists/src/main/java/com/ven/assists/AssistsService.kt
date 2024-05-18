package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils

class AssistsService : AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        Assists.service = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        LogUtils.d("onServiceConnected")
        Assists.service = this
        AssistsWindowManager.init(this)
        Assists.serviceListeners.forEach { it.onServiceConnected(this) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Assists.service = this
        Assists.serviceListeners.forEach { it.onAccessibilityEvent(event) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Assists.serviceListeners.forEach { it.onUnbind() }
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Assists.serviceListeners.forEach { it.onInterrupt() }
    }
}