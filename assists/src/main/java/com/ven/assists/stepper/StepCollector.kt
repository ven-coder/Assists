package com.ven.assists.stepper

/**
 * 步骤收集器
 */
class StepCollector(private val clazzName: String) {

    private val stepOperatorMap: HashMap<Int, StepOperator> = hashMapOf()

    fun get(step: Int): StepOperator {
        stepOperatorMap[step]?.let { return it } ?: throw RuntimeException("The class $clazzName does not have an implementation logic for Step $step. / 类${clazzName}中的没有步骤${step}的实现逻辑")
    }

    /**
     * 单次步骤
     * @param step  步骤序号
     * @param isRunCoroutineIO 是否在IO协程中执行
     * @param next 步骤逻辑接口
     */
    fun next(
        step: Int,
        isRunCoroutineIO: Boolean = false,
        next: (stepOperator: StepOperator) -> Unit
    ): StepCollector {
        nextLoop(
            step = step,
            loopMaxCount = 1,
            isRunCoroutineIO = isRunCoroutineIO,
            next = {
                next.invoke(it)
                true
            })
        return this
    }

    /**
     * 循环步骤
     * @param step 步骤序号
     * @param loopMaxCount 最大循环次数
     * @param isRunCoroutineIO 是否在IO协程中执行
     * @param next 步骤逻辑，需要返回[kotlin.Boolean]，false继续循环，true终止循环
     */
    fun nextLoop(
        step: Int,
        loopMaxCount: Int = 5,
        isRunCoroutineIO: Boolean = false,
        next: (stepOperator: StepOperator) -> Boolean
    ): StepCollector {
        stepOperatorMap[step] = StepOperator(
            clazzName,
            step,
            loopMaxCount = loopMaxCount,
            isRunCoroutineIO = isRunCoroutineIO,
            next = next
        )
        return this
    }

    fun allStop() {
        stepOperatorMap.forEach { it.value.stop() }
    }
}