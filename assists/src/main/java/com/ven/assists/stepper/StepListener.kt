package com.ven.assists.stepper

interface StepListener {
    fun onStepStart() {}
    fun onStepStop() {}
    fun onStep(step: StepOperator) {}
    fun onLoop(step: StepOperator) {}
    fun onIntercept(step: StepOperator): Boolean {
        return false
    }
}