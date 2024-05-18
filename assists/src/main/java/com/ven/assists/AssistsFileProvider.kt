package com.ven.assists

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.LogUtils

class AssistsFileProvider : FileProvider() {
    override fun onCreate(): Boolean {
        val applicationContext = context?.applicationContext
        if (applicationContext is Application) {
            Assists.init(applicationContext)
        }
        return super.onCreate()
    }
}