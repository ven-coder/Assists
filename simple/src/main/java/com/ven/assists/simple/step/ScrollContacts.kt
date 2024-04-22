package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.base.Assists
import com.ven.assists.base.Assists.click
import com.ven.assists.base.Assists.getBoundsInScreen
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepData
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

class ScrollContacts : StepImpl {
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
            Assists.findByText("通讯录").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 340) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【通讯录】")
                    it.parent.parent.click()
                    StepManager.execute(this::class.java, Step.STEP_3, data = StepData(data = 1))
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
        }.next(Step.STEP_3) { step ->
            OverManager.log("滚动下一页：${step.data}")
            Assists.findByTags("android.widget.ListView").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left >= 0 && screen.left < Assists.getX(1080, 1080) &&
                    screen.right >= Assists.getX(1080, 1080)
                ) {
                    it.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    if ((step.data as StepData).data as Int >= 5) {
                        OverManager.log("停止滚动")
                        return@next
                    }
                    StepManager.execute(this::class.java, Step.STEP_3, data = StepData(data = ((step.data as StepData).data as Int) + 1))
                    return@next
                }

            }
            Assists.findByTags("androidx.recyclerview.widget.RecyclerView").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left >= 0 && screen.left < Assists.getX(1080, 1080) &&
                    screen.right >= Assists.getX(1080, 1080)
                ) {
                    it.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    if ((step.data as StepData).data as Int >= 5) {
                        OverManager.log("停止滚动")
                        return@next
                    }
                    StepManager.execute(this::class.java, Step.STEP_3, data = StepData(data = ((step.data as StepData).data as Int) + 1))
                    return@next
                }
            }
        }
    }
}