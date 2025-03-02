package com.ven.assists.service

import android.view.accessibility.AccessibilityEvent

interface AssistsServiceListener {
    /**
     * 当界面发生事件时回调，即 [AssistsService.onAccessibilityEvent] 回调
     */
    fun onAccessibilityEvent(event: AccessibilityEvent) {}

    /**
     * 服务启用后的回调，即[AssistsService.onServiceConnected]回调
     */
    fun onServiceConnected(service: AssistsService) {}
    fun onInterrupt() {}

    /**
     * 服务关闭后的回调，即[AssistsService.onUnbind]回调
     */
    fun onUnbind() {}

    /**
     * 录屏权限开启
     */
    fun screenCaptureEnable() {

    }
}