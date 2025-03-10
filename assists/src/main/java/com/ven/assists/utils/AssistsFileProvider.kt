package com.ven.assists.utils

import android.app.Application
import androidx.core.content.FileProvider
import com.ven.assists.Assists

class AssistsFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            Assists.init(applicationContext)
        }
        return super.onCreate()
    }
}