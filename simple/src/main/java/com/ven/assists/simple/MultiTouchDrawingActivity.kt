package com.ven.assists.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.ven.assists.simple.databinding.ActivityMultiTouchDrawingBinding
import com.ven.assists.simple.databinding.ActivityTestBinding

class MultiTouchDrawingActivity : AppCompatActivity() {
    private val viewBinding: ActivityMultiTouchDrawingBinding by lazy {
        ActivityMultiTouchDrawingBinding.inflate(layoutInflater).apply {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
    }
}