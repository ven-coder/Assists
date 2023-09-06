package com.ven.assist.step

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
     * @param next 步骤逻辑接口
     */
    fun next(
        step: Int,
        next: (stepOperator: StepOperator) -> Unit
    ): StepCollector {
        stepOperatorMap[step] = StepOperator(clazzName, step, next = {
            next.invoke(it)
            true
        })
        return this
    }

    /**
     * 循环步骤
     * @param step 步骤序号
     * @param loopDuration 循环时长
     * @param next 步骤逻辑接口，接口中需要返回[kotlin.Boolean]，false继续循环，true终止循环
     */
    fun nextLoop(
        step: Int,
        loopDuration: Long = 5000,
        loopInterval: Long = 250,
        next: (stepOperator: StepOperator) -> Boolean
    ): StepCollector {
        stepOperatorMap[step] = StepOperator(clazzName, step, loopDuration = loopDuration, loopInterval = loopInterval, next)
        return this
    }
}