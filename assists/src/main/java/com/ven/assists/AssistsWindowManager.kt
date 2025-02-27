package com.ven.assists

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists.utils.runIO
import com.ven.assists.utils.runMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    fun getWindowManager(): WindowManager? {
        Assists.service?.getSystemService(Context.WINDOW_SERVICE)?.let { return (it as WindowManager) }
        return null
    }

    fun createLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
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

    suspend fun hideAll(isTouchable: Boolean = true) {
        withContext(Dispatchers.Main) {
            viewList.forEach {
                it.view.isInvisible = true
                if (isTouchable) {
                    it.touchableByWrapper()
                } else {
                    it.untouchableByWrapper()
                }
            }
        }
    }

    suspend fun hideTop(isTouchable: Boolean = true) {
        withContext(Dispatchers.Main) {
            viewList.lastOrNull()?.let {
                it.view.isInvisible = true
                if (isTouchable) {
                    it.touchableByWrapper()
                } else {
                    it.untouchableByWrapper()
                }
            }
        }
    }

    suspend fun showTop(isTouchable: Boolean = true) {
        withContext(Dispatchers.Main) {
            viewList.lastOrNull()?.let {
                it.view.isVisible = true
                if (isTouchable) {
                    it.touchableByWrapper()
                } else {
                    it.untouchableByWrapper()
                }
            }
        }
    }

    suspend fun showAll(isTouchable: Boolean = true) {
        withContext(Dispatchers.Main) {
            viewList.forEach {
                it.view.isVisible = true
                if (isTouchable) {
                    it.touchableByWrapper()
                } else {
                    it.untouchableByWrapper()
                }
            }
        }
    }


    fun add(windowWrapper: AssistsWindowWrapper?, isStack: Boolean = true, isTouchable: Boolean = true) {
        windowWrapper ?: return
        add(view = windowWrapper.getView(), layoutParams = windowWrapper.wmlp, isStack = isStack, isTouchable = isTouchable)
    }

    fun add(view: View?, layoutParams: WindowManager.LayoutParams = createLayoutParams(), isStack: Boolean = true, isTouchable: Boolean = true) {
        view ?: return
        if (!isStack) {
            viewList.lastOrNull()?.let { it.view.isInvisible = true }
        }
        windowManager.addView(view, layoutParams)
        if (isTouchable) {
            layoutParams.touchableByLayoutParams()
        } else {
            layoutParams.untouchableByLayoutParams()
        }
        viewList.add(ViewWrapper(view, layoutParams))
    }

    fun push(view: View?, params: WindowManager.LayoutParams = createLayoutParams()) {
        add(view, params, isStack = false)
    }

    suspend fun pop(showTop: Boolean = true) {
        viewList.lastOrNull()?.let { removeView(it.view) }
        if (showTop) showTop()
    }

    fun removeView(view: View?) {
        view ?: return
        try {
            windowManager.removeView(view)
            viewList.find {
                return@find view == it.view
            }?.let {
                viewList.remove(it)
            }
        } catch (e: Throwable) {
            LogUtils.e(e)
        }
    }

    fun contains(view: View?): Boolean {
        view ?: return false
        return viewList.find {
            return@find view == it.view
        } != null
    }

    fun contains(wrapper: AssistsWindowWrapper?): Boolean {
        wrapper ?: return false
        return viewList.find {
            return@find wrapper.getView() == it.view
        } != null
    }

    fun isVisible(view: View): Boolean {
        return viewList.find {
            return@find view == it.view
        }?.let {
            return@let it.view.isVisible
        } ?: false
    }

    suspend fun updateViewLayout(view: View, params: ViewGroup.LayoutParams) {
        runMain { windowManager.updateViewLayout(view, params) }
    }

    suspend fun touchableByAll() {
        viewList.forEach { it.touchableByWrapper() }
    }

    suspend fun untouchableByAll() {
        viewList.forEach { it.untouchableByWrapper() }
    }

    fun WindowManager.LayoutParams.touchableByLayoutParams() {
        flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    fun WindowManager.LayoutParams.untouchableByLayoutParams() {
        flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
    }

    suspend fun ViewWrapper.touchableByWrapper() {
        layoutParams.touchableByLayoutParams()
        updateViewLayout(view, layoutParams)
    }

    suspend fun ViewWrapper.untouchableByWrapper() {
        layoutParams.untouchableByLayoutParams()
        updateViewLayout(view, layoutParams)
    }

    fun String.overlayToast(delay: Long = 2000) {
        Assists.service?.let {
            CoroutineWrapper.launch(isMain = true) {
                val textView = TextView(it).apply {
                    text = this@overlayToast
                    setTextColor(Color.WHITE)
                    setPadding(SizeUtils.dp2px(10f))
//                    setBackgroundColor(Color.BLACK)
                }
                val assistsWindowWrapper = AssistsWindowWrapper(textView, wmLayoutParams = createLayoutParams().apply {
                    width = -2
                    height = -2
                }).apply {
                    showOption = false
                    initialCenter = true
//                    showBackground = false
                }
                add(assistsWindowWrapper, isTouchable = false)
                runIO { delay(delay) }
                removeView(assistsWindowWrapper.getView())
            }
        }

    }

    class ViewWrapper(val view: View, val layoutParams: WindowManager.LayoutParams)

}