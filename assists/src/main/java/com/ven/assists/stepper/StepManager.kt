package com.ven.assists.stepper

import android.util.Log
import com.ven.assists.AssistsCore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel


/**
 * 步骤管理器
 */
object StepManager {
    //步骤默认间隔时长
    var DEFAULT_STEP_DELAY = 1000L

    //步骤监听器
    var stepListeners: StepListener? = null

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




    /**
     * 控制停止操作的变量
     * 当设置为true时，将取消步骤器下的所有步骤（需要执行步骤时需要设置为false）
     */
    var isStop = false
        set(value) {
            field = value
            // 当设置为true时，取消协程
            if (field) {
                coroutine.cancel()
            }
        }


    fun <T : StepImpl> execute(stepImpl: Class<T>, stepTag: Int, delay: Long = DEFAULT_STEP_DELAY, data: Any? = null, begin: Boolean = false) {
        execute(stepImpl.name, stepTag, delay, data = data, begin = begin)
    }


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
