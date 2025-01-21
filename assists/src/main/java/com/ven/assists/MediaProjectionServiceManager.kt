package com.ven.assists

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.blankj.utilcode.util.ActivityUtils

object MediaProjectionServiceManager {
    const val REQUEST_CODE="request_code_media_projection"
    const val REQUEST_DATA="request_data_media_projection"

    private var requestLaunchers = hashMapOf<Activity, ActivityResultLauncher<Intent>>()

    private val activityLifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is ComponentActivity && requestLaunchers[activity] == null) {
                requestLaunchers[activity] = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        val service = Intent(activity, MediaProjectionService::class.java)
                        service.putExtra(REQUEST_CODE, result.resultCode)
                        service.putExtra(REQUEST_DATA, result.data)
                        activity.startService(service)
                    }
                }
            }
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            requestLaunchers.remove(activity)
        }
    }

    fun init(application: Application) {
        requestLaunchers.clear()
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    fun request(autoAllow: Boolean = true) {

        val projectionManager = ActivityUtils.getTopActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = projectionManager.createScreenCaptureIntent()
        requestLaunchers[ActivityUtils.getTopActivity()]?.launch(intent)
    }
}