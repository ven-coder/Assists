package com.ven.assists.stepper

import android.os.CountDownTimer
import com.blankj.utilcode.util.LogUtils

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
    var delayDownTimer: CountDownTimer? = null

    fun execute(delay: Long) {
        if (StepManager.isStop) {
            StepManager.stepListeners.forEach { it.onStepStop() }
            return
        }
        next?.let {
            startDelay(delay, it)
        } ?: let {
            LogUtils.e("The execution logic for Step [$step] in the class [${clazzName}] has not been implemented. / 类[${clazzName}]中的步骤[${step}]未实现执行逻辑")
        }
    }

    fun stop() {
        delayDownTimer?.cancel()
        loopDownTimer?.cancel()
    }

    private fun startDelay(delay: Long, next: (stepOperator: StepOperator) -> Boolean) {
        if (delay == 0L) {
            startLoopExecute(next)
            return
        }
        delayDownTimer?.cancel()
        delayDownTimer = object : CountDownTimer(delay, 1000) {
            override fun onTick(millisUntilFinished: Long) {

            }

            override fun onFinish() {
                startLoopExecute(next)
            }
        }.start()
    }

    private fun startLoopExecute(next: (stepOperator: StepOperator) -> Boolean) {
        if (StepManager.isStop) {
            loopDownTimer?.cancel()
            loopDownTimer = null
            StepManager.stepListeners.forEach { it.onStepStop() }
            return
        }

        if (loopDuration == 0L) {
            onStep(next)
            return
        }

        loopDownTimer?.cancel()
        loopDownTimer = object : CountDownTimer(loopDuration, loopInterval) {
            override fun onTick(millisUntilFinished: Long) {
                loopSurplusTime = millisUntilFinished
                loopSurplusSecond = millisUntilFinished / 1000f
                if (StepManager.isStop) StepManager.stepListeners.forEach { it.onStepStop() }
                StepManager.stepListeners.forEach { it.onLoop(this@StepOperator) }
                if (onStep(next) || StepManager.isStop) {
                    cancel()
                    loopDownTimer = null
                }
            }

            override fun onFinish() {
                loopSurplusTime = 0
                loopSurplusSecond = 0f
                onStep(next)
            }

        }.start()
    }

    private fun onStep(it: (stepOperator: StepOperator) -> Boolean): Boolean {
        StepManager.stepListeners.forEach { if (it.onIntercept(this)) return true }
        StepManager.stepListeners.forEach { it.onStep(this) }
        return it.invoke(this)
    }
}