package com.ven.assists.stepper

class Step private constructor(val tag: Int) {
    companion object {
        val none = Step(-1)
        val repeat = Step(-2)

        fun get(tag: Int): Step {
            return Step(tag)
        }
    }

    var stepImpl: StepImpl? = null
}