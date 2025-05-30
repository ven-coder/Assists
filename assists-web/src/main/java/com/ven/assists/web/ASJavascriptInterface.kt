package com.ven.assists.web

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ven.assists.AssistsCore
import com.ven.assists.AssistsCore.click
import com.ven.assists.AssistsCore.containsText
import com.ven.assists.AssistsCore.findById
import com.ven.assists.AssistsCore.findByTags
import com.ven.assists.AssistsCore.findByText
import com.ven.assists.AssistsCore.findFirstParentByTags
import com.ven.assists.AssistsCore.findFirstParentClickable
import com.ven.assists.AssistsCore.getAllText
import com.ven.assists.AssistsCore.getBoundsInParent
import com.ven.assists.AssistsCore.getBoundsInScreen
import com.ven.assists.AssistsCore.getChildren
import com.ven.assists.AssistsCore.getNodes
import com.ven.assists.AssistsCore.longClick
import com.ven.assists.AssistsCore.nodeGestureClick
import com.ven.assists.AssistsCore.paste
import com.ven.assists.AssistsCore.scrollBackward
import com.ven.assists.AssistsCore.scrollForward
import com.ven.assists.AssistsCore.selectionText
import com.ven.assists.AssistsCore.setNodeText
import com.ven.assists.AssistsCore.takeScreenshot
import com.ven.assists.utils.CoroutineWrapper
import com.ven.assists.window.AssistsWindowManager
import com.ven.assists.window.AssistsWindowManager.overlayToast
import com.ven.assists.mp.MPManager
import com.ven.assists.mp.MPManager.getBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ASJavascriptInterface(val webView: WebView) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    fun <T> callback(result: CallResponse<T>) {
        coroutineScope.launch {
            runCatching {
                val json = GsonUtils.toJson(result)
                webView.evaluateJavascript("javascript:assistsxCallback('${json}')", null)
            }.onFailure {
                LogUtils.e(it)
            }
        }
    }

    @JavascriptInterface
    fun call(json: String): String {
        var result = GsonUtils.toJson(CallResponse<Any>(code = -1))
        runCatching {
            val request = GsonUtils.fromJson<CallRequest<JsonObject>>(json, object : TypeToken<CallRequest<JsonObject>>() {}.type)
            when (request.method) {
                CallMethod.setOverlayFlags -> {
                    request.arguments?.apply {
                        val flagList = arrayListOf<Int>()
                        get("flags")?.asJsonArray?.forEach {
                            flagList.add(it.asInt)
                        }
                        val flags = flagList.reduce { a, b -> a or b }
                        CoroutineWrapper.launch { AssistsWindowManager.setFlags(flags) }
                    }
                }

                CallMethod.takeScreenshot -> {
                    CoroutineWrapper.launch {
                        val overlayHiddenScreenshotDelayMillis = request.arguments?.get("overlayHiddenScreenshotDelayMillis")?.asLong ?: 250
                        AssistsWindowManager.hideAll()
                        delay(overlayHiddenScreenshotDelayMillis)
                        val list = arrayListOf<String>()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val screenshot = AssistsCore.takeScreenshot()
                            AssistsWindowManager.showTop()

                            request.nodes?.forEach {
                                val bitmap = NodeCacheManager.get(it.nodeId)?.takeScreenshot(screenshot = screenshot)
                                bitmap?.let {
                                    val base64 = bitmapToBase64(it)
                                    list.add(base64)
                                }
                                bitmap?.recycle()
                            }
                        } else {
                            val takeScreenshot2Bitmap = MPManager.takeScreenshot2Bitmap()
                            AssistsWindowManager.showTop()

                            takeScreenshot2Bitmap ?: let {
                                callback(CallResponse<JsonObject>(code = -1, message = "截屏失败", callbackId = request.callbackId))
                                return@launch
                            }
                            request.nodes?.forEach {
                                val bitmap = NodeCacheManager.get(it.nodeId)?.getBitmap(screenshot = takeScreenshot2Bitmap)
                                bitmap?.let {
                                    val base64 = bitmapToBase64(it)
                                    list.add(base64)
                                }
                                bitmap?.recycle()
                            }
                            takeScreenshot2Bitmap.recycle()
                        }
                        callback(CallResponse<JsonObject>(code = 0, data = JsonObject().apply {
                            add("images", JsonArray().apply {
                                list.forEach {
                                    add(it)
                                }
                            })
                        }, callbackId = request.callbackId))

                    }

                    result = GsonUtils.toJson(CallResponse<JsonObject>(code = 0, data = JsonObject().apply {
                        addProperty("resultType", "callback")
                    }))
                }

                CallMethod.dispatchGesture -> {
                    ScreenUtils.getScreenHeight()
//                    AssistsCore.dispatchGesture()
//
//                    result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                }

                CallMethod.getAppScreenSize -> {
                    val bounds = AssistsCore.getAppBoundsInScreen()?.toBounds()
                    result = GsonUtils.toJson(CallResponse<Node.Bounds>(code = 0, data = bounds))
                }

                CallMethod.getScreenSize -> {
                    result = GsonUtils.toJson(CallResponse<JsonObject>(code = 0, data = JsonObject().apply {
                        addProperty("screenWidth", ScreenUtils.getScreenWidth())
                        addProperty("screenHeight", ScreenUtils.getScreenHeight())
                    }))
                }

                CallMethod.gestureClick -> {
                    CoroutineWrapper.launch {
                        AssistsCore.gestureClick(x = request.arguments?.get("x")?.asFloat ?: 0f, y = request.arguments?.get("y")?.asFloat ?: 0f)
                    }
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = true))
                }

                CallMethod.nodeGestureClick -> {
                    CoroutineWrapper.launch {
                        val offsetX = request.arguments?.get("offsetX")?.asFloat ?: (ScreenUtils.getScreenWidth() * 0.01953f)
                        val offsetY = request.arguments?.get("offsetY")?.asFloat ?: (ScreenUtils.getScreenWidth() * 0.01953f)
                        val switchWindowIntervalDelay = request.arguments?.get("switchWindowIntervalDelay")?.asLong ?: 250
                        val clickDuration = request.arguments?.get("clickDuration")?.asLong ?: 25
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.nodeGestureClick(
                            offsetX = offsetX,
                            offsetY = offsetY,
                            switchWindowIntervalDelay = switchWindowIntervalDelay,
                            duration = clickDuration
                        )
                        callback(CallResponse<Boolean>(code = 0, data = true, callbackId = request.callbackId))
                    }
                    result = GsonUtils.toJson(CallResponse<JsonObject>(code = 0, data = JsonObject().apply {
                        addProperty("resultType", "callback")
                    }))
                }

                CallMethod.nodeGestureClickByDouble -> {
                    CoroutineWrapper.launch {
                        val offsetX = request.arguments?.get("offsetX")?.asFloat ?: (ScreenUtils.getScreenWidth() * 0.01953f)
                        val offsetY = request.arguments?.get("offsetY")?.asFloat ?: (ScreenUtils.getScreenWidth() * 0.01953f)
                        val switchWindowIntervalDelay = request.arguments?.get("switchWindowIntervalDelay")?.asLong ?: 250
                        val clickDuration = request.arguments?.get("clickDuration")?.asLong ?: 25
                        val clickInterval = request.arguments?.get("clickInterval")?.asLong ?: 100
                        val bounds = NodeCacheManager.get(request.node?.nodeId ?: "")?.getBoundsInScreen()

                        AssistsWindowManager.nonTouchableByAll()
                        delay(switchWindowIntervalDelay)

                        val x = (bounds?.centerX()?.toFloat() ?: 0f) + offsetX
                        val y = (bounds?.centerY()?.toFloat() ?: 0f) + offsetY

                        AssistsCore.gestureClick(x, y, clickDuration)
                        delay(clickInterval)
                        AssistsCore.gestureClick(x, y, clickDuration)
                        AssistsWindowManager.touchableByAll()

                        callback(CallResponse<Boolean>(code = 0, data = true, callbackId = request.callbackId))
                    }
                    result = GsonUtils.toJson(CallResponse<JsonObject>(code = 0, data = JsonObject().apply {
                        addProperty("resultType", "callback")
                    }))
                }


                CallMethod.getBoundsInParent -> {

                    val bounds = NodeCacheManager.get(request.node?.nodeId ?: "")?.getBoundsInParent()?.toBounds()

                    result = GsonUtils.toJson(CallResponse<Node.Bounds>(code = 0, data = bounds))
                }

                CallMethod.getBoundsInScreen -> {

                    val bounds = NodeCacheManager.get(request.node?.nodeId ?: "")?.getBoundsInScreen()?.toBounds()

                    result = GsonUtils.toJson(CallResponse<Node.Bounds>(code = 0, data = bounds))
                }

                CallMethod.isVisible -> {

                    val value = NodeCacheManager.get(request.node?.nodeId ?: "")?.let letRoot@{ node ->
                        val compareNodeId = request.arguments?.get("compareNode")?.asJsonObject?.get("nodeId")?.asString ?: ""
                        val isFullyByCompareNode = request.arguments?.get("isFullyByCompareNode")?.asBoolean == true

                        val compareNode = if (compareNodeId.isNotEmpty()) {
                            NodeCacheManager.get(compareNodeId)
                        } else {
                            null
                        }

                        compareNode?.let {

                            if (!node.isVisibleToUser) return@let false

                            val compareNodeBounds = it.getBoundsInScreen()
                            val nodeBounds = node.getBoundsInScreen()
                            if (isFullyByCompareNode) {

                                if (compareNodeBounds.contains(nodeBounds)) {
                                    return@letRoot false
                                }
                                if (Rect.intersects(compareNodeBounds, nodeBounds)) {
                                    return@letRoot false
                                }
                                return@letRoot true

                            } else {

                                if (compareNodeBounds.contains(nodeBounds)) {
                                    return@letRoot false
                                }

                                return@letRoot true

                            }

                        }

                        return@letRoot node.isVisibleToUser
                    }
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = value))

                }

                CallMethod.getAllText -> {

                    val texts = NodeCacheManager.get(request.node?.nodeId ?: "")?.getAllText()

                    result = GsonUtils.toJson(CallResponse<List<String>>(code = 0, data = texts))
                }

                CallMethod.getChildren -> {

                    val nodes = NodeCacheManager.get(request.node?.nodeId ?: "")?.getChildren()?.toNodes()

                    result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                }

                CallMethod.getAllNodes -> {
                    val filterText = request.arguments?.get("filterText")?.asString ?: ""
                    val filterDes = request.arguments?.get("filterDes")?.asString ?: ""
                    val filterClass = request.arguments?.get("filterClass")?.asString ?: ""
                    val filterViewId = request.arguments?.get("filterViewId")?.asString ?: ""
                    val nodes = AssistsCore.getAllNodes(
                        filterClass = filterClass,
                        filterDes = filterDes,
                        filterViewId = filterViewId,
                        filterText = filterText
                    ).toNodes()
                    result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                }

                CallMethod.findById -> {
                    val id = request.arguments?.get("id")?.asString ?: ""
                    val filterText = request.arguments?.get("filterText")?.asString ?: ""
                    val filterDes = request.arguments?.get("filterDes")?.asString ?: ""
                    val filterClass = request.arguments?.get("filterClass")?.asString ?: ""
                    request.node?.get()?.let {
                        val nodes = it.findById(id, filterText = filterText, filterClass = filterClass, filterDes = filterDes).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    } ?: let {
                        val nodes = AssistsCore.findById(id, filterText = filterText, filterClass = filterClass, filterDes = filterDes).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    }
                }


                CallMethod.findByText -> {
                    val text = request.arguments?.get("text")?.asString ?: ""
                    val filterViewId = request.arguments?.get("filterViewId")?.asString ?: ""
                    val filterDes = request.arguments?.get("filterDes")?.asString ?: ""
                    val filterClass = request.arguments?.get("filterClass")?.asString ?: ""
                    request.node?.get()?.let {
                        val nodes = it.findByText(text, filterViewId = filterViewId, filterDes = filterDes, filterClass = filterClass).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    } ?: let {
                        val nodes =
                            AssistsCore.findByText(text, filterViewId = filterViewId, filterDes = filterDes, filterClass = filterClass).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    }
                }

                CallMethod.findByTags -> {
                    val className = request.arguments?.get("className")?.asString ?: ""
                    val text = request.arguments?.get("filterText")?.asString ?: ""
                    val viewId = request.arguments?.get("filterViewId")?.asString ?: ""
                    val des = request.arguments?.get("filterDes")?.asString ?: ""
                    request.node?.get()?.let {
                        val nodes = it.findByTags(className = className, viewId = viewId, des = des, text = text).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    } ?: let {
                        val nodes = AssistsCore.findByTags(className = className, text = text, viewId = viewId, des = des).toNodes()
                        result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                    }
                }

                CallMethod.selectionText -> {
                    val selectionStart = request.arguments?.get("selectionStart")?.asInt ?: 0
                    val selectionEnd = request.arguments?.get("selectionEnd")?.asInt ?: 0
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.selectionText(selectionStart, selectionEnd) == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.scrollForward -> {
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.scrollForward() == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.scrollBackward -> {
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.scrollBackward() == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.findByTextAllMatch -> {
                    val nodes = AssistsCore.findByTextAllMatch(request.arguments?.get("text")?.asString ?: "").toNodes()
                    result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                }

                CallMethod.containsText -> {
                    val isSuccess =
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.containsText(request.arguments?.get("text")?.asString ?: "") == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.findFirstParentByTags -> {
                    val node =
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.findFirstParentByTags(request.arguments?.get("className")?.asString ?: "")
                            ?.toNode()
                    result = GsonUtils.toJson(CallResponse<Node?>(code = 0, data = node))
                }

                CallMethod.getNodes -> {
                    val nodes =
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.getNodes()
                            ?.toNodes()
                    result = GsonUtils.toJson(CallResponse<List<Node>>(code = 0, data = nodes))
                }

                CallMethod.findFirstParentClickable -> {
                    val node =
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.findFirstParentClickable()
                            ?.toNode()
                    result = GsonUtils.toJson(CallResponse<Node?>(code = 0, data = node))
                }


                CallMethod.setNodeText -> {
                    val isSuccess =
                        NodeCacheManager.get(request.node?.nodeId ?: "")?.setNodeText(request.arguments?.get("text")?.asString ?: "") == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.click -> {
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.click() == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.longClick -> {
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.longClick() == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                CallMethod.paste -> {
                    val isSuccess = NodeCacheManager.get(request.node?.nodeId ?: "")?.paste(request.arguments?.get("text")?.asString ?: "") == true
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = isSuccess))
                }

                //其他方法
                CallMethod.launchApp -> {
                    val packageName = request.arguments?.get("packageName")?.asString ?: ""
                    CoroutineWrapper.launch { AssistsCore.launchApp(packageName) }
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = true))
                }

                CallMethod.getPackageName -> {
                    val packageName = AssistsCore.getPackageName()
                    result = GsonUtils.toJson(CallResponse<String>(code = 0, data = packageName))
                }

                CallMethod.overlayToast -> {
                    val text = request.arguments?.get("text")?.asString ?: ""
                    val delay = request.arguments?.get("delay")?.asLong ?: 2000L
                    text.overlayToast(delay)
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = true))
                }

                CallMethod.back -> {
                    val resultBack = AssistsCore.back()
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = resultBack))
                }

                CallMethod.home -> {
                    val resultBack = AssistsCore.home()
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = resultBack))
                }

                CallMethod.notifications -> {
                    val resultBack = AssistsCore.notifications()
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = resultBack))
                }

                CallMethod.recentApps -> {
                    val resultBack = AssistsCore.recentApps()
                    result = GsonUtils.toJson(CallResponse<Boolean>(code = 0, data = resultBack))
                }


                else -> {

                }
            }
        }.onFailure {
            LogUtils.e(it)
        }
        return result
    }


    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)  // 可选 JPEG/PNG
        val byteArray = outputStream.toByteArray()
        return "data:image/png;base64,${Base64.encodeToString(byteArray, Base64.NO_WRAP)}"
    }
} 