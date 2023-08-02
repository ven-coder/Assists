package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import com.ven.assist.Assists
import com.ven.assist.ext.clickScreen
import com.ven.assist.ext.getBoundsInScreen
import com.ven.assist.step.StepCollector
import com.ven.assist.step.StepImpl
import com.ven.assist.step.StepManager
import com.ven.assist.ui.UIOperate
import com.ven.assists.simple.OverManager

class GestureBottomTab : StepImpl {
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
            UIOperate.findByText("微信").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > UIOperate.getX(1080, 90) &&
                    screen.top > UIOperate.getY(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【微信】")
                    val delay = it.clickScreen()
                    StepManager.execute(this::class.java, Step.STEP_3, delay + Assists.Config.defaultStepDelay)
                    return@nextLoop true
                }
            }

            if (0f == it.loopSurplusSecond) {
                StepManager.execute(this::class.java, Step.STEP_1)
            }

            false
        }.next(Step.STEP_3) {
            OverManager.log("点击通讯录")
            UIOperate.findByText("通讯录").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > UIOperate.getX(1080, 340) &&
                    screen.top > UIOperate.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【通讯录】")
                    val delay = it.clickScreen()
                    StepManager.execute(this::class.java, Step.STEP_4, delay + Assists.Config.defaultStepDelay)
                    return@next
                }
            }
        }.next(Step.STEP_4) {
            OverManager.log("点击发现")
            UIOperate.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > UIOperate.getX(1080, 630) &&
                    screen.top > UIOperate.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【发现】")
                    val delay = it.clickScreen()
                    StepManager.execute(this::class.java, Step.STEP_5, delay + Assists.Config.defaultStepDelay)
                    return@next
                }
            }
        }.next(Step.STEP_5) {
            OverManager.log("点击我")
            UIOperate.findByText("我").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > UIOperate.getX(1080, 920) &&
                    screen.top > UIOperate.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【我】")
                    val delay = it.clickScreen()
                    StepManager.execute(this::class.java, Step.STEP_2, delay + Assists.Config.defaultStepDelay)
                    return@next
                }
            }
        }
    }
}