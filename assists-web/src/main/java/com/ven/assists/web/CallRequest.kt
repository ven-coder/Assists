package com.ven.assists.web

import com.google.gson.JsonObject

class CallRequest<T>(
    val method: String,
    val arguments: T?,
    val nodes: List<Node>? = null,
    val node: Node? = null,
    val callbackId: String? = ""
) {

}

fun <T> CallRequest<JsonObject>.createResponse(code: Int, data: T? = null, callbackId: String? = "", message: String? = ""): CallResponse<T> {
    return CallResponse<T>(code = code, data = data, message = message, callbackId = if (callbackId.isNullOrEmpty()) this.callbackId else callbackId)
}