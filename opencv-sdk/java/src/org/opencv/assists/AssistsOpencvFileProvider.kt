package org.opencv.assists

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.LogUtils

class AssistsOpencvFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            OpencvWrapper.init()
        }
        return super.onCreate()
    }
}