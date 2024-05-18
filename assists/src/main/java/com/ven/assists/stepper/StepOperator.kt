package com.ven.assists.stepper

import android.os.CountDownTimer
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepOperator(
    val implClassName: String,
    val step: Int,
    val next: (stepOperator: StepOperator) -> Step,
    val isRunCoroutineIO: Boolean = false
) {
    var repeatCount = 0
        private set

    fun execute(delay: Long) {
        if (StepManager.isStop) {
            StepManager.stepListeners.forEach { it.onStepStop() }
            return
        }
        StepManager.coroutine.launch {
            delay(delay)
            if (isRunCoroutineIO) {
                val nextStep = onStep(next)
                onNextStep(nextStep)
            } else {
                var nextStep: Step
                withContext(Dispatchers.Main) {
                    nextStep = onStep(next)
                }
                onNextStep(nextStep)
            }
        }
    }

    private fun onNextStep(nextStep: Step) {
        when (nextStep) {
            Step.none -> {

            }

            Step.repeat -> {
                StepManager.execute(implClassName, step)
            }

            else -> {
                nextStep.stepImpl?.let {
                    StepManager.execute(it::class.java, nextStep.tag)
                } ?: let {
                    StepManager.execute(implClassName, nextStep.tag)
                }
            }
        }
    }

    private fun onStep(next: (stepOperator: StepOperator) -> Step): Step {
        StepManager.stepListeners.forEach { if (it.onIntercept(this)) return Step.none }
        StepManager.stepListeners.forEach { it.onStep(this) }
        repeatCount++
        return next.invoke(this)
    }
}