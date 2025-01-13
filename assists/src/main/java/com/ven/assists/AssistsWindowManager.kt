package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

object AssistsWindowManager {
    private lateinit var windowManager: WindowManager
    private lateinit var mDisplayMetrics: DisplayMetrics
    private val viewList = Collections.synchronizedList(arrayListOf<ViewWrapper>())
    fun init(accessibilityService: AccessibilityService) {
        windowManager = accessibilityService.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mDisplayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
    }

    fun createLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
//        layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)

        layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.format = PixelFormat.RGBA_8888
        //此处layoutParams.type不建议使用TYPE_TOAST，因为在一些版本较低的系统中会出现拖动异常的问题，虽然它不需要权限
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        //背景明暗度0~1，数值越大背景越暗，只有在flags设置了WindowManager.LayoutParams.FLAG_DIM_BEHIND 这个属性才会生效
        layoutParams.dimAmount = 0.0f
        //透明度0~1，数值越大越不透明
        layoutParams.alpha = 1f
        return layoutParams
    }

    suspend fun hideAll() {
        withContext(Dispatchers.Main) {
            viewList.forEach {
                it.view.isInvisible = true
                if (it.view is AssistsWindowLayout) {
                    it.view.switchNotTouchable()
                }
            }

        }
    }

    suspend fun showLastView() {
        withContext(Dispatchers.Main) {
            viewList.lastOrNull()?.let {
                it.view.isVisible = true
            }
            switchTouchableAll()
        }
    }


    fun addView(view: View?, params: ViewGroup.LayoutParams = createLayoutParams(), isStack: Boolean = false) {
        view ?: return
        if (!isStack) {
            viewList.forEach {
                it.view.isInvisible = true
                if (it.view is AssistsWindowLayout) {
                    it.view.switchNotTouchable()
                }
            }
        }

        windowManager.addView(view, params)
        viewList.add(ViewWrapper(view, params))
    }

    fun addAssistsWindowLayout(view: AssistsWindowLayout?) {
        view ?: return
        viewList.forEach {
            if (it.view == view) return
        }
        viewList.forEach {
            it.view.isInvisible = true
            if (it.view is AssistsWindowLayout) {
                it.view.switchNotTouchable()
            }
        }

        windowManager.addView(view, view.layoutParams)
        viewList.add(ViewWrapper(view, view.layoutParams))
    }

    fun pop() {
        viewList.lastOrNull()?.let {
            removeView(it.view)
        }
    }

    fun removeView(view: View?) {
        view ?: return
        try {
            windowManager.removeView(view)
            for (viewWrapper in viewList) {
                if (viewWrapper.view == view) {
                    viewList.remove(viewWrapper)
                    break
                }
            }
            viewList.lastOrNull()?.let {
                it.view.isInvisible = false
                if (it.view is AssistsWindowLayout) {
                    it.view.switchTouchable()
                }
            }
        } catch (e: Throwable) {
            LogUtils.e(e)
        }
    }

    fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
        windowManager.updateViewLayout(view, params)
    }

    /**
     * 切换至不可消费事件
     */
    suspend fun switchNotTouchableAll() {
        withContext(Dispatchers.Main){
            viewList.forEach {
                if (it.layoutParams is WindowManager.LayoutParams) {
                    it.layoutParams.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                    updateViewLayout(it.view, it.layoutParams)
                }
            }
        }

    }

    /**
     * 切换至可消费事件
     */
    suspend fun switchTouchableAll() {
        withContext(Dispatchers.Main) {
            viewList.forEach {
                if (it.layoutParams is WindowManager.LayoutParams) {
                    it.layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)



                    updateViewLayout(it.view, it.layoutParams)
                }
            }

        }
    }

    class ViewWrapper(val view: View, val layoutParams: ViewGroup.LayoutParams)

}