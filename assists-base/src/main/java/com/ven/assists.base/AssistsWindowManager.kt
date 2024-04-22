package com.ven.assists.base

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.base.databinding.ViewWindowBinding
import java.lang.ref.SoftReference

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

    fun addView(resId: Int) {
        val view = View.inflate(Assists.service, resId, null);
        if (view is AssistsWindowLayout) {
            windowManager.addView(view, view.layoutParams)
        } else {
            throw RuntimeException("View 需继承 com.ven.assists.base.AssistsWindowLayout")
        }
    }


}