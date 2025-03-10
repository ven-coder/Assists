package com.ven.assists.simple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.ven.assists.window.AssistsWindowManager.overlayToast
import com.ven.assists.simple.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    companion object {
        var scrollUp: (() -> Unit)? = null
        var scrollDown: (() -> Unit)? = null
    }

    val viewBinding: ActivityTestBinding by lazy {
        ActivityTestBinding.inflate(layoutInflater).apply {
            btnTest.setOnClickListener {
                "测试按钮被点击".overlayToast()
            }
            btnTest.setOnLongClickListener {
                "测试按钮被长按".overlayToast()
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        scrollUp = {
            viewBinding.scrollView.fullScroll(NestedScrollView.FOCUS_UP)
        }
        scrollDown = {
            viewBinding.scrollView.fullScroll(NestedScrollView.FOCUS_DOWN)
        }
    }
}