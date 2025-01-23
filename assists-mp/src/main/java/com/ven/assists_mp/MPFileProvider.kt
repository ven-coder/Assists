package com.ven.assists_mp

import android.app.Application
import androidx.core.content.FileProvider

class MPFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            MPManager.init(applicationContext)
        }
        return super.onCreate()
    }
}