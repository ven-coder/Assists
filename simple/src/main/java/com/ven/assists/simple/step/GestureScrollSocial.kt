package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.logNode
import com.ven.assists.AssistsWindowManager
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager
import kotlinx.coroutines.delay

class GestureScrollSocial : StepImpl() {
    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) {
            OverManager.log("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                Assists.service?.startActivity(this)
            }
            return@next Step.get(StepTag.STEP_2)
        }.next(StepTag.STEP_2) {
            Assists.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 630) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【发现】")
                    it.parent.parent.click()
                    return@next Step.get(StepTag.STEP_3)
                }
            }
            if (Assists.getPackageName() == App.TARGET_PACKAGE_NAME) {
                Assists.back()
                return@next Step.get(StepTag.STEP_2)
            }
            if (it.repeatCount == 5) {
                return@next Step.get(StepTag.STEP_1)
            }

            return@next Step.repeat
        }.next(StepTag.STEP_3) {
            Assists.findByText("朋友圈").forEach {
                it.logNode()
                val screen = it.getBoundsInScreen()
                if (screen.left > 140 && screen.top > 240) {
                    OverManager.log("点击朋友圈")
                    it.findFirstParentClickable()?.let {
                        it.click()
                    }
                    return@next Step.get(StepTag.STEP_4)
                }
            }
            return@next Step.none
        }.next(StepTag.STEP_4) {
            Assists.findByText("朋友圈").forEach {
                OverManager.log("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            Assists.findByText("朋友圈封面，再点一次可以改封面").forEach {
                OverManager.log("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            Assists.findByText("朋友圈封面，点按两次修改封面").forEach {
                OverManager.log("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            if (it.repeatCount == 5) {
                OverManager.log("未进入朋友圈")
            }
            return@next Step.repeat
        }.next(StepTag.STEP_5) {
            AssistsWindowManager.switchNotTouchableAll()
            runIO { delay(250) }
            val x = ScreenUtils.getAppScreenWidth() / 2F
            val distance = ScreenUtils.getAppScreenHeight() / 2F
            val startY = distance + distance / 2F
            val endY = distance - distance / 2F
            OverManager.log("滑动：$x/$startY,$x/$endY")
            Assists.gesture(
                floatArrayOf(x, startY), floatArrayOf(x, endY), 0, 2000L
            )
            AssistsWindowManager.switchTouchableAll()
            runIO { delay(1000) }
            return@next Step.repeat
        }
    }
}