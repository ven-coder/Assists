package com.ven.assists.stepper

import android.util.Log
import com.ven.assists.AssistsCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

/**
 * 步骤管理器
 * 用于管理和执行自动化步骤的核心类。提供步骤注册、执行、监听等功能，
 * 支持协程异步执行和步骤间延时控制。
 */
object StepManager {
    /**
     * 步骤默认间隔时长（毫秒）
     * 用于控制连续步骤执行之间的默认等待时间
     */
    var DEFAULT_STEP_DELAY = 1000L

    /**
     * 步骤监听器
     * 用于监听步骤执行的状态和进度
     */
    var stepListeners: StepListener? = null

    /**
     * 步骤收集器映射
     * 存储所有已注册的步骤实现类及其对应的步骤收集器
     * key: 步骤实现类的完整类名
     * value: 对应的步骤收集器实例
     */
    private val stepCollector: HashMap<String, StepCollector> = hashMapOf()

    /**
     * 协程作业实例
     * 用于管理步骤执行的协程生命周期
     */
    private var job = Job()

    /**
     * 协程作用域
     * 提供步骤异步执行的协程环境
     * 当job被取消或不活跃时会自动创建新的协程作用域
     */
    var coroutine: CoroutineScope = CoroutineScope(job + Dispatchers.IO)
        private set
        get() {
            if (job.isCancelled || !job.isActive) {
                job = Job()
                field = CoroutineScope(job + Dispatchers.IO)
            }
            return field
        }

    /**
     * 控制停止操作的变量
     * 当设置为true时，将取消步骤器下的所有步骤
     * 在执行新的步骤前需要将其设置为false
     */
    var isStop = false
        set(value) {
            field = value
            // 当设置为true时，取消协程
            if (field) {
                coroutine.cancel()
            }
        }

    /**
     * 执行指定步骤
     * 
     * @param stepImpl 步骤实现类的Class对象
     * @param stepTag 步骤标识
     * @param delay 步骤执行前的延迟时间（毫秒），默认使用DEFAULT_STEP_DELAY
     * @param data 传递给步骤的数据
     * @param begin 是否作为起始步骤（如果是，会重置isStop标志）
     */
    fun <T : StepImpl> execute(stepImpl: Class<T>, stepTag: Int, delay: Long = DEFAULT_STEP_DELAY, data: Any? = null, begin: Boolean = false) {
        execute(stepImpl.name, stepTag, delay, data = data, begin = begin)
    }

    /**
     * 执行指定步骤
     * 
     * @param implClassName 步骤实现类的完整类名
     * @param stepTag 步骤标识
     * @param delay 步骤执行前的延迟时间（毫秒），默认使用DEFAULT_STEP_DELAY
     * @param data 传递给步骤的数据
     * @param begin 是否作为起始步骤（如果是，会重置isStop标志）
     */
    fun execute(implClassName: String, stepTag: Int, delay: Long = DEFAULT_STEP_DELAY, data: Any? = null, begin: Boolean = false) {
        if (begin) isStop = false
        if (isStop) return
        StringBuilder().apply {
            append("\n>>>>>>>>>>>>execute>>>>>>>>>>>")
            append("\n${implClassName}:$stepTag")
            append("\ndelay:$delay")
            append("\ndata:$data")
            append("\n")
            Log.d(AssistsCore.LOG_TAG, toString())
        }
        stepCollector[implClassName] ?: register(implClassName)
        stepCollector[implClassName]?.get(stepTag)?.execute(delay, data = data)
    }

    /**
     * 注册步骤实现类
     * 创建步骤收集器并初始化步骤实现类
     * 
     * @param implClassName 步骤实现类的完整类名
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
