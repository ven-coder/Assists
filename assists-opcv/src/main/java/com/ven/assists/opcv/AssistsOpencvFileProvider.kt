package com.ven.assists.opcv

import android.app.Application
import androidx.core.content.FileProvider

class AssistsOpencvFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            OpencvWrapper.init()
        }
        return super.onCreate()
    }
}