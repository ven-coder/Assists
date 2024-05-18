package com.ven.assists.simple

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts
import com.ven.assists.stepper.StepManager

class App : Application() {

    companion object{
        const val TARGET_PACKAGE_NAME = "com.tencent.mm"
    }
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}