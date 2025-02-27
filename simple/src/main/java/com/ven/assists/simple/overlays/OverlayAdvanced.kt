package com.ven.assists.simple.overlays

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.accessibility.AccessibilityEvent
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.containsText
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.getNodes
import com.ven.assists.Assists.logNode
import com.ven.assists.Assists.nodeGestureClick
import com.ven.assists.AssistsServiceListener
import com.ven.assists.AssistsWindowManager
import com.ven.assists.AssistsWindowManager.overlayToast
import com.ven.assists.AssistsWindowWrapper
import com.ven.assists.simple.OverManager
import com.ven.assists.simple.common.LogWrapper
import com.ven.assists.simple.common.LogWrapper.logAppend
import com.ven.assists.simple.databinding.AdvancedOverlayBinding
import com.ven.assists.simple.step.AntForestEnergy
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.simple.step.StepTag
import com.ven.assists.stepper.StepManager
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object OverlayAdvanced : AssistsServiceListener {


    var viewBinding: AdvancedOverlayBinding? = null
        private set
        get() {
            if (field == null) {
                field = AdvancedOverlayBinding.inflate(LayoutInflater.from(Assists.service)).apply {
                    btnAnswerWechatCall.setOnClickListener {
                        if (!Assists.serviceListeners.contains(answerWechatCallListener)) {
                            Assists.serviceListeners.add(answerWechatCallListener)
                        }
                        CoroutineWrapper.launch(isMain = true) {
                            OverlayLog.show()
                            "微信电话自动接听监听中...".logAppend()
                        }
                    }
                    btnOpenSocial.setOnClickListener {
                        OverlayLog.show()
                        StepManager.execute(OpenWechatSocial::class.java, StepTag.STEP_1, begin = true)
                    }
                    btnScrollSocial.setOnClickListener {
                        OverlayLog.show()

                        StepManager.execute(GestureScrollSocial::class.java, StepTag.STEP_1, begin = true)

                    }
                    btnPublishSocial.setOnClickListener {
                        OverlayLog.show()
                        StepManager.execute(PublishSocial::class.java, StepTag.STEP_1, begin = true)
                    }
                    btnClickBottomTab.setOnClickListener {
                        OverlayLog.show()

                        StepManager.execute(GestureBottomTab::class.java, StepTag.STEP_1, begin = true)

                    }
                    btnAntForestEnergy.setOnClickListener {
                        OverlayLog.show()

                        StepManager.execute(AntForestEnergy::class.java, StepTag.STEP_1, begin = true)

                    }
                    btnScrollContacts.setOnClickListener {
                        OverlayLog.show()
                        StepManager.execute(ScrollContacts::class.java, StepTag.STEP_1, begin = true)
                    }
                }
            }
            return field
        }

    private val answerWechatCallListener: AssistsServiceListener = object : AssistsServiceListener {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            super.onAccessibilityEvent(event)
            if (event.packageName == "com.tencent.mm") {
                var isInCall = false
                event.source?.getNodes()?.forEach {
                    if (it.containsText("邀请你语音通话") || it.containsText("邀请你视频通话")) {
                        it.getBoundsInScreen().let {
                            if (it.bottom < ScreenUtils.getScreenHeight() * 0.2) {
                                isInCall = true
                                StepManager.isStop = true
                            }
                            if (it.top > ScreenUtils.getScreenHeight() * 0.50 && it.bottom < ScreenUtils.getScreenHeight() * 0.8) {
                                isInCall = true
                                StepManager.isStop = true
                            }
                        }
                    }
                    if (isInCall && it.containsText("接听") && it.className == "android.widget.ImageButton") {
                        "收到微信电话，接听".overlayToast()
                        "收到微信电话，接听".logAppend()
                        it.click()
                    }
                    if (isInCall && it.containsText("接听") && it.className == "android.widget.Button") {
                        "收到微信电话，接听".overlayToast()
                        "收到微信电话，接听".logAppend()
                        CoroutineWrapper.launch { it.nodeGestureClick() }
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
                        viewBinding.tvTitle.text = "高级示例"
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