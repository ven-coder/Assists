package com.ven.assists.stepper

import android.util.Log
import com.ven.assists.Assists

/**
 * 步骤管理器
 */
object StepManager {
    var DEFAULT_STEP_DELAY = 1000L

    var stepListeners: ArrayList<StepListener> = arrayListOf()

    private val stepCollector: HashMap<String, StepCollector> = hashMapOf()
    var isStop = false
        set(value) {
            field = value
            if (field) {
                stepCollector.forEach { it.value.allStop() }
            }
        }

    /**
     * 执行步骤
     * @param stepImpl 执行的业务实现类
     * @param step 步骤序号
     * @param delay 步骤执行延迟时间，默认[Assists.Config.defaultStepDelay]
     * @param isBegin 是否是初始执行，true则会忽略[isStop]直接开始执行，false则会判断[isStop]是否停止
     */
    fun <T : StepImpl> execute(stepImpl: Class<T>, step: Int, delay: Long = DEFAULT_STEP_DELAY, data: StepData? = null, isBegin: Boolean = false) {
        if (isBegin) isStop = false
        if (isStop) return
        Log.d(Assists.LOG_TAG, "execute->${stepImpl.name}:$step-delay:$delay")
        stepCollector[stepImpl.name] ?: register(stepImpl)
        stepCollector[stepImpl.name]?.get(step)?.let {
            it.data = data
            it.execute(delay)
        }
    }

    /**
     * 注册业务实现类
     */
    fun <T : StepImpl> register(stepImpl: Class<T>) {
        StepCollector(stepImpl.name).let {
            stepCollector[stepImpl.name] = it
            stepImpl.newInstance().onImpl(it)
        }
    }
}
