package com.ven.assists.stepper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class StepImpl {

    abstract fun onImpl(collector: StepCollector)
    suspend fun runIO(function: suspend () -> Unit) {
        withContext(Dispatchers.IO) { function.invoke() }
    }

    suspend fun runMain(function: suspend () -> Unit) {
        withContext(Dispatchers.Main) { function.invoke() }
    }
}