package com.ven.assists.web

class CallResponse<T>(val code: Int, val data: T? = null, val callbackId: String? = "", val message: String? = "") {
}