package com.ven.assists.stepper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 步骤实现基类
 * 用于实现自动化脚本的基础类，提供步骤实现的框架和协程执行环境
 * 继承此类的子类需要实现onImpl方法来定义具体的步骤逻辑
 */
abstract class StepImpl {

    /**
     * 步骤实现方法
     * 子类必须实现此方法来定义具体的步骤逻辑
     * 通过StepCollector来收集和注册步骤的具体实现
     *
     * @param collector 步骤收集器，用于注册步骤的具体实现
     */
    abstract fun onImpl(collector: StepCollector)

    /**
     * IO协程执行任务
     * 在Dispatchers.IO协程上下文中执行指定的挂起函数
     * 适用于执行IO操作、网络请求等耗时任务
     *
     * @param function 要执行的挂起函数
     */
    suspend fun runIO(function: suspend () -> Unit) {
        withContext(Dispatchers.IO) { function.invoke() }
    }

    /**
     * 主协程执行任务
     * 在Dispatchers.Main协程上下文中执行指定的挂起函数
     * 适用于执行UI操作、更新界面等需要在主线程进行的任务
     *
     * @param function 要执行的挂起函数
     */
    suspend fun runMain(function: suspend () -> Unit) {
        withContext(Dispatchers.Main) { function.invoke() }
    }
}