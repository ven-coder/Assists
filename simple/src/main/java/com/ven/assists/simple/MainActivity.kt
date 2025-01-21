package com.ven.assists.simple

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.blankj.utilcode.util.BarUtils
import com.ven.assists.Assists
import com.ven.assists.AssistsService
import com.ven.assists.AssistsServiceListener
import com.ven.assists.MediaProjectionServiceManager
import com.ven.assists.simple.databinding.ActivityMainBinding
import com.ven.assists.simple.databinding.MainControlBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), AssistsServiceListener {
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
        }
    }

    val mainControlBinding: MainControlBinding by lazy {
        MainControlBinding.inflate(layoutInflater).apply {
            root.layoutParams = ViewGroup.LayoutParams(-1, -1)
            btnScreenCapture.setOnClickListener { MediaProjectionServiceManager.request() }
        }
    }

    override fun onResume() {
        super.onResume()
        checkServiceEnable()
    }

    private fun checkServiceEnable() {
        if (Assists.isAccessibilityServiceEnabled()) {
            viewBind.flContainer.indexOfChild(mainControlBinding.root).let {
                if (it == -1) {
                    viewBind.flContainer.addView(mainControlBinding.root)
                }
            }
            viewBind.btnOption.isVisible = false
        } else {
            viewBind.flContainer.indexOfChild(mainControlBinding.root).let {
                if (it > -1) {
                    viewBind.flContainer.removeView(mainControlBinding.root)
                }
            }
            viewBind.btnOption.isVisible = true
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
        if (event.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {

        }
    }

    override fun onServiceConnected(service: AssistsService) {
        onBackApp()
    }

    private fun onBackApp() {
        Assists.coroutine.launch {
            while (Assists.getPackageName() != packageName) {
                Assists.back()
                delay(500)
            }
        }
    }

    override fun onUnbind() {
        OverManager.clear()
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