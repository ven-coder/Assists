package com.ven.assists.simple

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class MultiTouchDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paths = mutableMapOf<Int, Path>()
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val points = mutableMapOf<Int, PointF>()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (path in paths.values) {
            canvas.drawPath(path, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerCount = event.pointerCount
        val actionIndex = event.actionIndex
        val pointerId = event.getPointerId(actionIndex)
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val path = Path()
                val point = PointF(event.getX(actionIndex), event.getY(actionIndex))
                path.moveTo(point.x, point.y)
                paths[pointerId] = path
                points[pointerId] = point
                invalidate()
            }
            
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until pointerCount) {
                    val id = event.getPointerId(i)
                    val path = paths[id]
                    val point = points[id]
                    if (path != null && point != null) {
                        val newX = event.getX(i)
                        val newY = event.getY(i)
                        path.quadTo(
                            point.x,
                            point.y,
                            (newX + point.x) / 2,
                            (newY + point.y) / 2
                        )
                        point.x = newX
                        point.y = newY
                    }
                }
                invalidate()
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                paths.remove(pointerId)
                points.remove(pointerId)
                invalidate()
            }
        }
        return true
    }

    fun clearCanvas() {
        paths.clear()
        points.clear()
        invalidate()
    }

    fun setStrokeColor(color: Int) {
        paint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        paint.strokeWidth = width
        invalidate()
    }
} 