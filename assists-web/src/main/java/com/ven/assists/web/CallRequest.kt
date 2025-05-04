package com.ven.assists.web

class CallRequest<T>(
    val method: String,
    val arguments: T?,
    val nodes: List<Node>? = null,
    val node: Node? = null,
    val callbackId: String? = ""
) {
}