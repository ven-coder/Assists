package com.ven.assist

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils

object Assists {
    /**
     * 无障碍服务，未开启前为null，使用注意判空
     */
    @JvmStatic
    var service: AssistsService? = null
    fun init() {
        LogUtils.getConfig().globalTag = Config.logTag
    }

    /**
     * 配置，均可自行修改
     */
    object Config {
        //默认步骤间隔时间，毫秒
        var defaultStepDelay: Long = 2000

        //日志TAG
        var logTag = "assists_log"
    }

    /**
     * 打开无障碍服务设置
     */
    @JvmStatic
    fun openAccessibilitySetting() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        ActivityUtils.startActivity(intent)
    }

    /**
     *检查无障碍服务是否开启
     */
    fun isAccessibilityServiceEnabled(context: Context, serviceName: String): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices?.contains(serviceName) == true
    }

    /**
     * 获取当前窗口所属包名
     */
    fun packageName(): String? {
        return service?.rootInActiveWindow?.packageName?.toString()
    }

    /**
     * 监听管理器
     */
    object ListenerManager {
        @JvmStatic
        val globalListeners: ArrayList<ServiceListener> = arrayListOf()
        val stepListener: ArrayList<StepListener> = arrayListOf()
        var gestureListener: GestureListener? = null

        /**
         * 服务监听，用无障碍服务的监听回调
         */
        interface ServiceListener {
            /**
             * 当界面发生事件时回调，即 [AssistsService.onAccessibilityEvent] 回调
             */
            fun onAccessibilityEvent(event: AccessibilityEvent) {}

            /**
             * 服务启用后的回调，即[AssistsService.onServiceConnected]回调
             */
            fun onServiceConnected(service: AssistsService) {}
            fun onInterrupt() {}

            /**
             * 服务关闭后的回调，即[AssistsService.onUnbind]回调
             */
            fun onUnbind() {}
        }

        /**
         * 步骤监听
         */
        interface StepListener {
            fun onStepStart() {}
            fun onStepStop() {}
        }

        /**
         * 手势监听
         */
        interface GestureListener {
            /**
             * 手势执行前，可用于判断手势执行前判断执行位置是否位于浮窗范围，如果位于浮窗范围可隐藏浮窗或设置浮窗为不可触摸
             * @param startLocation 手势开始位置
             * @param endLocation 手势结束位置
             * @return 需要延迟执行的时间，毫秒，默认0
             */
            fun onGestureBegin(startLocation: FloatArray, endLocation: FloatArray): Long {
                return 0
            }

            /**
             * 手势取消（在手势执行过程用户在操作或者有其他手势同步执行会回调此方法）
             */
            fun onGestureCancelled() {}

            /**
             * 手势执行完成
             */
            fun onGestureCompleted() {}

            /**
             * 手势结束，手势取消或完成后都会回调
             * 可用于恢复手势开始前[GestureListener.onGestureBegin]所作的逻辑
             */
            fun onGestureEnd() {}
        }
    }
}