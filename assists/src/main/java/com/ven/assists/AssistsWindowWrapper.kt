package com.ven.assists

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.base.databinding.AssistsWindowLayoutWrapperBinding

@SuppressLint("ClickableViewAccessibility")
class AssistsWindowWrapper(
    view: View,
    wmLayoutParams: WindowManager.LayoutParams? = null,
    onClose: ((parent: View) -> Unit)? = null,

    ) {
    private var layoutHeight: Int = 0
    private var layoutWidth: Int = 0
    private var eventDownRawX = 0
    private var eventDownRawY = 0
    var minHeight = -1
    var minWidth = -1
    var maxHeight = -1
    var maxWidth = -1
    var initialX = 0
    var initialY = 0
    var initialXOffset = 0
    var initialYOffset = 0
    var initialCenter = false
    var showOption: Boolean = true
    var showBackground = true
    var wmlp: WindowManager.LayoutParams = wmLayoutParams ?: let { AssistsWindowManager.createLayoutParams() }

    private val onTouchScaleListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                layoutHeight = viewBinding.root.measuredHeight
                layoutWidth = viewBinding.root.measuredWidth
                eventDownRawX = event.rawX.toInt()
                eventDownRawY = event.rawY.toInt()
                return true
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                val width = layoutWidth + (eventDownRawX - event.rawX.toInt())
                if (width > 0) {
                    if ((minWidth == -1 || width >= minWidth) && (maxWidth == -1 || width <= maxWidth)) {
                        wmlp.width = width
                        wmlp.x = event.rawX.toInt()
                    }
                }

                val height = layoutHeight - (eventDownRawY - event.rawY.toInt())

                if (height > 0) {
                    if ((minHeight == -1 || height >= minHeight) && (maxHeight == -1 || height <= maxHeight)) {
                        wmlp.height = height
                    }
                }
                AssistsWindowManager.updateViewLayout(viewBinding.root, wmlp)
                return true
            }

            return false
        }
    }
    private val onTouchMoveListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                return true
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                wmlp.x = event.rawX.toInt()
                wmlp.y = event.rawY.toInt() - BarUtils.getStatusBarHeight()
                AssistsWindowManager.updateViewLayout(viewBinding.root, wmlp)
                return true
            }

            return false
        }
    }

    val viewBinding: AssistsWindowLayoutWrapperBinding by lazy {
        AssistsWindowLayoutWrapperBinding.inflate(LayoutInflater.from(view.context)).apply {
            root.isInvisible = true
            root.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (root.measuredWidth > 0) {
                        root.isInvisible = false
                        if (initialCenter) {
                            val measuredWidth = root.measuredWidth
                            val measuredHeight = root.measuredHeight
                            val initialX = ScreenUtils.getScreenWidth() / 2 - measuredWidth / 2
                            val initialY = ScreenUtils.getScreenHeight() / 2 - measuredHeight / 2
                            wmlp.x = initialX
                            wmlp.y = initialY
                        }
                        AssistsWindowManager.updateViewLayout(root, wmlp)
                        root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
            ivMove.setOnTouchListener(onTouchMoveListener)
            ivScale.setOnTouchListener(onTouchScaleListener)
            ivClose.setOnClickListener { onClose?.invoke(root) ?: AssistsWindowManager.removeView(root) }
            flHeader.isVisible = showOption
            ivScale.isVisible = showOption
            if (!showBackground) {
                root.background = null
            }
            flContainer.addView(view)
        }
    }

    fun ignoreTouch() {
        wmlp.flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        AssistsWindowManager.getWindowManager()?.updateViewLayout(viewBinding.root, wmlp)
    }

    fun consumeTouch() {
        wmlp.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        AssistsWindowManager.getWindowManager()?.updateViewLayout(viewBinding.root, wmlp)
    }

    fun getView(): View {
        return viewBinding.root
    }
}