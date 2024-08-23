package com.ven.assists.simple

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ThreadUtils
import com.ven.assists.Assists
import com.ven.assists.simple.databinding.SettingGuideBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.setStatusBarColor(this, Color.TRANSPARENT)
        Assists.coroutine.launch {
            delay(500)
            withContext(Dispatchers.Main) {
                SettingGuideBinding.inflate(layoutInflater).apply {
                    setContentView(root)
                    ivClose.setOnClickListener {
                        finish()
                    }
                }
            }
        }
    }
}
