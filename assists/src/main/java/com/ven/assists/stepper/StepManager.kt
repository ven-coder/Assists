package com.ven.assists.stepper

import android.util.Log
import com.ven.assists.Assists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.lang.reflect.InvocationTargetException


/**
 * 步骤管理器
 */
object StepManager {
    var DEFAULT_STEP_DELAY = 1000L

    var stepListeners: ArrayList<StepListener> = arrayListOf()

    private val stepCollector: HashMap<String, StepCollector> = hashMapOf()
    private var job = Job()
    var coroutine: CoroutineScope = CoroutineScope(job + Dispatchers.IO)
        private set
        get() {
            if (job.isCancelled || !job.isActive) {
                job = Job()
                field = CoroutineScope(job + Dispatchers.IO)
            }
            return field
        }
    var isStop = false
        set(value) {
            field = value
            if (field) {
                coroutine.cancel()
            }
        }

    fun <T : StepImpl> execute(stepImpl: Class<T>, stepTag: Int, delay: Long = DEFAULT_STEP_DELAY, begin: Boolean = false) {
        execute(stepImpl.name, stepTag, delay, begin)
    }


    fun execute(implClassName: String, stepTag: Int, delay: Long = DEFAULT_STEP_DELAY, begin: Boolean = false) {
        if (begin) isStop = false
        if (isStop) return
        Log.d(Assists.LOG_TAG, "execute->${implClassName}:$stepTag-delay:$delay")
        stepCollector[implClassName] ?: register(implClassName)
        stepCollector[implClassName]?.get(stepTag)?.execute(delay)
    }

    /**
     * 注册业务实现类
     */
    fun register(implClassName: String) {
        StepCollector(implClassName).let {
            val clazz = Class.forName(implClassName)
            val instance = clazz.getDeclaredConstructor().newInstance()
            val stepImpl = instance as StepImpl
            stepImpl.onImpl(it)
            stepCollector[implClassName] = it
        }
    }
}
