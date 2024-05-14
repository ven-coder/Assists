package com.ven.assists

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.ven.assists.base.databinding.AssistsWindowLayoutWrapperBinding

@SuppressLint("ClickableViewAccessibility")
class AssistsWindowLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    var layoutHeight: Int = 0
    var layoutWidth: Int = 0
    var downRawX = 0
    var downRawY = 0
    var closeClickListener: (() -> Boolean)? = null
    var minHeight = -1
    var minWidth = -1

    private val onTouchScaleListener = object : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                layoutHeight = measuredHeight
                layoutWidth = measuredWidth
                downRawX = event.rawX.toInt()
                downRawY = event.rawY.toInt()
                return true
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                val width = layoutWidth + (downRawX - event.rawX.toInt())
                if (width > 0) {
                    if (minWidth == -1 || width >= minWidth) {
                        layoutParams.width = width
                        layoutParams.x = event.rawX.toInt()
                    }
                }

                val height = layoutHeight - (downRawY - event.rawY.toInt())

                if (height > 0) {
                    if (minHeight == -1 || height >= minHeight) {
                        layoutParams.height = height
                    }
                }
                AssistsWindowManager.updateViewLayout(this@AssistsWindowLayout, layoutParams)
                return true
            }

            return false
        }
    }
    val onTouchMoveListener = object : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                return true
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                layoutParams.x = event.rawX.toInt()
                layoutParams.y = event.rawY.toInt() - BarUtils.getStatusBarHeight()
                AssistsWindowManager.updateViewLayout(this@AssistsWindowLayout, layoutParams)
                return true
            }

            return false
        }
    }
    var layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    /**
     * 浮窗包装布局
     */
    lateinit var assistsWindowLayoutWrapperBinding: AssistsWindowLayoutWrapperBinding

    init {

        setBackgroundColor(Color.parseColor("#80000000"))

        assistsWindowLayoutWrapperBinding = AssistsWindowLayoutWrapperBinding.inflate(LayoutInflater.from(getContext()), this).apply {
            ivMove.setOnTouchListener(onTouchMoveListener)
            ivScale.setOnTouchListener(onTouchScaleListener)
            ivClose.setOnClickListener {
                val result = closeClickListener?.invoke()
                if (result == true) return@setOnClickListener
                AssistsWindowManager.removeView(this@AssistsWindowLayout)
            }
        }

        layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
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
    }

    fun setOnCloseClickListener(closeClickListener: () -> Boolean) {
        this.closeClickListener = closeClickListener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (childCount > 4) {
            getChildAt(4).let {
                (it.layoutParams as LayoutParams).apply {
                    topMargin = SizeUtils.dp2px(40f)
                    bottomMargin = SizeUtils.dp2px(40f)
                }
            }
        }
    }

    fun setCenter() {
        if (measuredWidth > 0 && measuredHeight > 0) {
            val x = (ScreenUtils.getScreenWidth() - measuredWidth) / 2
            val y = (ScreenUtils.getScreenHeight() - BarUtils.getStatusBarHeight() - measuredHeight) / 2
            layoutParams.x = x
            layoutParams.y = y
            AssistsWindowManager.updateViewLayout(this@AssistsWindowLayout, layoutParams)
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        val x = (ScreenUtils.getScreenWidth() - measuredWidth) / 2
                        val y = (ScreenUtils.getScreenHeight() - BarUtils.getStatusBarHeight() - measuredHeight) / 2
                        layoutParams.x = x
                        layoutParams.y = y
                        AssistsWindowManager.updateViewLayout(this@AssistsWindowLayout, layoutParams)
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }

            })
        }

    }

    /**
     * 切换至不可消费事件
     */
    fun switchNotTouchable() {
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        AssistsWindowManager.updateViewLayout(this, layoutParams)
    }

    /**
     * 切换至可消费事件
     */
    fun switchTouchable() {
        layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        AssistsWindowManager.updateViewLayout(this, layoutParams)
    }

}