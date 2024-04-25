package com.ven.assists.simple

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.ven.assists.GestureListener
import com.ven.assists.Assists
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.databinding.ViewMainOverBinding
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.simple.step.Step
import com.ven.assists.stepper.StepListener
import com.ven.assists.stepper.StepManager

object OverManager : StepListener, GestureListener {
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null

    private fun createView(): ViewMainOverBinding? {
        return Assists.service?.let {
            Assists.gestureListeners.add(this)
            StepManager.stepListeners.add(this)
            ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                llOption.isVisible = true
                llLog.isVisible = false
                btnCloseLog.isVisible = false
                btnOpenSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(OpenWechatSocial::class.java, Step.STEP_1, isBegin = true)
                }
                btnPublishSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(PublishSocial::class.java, Step.STEP_1, isBegin = true)
                }
                btnStop.setOnClickListener {
                    stop()
                }
                btnCloseLog.setOnClickListener { showOption() }
                btnStopScrollLog.setOnClickListener {
                    isAutoScrollLog = !isAutoScrollLog
                }
                btnLog.setOnClickListener {
                    showLog()
                    btnCloseLog.isVisible = true
                    btnStop.isVisible = false
                }
                btnScrollContacts.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(ScrollContacts::class.java, Step.STEP_1, isBegin = true)
                }
                btnClickBottomTab.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(GestureBottomTab::class.java, Step.STEP_1, isBegin = true)
                }
                btnScrollSocial.setOnClickListener {
                    beginStart(this)
                    StepManager.execute(GestureScrollSocial::class.java, Step.STEP_1, isBegin = true)
                }
                root.setOnCloseClickListener {
                    clear()
                }
            }

        }
    }

    fun show() {
        viewMainOver ?: let {
            viewMainOver = createView()
            AssistsWindowManager.addView(viewMainOver?.root)
        }
    }

    private fun beginStart(view: ViewMainOverBinding) {
        with(view) {
            clearLog()
            showLog()
            isAutoScrollLog = true
            btnCloseLog.isVisible = false
            btnStop.isVisible = true
        }
    }

    override fun onGestureBegin(startLocation: FloatArray, endLocation: FloatArray) {
        viewMainOver?.root?.let {
            val viewXY = IntArray(2)
            it.getLocationOnScreen(viewXY)
            if (startLocation[0] >= viewXY[0] &&
                startLocation[0] <= viewXY[0] + it.measuredWidth &&
                startLocation[1] >= viewXY[1] &&
                startLocation[1] <= viewXY[1] + it.measuredHeight
            ) {
                Assists.gestureBeginDelay = 1000L
                viewMainOver?.root?.switchNotTouchable()
            }
        }
    }

    override fun onGestureEnd() {
        viewMainOver?.root?.switchTouchable()
    }

    override fun onStepStop() {
        log("已停止")
    }

    private fun stop() {
        if (StepManager.isStop) {
            showOption()
            return
        }
        StepManager.isStop = true
        isAutoScrollLog = false
        viewMainOver?.btnStop?.isVisible = false
        viewMainOver?.btnCloseLog?.isVisible = true
    }

    fun showLog() {
        viewMainOver?.llOption?.isVisible = false
        viewMainOver?.llLog?.isVisible = true
    }

    fun showOption() {
        viewMainOver?.llOption?.isVisible = true
        viewMainOver?.llLog?.isVisible = false
    }

    fun clear() {
        AssistsWindowManager.windowManager.removeView(viewMainOver?.root)
        Assists.gestureListeners.remove(this)
        StepManager.stepListeners.remove(this)
        viewMainOver = null
    }

    private val logStr: StringBuilder = StringBuilder()
    fun log(value: Any) {
        if (logStr.length > 1000) logStr.delete(0, 50)
        if (logStr.isNotEmpty()) logStr.append("\n")
        logStr.append(TimeUtils.getNowString())
        logStr.append("\n")
        logStr.append(value.toString())
        viewMainOver?.tvLog?.text = logStr
    }

    fun clearLog() {
        logStr.delete(0, logStr.length)
        viewMainOver?.tvLog?.text = ""
    }

    var isAutoScrollLog = true
        set(value) {
            if (value) onAutoScrollLog()
            viewMainOver?.btnStopScrollLog?.text = if (value) "停止滚动" else "继续滚动"
            field = value
        }

    private fun onAutoScrollLog() {
        viewMainOver?.scrollView?.fullScroll(NestedScrollView.FOCUS_DOWN)
        ThreadUtils.runOnUiThreadDelayed({
            if (!isAutoScrollLog) return@runOnUiThreadDelayed
            onAutoScrollLog()
        }, 250)
    }
}