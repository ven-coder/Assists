package com.ven.assists.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CoroutineWrapper {
    private var job = Job()
    private var coroutine: CoroutineScope = CoroutineScope(job + Dispatchers.IO)
    fun launch(isMain: Boolean = false, block: suspend CoroutineScope.() -> Unit): Job {
        return coroutine.launch(block = block, context = if (isMain) Dispatchers.Main else Dispatchers.IO)
    }
}

suspend fun <T> runMain(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Main, block = block)
}

suspend fun <T> runIO(block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block = block)
}