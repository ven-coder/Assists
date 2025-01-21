package com.ven.assists.simple

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.bumptech.glide.Glide
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.containsText
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.getNodes
import com.ven.assists.Assists.logNode
import com.ven.assists.AssistsServiceListener
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.databinding.ViewMainOverBinding
import com.ven.assists.simple.databinding.ViewMatchImageBinding
import com.ven.assists.simple.step.AntForestEnergy
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.simple.step.StepTag
import com.ven.assists.stepper.StepListener
import com.ven.assists.stepper.StepManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgproc.Imgproc

object OverManager : StepListener {
    @SuppressLint("StaticFieldLeak")
    private var viewMainOver: ViewMainOverBinding? = null
    private var autoAnswerWechatCallListener: AssistsServiceListener? = null
    private val notificationListener = object : AssistsServiceListener {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            super.onAccessibilityEvent(event)
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                event.text.forEach {
                    OverManager.log("监听到通知：${it}")
                }
            }
        }
    }

    private fun createView(): ViewMainOverBinding? {
        return Assists.service?.let { it ->
            ViewMainOverBinding.inflate(LayoutInflater.from(it)).apply {
                initView(this)
            }

        }
    }

    private fun initView(viewMainOverBinding: ViewMainOverBinding) {
        with(viewMainOverBinding) {
//            parent.assistsWindowLayoutWrapperBinding.tvTitle.text = "AssistsSimple"
            llOption.isVisible = true
            llLog.isVisible = false
            btnCloseLog.isVisible = false
            btnSettingGuide.setOnClickListener {
                //引导示例
                Assists.openAccessibilitySetting()
                com.blankj.utilcode.util.Utils.getApp()
                    .startActivity(Intent(com.blankj.utilcode.util.Utils.getApp(), SettingGuideActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })

            }
            btnNotification.setOnClickListener {
                beginStart(this)
                if (!Assists.serviceListeners.contains(notificationListener)) {
                    Assists.serviceListeners.add(notificationListener)
                }
                Assists.coroutine.launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        OverManager.log("通知监听中...")
                    }
                }
            }
            btn1.setOnClickListener {
                if (btn1.tag is View) {
                    AssistsWindowManager.removeView(btn1.tag as View)
                    btn1.setText("禁止下拉通知栏")
                    btn1.tag = null
                    return@setOnClickListener
                }

                val layoutParams = AssistsWindowManager.createLayoutParams()
                layoutParams.flags = (WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
                layoutParams.width = ScreenUtils.getScreenWidth()
                layoutParams.height = BarUtils.getStatusBarHeight()
                AssistsWindowManager.addView(View(Assists.service!!).apply {
                    btn1.tag = this
                    setBackgroundColor(Color.parseColor("#80000000"))
                    setLayoutParams(ViewGroup.LayoutParams(ScreenUtils.getScreenWidth(), BarUtils.getStatusBarHeight()))
                }, layoutParams, isStack = true)
                btn1.setText("解除禁止下拉通知栏")
            }
            btn2.setOnClickListener {
                beginStart(this)
                Assists.coroutine.launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        OverManager.log("监听并自动接听微信电话...")
                    }
                }

                autoAnswerWechatCallListener ?: let {
                    autoAnswerWechatCallListener = object : AssistsServiceListener {
                        override fun onAccessibilityEvent(event: AccessibilityEvent) {
                            if (event.packageName == "com.tencent.mm") {
                                var isInCall = false
                                event.source?.getNodes()?.forEach {
                                    if (it.containsText("邀请你语音通话") || it.containsText("邀请你视频通话")) {
                                        it.logNode()
                                        it.getBoundsInScreen().let {
                                            if (it.bottom < ScreenUtils.getScreenHeight() * 0.2) {
                                                isInCall = true
                                            }
                                            if (it.top > ScreenUtils.getScreenHeight() * 0.50 && it.bottom < ScreenUtils.getScreenHeight() * 0.8) {
                                                isInCall = true
                                            }
                                        }
                                    }
                                    if (isInCall && it.containsText("接听") && it.className == "android.widget.ImageButton") {
                                        OverManager.log("收到微信电话，接听")
                                        it.click()
                                    }
                                    if (isInCall && it.containsText("接听") && it.className == "android.widget.Button") {
                                        OverManager.log("收到微信电话，接听")
                                        it.getBoundsInScreen().let {
                                            Assists.coroutine.launch {
                                                withContext(Dispatchers.Main) {
                                                    AssistsWindowManager.switchNotTouchableAll()
                                                }
                                                delay(100)
                                                Assists.gestureClick(it.left + 20f, it.top + 20f)
                                                delay(100)

                                                withContext(Dispatchers.Main) {
                                                    AssistsWindowManager.switchTouchableAll()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (!Assists.serviceListeners.contains(autoAnswerWechatCallListener)) {
                    Assists.serviceListeners.add(autoAnswerWechatCallListener!!)
                }

            }
            btnOpenSocial.setOnClickListener {
                beginStart(this)
                StepManager.execute(OpenWechatSocial::class.java, StepTag.STEP_1, begin = true)
            }
            btnPublishSocial.setOnClickListener {
                beginStart(this)
                StepManager.execute(PublishSocial::class.java, StepTag.STEP_1, begin = true, data = "字符串数据：1")
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
                StepManager.execute(ScrollContacts::class.java, StepTag.STEP_1, begin = true)
            }
            btnClickBottomTab.setOnClickListener {
                beginStart(this)
                StepManager.execute(GestureBottomTab::class.java, StepTag.STEP_1, begin = true)
            }
            btnScrollSocial.setOnClickListener {
                beginStart(this)
                StepManager.execute(GestureScrollSocial::class.java, StepTag.STEP_1, begin = true)
            }
//            root.setOnCloseClickListener {
//                clear()
//                return@setOnCloseClickListener false
//            }

            btnAntForestEnergy.setOnClickListener {
                beginStart(this)
                StepManager.execute(AntForestEnergy::class.java, StepTag.STEP_1, begin = true)
            }
        }
    }

    fun show() {
        viewMainOver ?: let {
            viewMainOver = createView()
            var width = ScreenUtils.getScreenWidth() - 60
            var height = SizeUtils.dp2px(300f)
            viewMainOver?.root?.layoutParams?.width = width
            viewMainOver?.root?.layoutParams?.height = height
//            viewMainOver?.root?.minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
//            viewMainOver?.root?.minHeight = height
//            viewMainOver?.root?.setCenter()
//            AssistsWindowManager.addAssistsWindowLayout(viewMainOver?.root)
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

    override fun onStepStop() {
        log("已停止")
    }

    private fun stop() {
        if (StepManager.isStop) {
            showOption()
            return
        }
        Assists.serviceListeners.remove(autoAnswerWechatCallListener)
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