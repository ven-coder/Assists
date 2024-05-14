package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.TimeUtils
import com.ven.assists.Assists
import com.ven.assists.Assists.click
import com.ven.assists.Assists.findByTags
import com.ven.assists.Assists.findFirstParentClickable
import com.ven.assists.Assists.getBoundsInScreen
import com.ven.assists.Assists.log
import com.ven.assists.Assists.paste
import com.ven.assists.simple.App
import com.ven.assists.simple.OverManager
import com.ven.assists.stepper.StepCollector
import com.ven.assists.stepper.StepImpl
import com.ven.assists.stepper.StepManager

class PublishSocial : StepImpl {
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
            Assists.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > 630 && screen.top > 1850) {
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

            if (it.isLastLoop) {
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
                        it.log()
                        it.click()
                        StepManager.execute(this::class.java, Step.STEP_4)
                    }
                }
                return@next
            }
        }.nextLoop(Step.STEP_4) {
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
            if (it.isLastLoop) {
                OverManager.log("未进入朋友圈")
            }
            false
        }.next(Step.STEP_5) {
            OverManager.log("点击拍照分享按钮")
            Assists.findByText("拍照分享").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_6)
            }
        }.next(Step.STEP_6) {
            OverManager.log("从相册选择")
            Assists.findByText("从相册选择").forEach {
                it.findFirstParentClickable()?.let {
                    it.click()
                    StepManager.execute(this::class.java, Step.STEP_7)
                }
            }
        }.next(Step.STEP_7) {
            Assists.findByText("我知道了").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_7)
                return@next
            }
            Assists.findByText("权限申请").forEach {
                Assists.findByText("确定").forEach {
                    it.click()
                    StepManager.execute(this::class.java, Step.STEP_7)
                    return@next
                }
            }
            Assists.findByText("允许").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_8)
                return@next
            }
            OverManager.log("选择第一张相片")
            StepManager.execute(this::class.java, Step.STEP_8, 1000)
        }.next(Step.STEP_8) {
            Assists.findByTags("android.support.v7.widget.RecyclerView").forEach {
                it.log()
                for (index in 0 until it.childCount) {
                    if (TextUtils.equals("android.widget.RelativeLayout", it.getChild(index).className)) {
                        it.getChild(index).findByTags("android.widget.CheckBox").forEach { it.click() }
                        StepManager.execute(this::class.java, Step.STEP_9)
                        return@next
                    }
                }
            }
            Assists.findByTags("androidx.recyclerview.widget.RecyclerView").forEach {
                for (index in 0 until it.childCount) {
                    if (TextUtils.equals("android.widget.RelativeLayout", it.getChild(index).className)) {
                        it.getChild(index).findByTags("android.widget.CheckBox").forEach { it.click() }
                        StepManager.execute(this::class.java, Step.STEP_9)
                        return@next
                    }
                }
            }
        }.next(Step.STEP_9) {
            OverManager.log("点击完成")
            Assists.findByText("完成").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_10)
                return@next
            }
        }.next(Step.STEP_10) {
            OverManager.log("输入发表内容")
            Assists.findByTags("android.widget.EditText").forEach {
                it.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                it.paste("${TimeUtils.getNowString()}: Assists发的一条私密朋友圈")
                StepManager.execute(this::class.java, Step.STEP_11)
                return@next
            }
        }.next(Step.STEP_11) {
            OverManager.log("点击谁可以看")
            Assists.findByText("谁可以看").forEach {
                it.findFirstParentClickable()?.let { it.click() }
                StepManager.execute(this::class.java, Step.STEP_12)
                return@next
            }
        }.next(Step.STEP_12) {
            OverManager.log("点击仅自己可见")
            Assists.findByText("仅自己可见").forEach {
                it.findFirstParentClickable()?.let { it.click() }
                StepManager.execute(this::class.java, Step.STEP_13)
                return@next
            }
        }.next(Step.STEP_13) {
            OverManager.log("点击完成")
            Assists.findByText("完成").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_14)
                return@next
            }
        }.next(Step.STEP_14) {
            OverManager.log("点击发表")
            Assists.findByText("发表").forEach {
                it.click()
                return@next
            }
        }
    }

}