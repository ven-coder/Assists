package com.ven.assists.stepper

import android.os.CountDownTimer
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StepOperator(
    val clazzName: String,
    val step: Int,
    val loopMaxCount: Int = 5,
    val next: ((stepOperator: StepOperator) -> Boolean)? = null,
    val isRunCoroutineIO: Boolean = false
) {
    var data: StepData? = null
    var loopCount = 0
        private set
    var isLastLoop = false
        private set

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
    }

    private fun startDelay(delay: Long, next: (stepOperator: StepOperator) -> Boolean) {

        StepManager.coroutine.launch {
            delay(delay)

            while (loopCount < loopMaxCount) {
                loopCount++
                isLastLoop = loopCount >= loopMaxCount
                if (isRunCoroutineIO) {
                    val result = onStep(next)
                    if (result) break
                } else {
                    var result: Boolean
                    withContext(Dispatchers.Main) {
                        result = onStep(next)
                    }
                    if (result) break;
                }
            }
        }
    }

    private fun onStep(it: (stepOperator: StepOperator) -> Boolean): Boolean {
        StepManager.stepListeners.forEach { if (it.onIntercept(this)) return true }
        StepManager.stepListeners.forEach { it.onStep(this) }
        return it.invoke(this)
    }
}