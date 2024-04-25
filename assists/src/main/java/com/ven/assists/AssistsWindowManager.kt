package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

object AssistsWindowManager {
    lateinit var windowManager: WindowManager
    private lateinit var mDisplayMetrics: DisplayMetrics
    fun init(accessibilityService: AccessibilityService) {
        windowManager = accessibilityService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
    }

    fun addView(view: AssistsWindowLayout?) {
        view ?: return
        windowManager.addView(view, view.layoutParams)
    }


}