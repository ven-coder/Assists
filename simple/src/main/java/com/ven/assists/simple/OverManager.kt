package com.ven.assists.simple

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.ven.assist.Assists
import com.ven.assist.step.StepManager
import com.ven.assist.ui.UIOver
import com.ven.assists.simple.databinding.ViewMainOverBinding
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.simple.step.Step

object OverManager : Assists.ListenerManager.StepListener, Assists.ListenerManager.GestureListener {
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null
        get() {
            if (field == null) {
                field = Assists.service?.let {
                    Assists.ListenerManager.stepListener.add(this)
                    ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                        llOption.isVisible = true
                        llLog.isVisible = false
                        btnCloseLog.isVisible = false
                        ivClose.setOnClickListener {
                            stop()
                            showOption()
                            mainOver?.hide()

                        }
                        btnOpenSocial.setOnClickListener {
                            beginStart(this)
                            StepManager.beginExecute(OpenWechatSocial::class.java, Step.STEP_1)
                        }
                        btnPublishSocial.setOnClickListener {
                            beginStart(this)
                            StepManager.beginExecute(PublishSocial::class.java, Step.STEP_1)
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
                            StepManager.beginExecute(ScrollContacts::class.java, Step.STEP_1)
                        }
                        btnClickBottomTab.setOnClickListener {
                            beginStart(this)
                            StepManager.beginExecute(GestureBottomTab::class.java, Step.STEP_1)
                        }
                        btnScrollSocial.setOnClickListener {
                            beginStart(this)
                            StepManager.beginExecute(GestureScrollSocial::class.java, Step.STEP_1)
                        }
                    }
                }
            }
            return field
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

    override fun onGestureBegin(startLocation: FloatArray, endLocation: FloatArray): Long {
        mainOver?.view?.let {
            val viewXY = IntArray(2)
            it.getLocationOnScreen(viewXY)
            if (startLocation[0] >= viewXY[0] &&
                startLocation[0] <= viewXY[0] + it.measuredWidth &&
                startLocation[1] >= viewXY[1] &&
                startLocation[1] <= viewXY[1] + it.measuredHeight
            ) {
                mainOver?.hide()
                return 1000
            }
        }
        return 0
    }

    override fun onGestureEnd() {
        mainOver?.show()
    }

    override fun onStepStop() {
        log("已停止")
    }

    private fun stop() {
        if (StepManager.isStop) {
            showOption()
            return
        }
        StepManager.isStop=true
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

    @SuppressLint("StaticFieldLeak")
    var mainOver: UIOver? = null
        get() {
            if (field == null) {
                field = viewMainOver?.let {
                    Assists.ListenerManager.gestureListener = this
                    UIOver.Builder(it.root.context, it.root)
                        .setModality(false)
                        .setMoveAble(true)
                        .setAutoAlign(false)
                        .isFirstCenterShow(true)
                        .build()
                }
            }
            return field
        }

    fun clear() {
        mainOver?.remove()
        mainOver = null
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