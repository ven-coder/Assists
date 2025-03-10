package com.ven.assists.utils

import android.app.Application
import androidx.core.content.FileProvider
import com.ven.assists.AssistsCore

class AssistsFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            AssistsCore.init(applicationContext)
        }
        return super.onCreate()
    }
}