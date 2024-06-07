package com.ven.assists.stepper

import com.ven.assists.Assists
import com.ven.assists.Assists.click

class ScreenCaptureAutoEnable : StepImpl() {
    override fun onImpl(collector: StepCollector) {
        collector.next(1) {
            Assists.findByText("立即开始").firstOrNull()?.let {
                it.click()
                return@next Step.none
            }
            return@next Step.repeat
        }
    }
}