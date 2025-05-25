package com.ven.assists.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@SuppressLint("SetJavaScriptEnabled")
class ASWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var onReceivedTitle: ((title: String) -> Unit)? = null

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
            domStorageEnabled=true
            databaseEnabled=true
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

        addJavascriptInterface(ASJavascriptInterface(this), "assistsx")
    }
}