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
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.flow.MutableSharedFlow

class AssistsService : AccessibilityService() {
    companion object {

        val event = MutableSharedFlow<Int>()

        /**
         * 无障碍服务，未开启前为null，使用注意判空
         */
        var instance: AssistsService? = null
            private set
    }



    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        AssistsWindowManager.init(this)
        Assists.serviceListeners.forEach { it.onServiceConnected(this) }
        CoroutineWrapper.launch { event.emit(1) }
        LogUtils.d(Assists.LOG_TAG, "assists service on service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        instance = this
        Assists.serviceListeners.forEach { it.onAccessibilityEvent(event) }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        Assists.serviceListeners.forEach { it.onUnbind() }
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Assists.serviceListeners.forEach { it.onInterrupt() }
    }
}