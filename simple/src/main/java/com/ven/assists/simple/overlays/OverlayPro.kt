package com.ven.assists.simple.overlays

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.isImageView
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.window.AssistsWindowManager.overlayToast
import com.ven.assists.window.AssistsWindowWrapper
import com.ven.assists.simple.ImageGalleryActivity
import com.ven.assists.simple.ScreenshotReviewActivity
import com.ven.assists.simple.common.LogWrapper
import com.ven.assists.simple.common.LogWrapper.logAppend
import com.ven.assists.simple.databinding.ProOverlayBinding
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists_mp.MPManager
import com.ven.assists_mp.MPManager.takeScreenshot2File
import kotlinx.coroutines.delay

@SuppressLint("StaticFieldLeak")
object OverlayPro : AssistsServiceListener {

    private var disableNotificationView: View? = null

    var viewBinding: ProOverlayBinding? = null
        private set
        get() {
            if (field == null) {
                field = ProOverlayBinding.inflate(LayoutInflater.from(AssistsService.instance)).apply {
                    btnListenerNotification.setOnClickListener {
                        if (!AssistsService.listeners.contains(notificationListener)) {
                            AssistsService.listeners.add(notificationListener)
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
                        disableNotificationView = View(AssistsService.instance).apply {
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
                            AssistsService.instance?.startActivity(Intent(AssistsService.instance, ScreenshotReviewActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra("path", file?.path)
                            })
                        }.onFailure {
                            LogUtils.d(it)
                            "截图失败，尝试请求授予屏幕录制后重试".overlayToast()
                        }
                    }
                    btnTakeScreenshotAllImage.setOnClickListener {
                        takeScreenshotAllImage()
                    }

                }
            }
            return field
        }

    private fun takeScreenshotAllImage() {
        CoroutineWrapper.launch(isMain = true) {
            runCatching {
                AssistsWindowManager.hideAll()
                delay(250)
                val screenshot = MPManager.takeScreenshot2Bitmap()
                screenshot ?: return@runCatching
                val list: ArrayList<String> = arrayListOf()
                AssistsCore.getAllNodes().forEach {
                    if (it.isImageView()) {
                        val file = it.takeScreenshot2File(screenshot)
                        file?.let { list.add(file.path) }
                    }
                }
                AssistsWindowManager.showAll()

                // 创建并显示通知
                val context = AssistsService.instance ?: return@runCatching
                val notificationManager = android.app.NotificationManager::class.java.getMethod("from", Context::class.java)
                    .invoke(null, context) as android.app.NotificationManager

                // 创建通知渠道（Android 8.0及以上需要）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channelId = "image_gallery_channel"
                    val channelName = "Image Gallery"
                    val importance = android.app.NotificationManager.IMPORTANCE_HIGH
                    val channel = android.app.NotificationChannel(channelId, channelName, importance)
                    notificationManager.createNotificationChannel(channel)
                }

                // 创建点击通知的PendingIntent
                val intent = Intent(context, ImageGalleryActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putStringArrayListExtra("extra_image_paths", list)
                }
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                    else
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )

                // 构建通知
                val notificationBuilder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.app.Notification.Builder(context, "image_gallery_channel")
                } else {
                    @Suppress("DEPRECATION")
                    android.app.Notification.Builder(context)
                }

                val notification = notificationBuilder
                    .setContentTitle("图片已捕获")
                    .setContentText("已捕获 ${list.size} 张图片，点击查看")
                    .setSmallIcon(android.R.drawable.ic_menu_gallery)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                // 显示通知
                notificationManager.notify(1001, notification)

                // 显示提示
                "已捕获 ${list.size} 张图片，请查看通知".overlayToast()
            }.onFailure {
                LogUtils.d(it)
                "截图失败，尝试请求授予屏幕录制后重试".overlayToast()
                AssistsWindowManager.showAll()

            }
        }

    }

    private val notificationListener = object : AssistsServiceListener {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            super.onAccessibilityEvent(event)
            if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                event.text.forEach {
                    CoroutineWrapper.launch {
                        "监听到通知：${it}".logAppend()
                        "监听到通知：${it}".overlayToast()
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
                        viewBinding.tvTitle.text = "进阶示例"
                    }
                }
            }
            return field
        }

    fun show() {
        if (!AssistsService.listeners.contains(this)) {
            AssistsService.listeners.add(this)
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