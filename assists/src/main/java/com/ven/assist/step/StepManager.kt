package com.ven.assist.step

import android.util.Log
import com.ven.assist.Assists

/**
 * 步骤管理器
 */
object StepManager {

    private val stepCollector: HashMap<String, StepCollector> = hashMapOf()
    var isStop = false

    /**
     * 开始执行，仅用于开始位置执行，如果执行过程调用会导致步骤无法停止
     * @param stepImpl 执行的业务实现类
     * @param step 步骤序号
     * @param delay 步骤执行延迟时间，默认[Assists.Config.defaultStepDelay]
     */
    private fun <T : StepImpl> beginExecute(stepImpl: Class<T>, step: Int, delay: Long = Assists.Config.defaultStepDelay, data: Any? = null): StepManager {
        isStop = false
        execute(stepImpl, step, delay, data)
        return this
    }

    /**
     * 执行步骤
     * @param stepImpl 执行的业务实现类
     * @param step 步骤序号
     * @param delay 步骤执行延迟时间，默认[Assists.Config.defaultStepDelay]
     * @param isBegin 是否是初始执行，true则会忽略[isStop]直接开始执行，false则会判断[isStop]是否停止
     */
    fun <T : StepImpl> execute(stepImpl: Class<T>, step: Int, delay: Long = Assists.Config.defaultStepDelay, data: Any? = null, isBegin: Boolean = false) {
        if (isBegin) isStop = false
        if (isStop) return
        Log.d(Assists.Config.logTag, "execute->${stepImpl.simpleName}:$step-delay:$delay")
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
