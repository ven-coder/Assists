package com.ven.assists.simple.common

import com.blankj.utilcode.util.TimeUtils
import com.ven.assists.utils.CoroutineWrapper
import kotlinx.coroutines.flow.MutableSharedFlow

object LogWrapper {
    var logCache = StringBuilder("")

    val logAppendValue = MutableSharedFlow<Pair<String, String>>()

    fun String.logAppend(): String {
        return logAppend(this)
    }

    fun logAppend(msg: CharSequence): String {
        if (logCache.isNotEmpty()) {
            logCache.append("\n")
        }
        if (logCache.length > 5000) {
            logCache.delete(0, logCache.length - 5000)
        }
        logCache.append(TimeUtils.getNowString())
        logCache.append("\n")
        logCache.append(msg)
        CoroutineWrapper.launch {
            logAppendValue.emit(Pair("\n${TimeUtils.getNowString()}\n$msg", logCache.toString()))
        }
        return msg.toString()
    }

    fun clearLog() {
        logCache = StringBuilder("")
        CoroutineWrapper.launch { logAppendValue.emit(Pair("", "")) }
    }

}