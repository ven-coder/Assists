package com.ven.assists_opcv

import android.app.Application
import androidx.core.content.FileProvider
import com.ven.assists.MediaProjectionServiceManager

class AssistsMPFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            OpencvWrapper.init()
            MediaProjectionServiceManager.init(applicationContext)
        }
        return super.onCreate()
    }
}