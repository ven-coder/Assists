package com.ven.assists.stepper

import android.view.View
import java.util.concurrent.ConcurrentHashMap

/**
 * 步骤收集器
 */
class StepCollector(private val implClassName: String) {

    private val stepOperatorMap: ConcurrentHashMap<Int, StepOperator> = ConcurrentHashMap()

    fun get(stepTag: Int): StepOperator {
        stepOperatorMap[stepTag]?.let { return it }
            ?: throw RuntimeException("The class $implClassName does not have an implementation logic for Step $stepTag. / 类${implClassName}中的没有步骤${stepTag}的实现逻辑")
    }

    /**
     * 循环步骤
     * @param stepTag 步骤序号
     * @param isRunCoroutineIO 是否在IO协程中执行
     * @param next 步骤逻辑
     */
    fun next(
        stepTag: Int,
        isRunCoroutineIO: Boolean = true,
        next: suspend (stepOperator: StepOperator) -> Step
    ): StepCollector {
        stepOperatorMap[stepTag] = StepOperator(
            implClassName,
            stepTag,
            isRunCoroutineIO = isRunCoroutineIO,
            next = next
        )
        return this
    }
}