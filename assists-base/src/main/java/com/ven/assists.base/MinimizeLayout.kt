package com.ven.assists.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.base.databinding.ViewMinimizeBinding

class MinimizeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    init {
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.format = PixelFormat.RGBA_8888
        //此处layoutParams.type不建议使用TYPE_TOAST，因为在一些版本较低的系统中会出现拖动异常的问题，虽然它不需要权限
        layoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        //背景明暗度0~1，数值越大背景越暗，只有在flags设置了WindowManager.LayoutParams.FLAG_DIM_BEHIND 这个属性才会生效
        layoutParams.dimAmount = 0.0f
        //透明度0~1，数值越大越不透明
        layoutParams.alpha = 1f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ViewMinimizeBinding.bind(this).apply {
            tvContent.setOnTouchListener { v, event ->

                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX > ScreenUtils.getAppScreenWidth() / 2) {
                        layoutParams.x = ScreenUtils.getAppScreenWidth() - root.measuredWidth
                    } else {
                        layoutParams.x = 0
                    }
                    AssistsWindowManager.windowManager.updateViewLayout(this@MinimizeLayout, layoutParams)
                    return@setOnTouchListener true
                }
                if (event.action == MotionEvent.ACTION_DOWN) {
                    return@setOnTouchListener true
                }
                if (event.action == MotionEvent.ACTION_MOVE) {
                    layoutParams.x = event.rawX.toInt() - root.measuredWidth / 2
                    layoutParams.y = event.rawY.toInt() - BarUtils.getStatusBarHeight() - root.measuredHeight / 2
                    AssistsWindowManager.windowManager.updateViewLayout(this@MinimizeLayout, layoutParams)
                    return@setOnTouchListener true
                }

                return@setOnTouchListener false
            }
        }
    }

}