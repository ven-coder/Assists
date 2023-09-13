package com.ven.assist.step

import android.os.CountDownTimer
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.ven.assist.Assists

class StepOperator(
    val clazzName: String,
    val step: Int,
    val loopDuration: Long = 0,
    val loopInterval: Long = 250,
    val next: ((stepOperator: StepOperator) -> Boolean)? = null,
) {

    private var loopDownTimer: CountDownTimer? = null
    var loopSurplusTime: Long = 0
    var loopSurplusSecond: Float = 0f
    var data: StepData? = null

    fun execute(delay: Long) {
        if (StepManager.isStop) {
            Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
            return
        }
        next?.let {
            if (loopDuration == 0L) {
                ThreadUtils.runOnUiThreadDelayed({
                    if (StepManager.isStop) {
                        Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
                        return@runOnUiThreadDelayed
                    }
                    onStep(it)
                }, delay)
            } else {
                ThreadUtils.runOnUiThreadDelayed({
                    if (StepManager.isStop) {
                        loopDownTimer?.cancel()
                        loopDownTimer = null
                        Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
                        return@runOnUiThreadDelayed
                    }
                    loopDownTimer?.cancel()
                    loopDownTimer = object : CountDownTimer(loopDuration, loopInterval) {
                        override fun onTick(millisUntilFinished: Long) {
                            loopSurplusTime = millisUntilFinished
                            loopSurplusSecond = millisUntilFinished / 1000f
                            if (StepManager.isStop) Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
                            Assists.ListenerManager.stepListener.forEach { it.onLoop(this@StepOperator) }
                            if (onStep(it) || StepManager.isStop) {
                                cancel()
                                loopDownTimer = null
                            }
                        }

                        override fun onFinish() {
                            loopSurplusTime = 0
                            loopSurplusSecond = 0f
                            onStep(it)
                        }

                    }
                    loopDownTimer?.start()
                }, delay)
            }
        } ?: let {
            LogUtils.e("The execution logic for Step [$step] in the class [${clazzName}] has not been implemented. / 类[${clazzName}]中的步骤[${step}]未实现执行逻辑")
        }
    }

    private fun onStep(it: (stepOperator: StepOperator) -> Boolean): Boolean {
        Assists.ListenerManager.stepListener.forEach { if (it.onIntercept(this)) return true }
        Assists.ListenerManager.stepListener.forEach { it.onStep(this) }
        return it.invoke(this)
    }
}