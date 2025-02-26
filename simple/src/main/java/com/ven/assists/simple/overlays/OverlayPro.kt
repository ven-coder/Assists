package com.ven.assists.simple.overlays

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.isImageView
import com.ven.assists.Assists.longClick
import com.ven.assists.Assists.nodeGestureClick
import com.ven.assists.Assists.scrollBackward
import com.ven.assists.Assists.scrollForward
import com.ven.assists.Assists.selectionText
import com.ven.assists.Assists.setNodeText
import com.ven.assists.AssistsServiceListener
import com.ven.assists.AssistsWindowManager
import com.ven.assists.AssistsWindowManager.overlayToast
import com.ven.assists.AssistsWindowWrapper
import com.ven.assists.simple.MultiTouchDrawingActivity
import com.ven.assists.simple.OverManager
import com.ven.assists.simple.ScreenshotReviewActivity
import com.ven.assists.simple.TestActivity
import com.ven.assists.simple.common.LogWrapper
import com.ven.assists.simple.common.toast
import com.ven.assists.simple.databinding.ProOverlayBinding
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists_mp.MPManager
import com.ven.assists_mp.MPManager.takeScreenshot2File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object OverlayPro : AssistsServiceListener {

    private var disableNotificationView: View? = null

    var viewBinding: ProOverlayBinding? = null
        private set
        get() {
            if (field == null) {
                field = ProOverlayBinding.inflate(LayoutInflater.from(Assists.service)).apply {
                    btnListenerNotification.setOnClickListener {
                        if (!Assists.serviceListeners.contains(notificationListener)) {
                            Assists.serviceListeners.add(notificationListener)
                        }
                        CoroutineWrapper.launch(isMain = true) {
                            OverlayLog.show()
                            LogWrapper.logAppend("通知监听中...")
                        }
                    }
                    btnDisablePullNotification.setOnClickListener {
                        disableNotificationView?.let {
                            AssistsWindowManager.removeView(it)
                            disableNotificationView = null
                            btnDisablePullNotification.setText("禁止下拉通知栏")
                            return@setOnClickListener
                        }
                        disableNotificationView = View(Assists.service).apply {
                            setBackgroundColor(Color.parseColor("#80000000"))
                            layoutParams = ViewGroup.LayoutParams(-1, BarUtils.getStatusBarHeight())
                        }
                        AssistsWindowManager.add(view = disableNotificationView, layoutParams = AssistsWindowManager.createLayoutParams().apply {
                            width = -1
                            height = BarUtils.getStatusBarHeight()
                        })
                        btnDisablePullNotification.setText("允许下拉通知栏")
                    }
                    btnScreenCapture.setOnClickListener {
                        CoroutineWrapper.launch {
                            val result = MPManager.request(autoAllow = false, timeOut = 5000)
                            if (result) {
                                "已获取屏幕录制权限".overlayToast()
                            } else {
                                "获取屏幕录制权限超时".overlayToast()
                            }
                        }
                    }
                    btnScreenCaptureAuto.setOnClickListener {
                        CoroutineWrapper.launch {
                            val result = MPManager.request(autoAllow = true, timeOut = 5000)
                            if (result) {
                                "已获取屏幕录制权限".overlayToast()
                            } else {
                                "获取屏幕录制权限超时".overlayToast()
                            }
                        }
                    }
                    btnTakeScreenshot.setOnClickListener {
                        runCatching {
                            val file = MPManager.takeScreenshot2File()
                            Assists.service?.startActivity(Intent(Assists.service, ScreenshotReviewActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra("path", file?.path)
                            })
                        }.onFailure {
                            LogUtils.d(it)
                            "截图失败，尝试请求授予屏幕录制后重试".overlayToast()
                        }
                    }
                    btnTakeScreenshotAllImage.setOnClickListener {
                        runCatching {
                            val screenshot = MPManager.takeScreenshot2Bitmap()
                            screenshot ?: return@runCatching
                            val list: ArrayList<String> = arrayListOf()
                            Assists.getAllNodes().forEach {
                                if (it.isImageView()) {
                                    val file = it.takeScreenshot2File(screenshot)
                                    file?.let { list.add(file.path) }
                                }
                            }
                            LogUtils.d(list)
                        }.onFailure {
                            LogUtils.d(it)
                            "截图失败，尝试请求授予屏幕录制后重试".overlayToast()
                        }
                    }

                }
            }
            return field
        }

    private val notificationListener = object : AssistsServiceListener {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            super.onAccessibilityEvent(event)
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                event.text.forEach {
                    CoroutineWrapper.launch {
                        LogWrapper.logAppend("监听到通知：${it}")
                    }
                }
            }
        }
    }


    var onClose: ((parent: View) -> Unit)? = null

    var showed = false
        private set
        get() {
            assistWindowWrapper?.let {
                return AssistsWindowManager.isVisible(it.getView())
            } ?: return false
        }

    var assistWindowWrapper: AssistsWindowWrapper? = null
        private set
        get() {
            viewBinding?.let {
                if (field == null) {
                    field = AssistsWindowWrapper(it.root, wmLayoutParams = AssistsWindowManager.createLayoutParams().apply {
                        width = (ScreenUtils.getScreenWidth() * 0.8).toInt()
                        height = (ScreenUtils.getScreenHeight() * 0.5).toInt()
                    }, onClose = this.onClose).apply {
                        minWidth = (ScreenUtils.getScreenWidth() * 0.6).toInt()
                        minHeight = (ScreenUtils.getScreenHeight() * 0.4).toInt()
                        initialCenter = true
                        viewBinding.tvTitle.text = "进阶"
                    }
                }
            }
            return field
        }

    fun show() {
        if (!Assists.serviceListeners.contains(this)) {
            Assists.serviceListeners.add(this)
        }
        AssistsWindowManager.add(assistWindowWrapper)
    }

    fun hide() {
        AssistsWindowManager.removeView(assistWindowWrapper?.getView())
    }

    override fun onUnbind() {
        viewBinding = null
        assistWindowWrapper = null
    }
}