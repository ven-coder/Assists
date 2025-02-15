package com.ven.assists.simple.overlays

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.longClick
import com.ven.assists.Assists.nodeGestureClick
import com.ven.assists.AssistsServiceListener
import com.ven.assists.AssistsWindowManager
import com.ven.assists.AssistsWindowWrapper
import com.ven.assists.simple.TestActivity
import com.ven.assists.simple.databinding.MainControlBinding
import com.ven.assists.simple.databinding.MainOverlayBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object OverlayMain : AssistsServiceListener {

    @SuppressLint("StaticFieldLeak")
    var viewBinding: MainOverlayBinding? = null
        private set
        get() {
            if (field == null) {
                field = MainOverlayBinding.inflate(LayoutInflater.from(Assists.service)).apply {
                    btnClick.setOnClickListener {
                        Assists.coroutine.launch {
                            ActivityUtils.getTopActivity()?.startActivity(Intent(Assists.service, TestActivity::class.java))
                            delay(1000)
                            Assists.findById("com.ven.assists.demo:id/btn_test").firstOrNull()?.click()
                        }
                    }
                    btnGestureClick.setOnClickListener {
                        Assists.coroutine.launch {
                            ActivityUtils.getTopActivity()?.startActivity(Intent(Assists.service, TestActivity::class.java))
                            delay(1000)
                            Assists.findById("com.ven.assists.demo:id/btn_test").firstOrNull()?.nodeGestureClick()
                        }
                    }
                    btnLongClick.setOnClickListener {
                        Assists.coroutine.launch {
                            ActivityUtils.getTopActivity()?.startActivity(Intent(Assists.service, TestActivity::class.java))
                            delay(1000)
                            Assists.findById("com.ven.assists.demo:id/btn_test").firstOrNull()?.longClick()
                        }
                    }
                    btnGestureLongClick.setOnClickListener {
                        Assists.coroutine.launch {
                            ActivityUtils.getTopActivity()?.startActivity(Intent(Assists.service, TestActivity::class.java))
                            delay(1000)
                            Assists.findById("com.ven.assists.demo:id/btn_test").firstOrNull()?.nodeGestureClick(duration = 1000)
                        }
                    }
                }
            }
            return field
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