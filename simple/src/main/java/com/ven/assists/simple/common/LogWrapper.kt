package com.ven.assists.simple.common

import com.blankj.utilcode.util.TimeUtils
import kotlinx.coroutines.flow.MutableSharedFlow

object LogWrapper {
    var logCache = StringBuilder("")

    val logAppendValue = MutableSharedFlow<Pair<String, String>>()

    suspend fun logAppend(msg: CharSequence): String {

        if (logCache.isNotEmpty()) {
            logCache.append("\n")
        }
        if (logCache.length > 5000) {
            logCache.delete(0, logCache.length - 5000)
        }
        logCache.append(TimeUtils.getNowString())
        logCache.append("\n")
        logCache.append(msg)
        logAppendValue.emit(Pair("\n${TimeUtils.getNowString()}\n$msg", logCache.toString()))

        return msg.toString()
    }

    suspend fun clearLog() {
        logCache = StringBuilder("")
        logAppendValue.emit(Pair("", ""))
    }

}