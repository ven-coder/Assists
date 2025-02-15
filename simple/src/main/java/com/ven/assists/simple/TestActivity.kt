package com.ven.assists.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.ven.assists.simple.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {
    val viewBinding: ActivityTestBinding by lazy {
        ActivityTestBinding.inflate(layoutInflater).apply {
            btnTest.setOnClickListener {
                ToastUtils.showShort("测试按钮被点击")
            }
            btnTest.setOnLongClickListener {
                ToastUtils.showShort("测试按钮被长按")
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }
}