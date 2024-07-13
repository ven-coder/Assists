package com.ven.assists.stepper

class Step private constructor(
    val tag: Int,
    val stepImpl: Class<Any>? = null,
    var data: Any? = null,
    var delay: Long = StepManager.DEFAULT_STEP_DELAY,
) {
    companion object {
        val none = Step(-1)
        val repeat = Step(-2)
        fun get(tag: Int, stepImpl: Class<Any>? = null, data: Any? = null, delay: Long = StepManager.DEFAULT_STEP_DELAY): Step {
            return Step(tag, stepImpl = stepImpl, data = data, delay = delay)
        }
    }
}