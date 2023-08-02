package com.ven.assists.simple.step

import android.content.ComponentName
import android.content.Intent
import android.text.TextUtils
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.TimeUtils
import com.ven.assist.Assists
import com.ven.assist.ext.click
import com.ven.assist.ext.getBoundsInScreen
import com.ven.assist.ext.logToText
import com.ven.assist.ext.pasteText
import com.ven.assist.step.StepCollector
import com.ven.assist.step.StepImpl
import com.ven.assist.step.StepManager
import com.ven.assist.ui.UIOperate
import com.ven.assists.simple.OverManager

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
            OverManager.log("检查是否已打开微信主页：\n剩余时间=${it.loopSurplusSecond}秒")
            UIOperate.findByText("发现").forEach {
                val screen = it.getBoundsInScreen()
                if (screen.left > 630 && screen.top > 1850) {
                    OverManager.log("已打开微信主页，点击【发现】")
                    it.parent.parent.click()
                    StepManager.execute(this::class.java, Step.STEP_3)
                    return@nextLoop true
                }
            }

            if (0f==it.loopSurplusSecond){
                StepManager.execute(this::class.java, Step.STEP_1)
            }

            false
        }.next(Step.STEP_3) {
            UIOperate.findByText("朋友圈").forEach {
                it.logToText()
                val screen = it.getBoundsInScreen()
                if (screen.left > 140 && screen.top > 240) {
                    OverManager.log("点击朋友圈")
                    UIOperate.findParentClickable(it) {
                        it.logToText()
                        it.click()
                        StepManager.execute(this::class.java, Step.STEP_4)
                    }
                }
                return@next
            }
        }.nextLoop(Step.STEP_4) {
            OverManager.log("检查是否进入朋友圈：剩余时间=${it.loopSurplusSecond}秒")
            UIOperate.findByText("朋友圈封面，再点一次可以改封面").forEach {
                OverManager.log("已进入朋友圈")
                StepManager.execute(this::class.java, Step.STEP_5)
                return@nextLoop true
            }
            UIOperate.findByText("朋友圈封面，点按两次修改封面").forEach {
                OverManager.log("已进入朋友圈")
                StepManager.execute(this::class.java, Step.STEP_5)
                return@nextLoop true
            }
            if (it.loopSurplusSecond == 0f) {
                OverManager.log("未进入朋友圈")
            }
            false
        }.next(Step.STEP_5) {
            OverManager.log("点击拍照分享按钮")
            UIOperate.findByText("拍照分享").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_6)
            }
        }.next(Step.STEP_6) {
            OverManager.log("从相册选择")
            UIOperate.findByText("从相册选择").forEach {
                UIOperate.findParentClickable(it) {
                    it.click()
                    StepManager.execute(this::class.java, Step.STEP_7)
                }
            }
        }.next(Step.STEP_7) {
            UIOperate.findByText("我知道了").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_7)
                return@next
            }
            UIOperate.findByText("权限申请").forEach {
                UIOperate.findByText("确定").forEach {
                    it.click()
                    StepManager.execute(this::class.java, Step.STEP_7)
                    return@next
                }
            }
            UIOperate.findByText("允许").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_8)
                return@next
            }
            OverManager.log("选择第一张相片")
            StepManager.execute(this::class.java, Step.STEP_8, 1000)
        }.next(Step.STEP_8) {
            UIOperate.findByTags("android.support.v7.widget.RecyclerView").forEach {
                it.logToText()
                for (index in 0 until it.childCount) {
                    if (TextUtils.equals("android.widget.RelativeLayout", it.getChild(index).className)) {
                        UIOperate.findByTags("android.widget.CheckBox", it.getChild(index)).forEach { it.click() }
                        StepManager.execute(this::class.java, Step.STEP_9)
                        return@next
                    }
                }
            }
            UIOperate.findByTags("androidx.recyclerview.widget.RecyclerView").forEach {
                for (index in 0 until it.childCount) {
                    if (TextUtils.equals("android.widget.RelativeLayout", it.getChild(index).className)) {
                        UIOperate.findByTags("android.widget.CheckBox", it.getChild(index)).forEach { it.click() }
                        StepManager.execute(this::class.java, Step.STEP_9)
                        return@next
                    }
                }
            }
        }.next(Step.STEP_9) {
            OverManager.log("点击完成")
            UIOperate.findByText("完成").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_10)
                return@next
            }
        }.next(Step.STEP_10) {
            OverManager.log("输入发表内容")
            UIOperate.findByTags("android.widget.EditText").forEach {
                it.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                it.pasteText("${TimeUtils.getNowString()}: Assists发的一条私密朋友圈")
                StepManager.execute(this::class.java, Step.STEP_11)
                return@next
            }
        }.next(Step.STEP_11) {
            OverManager.log("点击谁可以看")
            UIOperate.findByText("谁可以看").forEach {
                UIOperate.findParentClickable(it) { it.click() }
                StepManager.execute(this::class.java, Step.STEP_12)
                return@next
            }
        }.next(Step.STEP_12) {
            OverManager.log("点击仅自己可见")
            UIOperate.findByText("仅自己可见").forEach {
                UIOperate.findParentClickable(it) { it.click() }
                StepManager.execute(this::class.java, Step.STEP_13)
                return@next
            }
        }.next(Step.STEP_13) {
            OverManager.log("点击完成")
            UIOperate.findByText("完成").forEach {
                it.click()
                StepManager.execute(this::class.java, Step.STEP_14)
                return@next
            }
        }.next(Step.STEP_14) {
            OverManager.log("点击发表")
            UIOperate.findByText("发表").forEach {
                it.click()
                return@next
            }
        }
    }

}