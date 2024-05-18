package com.ven.assists.stepper

abstract class StepImpl {

    abstract fun onImpl(collector: StepCollector)

    fun next(stepTag: Int, delay: Long = StepManager.DEFAULT_STEP_DELAY) {
        StepManager.execute(this::class.java, stepTag, delay)
    }
}