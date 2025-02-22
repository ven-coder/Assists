package com.ven.assists.simple

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.ven.assists.Assists
import com.ven.assists.AssistsService
import com.ven.assists.AssistsServiceListener
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.databinding.ActivityMainBinding
import com.ven.assists.simple.databinding.MainControlBinding
import com.ven.assists.simple.overlays.OverlayMain
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists_mp.MPManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import android.widget.Button


class MainActivity : AppCompatActivity(), AssistsServiceListener {
    private var isActivityResumed = false
    val viewBind: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).apply {
            btnOption.setOnClickListener {
                if (Assists.isAccessibilityServiceEnabled()) {
                    OverManager.show()
                } else {
                    Assists.openAccessibilitySetting()
                    startActivity(Intent(this@MainActivity, SettingGuideActivity::class.java))
                }
            }
            btnOverlay.setOnClickListener {
                OverlayMain.onClose = {
                    OverlayMain.hide()
                    btnOverlay.setText("显示操作浮窗")
                }
                if (OverlayMain.showed) {
                    OverlayMain.hide()
                    btnOverlay.setText("显示操作浮窗")
                } else {
                    OverlayMain.show()
                    btnOverlay.setText("关闭操作浮窗")
                }
            }
        }
    }

    private var disableNotificationView: View? = null

    val mainControlBinding: MainControlBinding by lazy {
        MainControlBinding.inflate(layoutInflater).apply {
            root.layoutParams = ViewGroup.LayoutParams(-1, -1)
            btnScreenCapture.setOnClickListener {
                MPManager.request()
            }
            btnTakeScreenshot.setOnClickListener {
                runCatching {
                    val file = MPManager.takeScreenshot2File()
                    startActivity(Intent(this@MainActivity, ScreenshotReviewActivity::class.java).apply {
                        putExtra("path", file?.path)
                    })
                }.onFailure {
                    ToastUtils.showShort("截图失败，尝试请求授予屏幕录制后重试")
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
            btnListenerNotification.setOnClickListener {

            }
        }
    }

    private lateinit var drawingView: MultiTouchDrawingView

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        checkServiceEnable()
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
    }

    private fun checkServiceEnable() {
        if (!isActivityResumed) return
        if (Assists.isAccessibilityServiceEnabled()) {
            viewBind.btnOption.isVisible = false
            viewBind.btnOverlay.isVisible = true
        } else {
            viewBind.btnOption.isVisible = true
            viewBind.btnOverlay.isVisible = false
            viewBind.btnOverlay.text = "显示操作浮窗"
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
//        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//
//        }

    }

    override fun onServiceConnected(service: AssistsService) {
        onBackApp()
        checkServiceEnable()
    }

    private fun onBackApp() {
        CoroutineWrapper.launch {
            while (Assists.getPackageName() != packageName) {
                Assists.back()
                delay(500)
            }
        }
    }

    override fun onUnbind() {
        checkServiceEnable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.setStatusBarLightMode(this, true)
        setContentView(viewBind.root)
        Assists.serviceListeners.add(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Assists.serviceListeners.remove(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}