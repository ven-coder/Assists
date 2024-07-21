package com.ven.assists.stepper

class Step private constructor(
    val tag: Int,
    val stepImplClass: String? = null,
    var data: Any? = null,
    var delay: Long = StepManager.DEFAULT_STEP_DELAY,
) {
    companion object {
        val none = Step(-1)
        val repeat = Step(-2)
        fun get(tag: Int, stepImpl: Class<*>? = null, data: Any? = null, delay: Long = StepManager.DEFAULT_STEP_DELAY): Step {
            return Step(tag, stepImplClass = stepImpl?.name, data = data, delay = delay)
        }

        fun nextStepImpl(tag: Int, stepImpl: String? = null, data: Any? = null, delay: Long = StepManager.DEFAULT_STEP_DELAY): Step {
            return Step(tag, stepImplClass = stepImpl, data = data, delay = delay)
        }
    }
}