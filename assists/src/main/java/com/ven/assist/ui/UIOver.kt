package com.ven.assist.ui

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ScreenUtils

/**
 * 浮窗
 */
class UIOver private constructor(builder: Builder) {
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private lateinit var mWindowManager: WindowManager
    private var mDisplayMetrics: DisplayMetrics? = null

    /**
     * 触摸点相对于view左上角的坐标
     */
    private var downX = 0f
    private var downY = 0f

    /**
     * 触摸点相对于屏幕左上角的坐标
     */
    private var rowX = 0f
    private var rowY = 0f

    private val mContext: Context

    /**
     * 是否自动贴边
     */
    private val autoAlign: Boolean

    /**
     * 是否模态窗口
     */
    private val modality: Boolean

    /**
     * 是否可拖动
     */
    private val moveAble: Boolean

    /**
     * 透明度
     */
    private val alpha: Float

    /**
     * 初始位置
     */
    private val startX: Int
    private val startY: Int

    /**
     * View 高度
     */
    private val height: Int

    /**
     * View 宽度
     */
    private val width: Int

    /**
     * 内部定义的View，专门处理事件拦截的父View
     */
    var view: FloatView? = null

    /**
     * 外部传进来的需要悬浮的View
     */
    private val contentView: View
    private var windowTouchListener: WindowTouchListener? = null
    private var isAddWindow = false

    init {
        mContext = builder.context
        autoAlign = builder.autoAlign
        modality = builder.modality
        contentView = builder.contentView
        moveAble = builder.moveAble
        startX = builder.startX
        startY = builder.startY
        alpha = builder.alpha
        height = builder.height
        width = builder.width
        initWindowManager()
        initLayoutParams()
        initFloatView()
        if (builder.isCenterStart) initStartCenter()
    }

    private fun initWindowManager() {
        mWindowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        //获取一个DisplayMetrics对象，该对象用来描述关于显示器的一些信息，例如其大小，密度和字体缩放。
        mDisplayMetrics = DisplayMetrics()
        mWindowManager!!.defaultDisplay.getMetrics(mDisplayMetrics)
    }

    private fun initFloatView() {
        view = FloatView(mContext)
        if (moveAble) {
            windowTouchListener = WindowTouchListener()
            view!!.setOnTouchListener(windowTouchListener)
        }
        view?.visibility = View.GONE
    }

    private fun initLayoutParams() {
        mLayoutParams = WindowManager.LayoutParams()
        mLayoutParams!!.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        if (modality) {
            mLayoutParams!!.flags = mLayoutParams!!.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL.inv()
            mLayoutParams!!.flags = mLayoutParams!!.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        }
        mLayoutParams!!.height = WindowManager.LayoutParams.WRAP_CONTENT
        mLayoutParams!!.width = WindowManager.LayoutParams.WRAP_CONTENT
        if (height != WindowManager.LayoutParams.WRAP_CONTENT) {
            mLayoutParams!!.height = WindowManager.LayoutParams.MATCH_PARENT
        }
        if (width != WindowManager.LayoutParams.WRAP_CONTENT) {
            mLayoutParams!!.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        mLayoutParams!!.gravity = Gravity.START or Gravity.TOP
        mLayoutParams!!.format = PixelFormat.RGBA_8888
        //此处mLayoutParams.type不建议使用TYPE_TOAST，因为在一些版本较低的系统中会出现拖动异常的问题，虽然它不需要权限
        mLayoutParams!!.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        //悬浮窗背景明暗度0~1，数值越大背景越暗，只有在flags设置了WindowManager.LayoutParams.FLAG_DIM_BEHIND 这个属性才会生效
        mLayoutParams!!.dimAmount = 0.0f
        //悬浮窗透明度0~1，数值越大越不透明
        mLayoutParams!!.alpha = alpha
        //悬浮窗起始位置
        mLayoutParams!!.x = startX
        mLayoutParams!!.y = startY
    }

    fun setLayoutParams(width: Int, height: Int) {
        mLayoutParams!!.height = width
        mLayoutParams!!.width = height
        mWindowManager!!.updateViewLayout(view, mLayoutParams)
    }

    private fun initStartCenter() {
        view?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                view?.let {
                    if (it.measuredWidth > 0) {
                        val screenHeight = ScreenUtils.getAppScreenHeight()
                        val screenWidth = ScreenUtils.getAppScreenWidth()

                        val centerY = screenHeight / 2
                        val centerX = screenWidth / 2

                        val x = centerX - it.measuredWidth / 2
                        val y = centerY - it.measuredHeight / 2

                        mLayoutParams?.x = x
                        mLayoutParams?.y = y

                        mWindowManager.updateViewLayout(view, mLayoutParams)

                        it.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }

            }

        })
    }

    /**
     * 将窗体添加到屏幕上
     */
    fun show() {
        if (!isAddWindow) mWindowManager.addView(view, mLayoutParams)
        view?.visibility = View.VISIBLE
        isAddWindow = true
    }

    fun hide() {
        view?.visibility = View.GONE
    }

    /**
     * 悬浮窗是否正在显示
     *
     * @return true if it's showing.
     */
    fun isShowing(): Boolean {
        return view != null && view!!.visibility == View.VISIBLE
    }

    /**
     * 移除悬浮窗
     */
    fun remove() {
        view!!.removeView(contentView)
        if (isAddWindow) mWindowManager.removeView(view)
    }

    fun autoAlign() {
        windowTouchListener!!.autoAlign()
    }

    inner class FloatView(context: Context?) : FrameLayout(context!!) {
        /**
         * 记录按下位置
         */
        var interceptX = 0
        var interceptY = 0

        init {
            //这里由于一个ViewGroup不能add一个已经有Parent的contentView,所以需要先判断contentView是否有Parent
            //如果有则需要将contentView先移除
            if (contentView.parent != null && contentView.parent is ViewGroup) {
                (contentView.parent as ViewGroup).removeView(contentView)
            }
            addView(contentView)
        }

        /**
         * 解决点击与拖动冲突的关键代码
         *
         * @param ev
         * @return
         */
        override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
            //此回调如果返回true则表示拦截TouchEvent由自己处理，false表示不拦截TouchEvent分发出去由子view处理
            //解决方案：如果是拖动父View则返回true调用自己的onTouch改变位置，是点击则返回false去响应子view的点击事件
            var isIntercept = false
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    interceptX = ev.x.toInt()
                    interceptY = ev.y.toInt()
                    downX = ev.x
                    downY = ev.y
                    isIntercept = false
                }

                MotionEvent.ACTION_MOVE ->                     //在一些dpi较高的设备上点击view很容易触发 ACTION_MOVE，所以此处做一个过滤
                    isIntercept = Math.abs(ev.x - interceptX) > MINIMUM_OFFSET && Math.abs(ev.y - interceptY) > MINIMUM_OFFSET

                MotionEvent.ACTION_UP -> {}
                else -> {}
            }
            return isIntercept
        }
    }

    internal inner class WindowTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {

            //获取触摸点相对于屏幕左上角的坐标
            rowX = event.rawX
            rowY = event.rawY - BarUtils.getStatusBarHeight()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> actionDown(event)
                MotionEvent.ACTION_MOVE -> actionMove(event)
                MotionEvent.ACTION_UP -> actionUp(event)
                MotionEvent.ACTION_OUTSIDE -> actionOutSide(event)
                else -> {}
            }
            return false
        }

        /**
         * 手指点击窗口外的事件
         *
         * @param event
         */
        private fun actionOutSide(event: MotionEvent) {
            //由于我们在layoutParams中添加了FLAG_WATCH_OUTSIDE_TOUCH标记，那么点击悬浮窗之外时此事件就会被响应
            //这里可以用来扩展点击悬浮窗外部响应事件
        }

        /**
         * 手指抬起事件
         *
         * @param event
         */
        private fun actionUp(event: MotionEvent) {
            if (autoAlign) {
                autoAlign()
            }
        }

        /**
         * 拖动事件
         *
         * @param event
         */
        private fun actionMove(event: MotionEvent) {
            //拖动事件下一直计算坐标 然后更新悬浮窗位置
            updateLocation(rowX - downX, rowY - downY)
        }

        /**
         * 更新位置
         */
        fun updateLocation(x: Float, y: Float) {
            mLayoutParams!!.x = x.toInt()
            mLayoutParams!!.y = y.toInt()
            mWindowManager!!.updateViewLayout(view, mLayoutParams)
        }

        /**
         * 手指按下事件
         *
         * @param event
         */
        private fun actionDown(event: MotionEvent) {
//            downX = event.getX();
//            downY = event.getY();
        }

        /**
         * 自动贴边
         */
        fun autoAlign() {
            val fromX = mLayoutParams!!.x.toFloat()
            if (rowX <= mDisplayMetrics!!.widthPixels / 2) {
                mLayoutParams!!.x = 0
            } else {
                mLayoutParams!!.x = mDisplayMetrics!!.widthPixels
            }

            //这里使用ValueAnimator来平滑计算起始X坐标到结束X坐标之间的值，并更新悬浮窗位置
            val animator = ValueAnimator.ofFloat(fromX, mLayoutParams!!.x.toFloat())
            animator.duration = 300
            animator.addUpdateListener { animation: ValueAnimator ->
                //这里会返回fromX ~ mLayoutParams.x之间经过计算的过渡值
                val toX = animation.animatedValue as Float
                //我们直接使用这个值来更新悬浮窗位置
                updateLocation(toX, mLayoutParams!!.y.toFloat())
            }
            animator.start()
        }
    }

    /**
     * 浮窗构建
     */
    class Builder
    /**
     * @param context     上下文环境（如果开了悬浮窗则任意上下文即可，如果没开，需要无障碍服务的上下文）
     * @param contentView 需要悬浮的视图
     */(val context: Context, val contentView: View) {
        var autoAlign = false
        var modality = false
        var moveAble = false
        var isCenterStart = false
        var alpha = 1f

        /**
         * View 高度
         */
        var height = WindowManager.LayoutParams.WRAP_CONTENT

        /**
         * View 宽度
         */
        var width = WindowManager.LayoutParams.WRAP_CONTENT

        /**
         * 初始位置
         */
        var startX = 0
        var startY = 0

        /**
         * 是否自动贴边
         *
         * @param autoAlign
         * @return
         */
        fun setAutoAlign(autoAlign: Boolean): Builder {
            this.autoAlign = autoAlign
            return this
        }

        /**
         * 首次是否居中显示
         */
        fun isFirstCenterShow(value: Boolean): Builder {
            isCenterStart = value
            return this
        }

        /**
         * 是否模态窗口（事件是否可穿透当前窗口）
         *
         * @param modality
         * @return
         */
        fun setModality(modality: Boolean): Builder {
            this.modality = modality
            return this
        }

        /**
         * 是否可拖动
         *
         * @param moveAble
         * @return
         */
        fun setMoveAble(moveAble: Boolean): Builder {
            this.moveAble = moveAble
            return this
        }

        /**
         * 设置起始位置
         *
         * @param startX
         * @param startY
         * @return
         */
        fun setStartLocation(startX: Int, startY: Int): Builder {
            this.startX = startX
            this.startY = startY
            return this
        }

        fun setAlpha(alpha: Float): Builder {
            this.alpha = alpha
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun build(): UIOver {
            return UIOver(this)
        }
    }

    companion object {
        /**
         * 拖动最小偏移量
         */
        private const val MINIMUM_OFFSET = 5

    }
}