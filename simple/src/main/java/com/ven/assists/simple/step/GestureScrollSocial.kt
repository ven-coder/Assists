package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.blankj.utilcode.util.ScreenUtils
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.click
import com.ven.assists.AssistsCore.findFirstParentClickable
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.logNode
import com.ven.assists.service.AssistsService
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.simple.App
import com.ven.assists.simple.common.LogWrapper
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import kotlinx.coroutines.delay

class GestureScrollSocial : StepImpl() {
    override fun onImpl(collector: StepCollector) {
        collector.next(StepTag.STEP_1) {
            LogWrapper.logAppend("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                AssistsService.instance?.startActivity(this)
            }
            return@next Step.get(StepTag.STEP_2)
        }.next(StepTag.STEP_2) {
            AssistsCore.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > AssistsCore.getX(1080, 630) &&
                    screen.top > AssistsCore.getX(1920, 1850)
                ) {
                    LogWrapper.logAppend("已打开微信主页，点击【发现】")
                    it.parent.parent.click()
                    return@next Step.get(StepTag.STEP_3)
                }
            }
            if (AssistsCore.getPackageName() == App.TARGET_PACKAGE_NAME) {
                AssistsCore.back()
                return@next Step.get(StepTag.STEP_2)
            }
            if (it.repeatCount == 5) {
                return@next Step.get(StepTag.STEP_1)
            }

            return@next Step.repeat
        }.next(StepTag.STEP_3) {
            AssistsCore.findByText("朋友圈").forEach {
                it.logNode()
                val screen = it.getBoundsInScreen()
                if (screen.left > 140 && screen.top > 240) {
                    LogWrapper.logAppend("点击朋友圈")
                    it.findFirstParentClickable()?.let {
                        it.click()
                    }
                    return@next Step.get(StepTag.STEP_4)
                }
            }
            return@next Step.none
        }.next(StepTag.STEP_4) {
            AssistsCore.findByText("朋友圈").forEach {
                LogWrapper.logAppend("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            AssistsCore.findByText("朋友圈封面，再点一次可以改封面").forEach {
                LogWrapper.logAppend("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            AssistsCore.findByText("朋友圈封面，点按两次修改封面").forEach {
                LogWrapper.logAppend("已进入朋友圈")
                return@next Step.get(StepTag.STEP_5)
            }
            if (it.repeatCount == 5) {
                LogWrapper.logAppend("未进入朋友圈")
            }
            return@next Step.repeat
        }.next(StepTag.STEP_5) {
            AssistsWindowManager.nonTouchableByAll()
            delay(250)
            val x = ScreenUtils.getAppScreenWidth() / 2F
            val distance = ScreenUtils.getAppScreenHeight() / 2F
            val startY = distance + distance / 2F
            val endY = distance - distance / 2F
            LogWrapper.logAppend("滑动：$x/$startY,$x/$endY")
            AssistsCore.gesture(
                floatArrayOf(x, startY), floatArrayOf(x, endY), 0, 2000L
            )
            AssistsWindowManager.touchableByAll()
            delay(1000)
            return@next Step.repeat
        }
    }
}