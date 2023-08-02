package com.ven.assists.simple

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.ven.assist.step.StepManager
import com.ven.assists.simple.step.GestureBottomTab
import com.ven.assists.simple.step.GestureScrollSocial
import com.ven.assists.simple.step.OpenWechatSocial
import com.ven.assists.simple.step.PublishSocial
import com.ven.assists.simple.step.ScrollContacts

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        StepManager.register(OpenWechatSocial::class.java)
        StepManager.register(PublishSocial::class.java)
        StepManager.register(ScrollContacts::class.java)
        StepManager.register(GestureBottomTab::class.java)
        StepManager.register(GestureScrollSocial::class.java)
    }
}