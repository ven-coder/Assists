package com.ven.assists.stepper

import android.os.CountDownTimer
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.Assists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepOperator(
    val implClassName: String,
    val step: Int,
    val next: suspend (stepOperator: StepOperator) -> Step,
    val isRunCoroutineIO: Boolean = false,
    var data: Any? = null
) {
    var repeatCount = 0
        private set

    fun execute(delay: Long, data: Any? = null) {
        this.data = data
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
                repeatCount = 0
                StringBuilder().apply {
                    append("\n>>>>>>>>>>>>execute>>>>>>>>>>>")
                    append("\nStep.none（停止）")
                    append("\n")
                    Log.d(Assists.LOG_TAG, toString())
                }
            }

            Step.repeat -> {
                repeatCount++
                StepManager.execute(implClassName, step, delay = nextStep.delay)
            }

            else -> {
                repeatCount = 0
                nextStep.stepImplClass?.let {
                    StepManager.execute(it, nextStep.tag, data = nextStep.data, delay = nextStep.delay)
                } ?: let {
                    StepManager.execute(implClassName, nextStep.tag, data = nextStep.data, delay = nextStep.delay)
                }
            }
        }
    }

    private suspend fun onStep(next: suspend (stepOperator: StepOperator) -> Step): Step {
        StepManager.stepListeners.forEach { if (it.onIntercept(this)) return Step.none }
        StepManager.stepListeners.forEach { it.onStep(this) }
        return next.invoke(this)
    }
}