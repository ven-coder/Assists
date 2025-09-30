package com.ven.assists.web

/**
 * AccessibilityEvent过滤配置实体类
 * 用于配置AccessibilityEvent的处理方式和过滤条件
 */
data class AccessibilityEventFilter(
    /**
     * 包名过滤
     * 如果为空或null，则忽略所有包的事件
     * 如果指定包名，则只处理该包的事件
     */
    val packageName: String? = null,

    /**
     * 是否在子线程中处理AccessibilityEvent
     * true: 在子线程中处理，避免阻塞主线程
     * false: 在主线程中处理
     */
    val processInBackground: Boolean = false,

    /**
     * 是否获取节点信息
     * true: 获取并解析AccessibilityNodeInfo节点信息
     * false: 不获取节点信息，提高处理性能
     */
    val fetchNodeInfo: Boolean = true,

    /**
     * 是否启用日志输出
     * true: 输出AccessibilityEvent处理日志
     * false: 不输出日志
     */
    val enableLogging: Boolean = false,

    /**
     * 事件类型过滤
     */
    val eventTypes: List<Int>? = null,

    /**
     * 是否启用事件去重
     * true: 启用去重，避免重复处理相同事件
     * false: 不启用去重
     */
    val enableDeduplication: Boolean = false
) {

    /**
     * 检查是否应该处理指定包的事件
     * @param targetPackageName 目标包名
     * @return true表示应该处理，false表示应该过滤
     */
    fun shouldProcessPackage(targetPackageName: String?): Boolean {
        return packageName == null || packageName == targetPackageName
    }

    /**
     * 检查是否应该处理指定类型的事件
     * @param eventType 事件类型
     * @return true表示应该处理，false表示应该过滤
     */
    fun shouldProcessEventType(eventType: Int): Boolean {
        return eventTypes == null || eventTypes.contains(eventType)
    }

    /**
     * 创建默认的过滤配置
     * 所有包名，子线程处理，获取节点信息，启用日志
     */
    companion object {
        fun createDefault(): AccessibilityEventFilter {
            return AccessibilityEventFilter(
                packageName = null,
                processInBackground = true,
                fetchNodeInfo = true,
                enableLogging = false,
                eventTypes = null,
                enableDeduplication = false
            )
        }

        /**
         * 创建高性能配置
         * 不获取节点信息，不启用日志，启用去重
         */
        fun createHighPerformance(): AccessibilityEventFilter {
            return AccessibilityEventFilter(
                packageName = null,
                processInBackground = true,
                fetchNodeInfo = false,
                enableLogging = false,
                eventTypes = null,
                enableDeduplication = true
            )
        }

        /**
         * 创建调试配置
         * 启用所有功能，便于调试
         */
        fun createDebug(): AccessibilityEventFilter {
            return AccessibilityEventFilter(
                packageName = null,
                processInBackground = true,
                fetchNodeInfo = true,
                enableLogging = true,
                eventTypes = null,
                enableDeduplication = false
            )
        }

        /**
         * 创建指定包名的过滤配置
         * @param targetPackageName 目标包名
         */
        fun createForPackage(targetPackageName: String): AccessibilityEventFilter {
            return AccessibilityEventFilter(
                packageName = targetPackageName,
                processInBackground = true,
                fetchNodeInfo = true,
                enableLogging = false,
                eventTypes = null,
                enableDeduplication = false
            )
        }
    }
}
