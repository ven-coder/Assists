package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.base.Assists
import com.ven.assists.base.Assists.click
import com.ven.assists.base.Assists.findFirstParentClickable
import com.ven.assists.base.Assists.getBoundsInScreen
import com.ven.assists.base.Assists.log
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

class GestureScrollSocial : StepImpl {
    override fun onImpl(collector: StepCollector) {
        collector.next(Step.STEP_1) {
            OverManager.log("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                Assists.service?.startActivity(this)
            }
            StepManager.execute(this::class.java, Step.STEP_2)
        }.nextLoop(Step.STEP_2) {
            OverManager.log("检查是否已打开微信主页：\n剩余时间=${it.loopSurplusSecond}秒")
            Assists.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 630) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【发现】")
                    it.parent.parent.click()
                    StepManager.execute(this::class.java, Step.STEP_3)
                    return@nextLoop true
                }
            }
            if (Assists.getPackageName() == App.TARGET_PACKAGE_NAME) {
                Assists.back()
                StepManager.execute(this::class.java, Step.STEP_2)
                return@nextLoop true
            }
            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_1)
            }

            false
        }.next(Step.STEP_3) {
            Assists.findByText("朋友圈").forEach {
                it.log()
                val screen = it.getBoundsInScreen()
                if (screen.left > 140 && screen.top > 240) {
                    OverManager.log("点击朋友圈")
                    it.findFirstParentClickable()?.let {
                        it.click()
                        StepManager.execute(this::class.java, Step.STEP_4)
                    }
                    return@next
                }
            }
        }.nextLoop(Step.STEP_4) {
            OverManager.log("检查是否进入朋友圈：剩余时间=${it.loopSurplusSecond}秒")
            Assists.findByText("朋友圈").forEach {
                OverManager.log("已进入朋友圈")
                StepManager.execute(this::class.java, Step.STEP_5)
                return@nextLoop true
            }
            Assists.findByText("朋友圈封面，再点一次可以改封面").forEach {
                OverManager.log("已进入朋友圈")
                StepManager.execute(this::class.java, Step.STEP_5)
                return@nextLoop true
            }
            Assists.findByText("朋友圈封面，点按两次修改封面").forEach {
                OverManager.log("已进入朋友圈")
                StepManager.execute(this::class.java, Step.STEP_5)
                return@nextLoop true
            }
            if (it.loopSurplusSecond == 0f) {
                OverManager.log("未进入朋友圈")
            }
            false
        }.next(Step.STEP_5) {

            val x = ScreenUtils.getAppScreenWidth() / 2F
            val distance = ScreenUtils.getAppScreenHeight() / 2F
            val startY = distance + distance / 2F
            val endY = distance - distance / 2F
            OverManager.log("滑动：$x/$startY,$x/$endY")
            val delay = Assists.gesture(
                floatArrayOf(x, startY), floatArrayOf(x, endY), 0, 2000L
            )
            StepManager.execute(this::class.java, Step.STEP_5, StepManager.DEFAULT_STEP_DELAY + delay)
        }
    }
}