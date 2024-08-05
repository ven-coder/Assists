package com.ven.assists.simple

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.webkit.WebView
import com.blankj.utilcode.util.Utils
import com.tencent.shadow.core.common.LoggerFactory
import com.tencent.shadow.dynamic.host.DynamicRuntime
import com.tencent.shadow.dynamic.host.PluginManager
import com.ven.assists.simple.plugin.HostUiLayerProvider
import java.io.File

class App : Application() {

    companion object {
        const val TARGET_PACKAGE_NAME = "com.tencent.mm"
        var sApp: App? = null


        private fun setWebViewDataDirectorySuffix() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return
            }
            WebView.setDataDirectorySuffix(getProcessName())
        }

        private fun detectNonSdkApiUsageOnAndroidP() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                return
            }
            val builder = VmPolicy.Builder()
            builder.detectNonSdkApiUsage()
            StrictMode.setVmPolicy(builder.build())
        }

        fun getApp(): App? {
            return sApp
        }


        private fun isProcess(context: Context, processName: String): Boolean {
            var currentProcName = ""
            val manager =
                context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (processInfo in manager.runningAppProcesses) {
                if (processInfo.pid == Process.myPid()) {
                    currentProcName = processInfo.processName
                    break
                }
            }

            return currentProcName.endsWith(processName)
        }
    }

    private var mPluginManager: PluginManager? = null
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)

        detectNonSdkApiUsageOnAndroidP()
        setWebViewDataDirectorySuffix()
        LoggerFactory.setILoggerFactory(AndroidLogLoggerFactory())

        if (isProcess(this, ":plugin")) {
            //在全动态架构中，Activity组件没有打包在宿主而是位于被动态加载的runtime，
            //为了防止插件crash后，系统自动恢复crash前的Activity组件，此时由于没有加载runtime而发生classNotFound异常，导致二次crash
            //因此这里恢复加载上一次的runtime
            DynamicRuntime.recoveryRuntime(this)
        }

        if (isProcess(this, packageName)) {
            PluginHelper.getInstance().init(this)
        }

        HostUiLayerProvider.init(this)
    }

    fun loadPluginManager(apk: File?) {
        if (mPluginManager == null) {
            mPluginManager = Shadow.getPluginManager(apk)
        }
    }

    fun getPluginManager(): PluginManager? {
        return mPluginManager
    }
}