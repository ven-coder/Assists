package com.ven.assists.simple.common

import android.widget.Toast
import com.ven.assists.Assists
import com.ven.assists.utils.CoroutineWrapper

fun String.toast() {
    Assists.service?.let {
        CoroutineWrapper.launch(isMain = true) {
            Toast.makeText(it, this@toast, Toast.LENGTH_SHORT).show()
        }
    }
}