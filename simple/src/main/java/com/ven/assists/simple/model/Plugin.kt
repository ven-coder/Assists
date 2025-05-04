package com.ven.assists.simple.model
/**
 * 插件实体类
 * @property name 插件名称
 * @property path 插件路径
 */
data class Plugin(
    val name: String = "",
    var id: String = "",
    val version: String = "",
    val description: String = "",
    val isShowOverlay: Boolean = false,
    val needScreenCapture: Boolean = false,
    val overlayTitle: String = "",
    var path: String = "",
    val main: String = "",
    val icon: String = "",
    val packageName: String = ""  // 插件包名，用于创建插件目录
) {
    fun mainPath(): String {
        return "$path/$main"
    }
}