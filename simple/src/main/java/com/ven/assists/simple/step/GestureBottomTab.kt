package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.ven.assists.Assists
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.nodeGestureClick
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.Step
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

class GestureBottomTab : StepImpl() {
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
            Assists.findByText("微信").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 90) &&
                    screen.top > Assists.getY(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【微信】")
                    val delay = it.nodeGestureClick()
                    return@next Step.get(StepTag.STEP_3)
                }
            }
            if (Assists.getPackageName() == App.TARGET_PACKAGE_NAME) {
                Assists.back()
                return@next Step.repeat

            }
            if (it.repeatCount == 5) {
                StepManager.execute(this::class.java, StepTag.STEP_1)
                return@next Step.get(StepTag.STEP_1)
            }

            return@next Step.repeat
        }.next(StepTag.STEP_3) {
            OverManager.log("点击通讯录")
            Assists.findByText("通讯录").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 340) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【通讯录】")
                    val delay = it.nodeGestureClick()
                    return@next Step.get(StepTag.STEP_4)
                }
            }
            return@next Step.none
        }.next(StepTag.STEP_4) {
            OverManager.log("点击发现")
            Assists.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 630) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【发现】")
                    val delay = it.nodeGestureClick()
                    return@next Step.get(StepTag.STEP_5)
                }
            }
            return@next Step.none
        }.next(StepTag.STEP_5) {
            OverManager.log("点击我")
            Assists.findByText("我").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 920) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【我】")
                    val delay = it.nodeGestureClick()
                    return@next Step.get(StepTag.STEP_2)
                }
            }
            return@next Step.none
        }
    }
}