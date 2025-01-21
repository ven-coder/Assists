package com.ven.assists

import android.accessibilityservice.AccessibilityGestureEvent
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
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
        LogUtils.d(Assists.LOG_TAG, "assists service on service connected")
        Assists.service = this
        AssistsWindowManager.init(this)
        Assists.serviceListeners.forEach { it.onServiceConnected(this) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Assists.service = this
        Assists.serviceListeners.forEach { it.onAccessibilityEvent(event) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Assists.service = null
        Assists.serviceListeners.forEach { it.onUnbind() }
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Assists.serviceListeners.forEach { it.onInterrupt() }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        LogUtils.d(event?.action)
        return super.onKeyEvent(event)
    }

    override fun onMotionEvent(event: MotionEvent) {
        LogUtils.d(event.x, event.y)
        super.onMotionEvent(event)
    }

    override fun onGesture(gestureEvent: AccessibilityGestureEvent): Boolean {
        LogUtils.d(gestureEvent.toString())
        return super.onGesture(gestureEvent)
    }

}