package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepData
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

/**
 * ScrollContacts为该业务场景的分类，作用是可在执行过程按按业务分类来执行
 */
class ScrollContacts : StepImpl {
    override fun onImpl(collector: StepCollector) {
        //1. 打开微信
        collector.next(Step.STEP_1) {//Step.STEP_1为自己定义的常量
            OverManager.log("启动微信")
            Intent().apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                Assists.service?.startActivity(this)
            }
            //执行下一步Step.STEP_2，this::class.java为当前StepImpl实现类的步骤逻辑，如果传其他的StepImpl就会执行指定的StepImpl逻辑
            StepManager.execute(this::class.java, Step.STEP_2)

        }.nextLoop(Step.STEP_2) {//2. 定义Step.STEP_2逻辑，nextLoop该方法会在指定时间内按指定间隔循环执行
            OverManager.log("检查是否已打开微信主页：\n剩余时间=${it.loopSurplusSecond}秒")
            //按文本查找元素
            Assists.findByText("通讯录").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > Assists.getX(1080, 340) &&
                    screen.top > Assists.getX(1920, 1850)
                ) {
                    OverManager.log("已打开微信主页，点击【通讯录】")
                    it.findFirstParentClickable()?.click()
                    //执行步骤STEP_3，data为自定义数据
                    StepManager.execute(this::class.java, Step.STEP_3, data = StepData(data = 1))
                    return@nextLoop true //返回true表示结束循环检查
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
            false//false表示继续循环
        }.next(Step.STEP_3) { step ->//3. 定义Step.STEP_3逻辑
            OverManager.log("滚动下一页：${step.data}")
            //
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