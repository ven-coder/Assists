package com.ven.assists.stepper
@Deprecated("已弃用，请使用com.ven.assists.stepperx.Step")
interface StepListener {
    fun onStepStart(step: StepOperator) {}
    fun onStepStop() {}
    fun onStepCatch(e:Throwable) {}
    fun onStep(step: StepOperator) {}
    fun onLoop(step: StepOperator) {}
    fun onIntercept(step: StepOperator): Step? {
        return null
    }
}