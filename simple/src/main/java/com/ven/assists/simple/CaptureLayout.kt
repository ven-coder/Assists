package com.ven.assists.simple

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.Dispatchers
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

    val paintList = arrayListOf<PaintWrapper>()

    init {
        setBackgroundColor(Color.parseColor("#00000000"))
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
            CoroutineWrapper.launch {
                withContext(Dispatchers.Main) {
                    AssistsWindowManager.pop()
                }
            }
            return false
        }
        return super.dispatchTouchEvent(ev)
    }

    suspend fun setPoint(point: Point, width: Int, height: Int) {
        paintList.add(PaintWrapper().apply {
            rectLeft = point.x.toFloat()
            rectTop = point.y.toFloat() - BarUtils.getStatusBarHeight()
            rectRight = rectLeft + width
            rectBottom = rectTop + height
        })
        withContext(Dispatchers.Main) {
            invalidate()
        }
    }

    fun addPoint(point: Point, width: Int, height: Int) {
        paintList.add(PaintWrapper().apply {
            rectLeft = point.x.toFloat()
            rectTop = point.y.toFloat() - BarUtils.getStatusBarHeight()
            rectRight = rectLeft + width
            rectBottom = rectTop + height
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintList.forEach {
            canvas.drawRect(it.rectLeft, it.rectTop, it.rectRight, it.rectBottom, it.paint)

        }

    }

    class PaintWrapper {
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        var rectLeft = 0f
        var rectTop = 0f
        var rectRight = 0f
        var rectBottom = 0f
    }

}