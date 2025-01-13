package com.ven.assists.stepper

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