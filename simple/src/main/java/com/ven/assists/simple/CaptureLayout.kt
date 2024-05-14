package com.ven.assists.simple

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.ven.assists.AssistsWindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Point

class CaptureLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private var rectLeft = 500f
    private var rectTop = 500f
    private var rectRight = 800f
    private var rectBottom = 800f

    init {
        setBackgroundColor(Color.parseColor("#80000000"))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            rectLeft = ev.x
            rectTop = ev.y
            return false
        }
        if (ev.action == MotionEvent.ACTION_MOVE) {
            val x = ev.x
            val y = ev.y
            rectRight = x
            rectBottom = y
            invalidate()
            return false
        }
        if (ev.action == MotionEvent.ACTION_UP) {
            AssistsWindowManager.removeView(this)
            return false
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setPoint(point: Point, width: Int, height: Int) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                rectLeft = point.x.toFloat()
                rectTop = point.y.toFloat() - BarUtils.getStatusBarHeight()
                rectRight = rectLeft + width
                rectBottom = rectTop + height
                withContext(Dispatchers.Main) {
                    invalidate()
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, paint)
    }

}