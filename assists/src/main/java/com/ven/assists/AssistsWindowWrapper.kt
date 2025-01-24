package com.ven.assists

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.BarUtils
import com.ven.assists.base.databinding.AssistsWindowLayoutWrapperBinding

@SuppressLint("ClickableViewAccessibility")
class AssistsWindowWrapper(view: View, wmLayoutParams: WindowManager.LayoutParams? = null, onClose: ((parent: View) -> Unit)? = null) {
    private var layoutHeight: Int = 0
    private var layoutWidth: Int = 0
    private var downRawX = 0
    private var downRawY = 0
    private var minHeight = -1
    private var minWidth = -1
    private var initLocationX = 0
    private var initLocationY = 0
    private var wmlp: WindowManager.LayoutParams = wmLayoutParams ?: let { AssistsWindowManager.createLayoutParams() }


    private val onTouchScaleListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                layoutHeight = viewBinding.root.measuredHeight
                layoutWidth = viewBinding.root.measuredWidth
                downRawX = event.rawX.toInt()
                downRawY = event.rawY.toInt()
                return true
            }
            if (event.action == MotionEvent.ACTION_MOVE) {
                val width = layoutWidth + (downRawX - event.rawX.toInt())
                if (width > 0) {
                    if (minWidth == -1 || width >= minWidth) {
                        wmlp.width = width
                        wmlp.x = event.rawX.toInt()
                    }
                }

                val height = layoutHeight - (downRawY - event.rawY.toInt())

                if (height > 0) {
                    if (minHeight == -1 || height >= minHeight) {
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

    var viewBinding: AssistsWindowLayoutWrapperBinding =
        AssistsWindowLayoutWrapperBinding.inflate(LayoutInflater.from(view.context)).apply {
            ivMove.setOnTouchListener(onTouchMoveListener)
            ivScale.setOnTouchListener(onTouchScaleListener)
            ivClose.setOnClickListener { onClose?.invoke(root) ?: AssistsWindowManager.removeView(root) }
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
}