package com.ven.assist.step

import com.blankj.utilcode.util.LogUtils
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
    fun <T : StepImpl> beginExecute(stepImpl: Class<T>, step: Int, delay: Long = Assists.Config.defaultStepDelay): StepManager {
        isStop = false
        execute(stepImpl, step, delay)
        return this
    }

    /**
     * 执行步骤，如果是开始位置请使用[beginExecute]
     * @param stepImpl 执行的业务实现类
     * @param step 步骤序号
     * @param delay 步骤执行延迟时间，默认[Assists.Config.defaultStepDelay]
     */
    fun <T : StepImpl> execute(stepImpl: Class<T>, step: Int, delay: Long = Assists.Config.defaultStepDelay, data: Any? = null) {
        if (isStop) return
        LogUtils.d("execute->${stepImpl.name}:$step", "delay:$delay")
        stepCollector[stepImpl.name]?.get(step)?.let {
            it.data = data
            it.execute(delay)
        } ?: throw RuntimeException("The class ${stepImpl.name} is not registered. Please call StepManager.register() to register it first./类${stepImpl.name}未注册，请先调用StepManager.register()进行注册")
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
