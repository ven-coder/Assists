package com.ven.assists.stepper

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.ven.assists.AssistsCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * 步骤操作器
 * 负责单个步骤的执行控制、状态管理和异常处理
 * 支持步骤的重复执行、链式调用和协程调度
 *
 * @property implClassName 步骤实现类的完整类名
 * @property step 步骤标识
 * @property next 步骤执行的核心逻辑，返回下一个要执行的步骤
 * @property isRunCoroutineIO 是否在IO线程执行，默认为false（在主线程执行）
 * @property data 步骤执行时的附加数据
 */
class StepOperator(
    val implClassName: String,
    val step: Int,
    val next: suspend (stepOperator: StepOperator) -> Step,
    val isRunCoroutineIO: Boolean = false,
    var data: Any? = null
) {
    /**
     * 步骤重复执行的次数
     * 当步骤返回Step.repeat时会增加计数
     * 当步骤返回其他Step时重置为0
     */
    var repeatCount = 0
        private set

    /**
     * 执行步骤
     * 在协程中执行步骤，支持延迟执行和异常处理
     *
     * @param delay 执行前的延迟时间（毫秒）
     * @param data 可选的步骤数据，会覆盖原有的data属性
     */
    fun execute(delay: Long, data: Any? = null) {
        this.data = data
        if (StepManager.isStop) {
            StepManager.stepListeners?.onStepStop()
            return
        }
        StepManager.coroutine.launch {
            runCatching {
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
            }.onFailure {
                if (it is CancellationException) {
                    LogUtils.e("步骤执行异常: 主动停止")
                    return@launch
                }
                LogUtils.e("步骤执行异常", it)
                StepManager.stepListeners?.onStepCatch(it)
            }
        }
    }

    /**
     * 处理下一个步骤
     * 根据步骤的返回值决定后续操作：
     * - Step.none: 停止执行
     * - Step.repeat: 重复当前步骤
     * - 其他: 执行指定的下一个步骤
     *
     * @param nextStep 下一个要执行的步骤
     */
    private fun onNextStep(nextStep: Step) {
        when (nextStep) {
            Step.none -> {
                repeatCount = 0
                StringBuilder().apply {
                    append("\n>>>>>>>>>>>>execute>>>>>>>>>>>")
                    append("\nStep.none（停止）")
                    append("\n")
                    Log.d(AssistsCore.LOG_TAG, toString())
                }
            }

            Step.repeat -> {
                repeatCount++
                StepManager.execute(implClassName, step, delay = nextStep.delay, data = nextStep.data)
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

    /**
     * 执行步骤的核心逻辑
     * 处理步骤的生命周期回调和拦截逻辑
     *
     * @param next 步骤执行的具体逻辑
     * @return 下一个要执行的步骤
     */
    private suspend fun onStep(next: suspend (stepOperator: StepOperator) -> Step): Step {
        StepManager.stepListeners?.onStepStart(this)
        StepManager.stepListeners?.onIntercept(this)?.let { return it }
        return next.invoke(this)
    }
}