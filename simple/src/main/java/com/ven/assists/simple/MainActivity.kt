package com.ven.assists.simple

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ThreadUtils
import com.ven.assists.base.Assists
import com.ven.assists.base.AssistsService
import com.ven.assists.base.AssistsServiceListener
import com.ven.assists.base.AssistsWindowManager
import com.ven.assists.base.databinding.ViewMinimizeBinding
import com.ven.assists.simple.databinding.ActivityMainBinding
import com.ven.assists.simple.databinding.ViewMainOverBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), AssistsServiceListener {
    val viewBind: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater).apply {
            btnOption.setOnClickListener {
                if (Assists.isAccessibilityServiceEnabled()) {
                    OverManager.show()
                } else {
                    Assists.openAccessibilitySetting()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkServiceEnable()
    }

    private fun checkServiceEnable() {
        if (Assists.isAccessibilityServiceEnabled()) {
            viewBind.btnOption.text = "显示操作浮窗"
        } else {
            viewBind.btnOption.text = "开启服务"
        }
    }

    override fun onServiceConnected(service: AssistsService) {
        GlobalScope.launch {
            onBackApp()
        }
    }

    private suspend fun onBackApp() {
        flow<String> {
            while (Assists.getPackageName() != packageName) {
                Assists.back()
                delay(500)
            }
        }.flowOn(Dispatchers.IO).collect {}
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

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}