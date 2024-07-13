package com.ven.assists.stepper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class StepImpl {

    abstract fun onImpl(collector: StepCollector)

    /**
     * IO协程执行任务
     */
    suspend fun runIO(function: suspend () -> Unit) {
        withContext(Dispatchers.IO) { function.invoke() }
    }

    /**
     * 主协程执行任务
     */
    suspend fun runMain(function: suspend () -> Unit) {
        withContext(Dispatchers.Main) { function.invoke() }
    }
}