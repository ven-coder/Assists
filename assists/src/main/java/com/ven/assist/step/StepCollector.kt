package com.ven.assist.step

class StepCollector(private val clazzName: String) {

    private val stepOperatorMap: HashMap<Int, StepOperator> = hashMapOf()

    fun get(step: Int): StepOperator {
        stepOperatorMap[step]?.let { return it } ?: throw RuntimeException("The class $clazzName does not have an implementation logic for Step $step. / 类${clazzName}中的没有步骤${step}的实现逻辑")
    }

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

    fun nextLoop(
        step: Int,
        loopDuration: Long = 5000,
        next: (stepOperator: StepOperator) -> Boolean
    ): StepCollector {
        stepOperatorMap[step] = StepOperator(clazzName, step, loopDuration, next)
        return this
    }
}