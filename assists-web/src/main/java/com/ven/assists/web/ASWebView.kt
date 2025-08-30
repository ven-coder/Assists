package com.ven.assists.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.accessibility.AccessibilityEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.ven.assists.AssistsCore
import com.ven.assists.service.AssistsService
import com.ven.assists.service.AssistsServiceListener
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists.utils.runMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
class ASWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var onReceivedTitle: ((title: String) -> Unit)? = null
    val javascriptInterface = ASJavascriptInterface(webView = this)
    val javascriptInterfaceAsync = ASJavascriptInterfaceAsync(webView = this)

    var callIntercept: ((json: String) -> CallInterceptResult)? = null
        set(value) {
            field = value
            javascriptInterface.callIntercept = value
        }

    val assistsServiceListener = object : AssistsServiceListener {
        override fun onAccessibilityEvent(event: AccessibilityEvent) {
            runCatching {
                val node = event.source?.toNode()
                val jsonObject = JsonObject().apply {
                    addProperty("packageName", event.packageName?.toString() ?: "")
                    addProperty("className", event.className?.toString() ?: "")
                    addProperty("eventType", event.eventType)
                    addProperty("action", event.action)
                    add("texts", JsonArray().apply {
                        event.text.forEach { text -> this.add(text.toString()) }
                    })
                    node?.let {
                        val element = GsonUtils.getGson().toJsonTree(node)
                        add("node", element.asJsonObject)
                    }
                }
                onAccessibilityEvent(CallResponse(code = 0, data = jsonObject))
            }.onFailure {
                LogUtils.e(it)
            }
        }
    }


    init {
        // 初始化WebView设置
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            // 启用内置缩放控件
            builtInZoomControls = true
            // 隐藏缩放控件
            displayZoomControls = false
            // 支持viewport
            useWideViewPort = true
            // 加载内容自适应屏幕
            loadWithOverviewMode = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            domStorageEnabled = true
            databaseEnabled = true
            setWebContentsDebuggingEnabled(true)
        }

        // 设置WebViewClient
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 注入辅助函数到JS环境
            }
        }

        // 设置WebChromeClient用于处理JS的alert等
        webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                onReceivedTitle?.invoke(title ?: "")
            }
        }

        // 启用软键盘支持
        requestFocus()
        isFocusableInTouchMode = true
        isFocusable = true
        addJavascriptInterface(javascriptInterface, "assistsx")
        addJavascriptInterface(javascriptInterfaceAsync, "assistsxAsync")
        AssistsService.listeners.add(assistsServiceListener)
    }

    fun <T> onAccessibilityEvent(result: CallResponse<T>) {
        runCatching {
            val json = GsonUtils.toJson(result)
            evaluateJavascript("javascript:onAccessibilityEvent('${json}')", null)
        }.onFailure {
            LogUtils.e(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        AssistsService.listeners.remove(assistsServiceListener)
    }
}