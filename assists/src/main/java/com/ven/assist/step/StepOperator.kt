package com.ven.assist.step

import android.os.CountDownTimer
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.ven.assist.Assists

class StepOperator(
    private val clazzName: String,
    val step: Int,
    val loopDuration: Long = 0,
    val next: ((stepOperator: StepOperator) -> Boolean)? = null,
) {

    private var loopDownTimer: CountDownTimer? = null
    var loopSurplusTime: Long = 0
    var loopSurplusSecond: Float = 0f
    var data: Any? = null

    fun execute(delay: Long) {
        if (StepManager.isStop) {
            Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
            return
        }
        LogUtils.d("step->$clazzName:$step", "delay:$delay")

        next?.let {
            if (loopDuration == 0L) {
                ThreadUtils.runOnUiThreadDelayed({
                    if (StepManager.isStop) {
                        Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
                        return@runOnUiThreadDelayed
                    }
                    it.invoke(this@StepOperator)
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
                    loopDownTimer = object : CountDownTimer(loopDuration, 250) {
                        override fun onTick(millisUntilFinished: Long) {
                            loopSurplusTime = millisUntilFinished
                            loopSurplusSecond = millisUntilFinished / 1000f
                            if (StepManager.isStop) Assists.ListenerManager.stepListener.forEach { it.onStepStop() }
                            if (it.invoke(this@StepOperator) || StepManager.isStop) {
                                cancel()
                                loopDownTimer = null
                            }
                        }

                        override fun onFinish() {
                            loopSurplusTime = 0
                            loopSurplusSecond = 0f
                            it.invoke(this@StepOperator)
                        }

                    }
                    loopDownTimer?.start()
                }, delay)
            }
        } ?: let {
            LogUtils.e("The execution logic for Step [$step] in the class [${clazzName}] has not been implemented. / 类[${clazzName}]中的步骤[${step}]未实现执行逻辑")
        }
    }
}